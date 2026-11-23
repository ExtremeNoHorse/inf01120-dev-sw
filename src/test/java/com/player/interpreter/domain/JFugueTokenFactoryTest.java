package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JFugueTokenFactoryTest {

  private static final int BPM_120 = 120;
  private static final int BPM_90 = 90;
  private static final int VOICE_3 = 3;
  private static final int INSTRUMENT_BANDONEON = 24;
  private static final int INSTRUMENT_MAX = 127;
  private static final int OCTAVE_5 = 5;
  private static final int OCTAVE_6 = 6;
  private static final int OCTAVE_3 = 3;
  private static final int VELOCITY_80 = 80;
  private static final int VELOCITY_40 = 40;

  @Test
  void givenBpm_whenTempo_thenReturnsTempoToken() {
    // Given & When & Then
    assertEquals("T120", JFugueTokenFactory.tempo(BPM_120));
    assertEquals("T90", JFugueTokenFactory.tempo(BPM_90));
  }

  @Test
  void givenVoiceIndex_whenVoice_thenReturnsVoiceToken() {
    // Given & When & Then
    assertEquals("V0", JFugueTokenFactory.voice(0));
    assertEquals("V3", JFugueTokenFactory.voice(VOICE_3));
  }

  @Test
  void givenInstrumentId_whenInstrument_thenReturnsInstrumentToken() {
    // Given & When & Then
    assertEquals("I0", JFugueTokenFactory.instrument(0));
    assertEquals("I24", JFugueTokenFactory.instrument(INSTRUMENT_BANDONEON));
    assertEquals("I127", JFugueTokenFactory.instrument(INSTRUMENT_MAX));
  }

  @Test
  void givenVolume_whenChannelVolume_thenReturnsControllerToken() {
    // Given & When & Then
    assertEquals(":CE(7,100)", JFugueTokenFactory.channelVolume(100));
    assertEquals(":CE(7,0)", JFugueTokenFactory.channelVolume(0));
  }

  @Test
  void whenRest_thenReturnsQuarterNoteRest() {
    // Given & When & Then
    assertEquals("Rq", JFugueTokenFactory.rest());
  }

  @Test
  void givenNoteComponents_whenNote_thenReturnsFullNoteToken() {
    // Given & When & Then
    assertEquals("C5qa80", JFugueTokenFactory.note("C", OCTAVE_5, "q", VELOCITY_80));
    assertEquals("A#6wa100", JFugueTokenFactory.note("A#", OCTAVE_6, "w", 100));
    assertEquals("Bb3a40", JFugueTokenFactory.note("Bb", OCTAVE_3, "", VELOCITY_40));
  }
}
