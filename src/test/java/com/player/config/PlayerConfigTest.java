package com.player.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerConfigTest {
  private static final int TEST_OCTAVE_6 = 6;
  private static final int TEST_OCTAVE_2 = 2;
  private static final int TEST_OCTAVE_5 = 5;
  private static final int TEST_OCTAVE_4 = 4;
  private static final int TEST_OCTAVE_3 = 3;
  private static final int TEST_OCTAVE_7 = 7;
  private static final int TEST_OCTAVE_8 = 8;

  private static final int TEST_VOL_100 = 100;
  private static final int TEST_VOL_3 = 3;
  private static final int TEST_VOL_4 = 4;
  private static final int TEST_VOL_80 = 80;
  private static final int TEST_VOL_60 = 60;
  private static final int TEST_VOL_40 = 40;
  private static final int TEST_VOL_127 = 127;

  private static final int TEST_BPM_130 = 130;
  private static final int TEST_BPM_120 = 120;
  private static final int TEST_BPM_121 = 121;
  private static final int TEST_HIGH_BPM = 999;
  private static final int TEST_NEG_BPM = -10;

  private static final int MIN_GM_INSTRUMENTS = 25;
  private static final int TEST_INSTRUMENT_1 = 1;
  private static final int TEST_INSTRUMENT_2 = 2;

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

  @Test
  void givenVoiceIndex_whenForVoice_thenConfiguresAccordingToSpecs() {
    // Given & When
    final PlayerConfig v0 = PlayerConfig.forVoice(0, TEST_BPM_120);
    final PlayerConfig v1 = PlayerConfig.forVoice(1, TEST_BPM_120);
    final PlayerConfig v2 = PlayerConfig.forVoice(2, TEST_BPM_120);
    final PlayerConfig v3 = PlayerConfig.forVoice(3, TEST_BPM_120);
    final PlayerConfig v4 = PlayerConfig.forVoice(4, TEST_BPM_120);

    // Then
    assertEquals(TEST_OCTAVE_6, v0.getOctave());
    assertEquals(TEST_VOL_100, v0.getVolume());
    assertEquals(PlayerConfig.INSTRUMENT_HARPSICHORD, v0.getInstrument());

    assertEquals(TEST_OCTAVE_5, v1.getOctave());
    assertEquals(TEST_VOL_80, v1.getVolume());
    assertEquals(PlayerConfig.INSTRUMENT_CHURCH_ORGAN, v1.getInstrument());

    assertEquals(TEST_OCTAVE_4, v2.getOctave());
    assertEquals(TEST_VOL_60, v2.getVolume());
    assertEquals(PlayerConfig.INSTRUMENT_PIANO, v2.getInstrument());

    assertEquals(TEST_OCTAVE_3, v3.getOctave());
    assertEquals(TEST_VOL_40, v3.getVolume());
    assertEquals(PlayerConfig.INSTRUMENT_BASSOON, v3.getInstrument());

    assertEquals(TEST_OCTAVE_6, v4.getOctave());
    assertEquals(TEST_VOL_100, v4.getVolume());
    assertEquals(PlayerConfig.INSTRUMENT_HARPSICHORD, v4.getInstrument());
  }

  @Test
  void givenRelativeOctaveChanges_whenCalled_thenAccumulatesWithinBounds() {
    // Given
    final PlayerConfig v0 = PlayerConfig.forVoice(0, TEST_BPM_120);
    assertEquals(TEST_OCTAVE_6, v0.getOctave());

    // When & Then
    v0.increaseOctaveRelative();
    assertEquals(TEST_OCTAVE_7, v0.getOctave());

    v0.increaseOctaveRelative();
    assertEquals(TEST_OCTAVE_8, v0.getOctave());

    v0.decreaseOctaveRelative();
    assertEquals(TEST_OCTAVE_7, v0.getOctave());

    v0.decreaseOctaveRelative();
    assertEquals(TEST_OCTAVE_6, v0.getOctave());
  }

  @Test
  void givenOctaveAtBoundary_whenChanged_thenRemainsWithinBounds() {
    // Given
    final PlayerConfig maxConfig =
        new PlayerConfig(0, PlayerConfig.MAX_OCTAVE, TEST_VOL_40, TEST_BPM_120);
    final PlayerConfig minConfig =
        new PlayerConfig(0, PlayerConfig.MIN_OCTAVE, TEST_VOL_40, TEST_BPM_120);

    // When
    maxConfig.increaseOctaveRelative();
    minConfig.decreaseOctaveRelative();

    // Then
    assertEquals(PlayerConfig.MAX_OCTAVE, maxConfig.getOctave());
    assertEquals(PlayerConfig.MIN_OCTAVE, minConfig.getOctave());
  }

  @Test
  void givenVolumeOperations_whenDoubleVolume_thenClampsToMax() {
    // Given
    final PlayerConfig config = new PlayerConfig(0, 4, 70, TEST_BPM_120);
    final PlayerConfig lowConfig = new PlayerConfig(0, 4, 20, TEST_BPM_120);

    // When
    config.doubleVolume();
    lowConfig.doubleVolume();

    // Then
    assertEquals(TEST_VOL_127, config.getVolume());
    assertEquals(TEST_VOL_40, lowConfig.getVolume());
  }

  @Test
  void givenBpmChanges_whenIncreasedOrDecreased_thenClampsToRange() {
    // Given
    final PlayerConfig config = new PlayerConfig(0, 4, 50, TEST_BPM_120);

    // When & Then
    config.increaseBpm(10);
    assertEquals(TEST_BPM_130, config.getBpm());

    config.decreaseBpm(10);
    assertEquals(TEST_BPM_120, config.getBpm());

    config.setBpm(TEST_HIGH_BPM);
    assertEquals(PlayerConfig.MAX_BPM, config.getBpm());

    config.setBpm(TEST_NEG_BPM);
    assertEquals(PlayerConfig.MIN_BPM, config.getBpm());
  }

  @Test
  void givenGeneralMidiCatalog_whenQueried_thenContainsAtLeast25Instruments() {
    // Given & When
    final var catalog = PlayerConfig.getGeneralMidiInstruments();

    // Then
    assertTrue(catalog.size() >= MIN_GM_INSTRUMENTS);
    assertTrue(catalog.containsKey(PlayerConfig.INSTRUMENT_CHURCH_ORGAN));
    assertTrue(catalog.containsKey(PlayerConfig.INSTRUMENT_TUBULAR_BELLS));
  }

  @Test
  void givenConfigs_whenCompared_thenCoversEqualityBranches() {
    final PlayerConfig config =
        new PlayerConfig(TEST_INSTRUMENT_1, TEST_OCTAVE_2, TEST_VOL_3, TEST_BPM_120);

    assertTrue(config.equals(config));
    assertFalse(config.equals(new Object()));
    assertFalse(
        config.equals(
            new PlayerConfig(TEST_INSTRUMENT_2, TEST_OCTAVE_2, TEST_VOL_3, TEST_BPM_120)));
    assertFalse(
        config.equals(
            new PlayerConfig(TEST_INSTRUMENT_1, TEST_OCTAVE_3, TEST_VOL_3, TEST_BPM_120)));
    assertFalse(
        config.equals(
            new PlayerConfig(TEST_INSTRUMENT_1, TEST_OCTAVE_2, TEST_VOL_4, TEST_BPM_120)));
    assertFalse(
        config.equals(
            new PlayerConfig(TEST_INSTRUMENT_1, TEST_OCTAVE_2, TEST_VOL_3, TEST_BPM_121)));
    assertEquals(config.hashCode(), config.hashCode());
  }
}
