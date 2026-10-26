package com.player.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps playback progress to positions in the source text. Each non-blank line is a voice that plays
 * at the same time, so the highlight advances proportionally in every line.
 */
public final class TextProgress {

  private TextProgress() {}

  /**
   * Computes the character offset being played in each voice line.
   *
   * <p>This is an approximation: it assumes every character lasts the same time.
   *
   * @param text the source text
   * @param fraction playback progress from 0.0 to 1.0
   * @return one offset per non-blank line
   */
  public static List<Integer> offsets(final String text, final double fraction) {
    final List<Integer> offsets = new ArrayList<>();
    final double clamped = Math.clamp(fraction, 0.0, 1.0);
    int lineStart = 0;
    for (final String line : text.split("\n", -1)) {
      if (!line.isBlank()) {
        final int column = Math.min(line.length() - 1, (int) (clamped * line.length()));
        offsets.add(lineStart + column);
      }
      lineStart += line.length() + 1;
    }
    return offsets;
  }
}
