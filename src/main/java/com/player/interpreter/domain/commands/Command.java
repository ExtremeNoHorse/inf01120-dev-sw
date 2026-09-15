package com.player.interpreter.domain.commands;

import com.player.config.PlayerConfig;

/**
 * Represents an executable command within the interpreter domain.
 *
 * <p>Implementations of this interface encapsulate specific actions, such as playing a note or
 * updating configuration settings, to be applied to the player's state during the interpretation
 * process.
 */
public interface Command {

  /**
   * Executes the command, applying its specific logic to the provided player configuration.
   *
   * @param config the {@link PlayerConfig} instance to be modified or acted upon by this command
   * @return a formatted JFugue token string resulting from the command execution
   */
  String execute(final PlayerConfig config);
}
