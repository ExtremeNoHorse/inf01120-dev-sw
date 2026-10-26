package com.player.ui.swing;

import com.formdev.flatlaf.FlatLightLaf;
import com.player.repository.Music;
import com.player.ui.MainPresenter;
import com.player.ui.MainView;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Swing implementation of {@link MainView}: composes the panels drawn in the croquis. */
public final class MainWindow implements MainView {
  private static final String START_CARD = "start";
  private static final String EDITOR_CARD = "editor";
  private static final String APP_TITLE = "TTM - Text-To-Music";
  private static final int TICK_MILLIS = 100;

  private final JFrame frame = new JFrame(APP_TITLE);
  private final CardLayout cards = new CardLayout();
  private final JPanel center = new JPanel(cards);
  private final MusicListPanel musicList;
  private final EditorPanel editor;
  private final PlaybackBar playbackBar;

  private MainWindow(final MainPresenter presenter) {
    musicList = new MusicListPanel(presenter);
    editor = new EditorPanel(presenter);
    playbackBar = new PlaybackBar(presenter);

    center.add(new StartPanel(presenter).component(), START_CARD);
    center.add(editor.component(), EDITOR_CARD);

    final JPanel main = new JPanel(new BorderLayout());
    main.add(center, BorderLayout.CENTER);
    main.add(playbackBar.component(), BorderLayout.SOUTH);

    frame.setLayout(new BorderLayout());
    frame.add(musicList.component(), BorderLayout.WEST);
    frame.add(main, BorderLayout.CENTER);
    frame.setMinimumSize(Theme.MIN_WINDOW);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(final WindowEvent e) {
            presenter.onClose();
            System.exit(0);
          }
        });

    new Timer(TICK_MILLIS, e -> presenter.onTick()).start();
  }

  /**
   * Opens the main window on the Swing event thread.
   *
   * @param presenter the presenter that drives the window
   */
  public static void open(final MainPresenter presenter) {
    SwingUtilities.invokeLater(
        () -> {
          FlatLightLaf.setup();
          final MainWindow window = new MainWindow(presenter);
          presenter.attach(window);
          window.frame.setLocationRelativeTo(null);
          window.frame.setVisible(true);
        });
  }

  @Override
  public void showMusics(final List<Music> musics, final Music selected) {
    musicList.show(musics, selected);
  }

  @Override
  public void showStartScreen() {
    frame.setTitle(APP_TITLE);
    cards.show(center, START_CARD);
  }

  @Override
  public void showEditor(final Music music) {
    frame.setTitle(music.getName() + " - " + APP_TITLE);
    editor.setText(music.getContent());
    cards.show(center, EDITOR_CARD);
  }

  @Override
  public String editorText() {
    return editor.getText();
  }

  @Override
  public void showSettings(final int octave, final int bpm, final int volume) {
    playbackBar.showSettings(octave, bpm, volume);
  }

  @Override
  public void showPlaying(final boolean playing) {
    playbackBar.showPlaying(playing);
  }

  @Override
  public void showProgress(final int permille) {
    playbackBar.showProgress(permille);
  }

  @Override
  public void highlight(final List<Integer> offsets) {
    editor.highlight(offsets);
  }

  @Override
  public void showError(final String message) {
    JOptionPane.showMessageDialog(frame, message, "Erro", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void showInfo(final String message) {
    JOptionPane.showMessageDialog(frame, message);
  }

  @Override
  public Optional<String> askText(final String message, final String initialValue) {
    return Optional.ofNullable(JOptionPane.showInputDialog(frame, message, initialValue));
  }

  @Override
  public boolean confirm(final String message) {
    return JOptionPane.showConfirmDialog(frame, message, "Confirmar", JOptionPane.YES_NO_OPTION)
        == JOptionPane.YES_OPTION;
  }

  @Override
  public Optional<Path> chooseTextFile() {
    return FileDialogs.open(frame, "Arquivos de texto (.txt)", "txt");
  }

  @Override
  public Optional<Path> chooseExportFile(final String suggestedName) {
    return FileDialogs.save(frame, "Arquivo MIDI (.mid)", "mid", suggestedName);
  }
}
