package com.player.interpreter.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.player.config.PlayerConfig;
import org.junit.jupiter.api.Test;

class DomainMapperCommandTest {
  private static final String INSTRUMENT_PREFIX = "I";

  @Test
  void givenDefaultMapper_whenMappingTempoControls_thenUpdatesBpm() {
    final DomainMapper mapper = DomainMapper.createDefault();
    final PlayerConfig config = PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM);

    assertEquals(
        "T" + (PlayerConfig.DEFAULT_BPM + PlayerConfig.BPM_DELTA),
        mapper.toDomain(">").execute(config));
    assertEquals(PlayerConfig.DEFAULT_BPM + PlayerConfig.BPM_DELTA, config.getBpm());

    assertEquals("T" + PlayerConfig.DEFAULT_BPM, mapper.toDomain("<").execute(config));
    assertEquals(PlayerConfig.DEFAULT_BPM, config.getBpm());
  }

  @Test
  void givenDefaultMapper_whenMappingVoiceControls_thenUpdatesOctaveAndVolume() {
    final DomainMapper mapper = DomainMapper.createDefault();
    final PlayerConfig config = PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM);
    final int baseOctave = config.getOctave();

    mapper.toDomain("?").execute(config);
    assertEquals(baseOctave + 1, config.getOctave());

    mapper.toDomain("V").execute(config);
    assertEquals(baseOctave, config.getOctave());

    mapper.toDomain(" ").execute(config);
    assertEquals(PlayerConfig.MAX_VOLUME, config.getVolume());
  }

  @Test
  void givenDefaultMapper_whenMappingInstrumentControls_thenReturnsInstrumentTokens() {
    final DomainMapper mapper = DomainMapper.createDefault();
    final PlayerConfig config = PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM);

    assertEquals(
        INSTRUMENT_PREFIX + PlayerConfig.INSTRUMENT_BANDONEON,
        mapper.toDomain("!").execute(config));
    assertEquals(
        INSTRUMENT_PREFIX + PlayerConfig.INSTRUMENT_TUBULAR_BELLS,
        mapper.toDomain(";").execute(config));
    assertEquals(
        INSTRUMENT_PREFIX + PlayerConfig.INSTRUMENT_AGOGO, mapper.toDomain(",").execute(config));
    assertEquals(
        INSTRUMENT_PREFIX + PlayerConfig.INSTRUMENT_TUBULAR_BELLS,
        mapper.toDomain("3").execute(config));

    config.setInstrument(10);
    assertEquals(INSTRUMENT_PREFIX + "14", mapper.toDomain("4").execute(config));
  }
}
