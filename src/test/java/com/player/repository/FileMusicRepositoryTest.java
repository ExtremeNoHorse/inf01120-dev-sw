package com.player.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileMusicRepositoryTest {
  private static final String DELETE_TEST_ID = "del-id";
  private static final String TEST_CONTENT = "content";

  private Path tempDir;
  private FileMusicRepository repository;

  @BeforeEach
  void setUp(@TempDir final Path tempDir) {
    this.tempDir = tempDir;
    this.repository = new FileMusicRepository(tempDir.toString());
  }

  @Test
  void givenRepository_whenNewMusic_thenCreatesWithIdAndDefaults() {
    // When
    final Music music = repository.newMusic();

    // Then
    assertNotNull(music.getId());
    assertFalse(music.getId().isEmpty());
    assertEquals("Nova Música", music.getName());
    assertEquals("", music.getContent());
    assertFalse(music.hasRecording());
  }

  @Test
  void givenMusic_whenSaveAndGetById_thenReturnsSavedContent() {
    // Given
    final Music music = new Music("test-1", "Test Song", "A B C");

    // When
    repository.save(music);

    final Music loaded = repository.getById("test-1");

    // Then
    assertNotNull(loaded);
    assertEquals("test-1", loaded.getId());
    assertEquals("A B C", loaded.getContent());
  }

  @Test
  void givenNamedMusic_whenSaveAndReload_thenKeepsName() {
    // Given & When
    repository.save(new Music("named", "Minha Canção", "A B C"));

    // Then
    assertEquals("Minha Canção", repository.getById("named").getName());
    assertEquals("Minha Canção", repository.findAll().getFirst().getName());
  }

  @Test
  void givenNamedMusic_whenDeleted_thenRemovesItsNameFromMetadata() throws IOException {
    // Given
    repository.save(new Music("kept", "Kept", "A"));
    repository.save(new Music("gone", "Some", "A"));

    // When
    repository.deleteById("gone");

    // Then
    final String metadata = Files.readString(tempDir.resolve("metadata.properties"));
    assertTrue(metadata.contains("kept=Kept"));
    assertFalse(metadata.contains("gone="));
    assertEquals(List.of("Kept"), repository.findAll().stream().map(Music::getName).toList());
  }

  @Test
  void givenNamesWithSpecialCharacters_whenSaveAndReload_thenKeepsThemExactly() {
    // Given
    final String name = "Rock|Pop: ação = 100% #1";

    // When
    repository.save(new Music("special", name, "A"));

    // Then
    assertEquals(name, new FileMusicRepository(tempDir.toString()).getById("special").getName());
  }

  @Test
  void givenMusicWithoutId_whenSave_thenGeneratesIdAndPersists() {
    // Given
    final Music music = new Music(null, "No ID Song", "C D E");

    // When
    final Music saved = repository.save(music);

    // Then
    assertNotNull(saved.getId());
    final Music loaded = repository.getById(saved.getId());
    assertNotNull(loaded);
    assertEquals("C D E", loaded.getContent());
  }

  @Test
  void givenExistingMusic_whenSaveAgain_thenOverwritesFile() {
    // Given
    final Music music = new Music("overwrite-id", "Original", "Original Content");
    repository.save(music);

    // When
    music.setContent("Updated Content");
    repository.save(music);

    // Then
    final Music reloaded = repository.getById("overwrite-id");
    assertNotNull(reloaded);
    assertEquals("Updated Content", reloaded.getContent());
  }

  @Test
  void givenFile_whenGetByPath_thenReturnsMusic() throws IOException {
    // Given
    final Path filePath = tempDir.resolve("direct-file.txt");
    Files.writeString(filePath, "Direct file content");

    // When
    final Music loaded = repository.getByPath(filePath);

    // Then
    assertNotNull(loaded);
    assertEquals("direct-file", loaded.getId());
    assertEquals("Direct file content", loaded.getContent());
  }

  @Test
  void givenNonExistentId_whenGetById_thenReturnsNull() {
    // Given & When & Then
    assertNull(repository.getById("does-not-exist"));
    assertNull(repository.getById(null));
    assertNull(repository.getById("  "));
  }

  @Test
  void givenNonExistentPath_whenGetByPath_thenReturnsNull() {
    // Given & When & Then
    assertNull(repository.getByPath(null));
    assertNull(repository.getByPath(tempDir.resolve("non-existent.txt")));
  }

  @Test
  void givenSavedFiles_whenFindAll_thenReturnsAll() {
    // Given
    repository.save(new Music("song-1", "S1", "A:3"));
    repository.save(new Music("song-2", "S2", "B:3"));

    // When
    final List<Music> all = repository.findAll();

    // Then
    assertEquals(2, all.size());
  }

  @Test
  void givenSavedMusic_whenDeleteById_thenFileIsRemoved() {
    // Given
    final Music music = new Music(DELETE_TEST_ID, "ToDelete", TEST_CONTENT);
    repository.save(music);
    assertNotNull(repository.getById(DELETE_TEST_ID));

    // When
    repository.deleteById(DELETE_TEST_ID);

    // Then
    assertNull(repository.getById(DELETE_TEST_ID));

    // When & Then
    // Deleting non-existent does not throw
    repository.deleteById(DELETE_TEST_ID);
    repository.deleteById(null);
  }

  @Test
  void givenMusicProperties_whenGettersAndSettersCalled_thenValuesUpdated() {
    // Given
    final Music music = new Music();
    music.setId("m1");
    music.setName("Song Name");
    music.setContent("Notes");
    music.setHasRecording(true);

    // When & Then
    assertEquals("m1", music.getId());
    assertEquals("Song Name", music.getName());
    assertEquals("Notes", music.getContent());

    // When & Then
    final Music otherSameId = new Music("m1", "Other", "Notes2");
    assertEquals(music, otherSameId);
    assertEquals(music.hashCode(), otherSameId.hashCode());

    // When & Then
    final Music otherDiffId = new Music("m2", "Other", "Notes2");
    assertNotEquals(music, otherDiffId);
    assertNotEquals(new Object(), music);
  }

  @Test
  void givenNullDirectory_whenConstructed_thenThrowsNullPointerException() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new FileMusicRepository(null));
  }

  @Test
  void givenMissingBaseDirectory_whenConstructed_thenCreatesIt() {
    // Given
    final Path nestedDirectory = tempDir.resolve("new").resolve("nested");

    // When
    new FileMusicRepository(nestedDirectory.toString());

    // Then
    assertTrue(Files.isDirectory(nestedDirectory));
  }

  @Test
  void givenBlankIdAndNullContent_whenSaved_thenGeneratesIdAndStoresEmptyContent() {
    // Given
    final Music music = new Music("  ", "Blank ID", null);

    // When
    repository.save(music);

    // Then
    assertNotNull(music.getId());
    assertEquals("", repository.getById(music.getId()).getContent());
  }

  @Test
  void givenRootPath_whenLoaded_thenReturnsNull() {
    // Given & When & Then
    assertNull(repository.getByPath(tempDir.getRoot()));
  }

  @Test
  void givenShortExtensionlessFile_whenLoaded_thenUsesShortName() throws IOException {
    // Given
    final Path filePath = tempDir.resolve("a");
    Files.writeString(filePath, TEST_CONTENT);

    // When
    final Music loaded = repository.getByPath(filePath);

    // Then
    assertEquals("a", loaded.getId());
    assertEquals("Música a", loaded.getName());
    assertEquals(TEST_CONTENT, loaded.getContent());
  }

  @Test
  void givenDirectoryPath_whenLoaded_thenThrowsPersistenceFailure() {
    // When & Then
    final TtmException exception =
        assertThrows(TtmException.class, () -> repository.getByPath(tempDir));

    // Then
    assertEquals(ErrorCode.PERSISTENCE_FAILURE, exception.getCode());
  }

  @Test
  void givenBrokenTextLink_whenFindingAll_thenSkipsMissingTarget() throws IOException {
    // Given
    final Path brokenLink = tempDir.resolve("broken.txt");
    Files.createSymbolicLink(brokenLink, tempDir.resolve("missing.txt"));

    // When & Then
    assertTrue(repository.findAll().isEmpty());
  }

  @Test
  void givenFileAsBasePath_whenUsingRepository_thenReportsPersistenceFailures() throws IOException {
    // Given
    final Path baseFile = tempDir.resolve("base-file");
    Files.writeString(baseFile, "not a directory");
    final String childPath = baseFile.resolve("child").toString();

    // When & Then
    assertThrows(TtmException.class, () -> new FileMusicRepository(childPath));

    // Given
    final FileMusicRepository invalidRepository = new FileMusicRepository(baseFile.toString());
    final Music music = new Music("id", "Song", TEST_CONTENT);

    // When & Then
    assertThrows(TtmException.class, () -> invalidRepository.save(music));
    assertThrows(TtmException.class, invalidRepository::findAll);
    assertThrows(TtmException.class, () -> invalidRepository.deleteById("id"));
  }

  @Test
  void givenBlankId_whenDeleted_thenDoesNothing() {
    // When & Then
    assertDoesNotThrow(() -> repository.deleteById("  "));
  }
}
