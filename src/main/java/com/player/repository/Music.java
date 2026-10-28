package com.player.repository;

import java.nio.file.Path;
import java.util.Objects;

/** Represents a musical score or text source in the TTM system. */
public final class Music {
  private String id;
  private String name;
  private String content;
  private Path sourcePath;
  private boolean hasRecording;

  /** Constructs an empty {@code Music} instance. */
  public Music() {
    this("", "", "");
  }

  /**
   * Constructs a new {@code Music} instance with identifier, name, and textual content.
   *
   * @param id the unique identifier
   * @param name the display name
   * @param content the raw text score
   */
  public Music(final String id, final String name, final String content) {
    this.id = id;
    this.name = name;
    this.content = content;
    this.sourcePath = null;
    this.hasRecording = false;
  }

  /**
   * Retrieves the music identifier.
   *
   * @return the unique id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the music identifier.
   *
   * @param id the unique id
   */
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * Retrieves the display name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the display name.
   *
   * @param name the display name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Retrieves the textual content.
   *
   * @return the text content
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the textual content.
   *
   * @param content the text content
   */
  public void setContent(final String content) {
    this.content = content;
  }

  /**
   * Retrieves the original text file imported for this music, when one exists.
   *
   * @return the original file path, or {@code null} for music created in the library
   */
  public Path getSourcePath() {
    return sourcePath;
  }

  /**
   * Sets the original text file that must be updated when this music is saved.
   *
   * @param sourcePath the imported text file, or {@code null} for library-only music
   */
  public void setSourcePath(final Path sourcePath) {
    this.sourcePath = sourcePath;
  }

  /**
   * Checks if a recorded/exported audio version is available.
   *
   * @return {@code true} if recorded, {@code false} otherwise
   */
  public boolean hasRecording() {
    return hasRecording;
  }

  /** Marks that a recorded/exported audio version is available. */
  public void markAsRecorded() {
    this.hasRecording = true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Music other)) {
      return false;
    }
    return Objects.equals(this.id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }
}
