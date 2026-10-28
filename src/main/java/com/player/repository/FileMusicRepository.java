package com.player.repository;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * File-system based implementation of {@link MusicRepository}. Each music is stored as {@code
 * <id>.txt}, and the names live in a single {@code metadata.properties} that maps each id to its
 * name.
 */
public final class FileMusicRepository implements MusicRepository {
  private static final String FILE_EXTENSION = ".txt";
  private static final String METADATA_FILE = "metadata.properties";
  private static final String METADATA_COMMENT = "Nome de cada musica, indexado pelo id";
  private static final String SOURCE_KEY_PREFIX = "source.";
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
    prepareToSave(music);
    return saveLibraryCopy(music);
  }

  @Override
  public Music saveAndOverwriteSource(final Music music) {
    prepareToSave(music);
    try {
      overwriteSource(music);
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Falha ao salvar a música: " + music.getId(), e);
    }
    return saveLibraryCopy(music);
  }

  private static void prepareToSave(final Music music) {
    Objects.requireNonNull(music, "Music cannot be null");
    if (music.getId() == null || music.getId().trim().isEmpty()) {
      music.setId(UUID.randomUUID().toString());
    }
  }

  private Music saveLibraryCopy(final Music music) {
    final Path filePath = baseDir.resolve(music.getId() + FILE_EXTENSION);
    try {
      final String content = music.getContent() != null ? music.getContent() : "";
      Files.writeString(filePath, content);
      final Properties names = loadNames();
      if (music.getName() != null) {
        names.setProperty(music.getId(), music.getName());
      }
      storeSourcePath(names, music);
      storeNames(names);
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
    return readLibraryMusic(filePath);
  }

  @Override
  public Music getByPath(final Path path) {
    return readExternalMusic(path);
  }

  private Music readExternalMusic(final Path path) {
    try {
      final Music music = readMusic(path);
      if (music == null) {
        return null;
      }
      restoreName(music, loadNames());
      return music;
    } catch (final IOException e) {
      throw new TtmException(ErrorCode.PERSISTENCE_FAILURE, "Erro ao ler o arquivo: " + path, e);
    }
  }

  private Music readLibraryMusic(final Path path) {
    try {
      final Music music = readMusic(path);
      if (music == null) {
        return null;
      }
      final Properties names = loadNames();
      restoreName(music, names);
      final String sourcePath = names.getProperty(sourceKey(music.getId()));
      if (sourcePath != null) {
        music.setSourcePath(Path.of(sourcePath));
      }
      return music;
    } catch (final IOException e) {
      throw new TtmException(ErrorCode.PERSISTENCE_FAILURE, "Erro ao ler o arquivo: " + path, e);
    }
  }

  private static Music readMusic(final Path path) throws IOException {
    if (path == null || !Files.exists(path)) {
      return null;
    }
    final Path fileNamePath = path.getFileName();
    if (fileNamePath == null) {
      return null;
    }
    final String content = Files.readString(path);
    final String fileName = fileNamePath.toString();
    final String id =
        fileName.endsWith(FILE_EXTENSION)
            ? fileName.substring(0, fileName.length() - FILE_EXTENSION.length())
            : fileName;
    return new Music(id, defaultName(id), content);
  }

  @Override
  public List<Music> findAll() {
    final List<Music> musicList = new ArrayList<>();
    try (Stream<Path> paths = Files.list(baseDir)) {
      final List<Path> txtFiles = paths.filter(p -> p.toString().endsWith(FILE_EXTENSION)).toList();

      for (final Path path : txtFiles) {
        final Music music = readLibraryMusic(path);
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
      final Properties names = loadNames();
      final boolean metadataChanged = names.remove(id) != null;
      final boolean sourceRemoved = names.remove(sourceKey(id)) != null;
      if (metadataChanged || sourceRemoved) {
        storeNames(names);
      }
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Erro ao deletar o arquivo: " + filePath, e);
    }
  }

  private Properties loadNames() throws IOException {
    final Properties names = new Properties();
    final Path metadata = baseDir.resolve(METADATA_FILE);
    if (Files.exists(metadata)) {
      try (Reader reader = Files.newBufferedReader(metadata, StandardCharsets.UTF_8)) {
        names.load(reader);
      }
    }
    return names;
  }

  private void storeNames(final Properties names) throws IOException {
    try (Writer writer =
        Files.newBufferedWriter(baseDir.resolve(METADATA_FILE), StandardCharsets.UTF_8)) {
      names.store(writer, METADATA_COMMENT);
    }
  }

  private static void overwriteSource(final Music music) throws IOException {
    final Path sourcePath = music.getSourcePath();
    if (sourcePath == null) {
      return;
    }
    if (!Files.isRegularFile(sourcePath)) {
      throw new IOException("Arquivo de origem não encontrado: " + sourcePath);
    }
    Files.writeString(sourcePath, music.getContent() != null ? music.getContent() : "");
  }

  private static void restoreName(final Music music, final Properties names) {
    music.setName(names.getProperty(music.getId(), defaultName(music.getId())));
  }

  private static void storeSourcePath(final Properties names, final Music music) {
    final String key = sourceKey(music.getId());
    if (music.getSourcePath() == null) {
      names.remove(key);
    } else {
      names.setProperty(key, music.getSourcePath().toString());
    }
  }

  private static String sourceKey(final String id) {
    return SOURCE_KEY_PREFIX + id;
  }

  private static String defaultName(final String id) {
    return "Música " + id.substring(0, Math.min(id.length(), SHORT_ID_LENGTH));
  }
}
