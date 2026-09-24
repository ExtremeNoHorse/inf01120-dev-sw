package com.player.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerConfigTest {

  @Test
  void givenValidParameters_whenConstructed_thenValuesAreSet() {
    // Given
    final int instrument = 1;
    final int octave = 2;
    final int volume = 10;
    final int bpm = 100;

    // When
    final PlayerConfig config = new PlayerConfig(instrument, octave, volume, bpm);

    // Then
    assertEquals(instrument, config.getInstrument());
    assertEquals(octave, config.getOctave());
    assertEquals(volume, config.getVolume());
    assertEquals(bpm, config.getBpm());
  }

  @Test
  void givenConfig_whenSettersCalled_thenValuesAreUpdated() {
    // Given
    final PlayerConfig config = new PlayerConfig(0, 0, 0, 0);

    // When
    config.setInstrument(1);
    config.setOctave(2);
    config.setVolume(10);
    config.setBpm(100);

    // Then
    assertEquals(1, config.getInstrument());
    assertEquals(2, config.getOctave());
    assertEquals(10, config.getVolume());
    assertEquals(100, config.getBpm());
  }
}
