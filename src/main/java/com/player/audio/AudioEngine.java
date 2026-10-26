package com.player.audio;

/**
 * Interface abstracting audio playback mechanisms, isolating third-party sound libraries like
 * JFugue.
 */
public interface AudioEngine {

  /**
   * Plays the given musical pattern string.
   *
   * @param pattern the JFugue pattern to play
   */
  void play(final String pattern);

  /** Pauses the currently running playback. */
  void pause();

  /** Resumes playback from the paused state. */
  void resume();

  /** Stops playback and resets state. */
  void stop();

  /**
   * Seeks to a specific tick position in the audio stream.
   *
   * @param index tick or millisecond index to seek to
   */
  void seek(final int index);

  /**
   * Reports the current playback position.
   *
   * @return current tick position, or 0 when nothing is loaded
   */
  long getTickPosition();

  /**
   * Reports the total length of the loaded music.
   *
   * @return total length in ticks, or 0 when nothing is loaded
   */
  long getTickLength();

  /**
   * Checks whether a music is loaded and not finished (playing or paused).
   *
   * @return {@code true} while a playback session is active
   */
  boolean isActive();
}
