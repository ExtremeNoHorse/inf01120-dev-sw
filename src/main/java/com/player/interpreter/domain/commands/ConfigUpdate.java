package com.player.interpreter.domain.commands;

import com.player.config.PlayerConfig;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a command that updates the player configuration state or triggers an immediate action.
 */
public final class ConfigUpdate implements Command {
  private final Function<PlayerConfig, String> updateAction;

  /**
   * Constructs a new {@code ConfigUpdate} with the specified action logic.
   *
   * @param updateAction applies state changes to the configuration and returns a JFugue token
   * @throws NullPointerException if the provided update action is {@code null}
   */
  public ConfigUpdate(final Function<PlayerConfig, String> updateAction) {
    this.updateAction = Objects.requireNonNull(updateAction, "Update action cannot be null");
  }

  @Override
  public String execute(final PlayerConfig config) {
    return this.updateAction.apply(config);
  }
}
