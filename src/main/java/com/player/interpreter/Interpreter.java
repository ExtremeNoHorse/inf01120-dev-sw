package com.player.interpreter;

import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.domain.DomainMapper;
import com.player.interpreter.domain.MusicalDuration;
import com.player.interpreter.domain.commands.Command;
import java.util.ArrayList;
import java.util.List;
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
  // Notes without an explicit duration have no symbol: JFugue plays them as quarter notes.
  private static final Pattern SOUND_TOKEN_PATTERN =
      Pattern.compile("^(?:R|[A-G][#b]?\\d+)([whqistx]?)");
  private static final String REST_TOKEN = "Rq";
  private static final String WINDOWS_NEWLINE = "\r\n";
  private static final String SPACE = " ";
  private static final String CHANNEL_VOLUME = ":CE(7,%d)";
  private static final int MAX_DELAY_BEATS = 10_000;

  private final DomainMapper mapper;
  private PlayerConfig playerConfig;
  private StringBuilder pattern;
  private final List<List<Timeline.Event>> events = new ArrayList<>();
  private int lineStart;
  private int charOffset;
  private double beat;

  /** Constructs an {@code Interpreter} using the default mapper and configuration. */
  public Interpreter() {
    this(DomainMapper.createDefault(), PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));
  }

  /**
   * Constructs an {@code Interpreter} with a specified domain mapper and base configuration.
   *
   * @param mapper the domain mapper to resolve commands
   * @param playerConfig the playback configuration
   */
  public Interpreter(final DomainMapper mapper, final PlayerConfig playerConfig) {
    this.mapper = Objects.requireNonNull(mapper, "DomainMapper cannot be null");
    this.playerConfig = Objects.requireNonNull(playerConfig, "PlayerConfig cannot be null");
    this.pattern = new StringBuilder();
  }

  /**
   * Replaces the playback configuration used by the next interpretations.
   *
   * @param config the new playback configuration
   * @throws NullPointerException if config is null
   */
  public void configure(final PlayerConfig config) {
    this.playerConfig = Objects.requireNonNull(config, "PlayerConfig cannot be null");
  }

  /**
   * Retrieves a copy of the current playback configuration.
   *
   * @return a new {@link PlayerConfig} instance with current settings
   */
  public PlayerConfig getPlayerConfig() {
    return new PlayerConfig(playerConfig);
  }

  /**
   * Computes when each character of the text sounds, so the playback position can be traced back to
   * the source text.
   *
   * @param text the input text, where each line corresponds to an independent voice
   * @return the timeline with one entry per non-blank line
   * @throws TtmException if input is null
   */
  public Timeline timeline(final String text) {
    interpret(text);
    return new Timeline(events);
  }

  /**
   * Interprets text into a polyphonic JFugue pattern string using the current configuration.
   *
   * <p>The configuration's BPM is global. Its octave shifts every voice relative to the base
   * octave, so each voice keeps its own register (soprano stays above bass). Its volume sets the
   * MIDI channel volume of every voice, acting as a master volume over the note velocities (which
   * the space command keeps doubling).
   *
   * @param text the input text, where each line corresponds to an independent voice
   * @return the compiled JFugue pattern string
   * @throws TtmException if input is null
   */
  public String interpret(final String text) {
    final PlayerConfig settings = this.playerConfig;
    events.clear();
    if (text == null) {
      throw new TtmException(ErrorCode.INVALID_INPUT, "Input text cannot be null");
    }
    if (text.trim().isEmpty()) {
      return "";
    }

    this.pattern = new StringBuilder();
    this.pattern.append("T").append(settings.getBpm()).append(SPACE);

    final String[] lines = text.split("\\r?\\n");

    lineStart = 0;
    for (int voiceIndex = 0; voiceIndex < lines.length; voiceIndex++) {
      final String rawLine = lines[voiceIndex];
      if (!rawLine.trim().isEmpty()) {
        processVoiceLine(
            rawLine, voiceIndex, voiceConfig(voiceIndex, settings), settings.getVolume());
      }
      final int lineEnd = lineStart + rawLine.length();
      lineStart =
          lineEnd + (text.startsWith(WINDOWS_NEWLINE, lineEnd) ? WINDOWS_NEWLINE.length() : 1);
    }

    return this.pattern.toString().trim();
  }

  private static PlayerConfig voiceConfig(final int voiceIndex, final PlayerConfig settings) {
    final PlayerConfig voiceConfig = PlayerConfig.forVoice(voiceIndex, settings.getBpm());
    voiceConfig.setOctave(
        voiceConfig.getOctave() + settings.getOctave() - settings.getBaseOctave());
    return voiceConfig;
  }

  private void processVoiceLine(
      final String rawLine,
      final int voiceIndex,
      final PlayerConfig voiceConfig,
      final int channelVolume) {
    events.add(new ArrayList<>());
    beat = 0;
    charOffset = lineStart;
    pattern.append("V").append(voiceIndex).append(SPACE);
    pattern.append("I").append(voiceConfig.getInstrument()).append(SPACE);
    pattern.append(String.format(CHANNEL_VOLUME, channelVolume)).append(SPACE);

    String line = rawLine;

    // Check for delay prefix [n]
    final Matcher delayMatcher = DELAY_PATTERN.matcher(line);
    if (delayMatcher.find()) {
      final int delayBeats = delayBeats(delayMatcher.group(1));
      for (int i = 0; i < delayBeats; i++) {
        appendToken(REST_TOKEN);
      }
      line = line.substring(delayMatcher.end());
    }

    processLineCharacters(line, voiceConfig, lineStart + rawLine.length() - line.length());
  }

  private static int delayBeats(final String value) {
    try {
      final int delay = Integer.parseInt(value);
      if (delay > MAX_DELAY_BEATS) {
        throw new TtmException(
            ErrorCode.INVALID_INPUT, "O atraso inicial deve ter no máximo 10.000 beats.");
      }
      return delay;
    } catch (final NumberFormatException e) {
      throw new TtmException(ErrorCode.INVALID_INPUT, "O atraso inicial é inválido.", e);
    }
  }

  private void processLineCharacters(
      final String line, final PlayerConfig voiceConfig, final int firstOffset) {
    String lastNoteToken = null;
    int index = 0;
    while (index < line.length()) {
      charOffset = firstOffset + index;
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
      appendToken(result);
      return result;
    }
    return null;
  }

  private String processSingleCharacter(
      final char ch, final PlayerConfig voiceConfig, final String lastNoteToken) {
    // 2. Lowercase note letters: silence / rest
    if (ch >= 'a' && ch <= 'g') {
      appendToken(REST_TOKEN);
      return null;
    }

    final String charStr = String.valueOf(ch);

    // 3. Supported mapping commands (?, V, >, <, space, digits, !, ;, ,)
    if (mapper.supports(charStr)) {
      executeCommandToken(charStr, voiceConfig);
      return lastNoteToken;
    }

    // 4. Fallback / ELSE (consonants, vowels O/I/U, other characters)
    appendToken(Objects.requireNonNullElse(lastNoteToken, REST_TOKEN));
    return lastNoteToken;
  }

  /** Appends a token to the pattern and, if it is a note or a rest, records when it sounds. */
  private void appendToken(final String token) {
    pattern.append(token).append(SPACE);
    final Matcher sound = SOUND_TOKEN_PATTERN.matcher(token);
    if (sound.find()) {
      final double end = beat + MusicalDuration.fromJfugueToken(sound.group(1)).getBeats();
      events.getLast().add(new Timeline.Event(charOffset, beat, end));
      beat = end;
    }
  }
}
