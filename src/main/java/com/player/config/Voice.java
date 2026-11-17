package com.player.config;

/** Default playback settings for each of the four polyphonic voices. */
public enum Voice {
  SOPRANO(6, 100, Instrument.HARPSICHORD),
  ALTO(5, 80, Instrument.CHURCH_ORGAN),
  TENOR(4, 60, Instrument.PIANO),
  BASS(3, 40, Instrument.BASSOON);

  private static final Voice[] VOICES = values();

  private final int baseOctave;
  private final int baseVolume;
  private final Instrument instrument;

  Voice(final int baseOctave, final int baseVolume, final Instrument instrument) {
    this.baseOctave = baseOctave;
    this.baseVolume = baseVolume;
    this.instrument = instrument;
  }

  static Voice forIndex(final int voiceIndex) {
    return VOICES[Math.floorMod(voiceIndex, VOICES.length)];
  }

  public int getBaseOctave() {
    return baseOctave;
  }

  public int getBaseVolume() {
    return baseVolume;
  }

  public Instrument getInstrument() {
    return instrument;
  }
}
