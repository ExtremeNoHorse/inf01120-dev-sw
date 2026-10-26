package com.player.ui.swing;

import java.awt.Component;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Open/save file choosers filtered by extension. */
final class FileDialogs {

  private FileDialogs() {}

  static Optional<Path> open(final Component parent, final String description, final String ext) {
    final JFileChooser chooser = chooser(description, ext);
    return selected(chooser, chooser.showOpenDialog(parent));
  }

  static Optional<Path> save(
      final Component parent, final String description, final String ext, final String name) {
    final JFileChooser chooser = chooser(description, ext);
    chooser.setSelectedFile(new File(name));
    return selected(chooser, chooser.showSaveDialog(parent));
  }

  private static JFileChooser chooser(final String description, final String ext) {
    final JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter(description, ext));
    return chooser;
  }

  private static Optional<Path> selected(final JFileChooser chooser, final int result) {
    return result == JFileChooser.APPROVE_OPTION
        ? Optional.of(chooser.getSelectedFile().toPath())
        : Optional.empty();
  }
}
