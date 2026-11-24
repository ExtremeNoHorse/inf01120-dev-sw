package com.player.interpreter.domain.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.interpreter.domain.MusicalDuration;
import org.junit.jupiter.api.Test;

class SoundTokenTest {

  @Test
  void givenNullToken_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new SoundToken(null, null));
  }

  @Test
  void givenNonSoundingToken_whenChecked_thenDurationIsNullAndNotSound() {
    // Given
    final SoundToken token = SoundToken.of("T120");

    // When & Then
    assertEquals("T120", token.token());
    assertNull(token.duration());
    assertFalse(token.isSound());
  }

  @Test
  void givenSoundingToken_whenChecked_thenHasDurationAndIsSound() {
    // Given
    final SoundToken token = SoundToken.of("C5qa80", MusicalDuration.QUARTER_NOTE);

    // When & Then
    assertEquals("C5qa80", token.token());
    assertEquals(MusicalDuration.QUARTER_NOTE, token.duration());
    assertTrue(token.isSound());
  }

  @Test
  void whenRest_thenReturnsQuarterNoteDuration() {
    // Given & When
    final SoundToken rest = SoundToken.rest();

    // Then
    assertEquals("Rq", rest.token());
    assertEquals(MusicalDuration.QUARTER_NOTE, rest.duration());
    assertTrue(rest.isSound());
  }

  @Test
  void whenEmpty_thenHasEmptyTokenAndNoDuration() {
    // Given & When & Then
    assertEquals("", SoundToken.EMPTY.token());
    assertNull(SoundToken.EMPTY.duration());
    assertFalse(SoundToken.EMPTY.isSound());
  }
}
