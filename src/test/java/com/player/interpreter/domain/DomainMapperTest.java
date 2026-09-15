package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.interpreter.domain.commands.Command;
import com.player.interpreter.domain.commands.Note;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainMapperTest {

  @Test
  void givenNullTable_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new DomainMapper(null));
  }

  @Test
  void givenValidTable_whenToDomain_thenReturnsMappedCommand() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    final Command noteCommand = new Note("A");
    table.put("A", noteCommand);
    final DomainMapper mapper = new DomainMapper(table);

    // When
    final Command result = mapper.toDomain("A");

    // Then
    assertEquals(noteCommand, result);
  }

  @Test
  void givenMissingKey_whenToDomain_thenReturnsNull() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    final DomainMapper mapper = new DomainMapper(table);

    // When
    final Command result = mapper.toDomain("B");

    // Then
    assertNull(result);
  }

  @Test
  void givenValidTable_whenSupports_thenReturnsTrueForExistingKey() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    table.put("C", new Note("C"));
    final DomainMapper mapper = new DomainMapper(table);

    // When
    final boolean result = mapper.supports("C");

    // Then
    assertTrue(result);
  }

  @Test
  void givenValidTable_whenSupports_thenReturnsFalseForMissingKey() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    final DomainMapper mapper = new DomainMapper(table);

    // When
    final boolean result = mapper.supports("D");

    // Then
    assertFalse(result);
  }
}
