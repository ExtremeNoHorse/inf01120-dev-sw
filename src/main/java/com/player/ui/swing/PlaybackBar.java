package com.player.ui.swing;

import com.player.config.PlayerConfig;
import com.player.ui.MainPresenter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/** Bottom bar: progress, octave, previous, play/pause, next, BPM and volume (croqui). */
final class PlaybackBar {
  private static final int PROGRESS_MAX = 1000;
  private static final String PLAY = "▶";
  private static final String PAUSE = "❚❚";

  private final JPanel panel = new JPanel(new BorderLayout(0, Theme.GAP));
  private final JSlider progress = new JSlider(0, PROGRESS_MAX, 0);
  private final JSlider volume =
      new JSlider(PlayerConfig.MIN_VOLUME, PlayerConfig.MAX_VOLUME, PlayerConfig.MAX_VOLUME);
  private final JLabel octaveLabel = valueBox();
  private final JLabel bpmLabel = valueBox();
  private final JButton playButton;
  private boolean updating;

  PlaybackBar(final MainPresenter presenter) {
    panel.setBackground(Theme.BACKGROUND);
    panel.setBorder(Theme.padding(Theme.GAP, Theme.MARGIN));

    progress.setOpaque(false);
    progress.addChangeListener(
        e -> {
          if (!updating && !progress.getValueIsAdjusting()) {
            presenter.onSeek(progress.getValue());
          }
        });
    panel.add(progress, BorderLayout.NORTH);

    playButton = Theme.iconButton(PLAY, "Tocar / pausar", presenter::onPlayPause);
    playButton.setFont(Theme.PLAY_ICON);

    final JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, Theme.GAP, 0));
    controls.setOpaque(false);
    controls.add(Theme.iconButton("−", "Diminuir oitava", () -> presenter.onOctaveChange(-1)));
    controls.add(octaveLabel);
    controls.add(Theme.iconButton("+", "Aumentar oitava", () -> presenter.onOctaveChange(1)));
    controls.add(Theme.iconButton("|◀", "Voltar ao início", presenter::onRestart));
    controls.add(playButton);
    controls.add(Theme.iconButton("▶|", "Próxima música", presenter::onNext));
    controls.add(Theme.iconButton("−", "Diminuir BPM", () -> presenter.onBpmChange(-1)));
    controls.add(bpmLabel);
    controls.add(Theme.iconButton("+", "Aumentar BPM", () -> presenter.onBpmChange(1)));
    panel.add(controls, BorderLayout.CENTER);

    volume.setOpaque(false);
    volume.setPreferredSize(Theme.VOLUME_SIZE);
    volume.addChangeListener(
        e -> {
          if (!updating && !volume.getValueIsAdjusting()) {
            presenter.onVolumeChange(volume.getValue());
          }
        });
    final JPanel volumeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, Theme.GAP, 0));
    volumeRow.setOpaque(false);
    volumeRow.add(new JLabel("Volume"));
    volumeRow.add(volume);
    panel.add(volumeRow, BorderLayout.SOUTH);
  }

  JPanel component() {
    return panel;
  }

  void showSettings(final int octave, final int bpm, final int volumeValue) {
    octaveLabel.setText(box("OITAVA", octave));
    bpmLabel.setText(box("BPM", bpm));
    updating = true;
    volume.setValue(volumeValue);
    updating = false;
  }

  void showPlaying(final boolean playing) {
    playButton.setText(playing ? PAUSE : PLAY);
  }

  void showProgress(final int permille) {
    if (!progress.getValueIsAdjusting()) {
      updating = true;
      progress.setValue(permille);
      updating = false;
    }
  }

  private static JLabel valueBox() {
    final JLabel label = new JLabel("", SwingConstants.CENTER);
    label.setPreferredSize(Theme.VALUE_BOX);
    label.setBorder(BorderFactory.createLineBorder(Theme.SIDEBAR.darker()));
    return label;
  }

  private static String box(final String title, final int value) {
    return "<html><center>" + title + "<br>" + value + "</center></html>";
  }
}
