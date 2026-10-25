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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.MidiUnavailableException;
import org.jfugue.pattern.PatternProducer;
import org.jfugue.player.ManagedPlayer;
import org.jfugue.player.Player;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class JFuguePlayerTest {
  private static final int SEEK_TICK = 12;

  @Test
  void givenNullPlayer_whenConstructed_thenThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> new JFuguePlayer(null));
  }

  @Test
  void givenUnavailableAudio_whenConstructed_thenThrowsAudioUnavailable() {
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
  void givenFakePlayer_whenControlsInvoked_thenDelegatesWithoutHardware() throws Exception {
    final FakePlayer fakePlayer = new FakePlayer();
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    assertDoesNotThrow(() -> player.play(null));
    assertDoesNotThrow(() -> player.play(""));
    player.play("C6q");
    assertTrue(fakePlayer.played.await(1, TimeUnit.SECONDS));
    assertNotNull(fakePlayer.playedPattern);

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
  void givenIdlePlayer_whenPauseAndResumeInvoked_thenDoesNotDelegate() {
    final FakePlayer fakePlayer = new FakePlayer();
    fakePlayer.managedPlayer.setPlaying(false);
    final JFuguePlayer player = new JFuguePlayer(fakePlayer);

    player.pause();
    player.resume();

    assertFalse(fakePlayer.managedPlayer.paused);
    assertFalse(fakePlayer.managedPlayer.resumed);
  }

  private static final class FakePlayer extends Player {
    private final FakeManagedPlayer managedPlayer = new FakeManagedPlayer();
    private final CountDownLatch played = new CountDownLatch(1);
    private PatternProducer playedPattern;

    @Override
    public ManagedPlayer getManagedPlayer() {
      return managedPlayer;
    }

    @Override
    public void play(final PatternProducer patternProducer) {
      playedPattern = patternProducer;
      played.countDown();
    }
  }

  private static final class FakeManagedPlayer extends ManagedPlayer {
    private boolean playing = true;
    private boolean paused;
    private boolean resumed;
    private boolean finished;
    private boolean reset;
    private long seekTick;

    @Override
    public boolean isPlaying() {
      return playing;
    }

    private void setPlaying(final boolean playing) {
      this.playing = playing;
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
  }
}
