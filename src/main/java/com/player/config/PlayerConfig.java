package com.player.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration settings for musical playback, including instrument selection,
 * octave, volume, and tempo (BPM).
 *
 * <p>Supports voice-specific base parameters for polyphony in Phase 2.
 */
public final class PlayerConfig {
  public static final int DEFAULT_BPM = 120;
  public static final int MIN_BPM = 20;
  public static final int MAX_BPM = 300;
  public static final int BPM_DELTA = 10;

  public static final int MIN_VOLUME = 0;
  public static final int MAX_VOLUME = 127;

  public static final int MIN_OCTAVE = 0;
  public static final int MAX_OCTAVE = 9;

  public static final int MIN_INSTRUMENT = 0;
  public static final int MAX_INSTRUMENT = 127;

  public static final int VOICE_COUNT = 4;

  // General MIDI Instrument Constants
  public static final int INSTRUMENT_PIANO = 0;
  public static final int INSTRUMENT_HARPSICHORD = 6;
  public static final int INSTRUMENT_MARIMBA = 12;
  public static final int INSTRUMENT_XYLOPHONE = 13;
  public static final int INSTRUMENT_TUBULAR_BELLS = 15;
  public static final int INSTRUMENT_CHURCH_ORGAN = 20;
  public static final int INSTRUMENT_BANDONEON = 24;
  public static final int INSTRUMENT_GUITAR_ACOUSTIC = 25;
  public static final int INSTRUMENT_GUITAR_ELECTRIC = 27;
  public static final int INSTRUMENT_BASS_ACOUSTIC = 32;
  public static final int INSTRUMENT_VIOLIN = 40;
  public static final int INSTRUMENT_CELLO = 42;
  public static final int INSTRUMENT_TIMPANI = 47;
  public static final int INSTRUMENT_CHOIR_AAHS = 52;
  public static final int INSTRUMENT_TRUMPET = 56;
  public static final int INSTRUMENT_TROMBONE = 57;
  public static final int INSTRUMENT_FRENCH_HORN = 60;
  public static final int INSTRUMENT_ALTO_SAX = 65;
  public static final int INSTRUMENT_OBOE = 68;
  public static final int INSTRUMENT_BASSOON = 70;
  public static final int INSTRUMENT_CLARINET = 71;
  public static final int INSTRUMENT_FLUTE = 73;
  public static final int INSTRUMENT_SYNTH_LEAD = 80;
  public static final int INSTRUMENT_BAGPIPE = 110;
  public static final int INSTRUMENT_AGOGO = 114;
  public static final int INSTRUMENT_SEASHORE = 123;

  private static final int OCTAVE_VOICE_0 = 6;
  private static final int OCTAVE_VOICE_1 = 5;
  private static final int OCTAVE_VOICE_2 = 4;
  private static final int OCTAVE_VOICE_3 = 3;

  private static final int VOLUME_VOICE_0 = 100;
  private static final int VOLUME_VOICE_1 = 80;
  private static final int VOLUME_VOICE_2 = 60;
  private static final int VOLUME_VOICE_3 = 40;

  private static final int[] BASE_OCTAVES = {
    OCTAVE_VOICE_0, OCTAVE_VOICE_1, OCTAVE_VOICE_2, OCTAVE_VOICE_3
  };
  private static final int[] BASE_VOLUMES = {
    VOLUME_VOICE_0, VOLUME_VOICE_1, VOLUME_VOICE_2, VOLUME_VOICE_3
  };
  private static final int[] BASE_INSTRUMENTS = {
    INSTRUMENT_HARPSICHORD, INSTRUMENT_CHURCH_ORGAN, INSTRUMENT_PIANO, INSTRUMENT_BASSOON
  };

  private static final Map<Integer, String> GENERAL_MIDI_INSTRUMENTS;

  static {
    final Map<Integer, String> map = new LinkedHashMap<>();
    map.put(INSTRUMENT_PIANO, "Acoustic Grand Piano");
    map.put(INSTRUMENT_HARPSICHORD, "Harpsichord");
    map.put(INSTRUMENT_MARIMBA, "Marimba");
    map.put(INSTRUMENT_XYLOPHONE, "Xylophone");
    map.put(INSTRUMENT_TUBULAR_BELLS, "Tubular Bells");
    map.put(INSTRUMENT_CHURCH_ORGAN, "Church Organ");
    map.put(INSTRUMENT_BANDONEON, "Bandoneon");
    map.put(INSTRUMENT_GUITAR_ACOUSTIC, "Acoustic Guitar");
    map.put(INSTRUMENT_GUITAR_ELECTRIC, "Electric Guitar");
    map.put(INSTRUMENT_BASS_ACOUSTIC, "Acoustic Bass");
    map.put(INSTRUMENT_VIOLIN, "Violin");
    map.put(INSTRUMENT_CELLO, "Cello");
    map.put(INSTRUMENT_TIMPANI, "Timpani");
    map.put(INSTRUMENT_CHOIR_AAHS, "Choir Aahs");
    map.put(INSTRUMENT_TRUMPET, "Trumpet");
    map.put(INSTRUMENT_TROMBONE, "Trombone");
    map.put(INSTRUMENT_FRENCH_HORN, "French Horn");
    map.put(INSTRUMENT_ALTO_SAX, "Alto Sax");
    map.put(INSTRUMENT_OBOE, "Oboe");
    map.put(INSTRUMENT_BASSOON, "Bassoon");
    map.put(INSTRUMENT_CLARINET, "Clarinet");
    map.put(INSTRUMENT_FLUTE, "Flute");
    map.put(INSTRUMENT_SYNTH_LEAD, "Lead 1 (square)");
    map.put(INSTRUMENT_BAGPIPE, "Bagpipe");
    map.put(INSTRUMENT_AGOGO, "Agogo");
    map.put(INSTRUMENT_SEASHORE, "Seashore");
    GENERAL_MIDI_INSTRUMENTS = Collections.unmodifiableMap(map);
  }

