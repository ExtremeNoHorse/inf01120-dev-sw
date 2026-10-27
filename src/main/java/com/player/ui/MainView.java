package com.player.ui;

import com.player.repository.Music;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Passive view contract of the main window (MVP). Implementations only render what the {@link
 * MainPresenter} tells them and forward user events back to it.
 */
public interface MainView {

  /**
   * Renders the "Minhas músicas" list.
   *
   * @param musics the saved musics
   * @param selected the music to highlight, or {@code null}
   */
  void showMusics(List<Music> musics, Music selected);

  /** Shows the "Como deseja começar?" screen. */
  void showStartScreen();

  /**
   * Shows the text editor with the given music.
   *
   * @param music the music being edited
   */
  void showEditor(Music music);

  /**
   * Returns the text currently typed in the editor.
   *
   * @return the editor text
   */
  String editorText();

  /**
   * Renders the playback settings.
   *
   * @param octave the current octave
   * @param bpm the current tempo
   * @param volume the current volume (0-127)
   */
  void showSettings(int octave, int bpm, int volume);

  /**
   * Toggles the play/pause button.
   *
   * @param playing {@code true} to show the pause icon
   */
  void showPlaying(boolean playing);

  /**
   * Moves the progress bar.
   *
   * @param permille progress from 0 to 1000
   */
  void showProgress(int permille);

  /**
   * Highlights the characters being played (one per voice line).
   *
   * @param offsets character offsets in the editor text; empty clears the highlight
   */
  void highlight(List<Integer> offsets);

  /**
   * Shows an error message.
   *
   * @param message the message
   */
  void showError(String message);

  /**
   * Shows an information message.
   *
   * @param message the message
   */
  void showInfo(String message);

  /**
   * Asks the user for a line of text.
   *
   * @param message the question
   * @param initialValue the pre-filled answer
   * @return the answer, or empty if cancelled
   */
  Optional<String> askText(String message, String initialValue);

  /**
   * Asks the user to confirm an action.
   *
   * @param message the question
   * @return {@code true} if confirmed
   */
  boolean confirm(String message);

  /**
   * Lets the user pick a text file to import.
   *
   * @return the chosen file, or empty if cancelled
   */
  Optional<Path> chooseTextFile();

  /**
   * Lets the user pick where to save the exported MIDI file.
   *
   * @param suggestedName the default file name
   * @return the chosen file, or empty if cancelled
   */
  Optional<Path> chooseExportFile(String suggestedName);
}
