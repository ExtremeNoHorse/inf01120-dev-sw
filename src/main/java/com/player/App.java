package com.player;

import com.player.audio.JFuguePlayer;
import com.player.config.PlayerConfig;
import com.player.exception.TtmException;
import com.player.interpreter.Interpreter;
import com.player.interpreter.domain.DomainMapper;
import com.player.repository.FileMusicRepository;
import com.player.repository.Music;
import com.player.repository.MusicRepository;
import com.player.ui.MainPresenter;
import com.player.ui.swing.MainWindow;
import com.player.usecase.MusicLibrary;
import com.player.validation.InputValidator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/** Main application composition root and command-line entry point. */
public final class App {
  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int ERROR_EXIT_CODE = 1;
  private static final int MIN_PLAY_ARGS = 2;
  private static final int MIN_EXPORT_ARGS = 3;

  private final InputValidator validator;
  private final MusicRepository repository;
  private final Player player;

  /** Constructs {@code App} with default system implementations and storage directory. */
  public App() {
    this(
        new InputValidator(),
        new FileMusicRepository("musics"),
        new Player(
            new Interpreter(
                DomainMapper.createDefault(), PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM)),
            new JFuguePlayer(),
            PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM)));
  }

  /**
   * Constructs {@code App} with explicit dependencies injected.
   *
   * @param validator the input validator
   * @param repository the music persistence repository
   * @param player the player coordinator
   */
  public App(
      final InputValidator validator, final MusicRepository repository, final Player player) {
    this.validator = Objects.requireNonNull(validator, "Validator cannot be null");
    this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
    this.player = Objects.requireNonNull(player, "Player cannot be null");
  }

  /**
   * Creates the presenter of the graphical interface wired to this app's dependencies.
   *
   * @return the main window presenter
   */
  public MainPresenter createPresenter() {
    return new MainPresenter(new MusicLibrary(repository), player, validator);
  }

  /**
   * Main entry point for the application. Without arguments it opens the graphical interface.
   *
   * @param args command-line arguments
   */
  static void main(final String[] args) {
    final App app = new App();
    if (args.length == 0) {
      MainWindow.open(app.createPresenter());
      return;
    }
    final int exitCode = app.run(args);
    if (exitCode != SUCCESS_EXIT_CODE) {
      System.exit(exitCode);
    }
  }

  /**
   * Runs application logic based on supplied arguments.
   *
   * <p>Supported CLI commands:
   *
   * <ul>
   *   <li>play &lt;filePath&gt;
   *   <li>export &lt;sourcePath&gt; &lt;targetMidiPath&gt;
   * </ul>
   *
   * @param args command-line arguments
   * @return exit code (0 for success, non-zero for failure)
   */
  public int run(final String[] args) {
    if (args == null || args.length == 0) {
      return SUCCESS_EXIT_CODE;
    }

    int exitCode = SUCCESS_EXIT_CODE;
    try {
      final String command = args[0];
      if ("play".equalsIgnoreCase(command) && args.length >= MIN_PLAY_ARGS) {
        exitCode = executePlay(Paths.get(args[1]));
      } else if ("export".equalsIgnoreCase(command) && args.length >= MIN_EXPORT_ARGS) {
        exitCode = executeExport(Paths.get(args[1]), Paths.get(args[2]));
      }
    } catch (final TtmException _) {
      exitCode = ERROR_EXIT_CODE;
    }

    return exitCode;
  }

  private int executePlay(final Path sourcePath) {
    final Music music = repository.getByPath(sourcePath);
    if (music == null) {
      return ERROR_EXIT_CODE;
    }
    validator.validate(music.getContent());
    player.play(music);
    return SUCCESS_EXIT_CODE;
  }

  private int executeExport(final Path sourcePath, final Path targetPath) {
    final Music music = repository.getByPath(sourcePath);
    if (music == null) {
      return ERROR_EXIT_CODE;
    }
    validator.validate(music.getContent());
    player.export(music, targetPath);
    return SUCCESS_EXIT_CODE;
  }
}
