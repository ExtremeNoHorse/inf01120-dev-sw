package com.player.interpreter.domain.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.player.config.PlayerConfig;
import org.junit.jupiter.api.Test;

class NoteTest {

  @Test
  void givenNullCommand_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new Note(null));
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
  }
}
