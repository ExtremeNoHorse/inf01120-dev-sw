package com.player.interpreter.domain;

/**
 * Maps musical duration figures (as specified in Phase 2) to their JFugue duration symbols.
 *
 * <p>Figures:
 *
 * <ul>
 *   <li>1: whole note ('w')
 *   <li>2: half note ('h')
 *   <li>3: quarter note ('q')
 *   <li>4: eighth note ('i')
 *   <li>5: sixteenth note ('s')
 *   <li>6: thirty-second note ('t')
 *   <li>7: sixty-fourth note ('x')
 * </ul>
 */
public enum MusicalDuration {
  WHOLE_NOTE(1, "w"),
  HALF_NOTE(2, "h"),
  QUARTER_NOTE(3, "q"),
  EIGHTH_NOTE(4, "i"),
  SIXTEENTH_NOTE(5, "s"),
  THIRTY_SECOND(6, "t"),
  SIXTY_FOURTH(7, "x");

  private static final double WHOLE_NOTE_BEATS = 4.0;

  private final int id;
  private final String jfugueToken;

  MusicalDuration(final int id, final String jfugueToken) {
    this.id = id;
    this.jfugueToken = jfugueToken;
  }

  /**
   * Retrieves the numeric identifier of the duration figure.
   *
   * @return the figure number (1 to 7)
   */
  public int getId() {
    return id;
  }

  /**
   * Retrieves the corresponding JFugue duration token.
   *
   * @return the JFugue token character string (e.g., "q")
   */
  public String getJfugueToken() {
    return jfugueToken;
  }

  /**
   * Length of the figure in beats (quarter notes): a whole note lasts 4 and each next figure lasts
   * half of the previous one.
   *
   * @return the number of beats
   */
  public double getBeats() {
    return WHOLE_NOTE_BEATS / (1 << (id - 1));
  }

  /**
   * Finds the {@link MusicalDuration} matching the given JFugue duration symbol.
   *
   * @param jfugueToken the JFugue symbol (e.g., "q")
   * @return the matching {@link MusicalDuration}, or {@code QUARTER_NOTE} as default
   */
  public static MusicalDuration fromJfugueToken(final String jfugueToken) {
    for (final MusicalDuration duration : values()) {
      if (duration.getJfugueToken().equals(jfugueToken)) {
        return duration;
      }
    }
    return QUARTER_NOTE;
  }

  /**
   * Finds the {@link MusicalDuration} matching the given numeric identifier.
   *
   * @param id the figure number (1 to 7)
   * @return the matching {@link MusicalDuration}, or {@code SEMINIMA} as default
   */
  public static MusicalDuration fromId(final int id) {
    for (final MusicalDuration duration : values()) {
      if (duration.getId() == id) {
        return duration;
      }
    }
    return QUARTER_NOTE;
  }
}
