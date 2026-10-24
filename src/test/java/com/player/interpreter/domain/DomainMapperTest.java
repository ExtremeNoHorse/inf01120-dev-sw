package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.config.PlayerConfig;
import com.player.interpreter.domain.commands.Command;
import com.player.interpreter.domain.commands.Note;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DomainMapperTest {
  private static final int INITIAL_BPM = 120;
  private static final int FASTER_BPM = 130;
  private static final int RAISED_OCTAVE = 7;
  private static final int BASE_OCTAVE = 6;
  private static final int MAX_VOLUME = 127;
  private static final int BASE_INST_TEST = 10;
  private static final String INST_PREFIX = "I";
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

  @Test
  void givenDefaultMapper_whenCheckingPhase2Mappings_thenAllCommandsFunctionCorrectly() {
    // Given
    final DomainMapper mapper = DomainMapper.createDefault();
    final PlayerConfig config = PlayerConfig.forVoice(0, INITIAL_BPM);

    // When & Then - Notes
    assertTrue(mapper.supports(NOTE_A));
    assertTrue(mapper.supports("G"));
    assertTrue(mapper.supports("A#:3"));
    assertTrue(mapper.supports("Bb:2"));
    assertTrue(mapper.supports("C:1"));

    final Command noteWithDur = mapper.toDomain("A#:3");
    assertNotNull(noteWithDur);
    assertEquals("A#6qa100", noteWithDur.execute(config));

    // When & Then - Tempo controls
    final Command tempoUp = mapper.toDomain(">");
    assertEquals("T" + FASTER_BPM, tempoUp.execute(config));
    assertEquals(FASTER_BPM, config.getBpm());

    final Command tempoDown = mapper.toDomain("<");
    assertEquals("T" + INITIAL_BPM, tempoDown.execute(config));
    assertEquals(INITIAL_BPM, config.getBpm());

    // When & Then - Octave controls
    final Command octUp = mapper.toDomain("?");
    octUp.execute(config);
    assertEquals(RAISED_OCTAVE, config.getOctave());

    final Command octDown = mapper.toDomain("V");
    octDown.execute(config);
    assertEquals(BASE_OCTAVE, config.getOctave());

    // When & Then - Volume
    final Command vol = mapper.toDomain(" ");
    vol.execute(config);
    assertEquals(MAX_VOLUME, config.getVolume());

    // When & Then - Instruments
    final Command bandoneon = mapper.toDomain("!");
    assertEquals(INST_PREFIX + PlayerConfig.INSTRUMENT_BANDONEON, bandoneon.execute(config));

    final Command tubular = mapper.toDomain(";");
    assertEquals(INST_PREFIX + PlayerConfig.INSTRUMENT_TUBULAR_BELLS, tubular.execute(config));

    final Command agogo = mapper.toDomain(",");
    assertEquals(INST_PREFIX + PlayerConfig.INSTRUMENT_AGOGO, agogo.execute(config));

    // When & Then - Digits
    final Command oddDigit = mapper.toDomain("3");
    assertEquals(INST_PREFIX + PlayerConfig.INSTRUMENT_TUBULAR_BELLS, oddDigit.execute(config));

    config.setInstrument(BASE_INST_TEST);
    final Command evenDigit = mapper.toDomain("4");
    assertEquals(INST_PREFIX + "14", evenDigit.execute(config));
  }
}
