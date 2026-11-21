package com.player.interpreter.domain;

/**
 * Single point of construction for every JFugue token string used by the interpreter.
 *
 * <p>Centralising these literals here means that only this class needs to change if the underlying
 * music library is ever replaced.
 */
public final class JFugueTokenFactory {

  private static final String VOLUME_INTENSITY_SEPARATOR = "a";

  private JFugueTokenFactory() {}

  /**
   * Builds a tempo token.
   *
   * @param bpm beats per minute
   * @return e.g. {@code "T120"}
   */
  public static String tempo(final int bpm) {
    return "T" + bpm;
  }

  /**
   * Builds a voice (track) token.
   *
   * @param index zero-based voice index
   * @return e.g. {@code "V0"}
   */
  public static String voice(final int index) {
    return "V" + index;
  }

  /**
   * Builds an instrument-change token.
   *
   * @param id General-MIDI instrument number (0-127)
   * @return e.g. {@code "I24"}
   */
  public static String instrument(final int id) {
    return "I" + id;
  }

  /**
   * Builds a MIDI channel-volume controller token (CC 7).
   *
   * @param volume volume level (0-127)
   * @return e.g. {@code ":CE(7,100)"}
   */
  public static String channelVolume(final int volume) {
    return ":CE(7," + volume + ")";
  }

  /**
   * Builds a quarter-note rest token.
   *
   * @return {@code "Rq"}
   */
  public static String rest() {
    return "Rq";
  }

  /**
   * Builds a full note token with octave, optional duration symbol, and velocity.
   *
   * @param name note name with optional accidental (e.g. {@code "C#"}, {@code "Bb"})
   * @param octave octave number (0-9)
   * @param durationSymbol JFugue duration letter (e.g. {@code "q"}), or empty string for default
   * @param velocity MIDI velocity (0-127)
   * @return e.g. {@code "C#5qa80"}
   */
  public static String note(
      final String name, final int octave, final String durationSymbol, final int velocity) {
    return name + octave + durationSymbol + VOLUME_INTENSITY_SEPARATOR + velocity;
  }
}
