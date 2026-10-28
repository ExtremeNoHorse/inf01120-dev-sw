package com.player.ui.swing;

import com.player.ui.MainPresenter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/** Text editor with the "salvar" button and the playback highlight (Telas 2, 3 e 4). */
final class EditorPanel {
  private final JPanel panel = new JPanel(new BorderLayout(0, 10));
  private final JTextArea textArea = new JTextArea();
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Theme.HIGHLIGHT);

  EditorPanel(final MainPresenter presenter) {
    panel.setBackground(Theme.BACKGROUND);
    panel.setBorder(Theme.createBorder(Theme.PADDING, Theme.MARGIN));

    final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    toolbar.setOpaque(false);
    toolbar.add(Theme.primaryButton("⤓  salvar", presenter::onSave));
    panel.add(toolbar, BorderLayout.NORTH);

    textArea.setFont(Theme.TEXT);
    textArea.setBackground(Theme.PAPER);
    textArea.setBorder(Theme.createBorder(Theme.PADDING, Theme.PADDING));
    panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
  }

  JPanel component() {
    return panel;
  }

  void setText(final String text) {
    textArea.setText(text);
    textArea.setCaretPosition(0);
  }

  String getText() {
    return textArea.getText();
  }

  void setEditable(final boolean editable) {
    textArea.setEditable(editable);
  }

  void highlight(final List<Integer> offsets) {
    final Highlighter highlighter = textArea.getHighlighter();
    highlighter.removeAllHighlights();
    try {
      for (final int offset : offsets) {
        highlighter.addHighlight(offset, offset + 1, painter);
      }
    } catch (final BadLocationException _) {
      // The text was edited while playing; the next tick will fix the highlight.
      highlighter.removeAllHighlights();
    }
  }
}
