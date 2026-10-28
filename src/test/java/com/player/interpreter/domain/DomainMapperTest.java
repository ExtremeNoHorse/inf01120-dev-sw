package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.interpreter.domain.commands.Command;
import com.player.interpreter.domain.commands.ConfigUpdate;
import com.player.interpreter.domain.commands.Note;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DomainMapperTest {
  private static final String NOTE_A = "A";

  @Test
  void givenNullTable_whenConstructed_thenThrowsNullPointerException() {
    // Given, When & Then
    assertThrows(NullPointerException.class, () -> new DomainMapper(null));
  }

  @Test
  void givenValidTable_whenToDomain_thenReturnsMappedCommand() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    final Command noteCommand = new Note(NOTE_A);
    table.put(NOTE_A, noteCommand);
    final DomainMapper mapper = new DomainMapper(table);

    // When
    final Command result = mapper.toDomain(NOTE_A);

    // Then
    assertEquals(noteCommand, result);
  }

  @Test
  void givenMissingKey_whenToDomain_thenReturnsNull() {
    // Given
    final Map<String, Command> table = new HashMap<>();
    final DomainMapper mapper = new DomainMapper(table);

    // When & Then
    assertNull(mapper.toDomain("invalid"));
    assertNull(mapper.toDomain(null));
    assertFalse(mapper.supports(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"A", "B", "C", "D", "E", "F", "G"})
  void givenDefaultMapper_whenMappingBaseNote_thenReturnsNoteCommand(final String token) {
    // Given
    final DomainMapper mapper = DomainMapper.createDefault();

    // When & Then
    assertTrue(mapper.supports(token));
    final Note command = assertInstanceOf(Note.class, mapper.toDomain(token));
    assertEquals(token, command.cmd());
  }

  @ParameterizedTest
  @CsvSource({"A#:3,A#,QUARTER_NOTE", "Bb:2,Bb,HALF_NOTE", "C:1,C,WHOLE_NOTE"})
  void givenDefaultMapper_whenMappingStructuredNote_thenReturnsParsedNoteCommand(
      final String token, final String expectedNote, final MusicalDuration expectedDuration) {
    // Given
    final DomainMapper mapper = DomainMapper.createDefault();

    // When & Then
    assertTrue(mapper.supports(token));
    final Note command = assertInstanceOf(Note.class, mapper.toDomain(token));
    assertEquals(expectedNote, command.cmd());
    assertEquals(expectedDuration, command.duration());
  }

  @Test
  void givenStructuredNoteWithoutDuration_whenMapped_thenDurationIsNull() {
    // Given
    final DomainMapper mapper = DomainMapper.createDefault();

    // When
    final Note command = assertInstanceOf(Note.class, mapper.toDomain("A#"));

    // Then
    assertEquals("A#", command.cmd());
    assertNull(command.duration());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?", "V", ">", "<", " ", "!", ";", ",", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
      })
  void givenDefaultMapper_whenMappingSymbols_thenReturnsSymbolCommand(final String token) {
    // Given
    final DomainMapper mapper = DomainMapper.createDefault();

    // When & Then
    assertTrue(mapper.supports(token));
    assertInstanceOf(ConfigUpdate.class, mapper.toDomain(token));
  }
}
