package com.player.ui.swing;

import com.player.repository.Music;
import com.player.ui.MainPresenter;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

/** Sidebar "Minhas músicas" with the context menu (renomear, deletar, exportar). */
final class MusicListPanel {
  private static final int NAME_WIDTH = 190;

  private final JPanel panel = new JPanel(new BorderLayout());
  private final DefaultListModel<Music> model = new DefaultListModel<>();
  private final JList<Music> list = new JList<>(model);
  private boolean updating;

  MusicListPanel(final MainPresenter presenter) {
    panel.setBackground(Theme.SIDEBAR);
    panel.setPreferredSize(Theme.SIDEBAR_SIZE);
    panel.setBorder(Theme.createBorder(Theme.PADDING, Theme.PADDING));

    final JLabel title = new JLabel("Minhas músicas");
    title.setBorder(BorderFactory.createEmptyBorder(0, 0, Theme.GAP, 0));
    panel.add(title, BorderLayout.NORTH);

    final DefaultListCellRenderer renderer = new DefaultListCellRenderer();
    list.setCellRenderer(
        (lst, music, index, selected, focused) -> {
          final JComponent cell =
              (JComponent)
                  renderer.getListCellRendererComponent(
                      lst, wrapped(music.getName()), index, selected, focused);
          cell.setBorder(Theme.createBorder(Theme.GAP, Theme.PADDING));
          return cell;
        });
    list.setBackground(Theme.SIDEBAR);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(
        e -> {
          if (!updating && !e.getValueIsAdjusting()) {
            presenter.onSelect(list.getSelectedValue());
          }
        });
    list.addMouseListener(contextMenuOpener(presenter));

    final JScrollPane scroll =
        new JScrollPane(
            list,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    panel.add(scroll, BorderLayout.CENTER);

    final var newMusic =
        Theme.iconButton("+  Nova Música", "Criar nova música", presenter::onNewMusic);
    newMusic.setFont(Theme.TEXT);
    panel.add(newMusic, BorderLayout.SOUTH);
  }

  JPanel component() {
    return panel;
  }

  void show(final List<Music> musics, final Music selected) {
    updating = true;
    model.clear();
    musics.forEach(model::addElement);
    list.setSelectedValue(selected, true);
    updating = false;
  }

  private MouseAdapter contextMenuOpener(final MainPresenter presenter) {
    return new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        maybeShow(e);
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        maybeShow(e);
      }

      private void maybeShow(final MouseEvent e) {
        final int row = list.locationToIndex(e.getPoint());
        if (e.isPopupTrigger() && row >= 0 && list.getCellBounds(row, row).contains(e.getPoint())) {
          list.setSelectedIndex(row);
          final Music music = model.get(row);
          final JPopupMenu menu = new JPopupMenu();
          menu.add(item("renomear", music, presenter::onRename));
          menu.add(item("deletar", music, presenter::onDelete));
          menu.add(item("exportar música", music, presenter::onExport));
          menu.show(list, e.getX(), e.getY());
        }
      }
    };
  }

  /** Breaks long names into several lines, like the croquis. */
  private static String wrapped(final String name) {
    final String escaped = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    return "<html><div style='width:" + NAME_WIDTH + "px'>" + escaped + "</div></html>";
  }

  private static JMenuItem item(
      final String label, final Music music, final Consumer<Music> action) {
    final JMenuItem item = new JMenuItem(label);
    item.addActionListener(_ -> action.accept(music));
    return item;
  }
}
