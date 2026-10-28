package com.player.usecase;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.repository.Music;
import com.player.repository.MusicRepository;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Facade with the use cases of the "Minhas músicas" library (create, import, save, rename, delete
 * and list), so the UI never talks to the repository directly.
 */
public final class MusicLibrary {
  private static final String TEXT_EXTENSION = ".txt";

  private final MusicRepository repository;

  /**
   * Constructs a {@code MusicLibrary} over the given repository.
   *
   * @param repository the music persistence repository
   */
  public MusicLibrary(final MusicRepository repository) {
    this.repository = Objects.requireNonNull(repository, "Repository cannot be null");
  }

  /**
   * Lists every saved music sorted by name.
   *
   * @return the saved musics
   */
  public List<Music> listAll() {
    return repository.findAll().stream()
        .sorted(Comparator.comparing(Music::getName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  /**
   * Creates and persists an empty music with the given name.
   *
   * @param name the display name
   * @return the persisted music
   * @throws TtmException if the name is blank
   */
  public Music create(final String name) {
    final Music music = repository.newMusic();
    music.setName(requireName(name));
    return repository.save(music);
  }

  /**
   * Imports a text file as a new music named after the file.
   *
   * @param source the text file to import
   * @return the persisted music
   * @throws TtmException if the file cannot be read
   */
  public Music importFile(final Path source) {
    final Music read = repository.getByPath(source);
    if (read == null) {
      throw new TtmException(ErrorCode.MUSIC_NOT_FOUND, "Arquivo não encontrado: " + source);
    }
    final Music music = repository.newMusic();
    music.setName(nameFromFile(source));
    music.setContent(read.getContent());
    music.setSourcePath(source.toAbsolutePath().normalize());
    return repository.save(music);
  }

  /**
   * Saves new text content for a music.
   *
   * @param music the music to update
   * @param content the new text content
   */
  public void saveContent(final Music music, final String content) {
    music.setContent(content);
    repository.saveAndOverwriteSource(music);
  }

  /**
   * Renames a music.
   *
   * @param music the music to rename
   * @param newName the new display name
   * @throws TtmException if the name is blank
   */
  public void rename(final Music music, final String newName) {
    music.setName(requireName(newName));
    repository.save(music);
  }

  /**
   * Deletes a music from the library.
   *
   * @param music the music to delete
   */
  public void delete(final Music music) {
    repository.deleteById(music.getId());
  }

  private static String requireName(final String name) {
    if (name == null || name.isBlank()) {
      throw new TtmException(ErrorCode.INVALID_INPUT, "O nome da música não pode ser vazio.");
    }
    return name.strip();
  }

  private static String nameFromFile(final Path source) {
    final String fileName = String.valueOf(source.getFileName());
    return fileName.endsWith(TEXT_EXTENSION)
        ? fileName.substring(0, fileName.length() - TEXT_EXTENSION.length())
        : fileName;
  }
}
