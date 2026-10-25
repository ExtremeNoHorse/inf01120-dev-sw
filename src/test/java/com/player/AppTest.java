package com.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.audio.AudioEngine;
import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.Interpreter;
import com.player.repository.Music;
import com.player.repository.MusicRepository;
import com.player.validation.InputValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppTest {
  private static final String PLAY_COMMAND = "play";
  private InMemoryRepository mockRepository;
  private TestAudioEngine mockAudioEngine;
  private App app;

  static class InMemoryRepository implements MusicRepository {
    private final Map<String, Music> store = new HashMap<>();

    @Override
    public Music newMusic() {
      return new Music("new-id", "New", "");
    }

    @Override
    public Music save(final Music music) {
      store.put(music.getId(), music);
      return music;
    }

    @Override
    public Music getById(final String id) {
      return store.get(id);
    }

    @Override
    public Music getByPath(final Path path) {
      if (path == null) {
        return null;
      }
      try {
        final String content = Files.readString(path);
        return new Music(path.getFileName().toString(), "From Path", content);
      } catch (final IOException e) {
        return null;
      }
    }

    @Override
    public List<Music> findAll() {
      return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(final String id) {
      store.remove(id);
    }
  }

  static class TestAudioEngine implements AudioEngine {
    private String lastPlayed;

    @Override
    public void play(final String pattern) {
      this.lastPlayed = pattern;
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void stop() {}

    @Override
    public void seek(final int index) {}

    public String getLastPlayed() {
      return lastPlayed;
    }
  }

  @BeforeEach
  void setUp() {
    mockRepository = new InMemoryRepository();
    mockAudioEngine = new TestAudioEngine();
    final Player player =
        new Player(
            new Interpreter(), mockAudioEngine, PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));
    app = new App(new InputValidator(), mockRepository, player);
  }

  @Test
  void givenNoArgs_whenRun_thenReturnsZero() {
    assertEquals(0, app.run(new String[0]));
    assertEquals(0, app.run(null));
  }

  @Test
  void givenIncompleteOrUnknownCommand_whenRun_thenReturnsZero(@TempDir final Path tempDir) {
    assertEquals(0, app.run(new String[] {PLAY_COMMAND}));
    assertEquals(0, app.run(new String[] {"export", tempDir.resolve("source").toString()}));
    assertEquals(0, app.run(new String[] {"unknown", "argument"}));
  }

  @Test
  void givenValidatorFailure_whenRun_thenReturnsErrorCode(@TempDir final Path tempDir)
      throws IOException {
    final Path songFile = tempDir.resolve("song.txt");
    Files.writeString(songFile, "C:3");

    final InputValidator failingValidator = new InputValidator();
    failingValidator.addRule(
        input -> {
          throw new TtmException(ErrorCode.INVALID_INPUT, "forced validation failure");
        });
    final Player player =
        new Player(
            new Interpreter(), mockAudioEngine, PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM));
    final App failingApp = new App(failingValidator, mockRepository, player);

    assertEquals(1, failingApp.run(new String[] {PLAY_COMMAND, songFile.toString()}));
  }

  @Test
  void givenDefaultMainWithNoArgs_thenDoesNotExit() {
    try {
      App.main(new String[0]);
    } catch (final TtmException exception) {
      assertEquals(ErrorCode.AUDIO_UNAVAILABLE, exception.getCode());
      Assumptions.abort("Default App requires an available MIDI/audio device");
    }
  }

  @Test
  void givenPlayCommandWithValidFile_whenRun_thenPlaysAndReturnsZero(@TempDir final Path tempDir)
      throws IOException {
    final Path songFile = tempDir.resolve("song.txt");
    Files.writeString(songFile, "C:3 D:3");

    final int exitCode = app.run(new String[] {PLAY_COMMAND, songFile.toString()});
    assertEquals(0, exitCode);
    assertNotNull(mockAudioEngine.getLastPlayed());
    assertTrue(mockAudioEngine.getLastPlayed().contains("C6qa100"));
  }

  @Test
  void givenExportCommandWithValidFile_whenRun_thenExportsMidiAndReturnsZero(
      @TempDir final Path tempDir) throws IOException {
    final Path songFile = tempDir.resolve("song.txt");
    final Path midiFile = tempDir.resolve("out.mid");
    Files.writeString(songFile, "E:3 G:3");

    final int exitCode = app.run(new String[] {"export", songFile.toString(), midiFile.toString()});
    assertEquals(0, exitCode);
    assertTrue(Files.exists(midiFile));
    assertTrue(Files.size(midiFile) > 0);
  }

  @Test
  void givenMissingFile_whenPlayOrExport_thenReturnsFailureExitCode(@TempDir final Path tempDir) {
    final int playCode =
        app.run(new String[] {PLAY_COMMAND, tempDir.resolve("non-existent.txt").toString()});
    assertEquals(1, playCode);

    final int exportCode =
        app.run(
            new String[] {
              "export",
              tempDir.resolve("non-existent.txt").toString(),
              tempDir.resolve("out.mid").toString()
            });
    assertEquals(1, exportCode);
  }
}
