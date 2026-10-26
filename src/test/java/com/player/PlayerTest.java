package com.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.audio.AudioEngine;
import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.Interpreter;
import com.player.repository.Music;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerTest {
  private static final int DEFAULT_TEST_BPM = 120;
  private static final int NEW_OCTAVE = 4;
  private static final int NEW_VOLUME = 50;
  private static final int NEW_BPM = 140;

  private TestAudioEngine mockAudioEngine;
  private Interpreter interpreter;
  private Player player;

  static class TestAudioEngine implements AudioEngine {
    private String playedPattern;
    private boolean paused;
    private boolean resumed;
    private boolean stopped;
    private int seekIndex = -1;

    @Override
    public void play(final String pattern) {
      this.playedPattern = pattern;
    }

    @Override
    public void pause() {
      this.paused = true;
    }

    @Override
    public void resume() {
      this.resumed = true;
    }

    @Override
    public void stop() {
      this.stopped = true;
    }

    @Override
    public void seek(final int index) {
      this.seekIndex = index;
    }

    public String getPlayedPattern() {
      return playedPattern;
    }

    public boolean isPaused() {
      return paused;
    }

    public boolean isResumed() {
      return resumed;
    }

    public boolean isStopped() {
      return stopped;
    }

    public int getSeekIndex() {
      return seekIndex;
    }
  }

  @BeforeEach
  void setUp() {
    mockAudioEngine = new TestAudioEngine();
    interpreter = new Interpreter();
    final PlayerConfig config = PlayerConfig.forVoice(0, DEFAULT_TEST_BPM);
    player = new Player(interpreter, mockAudioEngine, config);
  }

  @Test
  void givenMusic_whenPlay_thenInterpretsAndDelegatesToAudioEngine() {
    final Music music = new Music("m1", "Song", "C:3D:3");
    player.play(music);

    assertNotNull(mockAudioEngine.getPlayedPattern());
    assertTrue(mockAudioEngine.getPlayedPattern().contains("C6qa100"));
    assertTrue(mockAudioEngine.getPlayedPattern().contains("D6qa100"));
  }

  @Test
  void givenNullMusic_whenPlay_thenThrowsTtmException() {
    final TtmException ex = assertThrows(TtmException.class, () -> player.play(null));
    assertEquals(ErrorCode.INVALID_INPUT, ex.getCode());
  }

  @Test
  void playbackControls_delegateToAudioEngine() {
    player.pause();
    assertTrue(mockAudioEngine.isPaused());

    player.resume();
    assertTrue(mockAudioEngine.isResumed());

    player.stop();
    assertTrue(mockAudioEngine.isStopped());
  }

  @Test
  void givenNewConfig_whenConfigure_thenUpdatesPlayerConfig() {
    final PlayerConfig newConfig = new PlayerConfig(10, NEW_OCTAVE, NEW_VOLUME, NEW_BPM);
    player.configure(newConfig);
    assertEquals(newConfig, player.getPlayerConfig());
  }

  @Test
  void givenMusicAndDestination_whenExport_thenCreatesValidMidiFile(@TempDir final Path tempDir)
      throws IOException {
    final Music music = new Music("exp-1", "Export Song", "C:3E:3G:3");
    final Path targetPath = tempDir.resolve("output").resolve("exported_music.mid");

    final Path result = player.export(music, targetPath);

    assertEquals(targetPath, result);
    assertTrue(Files.exists(targetPath));
    assertTrue(Files.size(targetPath) > 0);
    assertTrue(music.hasRecording());
  }

  @Test
  void givenDestinationWithoutParent_whenExport_thenCreatesValidMidiFile() throws IOException {
    final Music music = new Music("exp-2", "Export Song", "C:3");
    final Path targetPath = Path.of("coverage-" + System.nanoTime() + ".mid");

    try {
      final Path result = player.export(music, targetPath);

      assertEquals(targetPath, result);
      assertTrue(Files.exists(targetPath));
    } finally {
      Files.deleteIfExists(targetPath);
    }
  }

  @Test
  void givenDirectoryAsExportDestination_whenExport_thenThrowsPersistenceFailure(
      @TempDir final Path tempDir) throws IOException {
    final Path directory = tempDir.resolve("existing-directory");
    Files.createDirectory(directory);

    final TtmException exception =
        assertThrows(
            TtmException.class,
            () -> player.export(new Music("exp-3", "Export Song", "C:3"), directory));

    assertEquals(ErrorCode.PERSISTENCE_FAILURE, exception.getCode());
  }

  @Test
  void givenNullMusicOrPath_whenExport_thenThrowsTtmException(@TempDir final Path tempDir) {
    assertThrows(TtmException.class, () -> player.export(null, tempDir.resolve("out.mid")));
    assertThrows(TtmException.class, () -> player.export(new Music("1", "S", "A"), null));
  }
}
