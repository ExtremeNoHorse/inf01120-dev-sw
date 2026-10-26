package com.player.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextProgressTest {
  private static final double HALFWAY = 0.5;
  private static final double BEYOND_END = 1.5;
  private static final int SECOND_LINE_MIDDLE = 7;
  private static final int THIRD_LINE_START = 5;

  @Test
  void givenTwoVoices_whenHalfway_thenHighlightsMiddleOfEachLine() {
    // "ABCD" occupies offsets 0-3, "\n" is 4, "EFGH" occupies 5-8
    assertEquals(List.of(2, SECOND_LINE_MIDDLE), TextProgress.offsets("ABCD\nEFGH", HALFWAY));
  }

  @Test
  void givenBlankLines_whenHighlight_thenSkipsThemButKeepsOffsets() {
    assertEquals(List.of(0, THIRD_LINE_START), TextProgress.offsets("AB\n \nCD", 0.0));
  }

  @Test
  void givenOutOfRangeFraction_whenHighlight_thenClampsToLastCharacter() {
    assertEquals(List.of(2), TextProgress.offsets("ABC", BEYOND_END));
    assertEquals(List.of(0), TextProgress.offsets("ABC", -1));
  }
}