  private int instrument;
  private int octave;
  private int volume;
  private int bpm;

  private final int baseOctave;
  private final int baseVolume;
  private final int baseInstrument;

  /**
   * Constructs a new {@code PlayerConfig} instance with the specified parameters.
   *
   * @param instrument the identifier of the instrument to be used (0-127)
   * @param octave the octave level for playback
   * @param volume the volume level (0-127)
   * @param bpm the beats per minute (tempo) for playback
   */
  public PlayerConfig(final int instrument, final int octave, final int volume, final int bpm) {
    this(instrument, octave, volume, bpm, octave, volume, instrument);
  }

  /**
   * Constructs a new {@code PlayerConfig} instance with full base parameter specification.
   *
   * @param instrument current instrument
   * @param octave current octave
   * @param volume current volume
   * @param bpm tempo in BPM
   * @param baseOctave base octave of the current voice
   * @param baseVolume base volume of the current voice
   * @param baseInstrument base instrument of the current voice
   */
  public PlayerConfig(
      final int instrument,
      final int octave,
      final int volume,
      final int bpm,
      final int baseOctave,
      final int baseVolume,
      final int baseInstrument) {
    this.baseOctave = baseOctave;
    this.baseVolume = baseVolume;
    this.baseInstrument = baseInstrument;
    this.setInstrument(instrument);
    this.setOctave(octave);
    this.setVolume(volume);
    this.setBpm(bpm);
  }

  /**
   * Factory method creating a {@code PlayerConfig} tailored for a specific voice (0 to 3+).
   *
   * @param voiceIndex index of the voice (0 = soprano, 1 = alto, 2 = tenor, 3 = bass)
   * @param bpm the global tempo
   * @return initialized {@link PlayerConfig}
   */
  public static PlayerConfig forVoice(final int voiceIndex, final int bpm) {
    final int cycleIndex = Math.floorMod(voiceIndex, VOICE_COUNT);
    final int baseOct = BASE_OCTAVES[cycleIndex];
    final int baseVol = BASE_VOLUMES[cycleIndex];
    final int baseInst = BASE_INSTRUMENTS[cycleIndex];
    return new PlayerConfig(baseInst, baseOct, baseVol, bpm, baseOct, baseVol, baseInst);
  }

  /**
   * Returns a map containing at least 25 standard General MIDI instruments supported.
   *
   * @return unmodifiable map of instrument number to name
   */
  public static Map<Integer, String> getGeneralMidiInstruments() {
    return GENERAL_MIDI_INSTRUMENTS;
  }

  public int getInstrument() {
    return instrument;
  }

  /**
   * Sets a new instrument identifier for the player with boundary clamping.
   *
   * @param instrument the new instrument identifier
   */
  public void setInstrument(final int instrument) {
    this.instrument = Math.clamp(instrument, MIN_INSTRUMENT, MAX_INSTRUMENT);
  }

  public int getOctave() {
    return octave;
  }

  /**
   * Sets a new octave level for playback.
   *
   * @param octave the new octave level
   */
  public void setOctave(final int octave) {
    this.octave = Math.clamp(octave, MIN_OCTAVE, MAX_OCTAVE);
  }

  public int getVolume() {
    return volume;
  }

  /**
   * Sets a new volume level for playback with boundary clamping.
   *
   * @param volume the new volume level (0-127)
   */
  public void setVolume(final int volume) {
    this.volume = Math.clamp(volume, MIN_VOLUME, MAX_VOLUME);
  }

  public int getBpm() {
    return bpm;
  }

  /**
   * Sets tempo in BPM with boundary clamping.
   *
   * @param bpm tempo in BPM
   */
  public void setBpm(final int bpm) {
    this.bpm = Math.clamp(bpm, MIN_BPM, MAX_BPM);
  }

  public int getBaseOctave() {
    return baseOctave;
  }

  public int getBaseVolume() {
    return baseVolume;
  }

  public int getBaseInstrument() {
    return baseInstrument;
  }

  /** Raises the current octave by one, clamped to the supported octave range. */
  public void increaseOctaveRelative() {
    this.octave = Math.clamp(this.octave + 1, MIN_OCTAVE, MAX_OCTAVE);
  }

  /** Lowers the current octave by one, clamped to the supported octave range. */
  public void decreaseOctaveRelative() {
    this.octave = Math.clamp(this.octave - 1, MIN_OCTAVE, MAX_OCTAVE);
  }

  /** Doubles current volume, clamping to maximum allowable volume (127). */
  public void doubleVolume() {
    final int doubled = this.volume * 2;
    this.volume = Math.clamp(doubled, MIN_VOLUME, MAX_VOLUME);
  }

  /**
   * Increases global BPM by delta.
   *
   * @param delta beats to add
   */
  public void increaseBpm(final int delta) {
    this.setBpm(this.bpm + delta);
  }

  /**
   * Decreases global BPM by delta.
   *
   * @param delta beats to subtract
   */
  public void decreaseBpm(final int delta) {
    this.setBpm(this.bpm - delta);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof PlayerConfig other)) {
      return false;
    }
    return this.instrument == other.instrument
        && this.octave == other.octave
        && this.volume == other.volume
        && this.bpm == other.bpm;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.instrument, this.octave, this.volume, this.bpm);
  }
}
