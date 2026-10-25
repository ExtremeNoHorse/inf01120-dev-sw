package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MusicalDurationTest {
  private static final int ID_WHOLE_NOTE = 1;
  private static final int ID_HALF_NOTE = 2;
  private static final int ID_QUARTER_NOTE = 3;
  private static final int ID_EIGHTH_NOTE = 4;
  private static final int ID_SIXTEENTH_NOTE = 5;
  private static final int ID_THIRTY_SECOND = 6;
  private static final int ID_SIXTY_FOURTH = 7;
  private static final int INVALID_ID = 99;

  @Test
  void givenDurationId_whenFromId_thenReturnsCorrectEnum() {
    // Given & When & Then
    assertEquals(MusicalDuration.WHOLE_NOTE, MusicalDuration.fromId(ID_WHOLE_NOTE));
    assertEquals("w", MusicalDuration.WHOLE_NOTE.getJfugueToken());

    assertEquals(MusicalDuration.HALF_NOTE, MusicalDuration.fromId(ID_HALF_NOTE));
    assertEquals("h", MusicalDuration.HALF_NOTE.getJfugueToken());

    assertEquals(MusicalDuration.QUARTER_NOTE, MusicalDuration.fromId(ID_QUARTER_NOTE));
    assertEquals("q", MusicalDuration.QUARTER_NOTE.getJfugueToken());

    assertEquals(MusicalDuration.EIGHTH_NOTE, MusicalDuration.fromId(ID_EIGHTH_NOTE));
    assertEquals("i", MusicalDuration.EIGHTH_NOTE.getJfugueToken());

    assertEquals(MusicalDuration.SIXTEENTH_NOTE, MusicalDuration.fromId(ID_SIXTEENTH_NOTE));
    assertEquals("s", MusicalDuration.SIXTEENTH_NOTE.getJfugueToken());

    assertEquals(MusicalDuration.THIRTY_SECOND, MusicalDuration.fromId(ID_THIRTY_SECOND));
    assertEquals("t", MusicalDuration.THIRTY_SECOND.getJfugueToken());

    assertEquals(MusicalDuration.SIXTY_FOURTH, MusicalDuration.fromId(ID_SIXTY_FOURTH));
    assertEquals("x", MusicalDuration.SIXTY_FOURTH.getJfugueToken());
  }

  @Test
  void givenInvalidId_whenFromId_thenReturnsQuarterNoteAsDefault() {
    // Given & When & Then
    assertEquals(MusicalDuration.QUARTER_NOTE, MusicalDuration.fromId(INVALID_ID));
    assertEquals(MusicalDuration.QUARTER_NOTE, MusicalDuration.fromId(0));
  }
}
