package com.player.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TimelineTest {
  private static final double WHOLE_PLUS_QUARTER = 5.0;
  private static final double HALFWAY = 0.5;
  private static final double NEAR_END = 0.9;
  private static final double BEYOND_END = 1.5;
  private static final int SECOND_NOTE = 4;
  private static final int AFTER_DELAY = 4;
  private static final int SECOND_LINE_START = 4;
  private static final int SECOND_LINE_START_CRLF = 5;

  private final Interpreter interpreter = new Interpreter();

  @Test
  void givenDifferentDurations_whenHalfway_thenStillOnTheLongNote() {
    // Given & When
    // "C:1" lasts 4 beats and "D:3" lasts 1: halfway (2.5 beats) is still the whole note
    final Timeline timeline = interpreter.timeline("C:1 D:3");

    // Then
    assertEquals(WHOLE_PLUS_QUARTER, timeline.length());
    assertEquals(List.of(0), timeline.offsetsAt(HALFWAY));
    assertEquals(List.of(SECOND_NOTE), timeline.offsetsAt(NEAR_END));
  }

  @Test
  void givenDelayPrefix_whenStarting_thenHighlightsThePrefixThenTheNote() {
    // Given & When
    final Timeline timeline = interpreter.timeline("[1] C:3");

    // Then
    assertEquals(List.of(0), timeline.offsetsAt(0.0));
    assertEquals(List.of(AFTER_DELAY), timeline.offsetsAt(HALFWAY));
  }

  @Test
  void givenShorterSecondVoice_whenItEnds_thenOnlyTheLongerVoiceIsHighlighted() {
    // Given & When
    final Timeline timeline = interpreter.timeline("C:1\nC:3");

    // Then
    assertEquals(List.of(0, SECOND_LINE_START), timeline.offsetsAt(0.0));
    assertEquals(List.of(0), timeline.offsetsAt(HALFWAY));
  }

  @Test
  void givenWindowsLineBreaks_whenInterpret_thenOffsetsPointToTheText() {
    // Given & When
    final Timeline timeline = interpreter.timeline("C:3\r\nD:3");

    // Then
    assertEquals(List.of(0, SECOND_LINE_START_CRLF), timeline.offsetsAt(0.0));
  }

  @Test
  void givenSilentCommandsOrEmptyText_whenInterpret_thenNothingIsHighlighted() {
    // Given & When & Then
    assertTrue(interpreter.timeline("").offsetsAt(0.0).isEmpty());
    assertTrue(interpreter.timeline("C:3").offsetsAt(BEYOND_END).isEmpty());
    assertEquals(0.0, interpreter.timeline("??").length());
  }
}
