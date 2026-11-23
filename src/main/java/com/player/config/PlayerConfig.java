package com.player.config;

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
   * Copies another configuration, including its voice base parameters.
   *
   * @param source configuration to copy
   */
  public PlayerConfig(final PlayerConfig source) {
    final PlayerConfig config = Objects.requireNonNull(source, "PlayerConfig cannot be null");
    this.instrument = config.instrument;
    this.octave = config.octave;
    this.volume = config.volume;
    this.bpm = config.bpm;
    this.baseOctave = config.baseOctave;
    this.baseVolume = config.baseVolume;
    this.baseInstrument = config.baseInstrument;
  }

  private PlayerConfig(
      final int instrument,
      final int octave,
      final int volume,
      final int bpm,
      final int baseOctave,
      final int baseVolume,
      final int baseInstrument) {
    this.baseOctave = Math.clamp(baseOctave, MIN_OCTAVE, MAX_OCTAVE);
    this.baseVolume = Math.clamp(baseVolume, MIN_VOLUME, MAX_VOLUME);
    this.baseInstrument = Math.clamp(baseInstrument, MIN_INSTRUMENT, MAX_INSTRUMENT);
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
    final Voice voice = Voice.forIndex(voiceIndex);
    final int baseOct = voice.getBaseOctave();
    final int baseVol = voice.getBaseVolume();
    final int baseInst = voice.getInstrument().getProgram();
    return new PlayerConfig(baseInst, baseOct, baseVol, bpm, baseOct, baseVol, baseInst);
  }

  /**
   * Creates this configuration's voice-specific settings, preserving its relative octave shift.
   *
   * @param voiceIndex index of the voice (0 = soprano, 1 = alto, 2 = tenor, 3 = bass)
   * @return initialized voice-specific configuration
   */
  public PlayerConfig forVoice(final int voiceIndex) {
    final PlayerConfig voiceConfig = forVoice(voiceIndex, bpm);
    voiceConfig.setOctave(voiceConfig.octave + octave - baseOctave);
    return voiceConfig;
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

  /** Raises the current octave by one, returning to the voice base octave at the upper limit. */
  public void increaseOctaveRelative() {
    this.octave = this.octave == MAX_OCTAVE ? baseOctave : this.octave + 1;
  }

  /** Lowers the current octave by one, returning to the voice base octave at the lower limit. */
  public void decreaseOctaveRelative() {
    this.octave = this.octave == MIN_OCTAVE ? baseOctave : this.octave - 1;
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
