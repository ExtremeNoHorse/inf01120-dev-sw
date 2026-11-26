package com.player;

import com.player.audio.AudioEngine;
import com.player.audio.JFuguePlayer;
import com.player.config.PlayerConfig;
import com.player.interpreter.Interpreter;
import com.player.interpreter.domain.DomainMapper;
import com.player.repository.FileMusicRepository;
import com.player.repository.MusicRepository;
import com.player.ui.MainPresenter;
import com.player.ui.swing.MainWindow;
import com.player.usecase.MusicLibrary;
import com.player.validation.InputValidator;

/** Main application composition root entry point. */
public final class App {
  private App() {}

  static void main() {
    final MusicRepository musicRepository = new FileMusicRepository("musics");
    final MusicLibrary musicLibrary = new MusicLibrary(musicRepository);
    final DomainMapper domainMapper = DomainMapper.createDefault();
    final PlayerConfig playerConfig = PlayerConfig.forVoice(0, PlayerConfig.DEFAULT_BPM);
    final Interpreter interpreter = new Interpreter(domainMapper, playerConfig);
    final AudioEngine audioEngine = new JFuguePlayer();
    final Player player = new Player(interpreter, audioEngine);
    final InputValidator inputValidator = new InputValidator();
    final MainPresenter presenter = new MainPresenter(musicLibrary, player, inputValidator);

    MainWindow.open(presenter);
  }
}
