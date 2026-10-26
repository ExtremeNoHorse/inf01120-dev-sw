package com.player.repository;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/** File-system based implementation of {@link MusicRepository}. */
public final class FileMusicRepository implements MusicRepository {
  private static final String FILE_EXTENSION = ".txt";
  private static final String NAME_EXTENSION = ".name";
  private static final int SHORT_ID_LENGTH = 4;
  private final Path baseDir;

  /**
   * Constructs a new {@code FileMusicRepository} rooted at the given directory.
   *
   * @param directoryPath the directory path where music files will be stored
   * @throws TtmException if directory creation fails
   */
  public FileMusicRepository(final String directoryPath) {
    Objects.requireNonNull(directoryPath, "Directory path cannot be null");
    this.baseDir = Paths.get(directoryPath);
    try {
      if (!Files.exists(this.baseDir)) {
        Files.createDirectories(this.baseDir);
      }
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Erro ao criar o diretório base: " + e.getMessage(), e);
    }
  }

  @Override
  public Music newMusic() {
    final String id = UUID.randomUUID().toString();
    return new Music(id, "Nova Música", "");
  }

  @Override
  public Music save(final Music music) {
    Objects.requireNonNull(music, "Music cannot be null");
    if (music.getId() == null || music.getId().trim().isEmpty()) {
      music.setId(UUID.randomUUID().toString());
    }

    final Path filePath = baseDir.resolve(music.getId() + FILE_EXTENSION);
    try {
      final String content = music.getContent() != null ? music.getContent() : "";
      Files.writeString(filePath, content);
      if (music.getName() != null) {
        Files.writeString(baseDir.resolve(music.getId() + NAME_EXTENSION), music.getName());
      }
      return music;
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Falha ao salvar a música: " + music.getId(), e);
    }
  }

  @Override
  public Music getById(final String id) {
    if (id == null || id.trim().isEmpty()) {
      return null;
    }
    final Path filePath = baseDir.resolve(id + FILE_EXTENSION);
    return getByPath(filePath);
  }

  @Override
  public Music getByPath(final Path path) {
    if (path == null || !Files.exists(path)) {
      return null;
    }
    final Path fileNamePath = path.getFileName();
    if (fileNamePath == null) {
      return null;
    }
    try {
      final String content = Files.readString(path);
      final String fileName = fileNamePath.toString();
      final String id =
          fileName.endsWith(FILE_EXTENSION)
              ? fileName.substring(0, fileName.length() - FILE_EXTENSION.length())
              : fileName;
      final Path namePath = path.resolveSibling(id + NAME_EXTENSION);
      final String name = Files.exists(namePath) ? Files.readString(namePath) : defaultName(id);
      return new Music(id, name, content);
    } catch (final IOException e) {
      throw new TtmException(ErrorCode.PERSISTENCE_FAILURE, "Erro ao ler o arquivo: " + path, e);
    }
  }

  @Override
  public List<Music> findAll() {
    final List<Music> musicList = new ArrayList<>();
    try (Stream<Path> paths = Files.list(baseDir)) {
      final List<Path> txtFiles = paths.filter(p -> p.toString().endsWith(FILE_EXTENSION)).toList();

      for (final Path path : txtFiles) {
        final Music music = getByPath(path);
        if (music != null) {
          musicList.add(music);
        }
      }
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Erro ao listar músicas no diretório base", e);
    }
    return musicList;
  }

  @Override
  public void deleteById(final String id) {
    if (id == null || id.trim().isEmpty()) {
      return;
    }
    final Path filePath = baseDir.resolve(id + FILE_EXTENSION);
    try {
      Files.deleteIfExists(filePath);
      Files.deleteIfExists(baseDir.resolve(id + NAME_EXTENSION));
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Erro ao deletar o arquivo: " + filePath, e);
    }
  }

  private static String defaultName(final String id) {
    return "Música " + id.substring(0, Math.min(id.length(), SHORT_ID_LENGTH));
  }
}
