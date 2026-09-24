package com.player.interpreter.domain.commands;

import com.player.config.PlayerConfig;
import java.util.Objects;

/**
 * Represents a musical note command that reads the player's configuration state to generate a
 * correctly formatted JFugue note token.
 */
public final class Note implements Command {
  private static final String VOLUME_INTENSITY_SEPARATOR = "a";
  private final String cmd;

  /**
   * Constructs a new {@code Note} command for the given musical character.
   *
   * @param cmd the musical note character (e.g., "A", "B", "C")
   * @throws NullPointerException if the provided command character is {@code null}
   */
  public Note(final String cmd) {
    this.cmd = Objects.requireNonNull(cmd, "Note command cannot be null");
  }

  @Override
  public String execute(final PlayerConfig config) {
    return buildJfugueToken(this.cmd, config);
  }

  private static String buildJfugueToken(String jfugueNote, PlayerConfig config) {
    return jfugueNote + config.getOctave() + VOLUME_INTENSITY_SEPARATOR + config.getVolume();
  }
}
