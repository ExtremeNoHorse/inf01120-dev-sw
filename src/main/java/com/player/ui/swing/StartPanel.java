package com.player.ui.swing;

import com.player.ui.MainPresenter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** "Como deseja começar?" screen with the two start options (Tela 1). */
final class StartPanel {
  private final JPanel panel = new JPanel(new GridBagLayout());

  StartPanel(final MainPresenter presenter) {
    panel.setBackground(Theme.BACKGROUND);

    final JPanel content = new JPanel(new BorderLayout(0, Theme.MARGIN * 2));
    content.setOpaque(false);
    final JLabel title = new JLabel("Como deseja começar?", SwingConstants.CENTER);
    title.setFont(Theme.TITLE);
    content.add(title, BorderLayout.NORTH);

    final JPanel cards = new JPanel(new GridLayout(1, 2, Theme.MARGIN, 0));
    cards.setOpaque(false);
    cards.add(
        card(
            "✎",
            "Digitar Texto",
            "Digite um texto para começar um novo arquivo de música do zero.",
            "Digitar Texto",
            presenter::onTypeText));
    cards.add(
        card(
            "⇪",
            "Carregar Arquivo",
            "Faça o upload de um arquivo de texto existente.",
            "Fazer Upload",
            presenter::onUpload));
    content.add(cards, BorderLayout.CENTER);
    panel.add(content);
  }

  JPanel component() {
    return panel;
  }

  private static JPanel card(
      final String icon,
      final String title,
      final String description,
      final String buttonText,
      final Runnable action) {
    final JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setOpaque(false);
    card.setBorder(
        BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.SIDEBAR),
            Theme.padding(Theme.MARGIN, Theme.MARGIN)));

    final JLabel iconLabel = centered(new JLabel(icon));
    iconLabel.setFont(Theme.CARD_ICON);
    final JLabel titleLabel = centered(new JLabel(title));
    titleLabel.setFont(Theme.CARD_TITLE);
    final JLabel descriptionLabel =
        centered(new JLabel("<html><div style='text-align:center;width:160px'>" + description));

    card.add(iconLabel);
    card.add(titleLabel);
    card.add(descriptionLabel);
    card.add(Box.createVerticalStrut(Theme.MARGIN));
    card.add(centered(Theme.primaryButton(buttonText, action)));
    return card;
  }

  private static <T extends JComponent> T centered(final T component) {
    component.setAlignmentX(Component.CENTER_ALIGNMENT);
    return component;
  }
}
