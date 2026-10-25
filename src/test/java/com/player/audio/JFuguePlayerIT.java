package com.player.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JFuguePlayerIT {

  @Test
  void givenNullPlayer_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new JFuguePlayer(null));
  }

  @Test
  void givenPlayer_whenPlaybackControlsInvoked_thenDoesNotThrowExceptions() {
    // Given
    final JFuguePlayer player = new JFuguePlayer();

    // When & Then
    assertDoesNotThrow(() -> player.play(null));
    assertDoesNotThrow(() -> player.play(""));
    assertDoesNotThrow(player::pause);
    assertDoesNotThrow(player::resume);
    assertDoesNotThrow(player::stop);
    assertDoesNotThrow(() -> player.seek(0));
  }
}
