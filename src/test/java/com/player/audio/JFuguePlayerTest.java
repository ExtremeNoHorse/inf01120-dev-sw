package com.player.audio;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import org.jfugue.player.ManagedPlayer;
import org.jfugue.player.Player;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class JFuguePlayerTest {
  private static final int SEEK_TICK = 12;
  private static final long TICK_LENGTH = 480;
  private static final String TWO_VOICES = "T300 V0 C6q D6q V1 C5h";

  @Test
  void givenNullPlayer_whenConstructed_thenThrowsNullPointerException() {
    // When & Then
    assertThrows(NullPointerException.class, () -> new JFuguePlayer(null));
  }

  @Test
  void givenUnavailableAudio_whenConstructed_thenThrowsAudioUnavailable() {
    // When & Then
    try {
      new JFuguePlayer();
    } catch (final TtmException exception) {
      assertEquals(ErrorCode.AUDIO_UNAVAILABLE, exception.getCode());
      assertInstanceOf(MidiUnavailableException.class, exception.getCause());
      return;
    }

    Assumptions.abort("Audio is available; the unavailable-audio case does not apply");
  }

  @Test
  void givenFakePlayer_whenControlsInvoked_thenDelegatesWithoutHardware() {
    // Given
    final FakePlayer fakePlayer = new FakePlayer();
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    // When & Then
    assertDoesNotThrow(() -> player.play(null));
    assertDoesNotThrow(() -> player.play(""));
    player.play("C6q");
    assertNotNull(fakePlayer.managedPlayer.startedSequence);

    player.pause();
    assertTrue(fakePlayer.managedPlayer.paused);

    player.resume();
    assertTrue(fakePlayer.managedPlayer.resumed);

    player.stop();
    assertTrue(fakePlayer.managedPlayer.finished);
    assertTrue(fakePlayer.managedPlayer.reset);

    player.seek(SEEK_TICK);
    assertEquals(SEEK_TICK, fakePlayer.managedPlayer.seekTick);
  }

  @Test
  void givenFakePlayer_whenProgressQueried_thenReportsManagedPlayerState() {
    // Given
    final FakePlayer fakePlayer = new FakePlayer();
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    // When & Then
    player.seek(SEEK_TICK);
    assertEquals(SEEK_TICK, player.getTickPosition());
    assertEquals(TICK_LENGTH, player.getTickLength());
    assertTrue(player.isActive());

    player.stop();
    assertFalse(player.isActive());
  }

  @Test
  void givenTwoVoices_whenPlayedTwice_thenSecondSequenceDoesNotStartLate() {
    // Given
    final FakePlayer fakePlayer = new FakePlayer();
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    // When
    player.play(TWO_VOICES);
    final long firstLength = fakePlayer.managedPlayer.startedSequence.getTickLength();
    player.play(TWO_VOICES);

    // Then
    assertEquals(firstLength, fakePlayer.managedPlayer.startedSequence.getTickLength());
  }

  @Test
  void givenSequencerFailure_whenPlay_thenThrowsAudioUnavailable() {
    // Given
    final FakePlayer fakePlayer = new FakePlayer();
    fakePlayer.managedPlayer.failOnStart = true;
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    // When & Then
    final TtmException exception = assertThrows(TtmException.class, () -> player.play("C6q"));
    assertEquals(ErrorCode.AUDIO_UNAVAILABLE, exception.getCode());
  }

  @Test
  void givenIdlePlayer_whenPauseAndResumeInvoked_thenDoesNotDelegate() {
    // Given
    final FakePlayer fakePlayer = new FakePlayer();
    fakePlayer.managedPlayer.setPlaying();
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    // When
    player.pause();
    player.resume();

    // Then
    assertFalse(fakePlayer.managedPlayer.paused);
    assertFalse(fakePlayer.managedPlayer.resumed);
  }

  private static final class FakePlayer extends Player {
    private final FakeManagedPlayer managedPlayer = new FakeManagedPlayer();

    @Override
    public ManagedPlayer getManagedPlayer() {
      return managedPlayer;
    }
  }

  private static final class FakeManagedPlayer extends ManagedPlayer {
    private boolean failOnStart;
    private Sequence startedSequence;
    private boolean playing = true;
    private boolean paused;
    private boolean resumed;
    private boolean finished;
    private boolean reset;
    private long seekTick;

    @Override
    public void start(final Sequence sequence) throws MidiUnavailableException {
      if (failOnStart) {
        throw new MidiUnavailableException("no sequencer");
      }
      startedSequence = sequence;
    }

    @Override
    public boolean isPlaying() {
      return playing;
    }

    private void setPlaying() {
      this.playing = false;
    }

    @Override
    public void pause() {
      paused = true;
    }

    @Override
    public boolean isPaused() {
      return paused;
    }

    @Override
    public void resume() {
      resumed = true;
    }

    @Override
    public void finish() {
      finished = true;
    }

    @Override
    public void reset() {
      reset = true;
    }

    @Override
    public void seek(final long tick) {
      seekTick = tick;
    }

    @Override
    public long getTickPosition() {
      return seekTick;
    }

    @Override
    public long getTickLength() {
      return TICK_LENGTH;
    }

    @Override
    public boolean isStarted() {
      return true;
    }

    @Override
    public boolean isFinished() {
      return finished;
    }
  }
}
