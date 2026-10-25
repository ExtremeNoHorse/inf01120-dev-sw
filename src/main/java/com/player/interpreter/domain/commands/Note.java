package com.player.interpreter.domain.commands;

import com.player.config.PlayerConfig;
import com.player.interpreter.domain.MusicalDuration;
import java.util.Objects;

/**
 * Represents a musical note command that reads the player's configuration state to generate a
 * correctly formatted JFugue note token.
 *
 * <p>Supports accidentals (e.g., "#", "b") and duration figures (e.g., semibreve, semínima).
 */
public final class Note implements Command {
  private static final String VOLUME_INTENSITY_SEPARATOR = "a";
  private final String cmd;
  private final MusicalDuration duration;

  /**
   * Constructs a new {@code Note} command with no explicit duration (default behavior).
   *
   * @param cmd the musical note character and optional accidental (e.g., "A", "C#", "Bb")
   * @throws NullPointerException if the provided command character is {@code null}
   */
  public Note(final String cmd) {
    this(cmd, null);
  }

  /**
   * Constructs a new {@code Note} command with an explicit musical duration.
   *
   * @param cmd the musical note character and optional accidental (e.g., "A", "C#", "Bb")
   * @param duration the {@link MusicalDuration} figure, or {@code null} for default
   * @throws NullPointerException if the provided command character is {@code null}
   */
  public Note(final String cmd, final MusicalDuration duration) {
    this.cmd = Objects.requireNonNull(cmd, "Note command cannot be null");
    this.duration = duration;
  }

  /**
   * Retrieves the raw note command name including accidentals.
   *
   * @return the command string
   */
  public String getCmd() {
    return cmd;
  }

  /**
   * Retrieves the musical duration figure, if set.
   *
   * @return the {@link MusicalDuration}, or {@code null} if none
   */
  public MusicalDuration getDuration() {
    return duration;
  }

  @Override
  public String execute(final PlayerConfig config) {
    return buildJfugueToken(this.cmd, this.duration, config);
  }

  private static String buildJfugueToken(
      final String jfugueNote, final MusicalDuration duration, final PlayerConfig config) {
    final String durationToken = duration != null ? duration.getJfugueToken() : "";
    return jfugueNote
        + config.getOctave()
        + durationToken
        + VOLUME_INTENSITY_SEPARATOR
        + config.getVolume();
  }
}
