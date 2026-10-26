package com.player.interpreter;

import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.domain.DomainMapper;
import com.player.interpreter.domain.commands.Command;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interprets textual input into JFugue musical patterns, applying Phase 2 polyphonic voice
 * separation and mapping rules.
 */
public final class Interpreter {
  private static final Pattern DELAY_PATTERN = Pattern.compile("^\\[(\\d+)]\\s*");
  private static final Pattern NOTE_TOKEN_PATTERN = Pattern.compile("^([A-G][#b]?(?::[1-7])?)");
  private static final String REST_TOKEN = "Rq";
  private static final String SPACE = " ";
  private static final String CHANNEL_VOLUME = ":CE(7,%d)";
  private static final int NO_CHANNEL_VOLUME = -1;

  private final DomainMapper mapper;
  private final PlayerConfig playerConfig;
  private StringBuilder pattern;

  /** Constructs an {@code Interpreter} using the default mapper and configuration. */
  public Interpreter() {
    this(DomainMapper.createDefault(), PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));
  }

  /**
   * Constructs an {@code Interpreter} with a specified domain mapper and base configuration.
   *
   * @param mapper the domain mapper to resolve commands
   * @param playerConfig the base player configuration
   */
  public Interpreter(final DomainMapper mapper, final PlayerConfig playerConfig) {
    this.mapper = Objects.requireNonNull(mapper, "DomainMapper cannot be null");
    this.playerConfig = Objects.requireNonNull(playerConfig, "PlayerConfig cannot be null");
    this.pattern = new StringBuilder();
  }

  /**
   * Interprets text into a polyphonic JFugue pattern string.
   *
   * @param text the input text, where each line corresponds to an independent voice
   * @return the compiled JFugue pattern string
   * @throws TtmException if input is null
   */
  public String interpret(final String text) {
    return interpret(text, this.playerConfig, false);
  }

  /**
   * Interprets text using the given user settings instead of the base configuration.
   *
   * <p>The settings' BPM is global. Octave shifts every voice relative to the settings' base
   * octave, so each voice keeps its own register (soprano stays above bass). The volume sets the
   * MIDI channel volume of every voice, acting as a master volume over the note velocities (which
   * the space command keeps doubling). It is always written, so going back to the default volume
   * also overrides a lower volume left in the synthesizer by a previous playback.
   *
   * @param text the input text, where each line corresponds to an independent voice
   * @param settings the user playback settings
   * @return the compiled JFugue pattern string
   * @throws TtmException if input is null
   */
  public String interpret(final String text, final PlayerConfig settings) {
    return interpret(text, settings, true);
  }

  private String interpret(
      final String text, final PlayerConfig settings, final boolean withChannelVolume) {
    Objects.requireNonNull(settings, "PlayerConfig cannot be null");
    if (text == null) {
      throw new TtmException(ErrorCode.INVALID_INPUT, "Input text cannot be null");
    }
    if (text.trim().isEmpty()) {
      return "";
    }

    this.pattern = new StringBuilder();
    this.pattern.append("T").append(settings.getBpm()).append(SPACE);

    final int channelVolume = withChannelVolume ? settings.getVolume() : NO_CHANNEL_VOLUME;
    final String[] lines = text.split("\\r?\\n");

    for (int voiceIndex = 0; voiceIndex < lines.length; voiceIndex++) {
      final String rawLine = lines[voiceIndex];
      if (!rawLine.trim().isEmpty()) {
        processVoiceLine(rawLine, voiceIndex, voiceConfig(voiceIndex, settings), channelVolume);
      }
    }

    return this.pattern.toString().trim();
  }

  private static PlayerConfig voiceConfig(final int voiceIndex, final PlayerConfig settings) {
    final PlayerConfig base = PlayerConfig.forVoice(voiceIndex, settings.getBpm());
    final int octave = base.getOctave() + settings.getOctave() - settings.getBaseOctave();
    return new PlayerConfig(
        base.getInstrument(),
        octave,
        base.getVolume(),
        settings.getBpm(),
        base.getBaseOctave(),
        base.getBaseVolume(),
        base.getBaseInstrument());
  }

  private void processVoiceLine(
      final String rawLine,
      final int voiceIndex,
      final PlayerConfig voiceConfig,
      final int channelVolume) {
    pattern.append("V").append(voiceIndex).append(SPACE);
    pattern.append("I").append(voiceConfig.getInstrument()).append(SPACE);
    if (channelVolume != NO_CHANNEL_VOLUME) {
      pattern.append(String.format(CHANNEL_VOLUME, channelVolume)).append(SPACE);
    }

    String line = rawLine;

    // Check for delay prefix [n]
    final Matcher delayMatcher = DELAY_PATTERN.matcher(line);
    if (delayMatcher.find()) {
      final int delayBeats = Integer.parseInt(delayMatcher.group(1));
      for (int i = 0; i < delayBeats; i++) {
        pattern.append(REST_TOKEN).append(SPACE);
      }
      line = line.substring(delayMatcher.end());
    }

    processLineCharacters(line, voiceConfig);
  }

  private void processLineCharacters(final String line, final PlayerConfig voiceConfig) {
    String lastNoteToken = null;
    int index = 0;
    while (index < line.length()) {
      final String remainder = line.substring(index);

      // 1. Try to match structured note (A-G with optional #/b and :duration)
      final Matcher noteMatcher = NOTE_TOKEN_PATTERN.matcher(remainder);
      if (noteMatcher.find()) {
        final String noteToken = noteMatcher.group(1);
        final String executedNote = executeCommandToken(noteToken, voiceConfig);
        if (executedNote != null) {
          lastNoteToken = executedNote;
        }
        index += noteToken.length();
        continue;
      }

      final char ch = line.charAt(index);
      lastNoteToken = processSingleCharacter(ch, voiceConfig, lastNoteToken);
      index++;
    }
  }

  private String executeCommandToken(final String token, final PlayerConfig voiceConfig) {
    final Command cmd = mapper.toDomain(token);
    if (cmd == null) {
      return null;
    }
    final String result = cmd.execute(voiceConfig);
    if (result != null && !result.isEmpty()) {
      pattern.append(result).append(SPACE);
      return result;
    }
    return null;
  }

  private String processSingleCharacter(
      final char ch, final PlayerConfig voiceConfig, final String lastNoteToken) {
    // 2. Lowercase note letters: silence / rest
    if (ch >= 'a' && ch <= 'g') {
      pattern.append(REST_TOKEN).append(SPACE);
      return null;
    }

    final String charStr = String.valueOf(ch);

    // 3. Supported mapping commands (?, V, >, <, space, digits, !, ;, ,)
    if (mapper.supports(charStr)) {
      executeCommandToken(charStr, voiceConfig);
      return lastNoteToken;
    }

    // 4. Fallback / ELSE (consonants, vowels O/I/U, other characters)
    pattern.append(Objects.requireNonNullElse(lastNoteToken, REST_TOKEN)).append(SPACE);
    return lastNoteToken;
  }
}
