package com.player.ui;

import com.player.Player;
import com.player.config.PlayerConfig;
import com.player.exception.TtmException;
import com.player.interpreter.Timeline;
import com.player.repository.Music;
import com.player.usecase.MusicLibrary;
import com.player.validation.InputValidator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Presenter of the main window (MVP). Holds the screen state and turns user events into use case
 * calls, so the Swing code stays free of business rules and this class is testable without a GUI.
 */
public final class MainPresenter {
  private static final int PROGRESS_SCALE = 1000;
  private static final long NO_PENDING_SEEK = -1;

  private enum PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
  }

  private final MusicLibrary library;
  private final Player player;
  private final InputValidator validator;
  private MainView view;
  private Music current;
  private Timeline timeline = new Timeline(List.of());
  private PlaybackState state = PlaybackState.STOPPED;
  private boolean sessionStarted;
  private long pendingSeek = NO_PENDING_SEEK;

  /**
   * Constructs the presenter with the use cases it coordinates.
   *
   * @param library the music library use cases
   * @param player the playback coordinator
   * @param validator the input validator applied before playing or exporting
   */
  public MainPresenter(
      final MusicLibrary library, final Player player, final InputValidator validator) {
    this.library = Objects.requireNonNull(library, "Library cannot be null");
    this.player = Objects.requireNonNull(player, "Player cannot be null");
    this.validator = Objects.requireNonNull(validator, "Validator cannot be null");
  }

  /**
   * Binds the view and renders the initial screen.
   *
   * @param mainView the view to drive
   */
  public void attach(final MainView mainView) {
    this.view = Objects.requireNonNull(mainView, "View cannot be null");
    refreshList();
    view.showStartScreen();
    showSettings();
  }

  /** Handles "Nova Música": goes back to the start screen. */
  public void onNewMusic() {
    stop();
    current = null;
    refreshList();
    view.showStartScreen();
  }

  /** Handles "Digitar Texto": asks a name and opens an empty music. */
  public void onTypeText() {
    view.askText("Nome da nova música:", "Nova Música")
        .ifPresent(name -> run(() -> open(library.create(name))));
  }

  /** Handles "Fazer Upload": imports a text file as a new music. */
  public void onUpload() {
    view.chooseTextFile().ifPresent(path -> run(() -> open(library.importFile(path))));
  }

  /**
   * Handles a click on a music of the list.
   *
   * @param music the selected music
   */
  public void onSelect(final Music music) {
    if (music != null && !music.equals(current)) {
      open(music);
    }
  }

  /** Handles "salvar": persists the editor text into the current music. */
  public void onSave() {
    if (current != null) {
      run(() -> library.saveContent(current, view.editorText()));
    }
  }

  /**
   * Handles "renomear" from the context menu.
   *
   * @param music the music to rename
   */
  public void onRename(final Music music) {
    view.askText("Novo nome:", music.getName())
        .ifPresent(
            name ->
                run(
                    () -> {
                      library.rename(music, name);
                      refreshList();
                    }));
  }

  /**
   * Handles "deletar" from the context menu.
   *
   * @param music the music to delete
   */
  public void onDelete(final Music music) {
    if (!view.confirm("Deletar \"" + music.getName() + "\"?")) {
      return;
    }
    run(
        () -> {
          library.delete(music);
          if (music.equals(current)) {
            onNewMusic();
          } else {
            refreshList();
          }
        });
  }

  /**
   * Handles "exportar música": saves the interpreted music as a MIDI file.
   *
   * @param music the music to export
   */
  public void onExport(final Music music) {
    final Music source = music.equals(current) ? withEditorText(music) : music;
    view.chooseExportFile(music.getName() + ".mid")
        .ifPresent(
            path ->
                run(
                    () -> {
                      validator.validate(source.getContent());
                      view.showInfo("Música exportada em " + player.export(source, path));
                    }));
  }

  /** Handles the play/pause button. */
  public void onPlayPause() {
    switch (state) {
      case PLAYING -> {
        player.pause();
        setState(PlaybackState.PAUSED);
      }
      case PAUSED -> {
        player.resume();
        setState(PlaybackState.PLAYING);
      }
      default -> run(this::play);
    }
  }

  /** Handles the stop button. */
  public void onStop() {
    stop();
  }

  /** Handles the "previous" button: restarts the current music. */
  public void onRestart() {
    if (state != PlaybackState.STOPPED) {
      player.seek(0);
    }
  }

  /** Handles the "next" button: opens the next music of the list. */
  public void onNext() {
    final List<Music> musics = library.listAll();
    final int next = musics.indexOf(current) + 1;
    if (current != null && next < musics.size()) {
      open(musics.get(next));
    }
  }

  /**
   * Handles a drag on the progress bar.
   *
   * @param permille target position from 0 to 1000
   */
  public void onSeek(final int permille) {
    if (state != PlaybackState.STOPPED) {
      player.seek((int) (player.getTickLength() * permille / PROGRESS_SCALE));
    }
  }

  /**
   * Handles the octave "-" and "+" buttons.
   *
   * @param delta octaves to add (negative to lower)
   */
  public void onOctaveChange(final int delta) {
    updateSettings(cfg -> cfg.setOctave(cfg.getOctave() + delta));
  }

  /**
   * Handles the BPM "-" and "+" buttons.
   *
   * @param steps number of {@link PlayerConfig#BPM_DELTA} steps to add (negative to slow down)
   */
  public void onBpmChange(final int steps) {
    updateSettings(cfg -> cfg.increaseBpm(steps * PlayerConfig.BPM_DELTA));
  }

  /**
   * Handles the volume slider.
   *
   * @param volume the new volume (0-127)
   */
  public void onVolumeChange(final int volume) {
    updateSettings(cfg -> cfg.setVolume(volume));
  }

  /** Periodic callback (driven by the view's timer) that updates progress and detects the end. */
  public void onTick() {
    if (state == PlaybackState.STOPPED) {
      return;
    }
    if (player.isActive()) {
      sessionStarted = true;
      applyPendingSeek();
      showProgress();
    } else if (sessionStarted) {
      stop();
    }
  }

  /** Handles the window closing: stops any playback. */
  public void onClose() {
    stop();
  }

  private void open(final Music music) {
    stop();
    current = music;
    refreshList();
    view.showEditor(music);
  }

  private void play() {
    if (current == null) {
      return;
    }
    final String text = view.editorText();
    validator.validate(text);
    timeline = player.timeline(text);
    player.play(withEditorText(current));
    sessionStarted = false;
    setState(PlaybackState.PLAYING);
  }

  private void stop() {
    if (state != PlaybackState.STOPPED) {
      player.stop();
    }
    pendingSeek = NO_PENDING_SEEK;
    setState(PlaybackState.STOPPED);
    view.showProgress(0);
    view.highlight(List.of());
  }

  private void updateSettings(final Consumer<PlayerConfig> change) {
    final PlayerConfig settings = player.getPlayerConfig();
    change.accept(settings);
    player.configure(settings);
    showSettings();
    if (state != PlaybackState.STOPPED) {
      restartWithNewSettings();
    }
  }

  private void restartWithNewSettings() {
    final long position = player.getTickPosition();
    stop();
    run(this::play);
    pendingSeek = position;
  }

  private void applyPendingSeek() {
    if (pendingSeek != NO_PENDING_SEEK) {
      player.seek((int) pendingSeek);
      pendingSeek = NO_PENDING_SEEK;
    }
  }

  private void showProgress() {
    final long length = player.getTickLength();
    if (length > 0) {
      final double fraction = (double) player.getTickPosition() / length;
      view.showProgress((int) (fraction * PROGRESS_SCALE));
      view.highlight(timeline.offsetsAt(fraction));
    }
  }

  private void showSettings() {
    final PlayerConfig settings = player.getPlayerConfig();
    view.showSettings(settings.getOctave(), settings.getBpm(), settings.getVolume());
  }

  private void setState(final PlaybackState newState) {
    state = newState;
    view.showPlaying(newState == PlaybackState.PLAYING);
    view.setEditorEditable(newState == PlaybackState.STOPPED);
  }

  private void refreshList() {
    view.showMusics(library.listAll(), current);
  }

  private Music withEditorText(final Music music) {
    return new Music(music.getId(), music.getName(), view.editorText());
  }

  private void run(final Runnable action) {
    try {
      action.run();
    } catch (final TtmException e) {
      view.showError(e.getMessage());
    }
  }
}
