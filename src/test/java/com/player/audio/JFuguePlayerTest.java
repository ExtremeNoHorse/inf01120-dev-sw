package com.player.audio;

import static org.junit.jupiter.api.Assertions.*;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import org.junit.jupiter.api.Test;

class JFuguePlayerTest {

  @Test
  void givenNullPlayer_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new JFuguePlayer(null));
  }

  @Test
  void givenDefaultConstructor_whenCreated_thenManagedPlayerIsAvailable() {
    // Given & When
    final JFuguePlayer player = new JFuguePlayer();

    // Then
    assertNotNull(player.getManagedPlayer());
  }

  @Test
  void givenPlayer_whenPlaybackControlsInvoked_thenDoesNotThrowExceptions() {
    // Given
    final JFuguePlayer player = new JFuguePlayer();

    // When & Then
    assertDoesNotThrow(() -> player.play(null));
    assertDoesNotThrow(() -> player.play(""));

    TtmException exception = assertThrows(TtmException.class, player::pause);
    assertEquals(ErrorCode.AUDIO_ENGINE_FAILURE, exception.getCode());
    assertEquals("Environment does not have an Audio Device", exception.getMessage());
    assertInstanceOf(NullPointerException.class, exception.getCause());

    exception = assertThrows(TtmException.class, player::resume);
    assertEquals(ErrorCode.AUDIO_ENGINE_FAILURE, exception.getCode());
    assertEquals("Environment does not have an Audio Device", exception.getMessage());
    assertInstanceOf(NullPointerException.class, exception.getCause());

    exception = assertThrows(TtmException.class, player::stop);
    assertEquals(ErrorCode.AUDIO_ENGINE_FAILURE, exception.getCode());
    assertEquals("Environment does not have an Audio Device", exception.getMessage());
    assertInstanceOf(NullPointerException.class, exception.getCause());

    exception = assertThrows(TtmException.class, () -> player.seek(0));
    assertEquals(ErrorCode.AUDIO_ENGINE_FAILURE, exception.getCode());
    assertEquals("Environment does not have an Audio Device", exception.getMessage());
    assertInstanceOf(NullPointerException.class, exception.getCause());
  }
}
