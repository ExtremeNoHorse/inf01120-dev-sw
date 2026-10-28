package com.player.ui.swing;

import java.awt.Component;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Open/save file choosers filtered by extension. */
final class FileDialogs {

  private FileDialogs() {}

  static Optional<Path> open(final Component parent) {
    final JFileChooser chooser = chooser("Arquivos de texto (.txt)", "txt");
    return selected(chooser, chooser.showOpenDialog(parent));
  }

  static Optional<Path> save(final Component parent, final String name) {
    final JFileChooser chooser = chooser("Arquivo MIDI (.mid)", "mid");
    chooser.setSelectedFile(new File(name));
    final Optional<Path> selected = selected(chooser, chooser.showSaveDialog(parent));
    if (selected.isEmpty()) {
      return Optional.empty();
    }
    final Path target = withExtension(selected.get());
    if (Files.exists(target)
        && JOptionPane.showConfirmDialog(
                parent,
                "Substituir arquivo existente?",
                "Confirmar substituição",
                JOptionPane.YES_NO_OPTION)
            != JOptionPane.YES_OPTION) {
      return Optional.empty();
    }
    return Optional.of(target);
  }

  private static JFileChooser chooser(final String description, final String ext) {
    final JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter(description, ext));
    chooser.setAcceptAllFileFilterUsed(false);
    return chooser;
  }

  private static Optional<Path> selected(final JFileChooser chooser, final int result) {
    return result == JFileChooser.APPROVE_OPTION
        ? Optional.of(chooser.getSelectedFile().toPath())
        : Optional.empty();
  }

  private static Path withExtension(final Path path) {
    final Path fileNamePath = path.getFileName();
    if (fileNamePath == null) {
      return path;
    }
    final String fileName = fileNamePath.toString();
    final String midiExtension = ".mid";
    return fileName.regionMatches(
            true,
            fileName.length() - midiExtension.length(),
            midiExtension,
            0,
            midiExtension.length())
        ? path
        : path.resolveSibling(fileName + midiExtension);
  }
}
