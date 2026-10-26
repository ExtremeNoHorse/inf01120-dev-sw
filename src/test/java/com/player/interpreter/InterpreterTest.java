package com.player.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.domain.DomainMapper;
import com.player.interpreter.domain.commands.Command;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InterpreterTest {

  private Interpreter interpreter;

  @BeforeEach
  void setUp() {
    interpreter = new Interpreter();
  }

  @Test
  void givenNullInput_whenInterpret_thenThrowsTtmException() {
    // Given
    final String nullText = null;

    // When & Then
    final TtmException ex = assertThrows(TtmException.class, () -> interpreter.interpret(nullText));
    assertEquals(ErrorCode.INVALID_INPUT, ex.getCode());
  }

  @Test
  void givenEmptyInput_whenInterpret_thenReturnsEmptyString() {
    // Given & When & Then
    assertEquals("", interpreter.interpret(""));
    assertEquals("", interpreter.interpret("   "));
  }

  @Test
  void givenSingleLineNotesWithoutSpaces_whenInterpret_thenBuildsVoice0WithDefaultVolume() {
    // Given
    final String input = "C:3D:3E:3";

    // When
    final String result = interpreter.interpret(input);

    // Then
    assertTrue(result.startsWith("T120 V0 I6 "));
    assertTrue(result.contains("C6qa100"));
    assertTrue(result.contains("D6qa100"));
    assertTrue(result.contains("E6qa100"));
  }

  @Test
  void givenSpacesBetweenNotes_whenInterpret_thenDoublesVolumePerMappingRule() {
    // Given
    final String input = "C:3 D:3";

    // When
    final String result = interpreter.interpret(input);

    // Then
    assertTrue(result.contains("C6qa100"));
    assertTrue(result.contains("D6qa127"));
  }

  @Test
  void givenMultipleLines_whenInterpret_thenGeneratesPolyphonicVoices() {
    // Given
    final String text = "C:3\nE:3\nG:3\nC:1";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("V0 I6 C6qa100"));
    assertTrue(result.contains("V1 I20 E5qa80"));
    assertTrue(result.contains("V2 I0 G4qa60"));
    assertTrue(result.contains("V3 I70 C3wa40"));
  }

  @Test
  void givenBlankVoiceLine_whenInterpret_thenSkipsIt() {
    final String text = String.join(System.lineSeparator(), "C:3", "   ", "D:3");
    final String result = interpreter.interpret(text);

    assertTrue(result.contains("V0 I6 C6qa100"));
    assertFalse(result.contains("V1 I20"));
    assertTrue(result.contains("V2 I0 D4qa60"));
  }

  @Test
  void givenDelayPrefix_whenInterpret_thenInsertsInitialRestBeats() {
    // Given
    final String text = "[3] A:3";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("V0 I6 Rq Rq Rq A6qa100"));
  }

  @Test
  void givenNotesWithAccidentals_whenInterpret_thenGeneratesCorrectJfugueAccidentals() {
    // Given
    final String text = "A#:3Bb:2";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("A#6qa100"));
    assertTrue(result.contains("Bb6ha100"));
  }

  @Test
  void givenConsecutiveConsonants_whenPreviousWasNote_thenRepeatsLastNote() {
    // Given
    final String text = "C:3H";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("C6qa100 C6qa100"));
  }

  @Test
  void givenConsonantsWithoutPreviousNote_whenInterpret_thenProducesRest() {
    // Given
    final String text = "HC:3";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("Rq C6qa100"));
  }

  @Test
  void givenLowercaseNoteLetters_whenInterpret_thenProducesRests() {
    // Given
    final String text = "ab";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("Rq Rq"));
  }

  @Test
  void givenOctaveAndTempoAndInstrumentChanges_whenInterpret_thenAppliesUpdates() {
    // Given
    final String text = ">!?C";

    // When
    final String result = interpreter.interpret(text);

    // Then
    assertTrue(result.contains("T130"));
    assertTrue(result.contains("I24"));
    assertTrue(result.contains("C7a100"));
  }

  @Test
  void givenMappedNullCommand_whenInterpret_thenSkipsTheToken() {
    final Map<String, Command> table = new HashMap<>();
    table.put("C", null);
    final Interpreter customInterpreter =
        new Interpreter(
            new DomainMapper(table), PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));

    assertEquals("T120 V0 I6", customInterpreter.interpret("C"));
  }

  @Test
  void givenCommandReturningNull_whenInterpret_thenSkipsItsResult() {
    final Map<String, Command> table = new HashMap<>();
    table.put("?", config -> null);
    final Interpreter customInterpreter =
        new Interpreter(
            new DomainMapper(table), PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));

    assertEquals("T120 V0 I6", customInterpreter.interpret("?"));
  }

  @Test
  void givenLowercaseOutsideNoteRange_whenInterpret_thenProducesRest() {
    assertTrue(interpreter.interpret("h").contains("Rq"));
  }
}
