package com.player.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A named General MIDI instrument and its program number. */
public enum Instrument {
  PIANO(0, "Acoustic Grand Piano"),
  HARPSICHORD(6, "Harpsichord"),
  MARIMBA(12, "Marimba"),
  XYLOPHONE(13, "Xylophone"),
  TUBULAR_BELLS(15, "Tubular Bells"),
  CHURCH_ORGAN(20, "Church Organ"),
  BANDONEON(24, "Bandoneon"),
  GUITAR_ACOUSTIC(25, "Acoustic Guitar"),
  GUITAR_ELECTRIC(27, "Electric Guitar"),
  BASS_ACOUSTIC(32, "Acoustic Bass"),
  VIOLIN(40, "Violin"),
  CELLO(42, "Cello"),
  TIMPANI(47, "Timpani"),
  CHOIR_AAHS(52, "Choir Aahs"),
  TRUMPET(56, "Trumpet"),
  TROMBONE(57, "Trombone"),
  FRENCH_HORN(60, "French Horn"),
  ALTO_SAX(65, "Alto Sax"),
  OBOE(68, "Oboe"),
  BASSOON(70, "Bassoon"),
  CLARINET(71, "Clarinet"),
  FLUTE(73, "Flute"),
  SYNTH_LEAD(80, "Lead 1 (square)"),
  BAGPIPE(110, "Bagpipe"),
  AGOGO(114, "Agogo"),
  SEASHORE(123, "Seashore");

  private static final Map<Integer, String> GENERAL_MIDI_INSTRUMENTS;

  static {
    final Map<Integer, String> instruments = new LinkedHashMap<>();
    for (final Instrument instrument : values()) {
      instruments.put(instrument.program, instrument.displayName);
    }
    GENERAL_MIDI_INSTRUMENTS = Collections.unmodifiableMap(instruments);
  }

  private final int program;
  private final String displayName;

  Instrument(final int program, final String name) {
    this.program = program;
    this.displayName = name;
  }

  public int getProgram() {
    return program;
  }

  public String getName() {
    return displayName;
  }

  public static Map<Integer, String> getGeneralMidiInstruments() {
    return GENERAL_MIDI_INSTRUMENTS;
  }
}
