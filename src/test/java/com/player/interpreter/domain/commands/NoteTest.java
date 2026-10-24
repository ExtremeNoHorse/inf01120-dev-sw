package com.player.interpreter.domain.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.player.config.PlayerConfig;
import com.player.interpreter.domain.MusicalDuration;
import org.junit.jupiter.api.Test;

class NoteTest {

  @Test
  void givenNullCommand_whenConstructed_thenThrowsNullPointerException() {
    // Given
    final String nullCmd = null;

    // When & Then
    assertThrows(NullPointerException.class, () -> new Note(nullCmd));
    assertThrows(NullPointerException.class, () -> new Note(nullCmd, MusicalDuration.QUARTER_NOTE));
  }

  @Test
  void givenValidNoteAndConfig_whenExecute_thenReturnsFormattedToken() {
    // Given
    final Note note = new Note("C");
    final PlayerConfig config = new PlayerConfig(1, 2, 10, 100);

    // When
    final String result = note.execute(config);

    // Then
    assertEquals("C2a10", result);
    assertEquals("C", note.getCmd());
    assertNull(note.getDuration());
  }

  @Test
  void givenNoteWithAccidentalAndDuration_whenExecute_thenReturnsFullJfugueToken() {
    // Given
    final Note sharpNote = new Note("A#", MusicalDuration.QUARTER_NOTE);
    final Note flatNote = new Note("Bb", MusicalDuration.WHOLE_NOTE);
    final PlayerConfig config = new PlayerConfig(1, 5, 80, 120);

    // When
    final String sharpResult = sharpNote.execute(config);
    final String flatResult = flatNote.execute(config);

    // Then
    assertEquals("A#5qa80", sharpResult);
    assertEquals(MusicalDuration.QUARTER_NOTE, sharpNote.getDuration());

    assertEquals("Bb5wa80", flatResult);
    assertEquals(MusicalDuration.WHOLE_NOTE, flatNote.getDuration());
  }
}
