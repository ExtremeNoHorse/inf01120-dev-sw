package com.player.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.Player;
import com.player.audio.AudioEngine;
import com.player.config.PlayerConfig;
import com.player.interpreter.Interpreter;
import com.player.repository.FileMusicRepository;
import com.player.repository.Music;
import com.player.usecase.MusicLibrary;
import com.player.validation.InputValidator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainPresenterTest {
  private static final long LENGTH = 1000;
  private static final int HALF = 500;
  private static final int HALF_VOLUME = 50;
  private static final int DEFAULT_OCTAVE = 6;
  private static final String START = "start";
  private static final String EDITOR = "editor";
  private static final String NOTE = "C:3";
  private static final String SONG = "Song";
  private static final String SECOND = "B";

  private Path tempDir;
  private FakeEngine engine;
  private FakeView view;
  private MusicLibrary library;
  private Player player;
  private MainPresenter presenter;

  @BeforeEach
  void setUp(@TempDir final Path dir) {
    tempDir = dir;
    engine = new FakeEngine();
    view = new FakeView();
    library = new MusicLibrary(new FileMusicRepository(dir.resolve("musics").toString()));
    player = new Player(new Interpreter(), engine);
    presenter = new MainPresenter(library, player, new InputValidator());
    presenter.attach(view);
  }

  @Test
  void givenAttach_thenShowsStartScreenEmptyListAndDefaultSettings() {
    // Then
    assertEquals(START, view.screen);
    assertTrue(view.musics.isEmpty());
    assertEquals(List.of(DEFAULT_OCTAVE, PlayerConfig.DEFAULT_BPM, 100), view.settings);
  }

  @Test
  void givenTypeText_whenNameGiven_thenCreatesAndOpensEditor() {
    // Given
    view.answers.add("Pedra");

    // When
    presenter.onTypeText();

    // Then
    assertEquals(EDITOR, view.screen);
    assertEquals("Pedra", view.selected.getName());
    assertEquals(1, view.musics.size());
  }

  @Test
  void givenTypeText_whenCancelled_thenStaysOnStartScreen() {
    // When
    presenter.onTypeText();

    // Then
    assertEquals(START, view.screen);
    assertTrue(view.musics.isEmpty());
  }

  @Test
  void givenTypeText_whenBlankName_thenShowsError() {
    // Given
    view.answers.add(" ");

    // When
    presenter.onTypeText();

    // Then
    assertEquals(1, view.errors.size());
  }

  @Test
  void givenUpload_whenFileChosen_thenImportsAndOpensIt() throws IOException {
    // Given
    final Path file = tempDir.resolve("poema.txt");
    Files.writeString(file, NOTE);
    view.textFile = file;

    // When
    presenter.onUpload();

    // Then
    assertEquals(EDITOR, view.screen);
    assertEquals(NOTE, view.editorText);
    assertEquals("poema", view.selected.getName());
  }

  @Test
  void givenEditedText_whenSave_thenPersists() {
    // Given
    final Music music = openNew(SONG);
    view.editorText = "D:3";

    // When
    presenter.onSave();

    // Then
    assertEquals("D:3", library.listAll().getFirst().getContent());
    assertEquals("D:3", music.getContent());
  }

  @Test
  void givenNoMusicOpen_whenSaveOrPlay_thenDoesNothing() {
    // When
    presenter.onSave();
    presenter.onPlayPause();

    // Then
    assertNull(engine.played);
    assertTrue(view.errors.isEmpty());
  }

  @Test
  void givenMusic_whenRenamed_thenListShowsNewName() {
    // Given
    final Music music = openNew("Old");
    view.answers.add("New");

    // When
    presenter.onRename(music);

    // Then
    assertEquals("New", view.musics.getFirst().getName());
  }

  @Test
  void givenOpenMusic_whenDeletedAndConfirmed_thenGoesBackToStart() {
    // Given
    final Music music = openNew(SONG);
    view.confirmAnswer = true;

    // When
    presenter.onDelete(music);

    // Then
    assertEquals(START, view.screen);
    assertTrue(view.musics.isEmpty());
  }

  @Test
  void givenOtherMusic_whenDeleted_thenKeepsEditorOpen() {
    // Given
    final Music first = openNew("A");
    openNew(SECOND);
    view.confirmAnswer = true;

    // When
    presenter.onDelete(first);

    // Then
    assertEquals(EDITOR, view.screen);
    assertEquals(1, view.musics.size());
  }

  @Test
  void givenDeleteNotConfirmed_thenKeepsMusic() {
    // Given
    final Music music = openNew(SONG);

    // When
    presenter.onDelete(music);

    // Then
    assertEquals(1, view.musics.size());
  }

  @Test
  void givenOpenMusic_whenExported_thenWritesMidiWithEditorText() {
    // Given
    final Music music = openNew(SONG);
    view.editorText = NOTE;
    view.exportFile = tempDir.resolve("out.mid");

    // When
    presenter.onExport(music);

    // Then
    assertTrue(Files.exists(view.exportFile));
    assertEquals(1, view.infos.size());
  }

  @Test
  void givenEmptyMusic_whenExported_thenShowsValidationError() {
    // Given
    final Music music = openNew(SONG);
    openNew("Other");
    view.exportFile = tempDir.resolve("out.mid");

    // When
    presenter.onExport(music);

    // Then
    assertEquals(1, view.errors.size());
    assertFalse(Files.exists(view.exportFile));
  }

  @Test
  void givenText_whenPlayPauseResume_thenTogglesEngine() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;

    // When & Then
    presenter.onPlayPause();
    assertEquals("T120 V0 I6 :CE(7,100) C6qa100", engine.played);
    assertTrue(view.playing);
    assertFalse(view.editorEditable);

    // When & Then
    presenter.onPlayPause();
    assertTrue(engine.paused);
    assertFalse(view.playing);
    assertFalse(view.editorEditable);

    // When & Then
    presenter.onPlayPause();
    assertTrue(engine.resumed);
    assertTrue(view.playing);
    assertFalse(view.editorEditable);
  }

  @Test
  void givenPlayingMusic_whenStopped_thenStopsEngineAndUnlocksEditor() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;
    presenter.onPlayPause();
    assertFalse(view.editorEditable);

    // When
    presenter.onStop();

    // Then
    assertTrue(engine.stopped);
    assertFalse(view.playing);
    assertTrue(view.editorEditable);
    assertEquals(0, view.progress);
    assertTrue(view.highlight.isEmpty());
  }

  @Test
  void givenEmptyText_whenPlay_thenShowsError() {
    // Given
    openNew(SONG);

    // When
    presenter.onPlayPause();

    // Then
    assertNull(engine.played);
    assertEquals(1, view.errors.size());
  }

  @Test
  void givenPlaying_whenTicks_thenShowsProgressAndStopsAtEnd() {
    // Given
    openNew(SONG);
    view.editorText = "ABCD";
    presenter.onPlayPause();

    // When & Then
    engine.active = true;
    engine.position = HALF;
    presenter.onTick();
    assertEquals(HALF, view.progress);
    assertEquals(List.of(2), view.highlight);

    // When & Then
    engine.active = false;
    presenter.onTick();
    assertFalse(view.playing);
    assertEquals(0, view.progress);
    assertTrue(view.highlight.isEmpty());
  }

  @Test
  void givenPlayStillLoading_whenTick_thenKeepsPlaying() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;
    presenter.onPlayPause();

    // When
    presenter.onTick();

    // Then
    assertTrue(view.playing);
  }

  @Test
  void givenStopped_whenTickRestartSeek_thenEngineUntouched() {
    // When
    presenter.onTick();
    presenter.onRestart();
    presenter.onSeek(HALF);

    // Then
    assertEquals(-1, engine.seek);
  }

  @Test
  void givenPlaying_whenSeekAndRestart_thenMovesEngine() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;
    presenter.onPlayPause();

    // When & Then
    presenter.onSeek(HALF);
    assertEquals(HALF, engine.seek);

    // When & Then
    presenter.onRestart();
    assertEquals(0, engine.seek);
  }

  @Test
  void givenSettingsChange_whenStopped_thenUpdatesViewAndPlayer() {
    // When
    presenter.onOctaveChange(1);
    presenter.onBpmChange(-1);
    presenter.onBpmChange(-1);
    presenter.onVolumeChange(HALF_VOLUME);

    // Then
    assertEquals(List.of(DEFAULT_OCTAVE + 1, 100, HALF_VOLUME), view.settings);
    assertEquals(DEFAULT_OCTAVE + 1, player.getPlayerConfig().getOctave());
  }

  @Test
  void givenPlaying_whenSettingsChange_thenRestartsAtSamePosition() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;
    presenter.onPlayPause();
    engine.active = true;
    engine.position = HALF;

    // When
    presenter.onBpmChange(1);

    // Then
    assertTrue(engine.stopped);
    assertTrue(engine.played.startsWith("T130"));
    presenter.onTick();
    assertEquals(HALF, engine.seek);
  }

  @Test
  void givenTwoMusics_whenNext_thenOpensFollowingOneOnly() {
    // Given
    openNew("A");
    final Music first = view.musics.getFirst();
    openNew(SECOND);
    presenter.onSelect(first);

    // When & Then
    presenter.onNext();
    assertEquals(SECOND, view.selected.getName());

    // When & Then
    presenter.onNext();
    assertEquals(SECOND, view.selected.getName());
  }

  @Test
  void givenSameOrNullSelection_whenSelect_thenIgnores() {
    // Given
    final Music music = openNew("A");
    view.editorText = "edited";

    // When
    presenter.onSelect(music);
    presenter.onSelect(null);

    // Then
    assertEquals("edited", view.editorText);
  }

  @Test
  void givenNoCurrent_whenNext_thenDoesNothing() {
    // When
    presenter.onNext();

    // Then
    assertEquals(START, view.screen);
  }

  @Test
  void givenPlaying_whenNewMusicOrClose_thenStopsEngine() {
    // Given
    openNew(SONG);
    view.editorText = NOTE;
    presenter.onPlayPause();

    // When & Then
    presenter.onNewMusic();
    assertTrue(engine.stopped);
    assertEquals(START, view.screen);

    // When & Then
    presenter.onClose();
    assertFalse(view.playing);
  }

  private Music openNew(final String name) {
    view.answers.add(name);
    presenter.onTypeText();
    return view.selected;
  }

  private static final class FakeEngine implements AudioEngine {
    private String played;
    private boolean paused;
    private boolean resumed;
    private boolean stopped;
    private boolean active;
    private long position;
    private int seek = -1;

    @Override
    public void play(final String pattern) {
      played = pattern;
    }

    @Override
    public void pause() {
      paused = true;
    }

    @Override
    public void resume() {
      resumed = true;
    }

    @Override
    public void stop() {
      stopped = true;
    }

    @Override
    public void seek(final int index) {
      seek = index;
    }

    @Override
    public long getTickPosition() {
      return position;
    }

    @Override
    public long getTickLength() {
      return LENGTH;
    }

    @Override
    public boolean isActive() {
      return active;
    }
  }

  private static final class FakeView implements MainView {
    private final List<String> answers = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> infos = new ArrayList<>();
    private List<Music> musics = List.of();
    private Music selected;
    private String screen = "";
    private String editorText = "";
    private List<Integer> settings = List.of();
    private boolean playing;
    private boolean editorEditable = true;
    private int progress = -1;
    private List<Integer> highlight = List.of();
    private boolean confirmAnswer;
    private Path textFile;
    private Path exportFile;

    @Override
    public void showMusics(final List<Music> list, final Music current) {
      musics = list;
      selected = current;
    }

    @Override
    public void showStartScreen() {
      screen = START;
    }

    @Override
    public void showEditor(final Music music) {
      screen = EDITOR;
      editorText = music.getContent();
    }

    @Override
    public String editorText() {
      return editorText;
    }

    @Override
    public void showSettings(final int octave, final int bpm, final int volume) {
      settings = List.of(octave, bpm, volume);
    }

    @Override
    public void showPlayButton() {
      playing = false;
    }

    @Override
    public void showPauseButton() {
      playing = true;
    }

    @Override
    public void enableEditorEditing() {
      editorEditable = true;
    }

    @Override
    public void disableEditorEditing() {
      editorEditable = false;
    }

    @Override
    public void showProgress(final int permille) {
      progress = permille;
    }

    @Override
    public void highlight(final List<Integer> offsets) {
      highlight = offsets;
    }

    @Override
    public void showError(final String message) {
      errors.add(message);
    }

    @Override
    public void showInfo(final String message) {
      infos.add(message);
    }

    @Override
    public Optional<String> askText(final String message, final String initialValue) {
      return answers.isEmpty() ? Optional.empty() : Optional.of(answers.removeFirst());
    }

    @Override
    public boolean confirm(final String message) {
      return confirmAnswer;
    }

    @Override
    public Optional<Path> chooseTextFile() {
      return Optional.ofNullable(textFile);
    }

    @Override
    public Optional<Path> chooseExportFile(final String suggestedName) {
      return Optional.ofNullable(exportFile);
    }
  }
}
