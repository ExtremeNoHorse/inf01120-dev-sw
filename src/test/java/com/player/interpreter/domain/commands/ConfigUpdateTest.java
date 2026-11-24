package com.player.interpreter.domain.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    final SoundToken result = update.execute(config);

    // Then
    assertEquals("I10", result.token());
    assertNull(result.duration());
    assertEquals(1, config.getOctave());
  }

  @Test
  void givenActionReturningEmptyOrNull_whenExecute_thenReturnsEmptyOrNull() {
    // Given
    final PlayerConfig config = new PlayerConfig(1, 2, 10, 100);
    final ConfigUpdate emptyUpdate = new ConfigUpdate(cfg -> "");
    final ConfigUpdate nullUpdate = new ConfigUpdate(cfg -> null);

    // When & Then
    assertEquals(SoundToken.EMPTY, emptyUpdate.execute(config));
    assertNull(nullUpdate.execute(config));
  }
}
