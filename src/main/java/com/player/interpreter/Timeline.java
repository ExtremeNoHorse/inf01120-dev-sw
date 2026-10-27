package com.player.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * When each character of the source text sounds, measured in beats (quarter notes). Beats do not
 * depend on the tempo, so they map directly to the MIDI ticks of the played sequence.
 */
public final class Timeline {

  /**
   * A character of the source text that produces a note or a rest.
   *
   * @param offset position of the character in the source text
   * @param start beat where it starts
   * @param end beat where it ends
   */
  public record Event(int offset, double start, double end) {}

  private final List<List<Event>> voices;
  private final double length;

  /**
   * Creates a timeline with one list of events per voice, in playing order.
   *
   * @param voices the events of each voice
   */
  public Timeline(final List<List<Event>> voices) {
    this.voices = voices.stream().map(List::copyOf).toList();
    this.length =
        this.voices.stream()
            .filter(events -> !events.isEmpty())
            .mapToDouble(events -> events.getLast().end())
            .max()
            .orElse(0);
  }

  /**
   * Total length of the music, given by its longest voice.
   *
   * @return the length in beats
   */
  public double length() {
    return length;
  }

  /**
   * Finds the character sounding in each voice at the given point of the music.
   *
   * @param fraction playback progress from 0.0 to 1.0
   * @return the offset of the sounding character of each voice; finished voices are left out
   */
  public List<Integer> offsetsAt(final double fraction) {
    final double beat = Math.clamp(fraction, 0.0, 1.0) * length;
    final List<Integer> offsets = new ArrayList<>();
    for (final List<Event> events : voices) {
      for (final Event event : events) {
        if (event.start() <= beat && beat < event.end()) {
          offsets.add(event.offset());
          break;
        }
      }
    }
    return offsets;
  }
}
