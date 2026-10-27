package com.player.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.repository.FileMusicRepository;
import com.player.repository.Music;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MusicLibraryTest {
  private MusicLibrary library;

  @BeforeEach
  void setUp(@TempDir final Path tempDir) {
    library = new MusicLibrary(new FileMusicRepository(tempDir.resolve("musics").toString()));
  }

  @Test
  void givenNames_whenCreate_thenListsSortedByName() {
    // When
    library.create("  beta ");
    library.create("Alfa");

    // Then
    final List<String> names = library.listAll().stream().map(Music::getName).toList();
    assertEquals(List.of("Alfa", "beta"), names);
  }

  @Test
  void givenBlankName_whenCreateOrRename_thenThrowsInvalidInput() {
    // When & Then
    final TtmException created = assertThrows(TtmException.class, () -> library.create(" "));
    assertEquals(ErrorCode.INVALID_INPUT, created.getCode());

    // Given
    final Music music = library.create("ok");

    // When & Then
    assertThrows(TtmException.class, () -> library.rename(music, null));
  }

  @Test
  void givenTextFile_whenImport_thenCreatesMusicNamedAfterFile(@TempDir final Path dir)
      throws IOException {
    // Given
    final Path file = dir.resolve("pedra.txt");
    Files.writeString(file, "C:3 D:3");

    // When
    final Music imported = library.importFile(file);

    // Then
    assertEquals("pedra", imported.getName());
    assertEquals("C:3 D:3", library.listAll().getFirst().getContent());
  }

  @Test
  void givenFileWithoutTxtExtension_whenImport_thenKeepsWholeFileName(@TempDir final Path dir)
      throws IOException {
    // Given
    final Path file = dir.resolve("song");
    Files.writeString(file, "A");

    // When & Then
    assertEquals("song", library.importFile(file).getName());
  }

  @Test
  void givenMissingFile_whenImport_thenThrowsMusicNotFound(@TempDir final Path dir) {
    // When & Then
    final TtmException error =
        assertThrows(TtmException.class, () -> library.importFile(dir.resolve("none.txt")));
    assertEquals(ErrorCode.MUSIC_NOT_FOUND, error.getCode());
  }

  @Test
  void givenMusic_whenSaveRenameAndDelete_thenRepositoryReflectsChanges() {
    // Given
    final Music music = library.create("old");

    // When
    library.saveContent(music, "E:3");
    library.rename(music, "new");
    final Music stored = library.listAll().getFirst();

    // Then
    assertEquals("new", stored.getName());
    assertEquals("E:3", stored.getContent());

    // When
    library.delete(music);

    // Then
    assertTrue(library.listAll().isEmpty());
  }
}
