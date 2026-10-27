package com.player.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

/** Colors, fonts and button factories shared by every panel, following the croquis. */
final class Theme {
  static final Color SIDEBAR = new Color(0xA9A9A9);
  static final Color BACKGROUND = new Color(0xDCDCDC);
  static final Color PAPER = new Color(0xFFFCFC);
  static final Color ACCENT = new Color(0x2F95EA);
  static final Color HIGHLIGHT = new Color(0xF0524F);
  static final Font TITLE = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
  static final Font CARD_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 17);
  static final Font TEXT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
  static final Font ICON = new Font(Font.SANS_SERIF, Font.BOLD, 20);
  static final Font CARD_ICON = TITLE.deriveFont(48f);
  static final Font PLAY_ICON = ICON.deriveFont(32f);

  static final int GAP = 8;
  static final int PADDING = 12;
  static final int MARGIN = 24;
  static final Dimension MIN_WINDOW = new Dimension(900, 620);
  static final Dimension SIDEBAR_SIZE = new Dimension(260, 0);
  static final Dimension VALUE_BOX = new Dimension(64, 56);
  static final Dimension VOLUME_SIZE = new Dimension(120, 24);

  private Theme() {}

  /** Creates an empty border with the same top/bottom and left/right spacing. */
  static Border padding(final int vertical, final int horizontal) {
    return BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal);
  }

  /** Creates a filled blue button, like "Fazer Upload" and "salvar". */
  static JButton primaryButton(final String text, final Runnable action) {
    final JButton button = new JButton(text);
    button.setBackground(ACCENT);
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.addActionListener(e -> action.run());
    return button;
  }

  /** Creates a borderless icon button, like the playback controls. */
  static JButton iconButton(final String text, final String tooltip, final Runnable action) {
    final JButton button = new JButton(text);
    button.setFont(ICON);
    button.setToolTipText(tooltip);
    button.setContentAreaFilled(false);
    button.setBorder(padding(GAP / 2, 10));
    button.setFocusPainted(false);
    button.addActionListener(e -> action.run());
    return button;
  }
}
