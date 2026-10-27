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
    final Music music = repository.newMusic();
    assertNotNull(music.getId());
    assertFalse(music.getId().isEmpty());
    assertEquals("Nova Música", music.getName());
    assertEquals("", music.getContent());
    assertFalse(music.hasRecording());
  }

  @Test
  void givenMusic_whenSaveAndGetById_thenReturnsSavedContent() {
    final Music music = new Music("test-1", "Test Song", "A B C");
    repository.save(music);

    final Music loaded = repository.getById("test-1");
    assertNotNull(loaded);
    assertEquals("test-1", loaded.getId());
    assertEquals("A B C", loaded.getContent());
  }

  @Test
  void givenNamedMusic_whenSaveAndReload_thenKeepsName() {
    repository.save(new Music("named", "Minha Canção", "A B C"));

    assertEquals("Minha Canção", repository.getById("named").getName());
    assertEquals("Minha Canção", repository.findAll().getFirst().getName());
  }

  @Test
  void givenNamedMusic_whenDeleted_thenRemovesItsNameFromMetadata() throws IOException {
    repository.save(new Music("kept", "Kept", "A"));
    repository.save(new Music("gone", "Some", "A"));
    repository.deleteById("gone");

    final String metadata = Files.readString(tempDir.resolve("metadata.properties"));
    assertTrue(metadata.contains("kept=Kept"));
    assertFalse(metadata.contains("gone="));
    assertEquals(List.of("Kept"), repository.findAll().stream().map(Music::getName).toList());
  }

  @Test
  void givenNamesWithSpecialCharacters_whenSaveAndReload_thenKeepsThemExactly() {
    final String name = "Rock/Pop: ação = 100% #1";
    repository.save(new Music("special", name, "A"));

    assertEquals(name, new FileMusicRepository(tempDir.toString()).getById("special").getName());
  }

  @Test
  void givenMusicWithoutId_whenSave_thenGeneratesIdAndPersists() {
    final Music music = new Music(null, "No ID Song", "C D E");
    final Music saved = repository.save(music);

    assertNotNull(saved.getId());
    final Music loaded = repository.getById(saved.getId());
    assertNotNull(loaded);
    assertEquals("C D E", loaded.getContent());
  }

  @Test
  void givenExistingMusic_whenSaveAgain_thenOverwritesFile() {
    final Music music = new Music("overwrite-id", "Original", "Original Content");
    repository.save(music);

    music.setContent("Updated Content");
    repository.save(music);

    final Music reloaded = repository.getById("overwrite-id");
    assertNotNull(reloaded);
    assertEquals("Updated Content", reloaded.getContent());
  }

  @Test
  void givenFile_whenGetByPath_thenReturnsMusic() throws IOException {
    final Path filePath = tempDir.resolve("direct-file.txt");
    Files.writeString(filePath, "Direct file content");

    final Music loaded = repository.getByPath(filePath);
    assertNotNull(loaded);
    assertEquals("direct-file", loaded.getId());
    assertEquals("Direct file content", loaded.getContent());
  }

  @Test
  void givenNonExistentId_whenGetById_thenReturnsNull() {
    assertNull(repository.getById("does-not-exist"));
    assertNull(repository.getById(null));
    assertNull(repository.getById("  "));
  }

  @Test
  void givenNonExistentPath_whenGetByPath_thenReturnsNull() {
    assertNull(repository.getByPath(null));
    assertNull(repository.getByPath(tempDir.resolve("non-existent.txt")));
  }

  @Test
  void givenSavedFiles_whenFindAll_thenReturnsAll() {
    repository.save(new Music("song-1", "S1", "A:3"));
    repository.save(new Music("song-2", "S2", "B:3"));

    final List<Music> all = repository.findAll();
    assertEquals(2, all.size());
  }

  @Test
  void givenSavedMusic_whenDeleteById_thenFileIsRemoved() {
    final Music music = new Music(DELETE_TEST_ID, "ToDelete", TEST_CONTENT);
    repository.save(music);
    assertNotNull(repository.getById(DELETE_TEST_ID));

    repository.deleteById(DELETE_TEST_ID);
    assertNull(repository.getById(DELETE_TEST_ID));

    // Deleting non-existent does not throw
    repository.deleteById(DELETE_TEST_ID);
    repository.deleteById(null);
  }

  @Test
  void givenMusicProperties_whenGettersAndSettersCalled_thenValuesUpdated() {
    final Music music = new Music();
    music.setId("m1");
    music.setName("Song Name");
    music.setContent("Notes");
    music.setHasRecording(true);

    assertEquals("m1", music.getId());
    assertEquals("Song Name", music.getName());
    assertEquals("Notes", music.getContent());
    assertTrue(music.hasRecording());

    final Music otherSameId = new Music("m1", "Other", "Notes2");
    assertEquals(music, otherSameId);
    assertEquals(music.hashCode(), otherSameId.hashCode());

    final Music otherDiffId = new Music("m2", "Other", "Notes2");
    assertNotEquals(music, otherDiffId);
    assertTrue(music.equals(music));
    assertFalse(music.equals(new Object()));
  }

  @Test
  void givenNullDirectory_whenConstructed_thenThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new FileMusicRepository(null));
  }

  @Test
  void givenMissingBaseDirectory_whenConstructed_thenCreatesIt() {
    final Path nestedDirectory = tempDir.resolve("new").resolve("nested");

    new FileMusicRepository(nestedDirectory.toString());

    assertTrue(Files.isDirectory(nestedDirectory));
  }

  @Test
  void givenBlankIdAndNullContent_whenSaved_thenGeneratesIdAndStoresEmptyContent() {
    final Music music = new Music("  ", "Blank ID", null);

    repository.save(music);

    assertNotNull(music.getId());
    assertEquals("", repository.getById(music.getId()).getContent());
  }

  @Test
  void givenRootPath_whenLoaded_thenReturnsNull() {
    assertNull(repository.getByPath(tempDir.getRoot()));
  }

  @Test
  void givenShortExtensionlessFile_whenLoaded_thenUsesShortName() throws IOException {
    final Path filePath = tempDir.resolve("a");
    Files.writeString(filePath, TEST_CONTENT);

    final Music loaded = repository.getByPath(filePath);

    assertEquals("a", loaded.getId());
    assertEquals("Música a", loaded.getName());
    assertEquals(TEST_CONTENT, loaded.getContent());
  }

  @Test
  void givenDirectoryPath_whenLoaded_thenThrowsPersistenceFailure() {
    final TtmException exception =
        assertThrows(TtmException.class, () -> repository.getByPath(tempDir));

    assertEquals(ErrorCode.PERSISTENCE_FAILURE, exception.getCode());
  }

  @Test
  void givenBrokenTextLink_whenFindingAll_thenSkipsMissingTarget() throws IOException {
    final Path brokenLink = tempDir.resolve("broken.txt");
    Files.createSymbolicLink(brokenLink, tempDir.resolve("missing.txt"));

    assertTrue(repository.findAll().isEmpty());
  }

  @Test
  void givenFileAsBasePath_whenUsingRepository_thenReportsPersistenceFailures() throws IOException {
    final Path baseFile = tempDir.resolve("base-file");
    Files.writeString(baseFile, "not a directory");

    assertThrows(
        TtmException.class, () -> new FileMusicRepository(baseFile.resolve("child").toString()));

    final FileMusicRepository invalidRepository = new FileMusicRepository(baseFile.toString());
    assertThrows(
        TtmException.class, () -> invalidRepository.save(new Music("id", "Song", TEST_CONTENT)));
    assertThrows(TtmException.class, invalidRepository::findAll);
    assertThrows(TtmException.class, () -> invalidRepository.deleteById("id"));
  }

  @Test
  void givenBlankId_whenDeleted_thenDoesNothing() {
    assertDoesNotThrow(() -> repository.deleteById("  "));
  }
}
