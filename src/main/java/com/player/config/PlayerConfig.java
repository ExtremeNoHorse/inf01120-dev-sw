package com.player.config;

/**
 * Represents the configuration settings for a player, including instrument selection, octave,
 * volume, and tempo (BPM).
 *
 * <p>This configuration maintains the current state of the player and can be updated dynamically
 * during execution via its setter methods.
 */
public final class PlayerConfig {
  private int instrument;
  private int octave;
  private int volume;
  private int bpm;

  /**
   * Constructs a new {@code PlayerConfig} instance with the specified parameters.
   *
   * @param instrument the identifier of the instrument to be used
   * @param octave the octave level for playback
   * @param volume the volume level (typically ranging within a specific scale)
   * @param bpm the beats per minute (tempo) for playback
   */
  public PlayerConfig(final int instrument, final int octave, final int volume, final int bpm) {
    this.setInstrument(instrument);
    this.setOctave(octave);
    this.setVolume(volume);
    this.setBpm(bpm);
  }

  /**
   * Retrieves the current instrument identifier.
   *
   * @return the identifier of the instrument
   */
  public int getInstrument() {
    return instrument;
  }

  /**
   * Sets a new instrument identifier for the player.
   *
   * @param instrument the new instrument identifier to be used
   */
  public void setInstrument(final int instrument) {
    this.instrument = instrument;
  }

  /**
   * Retrieves the current octave level.
   *
   * @return the octave level for playback
   */
  public int getOctave() {
    return octave;
  }

  /**
   * Sets a new octave level for the player.
   *
   * @param octave the new octave level for playback
   */
  public void setOctave(final int octave) {
    this.octave = octave;
  }

  /**
   * Retrieves the current volume level.
   *
   * @return the volume level
   */
  public int getVolume() {
    return volume;
  }

  /**
   * Sets a new volume level for the player.
   *
   * @param volume the new volume level (typically ranging within a specific scale)
   */
  public void setVolume(final int volume) {
    this.volume = volume;
  }

  /**
   * Retrieves the current tempo in beats per minute (BPM).
   *
   * @return the BPM for playback
   */
  public int getBpm() {
    return bpm;
  }

  /**
   * Sets a new tempo in beats per minute (BPM) for the player.
   *
   * @param bpm the new beats per minute (tempo) for playback
   */
  public void setBpm(final int bpm) {
    this.bpm = bpm;
  }
}
