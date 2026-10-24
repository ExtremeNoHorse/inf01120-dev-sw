package com.player.interpreter.domain;

import com.player.config.PlayerConfig;
import com.player.interpreter.domain.commands.Command;
import com.player.interpreter.domain.commands.ConfigUpdate;
import com.player.interpreter.domain.commands.Note;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps input strings (characters or tokens) to their corresponding executable {@link Command}s.
 *
 * <p>Acts as a dictionary for the interpreter, encapsulating the rules defined in Phases 1 and 2.
 */
public final class DomainMapper {
  private static final Pattern NOTE_PATTERN = Pattern.compile("^([A-G][#b]?)(?::([1-7]))?$");
  private static final String INSTRUMENT_TOKEN = "I";

  private final Map<String, Command> table;

  /**
   * Constructs a new {@code DomainMapper} with the specified command mapping.
   *
   * @param table a map connecting string inputs to their respective {@link Command} objects
   * @throws NullPointerException if the provided table is {@code null}
   */
  public DomainMapper(final Map<String, Command> table) {
    this.table = new HashMap<>(Objects.requireNonNull(table, "Command table cannot be null"));
  }

  /**
   * Factory method to create a fully configured {@code DomainMapper} aligned with Phase 2
   * specifications while preserving backward-compatible commands.
   *
   * @return a pre-configured {@link DomainMapper} instance
   */
  public static DomainMapper createDefault() {
    final Map<String, Command> map = new HashMap<>();
    registerNotes(map);
    registerControls(map);
    registerInstruments(map);
    registerDigits(map);
    return new DomainMapper(map);
  }

  private static void registerNotes(final Map<String, Command> map) {
    final String[] baseNotes = {"A", "B", "C", "D", "E", "F", "G"};
    for (final String note : baseNotes) {
      map.put(note, new Note(note));
    }
  }

  private static void registerControls(final Map<String, Command> map) {
    map.put(
        "?",
        new ConfigUpdate(
            cfg -> {
              cfg.increaseOctaveRelative();
              return "";
            }));
    map.put(
        "V",
        new ConfigUpdate(
            cfg -> {
              cfg.decreaseOctaveRelative();
              return "";
            }));
    map.put(
        ">",
        new ConfigUpdate(
            cfg -> {
              cfg.increaseBpm(PlayerConfig.BPM_DELTA);
              return "T" + cfg.getBpm();
            }));
    map.put(
        "<",
        new ConfigUpdate(
            cfg -> {
              cfg.decreaseBpm(PlayerConfig.BPM_DELTA);
              return "T" + cfg.getBpm();
            }));
    map.put(
        " ",
        new ConfigUpdate(
            cfg -> {
              cfg.doubleVolume();
              return "";
            }));
  }

  private static void registerInstruments(final Map<String, Command> map) {
    map.put(
        "!",
        new ConfigUpdate(
            cfg -> {
              cfg.setInstrument(PlayerConfig.INSTRUMENT_BANDONEON);
              return INSTRUMENT_TOKEN + PlayerConfig.INSTRUMENT_BANDONEON;
            }));
    map.put(
        ";",
        new ConfigUpdate(
            cfg -> {
              cfg.setInstrument(PlayerConfig.INSTRUMENT_TUBULAR_BELLS);
              return INSTRUMENT_TOKEN + PlayerConfig.INSTRUMENT_TUBULAR_BELLS;
            }));
    map.put(
        ",",
        new ConfigUpdate(
            cfg -> {
              cfg.setInstrument(PlayerConfig.INSTRUMENT_AGOGO);
              return INSTRUMENT_TOKEN + PlayerConfig.INSTRUMENT_AGOGO;
            }));
  }

  private static void registerDigits(final Map<String, Command> map) {
    final int[] oddDigits = {1, 3, 5, 7, 9};
    for (final int odd : oddDigits) {
      map.put(
          String.valueOf(odd),
          new ConfigUpdate(
              cfg -> {
                cfg.setInstrument(PlayerConfig.INSTRUMENT_TUBULAR_BELLS);
                return INSTRUMENT_TOKEN + PlayerConfig.INSTRUMENT_TUBULAR_BELLS;
              }));
    }

    final int[] evenDigits = {0, 2, 4, 6, 8};
    for (final int even : evenDigits) {
      map.put(
          String.valueOf(even),
          new ConfigUpdate(
              cfg -> {
                final int newInst = cfg.getInstrument() + even;
                cfg.setInstrument(newInst);
                return INSTRUMENT_TOKEN + cfg.getInstrument();
              }));
    }
  }

  /**
   * Retrieves the command associated with the given input string.
   *
   * <p>If a structured note pattern (e.g., "A:3", "C#:2", "Bb:4") is supplied and not already in
   * the table, it is dynamically parsed and cached.
   *
   * @param c the input string to map
   * @return the corresponding {@link Command}, or {@code null} if unsupported
   */
  public Command toDomain(final String c) {
    if (c == null) {
      return null;
    }
    if (table.containsKey(c)) {
      return table.get(c);
    }

    final Matcher matcher = NOTE_PATTERN.matcher(c);
    if (matcher.matches()) {
      final String noteName = matcher.group(1);
      final String durationGroup = matcher.group(2);
      final MusicalDuration duration =
          durationGroup != null ? MusicalDuration.fromId(Integer.parseInt(durationGroup)) : null;
      final Command noteCommand = new Note(noteName, duration);
      table.put(c, noteCommand);
      return noteCommand;
    }

    return null;
  }

  /**
   * Checks whether the given input string has a mapped command or valid note format.
   *
   * @param c the input string to check
   * @return {@code true} if supported, {@code false} otherwise
   */
  public boolean supports(final String c) {
    if (c == null) {
      return false;
    }
    return table.containsKey(c) || NOTE_PATTERN.matcher(c).matches();
  }
}
