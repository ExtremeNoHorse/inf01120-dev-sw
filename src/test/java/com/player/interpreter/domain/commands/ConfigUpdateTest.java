package com.player.interpreter.domain.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.player.config.PlayerConfig;
import org.junit.jupiter.api.Test;

class ConfigUpdateTest {

  @Test
  void givenNullAction_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new ConfigUpdate(null));
  }

  @Test
  void givenValidAction_whenExecute_thenUpdatesConfigAndReturnsString() {
    // Given
    final PlayerConfig config = new PlayerConfig(1, 2, 10, 100);
    final ConfigUpdate update =
        new ConfigUpdate(
            cfg -> {
              cfg.setOctave(1);
              return "I10";
            });

    // When
    final String result = update.execute(config);

    // Then
    assertEquals("I10", result);
    assertEquals(1, config.getOctave());
  }
}
