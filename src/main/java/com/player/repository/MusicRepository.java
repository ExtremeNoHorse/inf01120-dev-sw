package com.player.repository;

import java.nio.file.Path;
import java.util.List;

/** Interface defining CRUD operations for storing and retrieving {@link Music} instances. */
public interface MusicRepository {

  /**
   * Creates a new blank {@link Music} instance with a generated identifier.
   *
   * @return a new empty {@link Music} object
   */
  Music newMusic();

  /**
   * Persists a {@link Music} object to the underlying storage.
   *
   * @param music the music instance to save
   * @return the persisted music instance
   */
  Music save(final Music music);

  /**
   * Persists a music and, when it was imported, overwrites its original text file.
   *
   * @param music the music instance to save
   * @return the persisted music instance
   */
  default Music saveAndOverwriteSource(final Music music) {
    return save(music);
  }

  /**
   * Retrieves a {@link Music} instance by its unique identifier.
   *
   * @param id the unique identifier
   * @return the matching music instance, or {@code null} if not found
   */
  Music getById(final String id);

  /**
   * Retrieves a {@link Music} instance by reading directly from a filesystem path.
   *
   * @param path the path to the text file
   * @return the loaded music instance, or {@code null} if file does not exist
   */
  Music getByPath(final Path path);

  /**
   * Lists all stored {@link Music} instances.
   *
   * @return a list of all saved musics
   */
  List<Music> findAll();

  /**
   * Deletes a {@link Music} instance by its identifier.
   *
   * @param id the unique identifier to delete
   */
  void deleteById(final String id);
}
