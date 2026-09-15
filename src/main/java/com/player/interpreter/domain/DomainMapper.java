package com.player.interpreter.domain;

import com.player.interpreter.domain.commands.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Maps input strings (characters) to their corresponding executable {@link Command}s.
 *
 * <p>This class acts as a dictionary for the interpreter, dictating which musical action or
 * configuration update should be executed based on the current text input.
 */
public final class DomainMapper {
  private final Map<String, Command> table;
  private final List<String> playedCommands;

  /**
   * Constructs a new {@code DomainMapper} with the specified command mapping.
   *
   * @param table a map connecting string inputs to their respective {@link Command} objects
   * @throws NullPointerException if the provided table is {@code null}
   */
  public DomainMapper(final Map<String, Command> table) throws NullPointerException {
    this.table = Objects.requireNonNull(table, "Command table cannot be null");
    this.playedCommands = new ArrayList<>();
  }

  /**
   * Retrieves the command associated with the given input string.
   *
   * @param c the input string (or character) to map
   * @return the corresponding {@link Command}, or {@code null} if no mapping exists for the input
   */
  public Command toDomain(final String c) {
    return table.get(c);
  }

  /**
   * Checks whether the given input string has a mapped command in this mapper.
   *
   * @param c the input string (or character) to check
   * @return {@code true} if a mapping exists for the input, {@code false} otherwise
   */
  boolean supports(final String c) {
    return this.table.containsKey(c);
  }
}
