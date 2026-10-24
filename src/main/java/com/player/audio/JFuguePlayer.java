package com.player.audio;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.util.Objects;
import org.jfugue.pattern.Pattern;
import org.jfugue.player.ManagedPlayer;
import org.jfugue.player.Player;

/** Concrete {@link AudioEngine} implementation wrapping JFugue's {@link ManagedPlayer}. */
public final class JFuguePlayer implements AudioEngine {
  private final Player jfuguePlayer;
  private final ManagedPlayer managedPlayer;

  /** Constructs a new {@code JFuguePlayer} with standard managed player settings. */
  public JFuguePlayer() {
    this(new Player());
  }

  /**
   * Constructs a new {@code JFuguePlayer} wrapping the provided JFugue {@link Player}.
   *
   * @param player the JFugue player instance
   */
  public JFuguePlayer(final Player player) {
    this.jfuguePlayer = Objects.requireNonNull(player, "Player cannot be null");
    this.managedPlayer = player.getManagedPlayer();
  }

  @Override
  public void play(final String pattern) {
    try {
      if (pattern == null || pattern.trim().isEmpty()) {
        return;
      }
      final Pattern jfuguePattern = new Pattern(pattern);
      new Thread(() -> jfuguePlayer.play(jfuguePattern)).start();
    } catch (final NullPointerException ex) {
      throw new TtmException(
          ErrorCode.AUDIO_ENGINE_FAILURE, "Environment does not have an Audio Device", ex);
    }
  }

  @Override
  public void pause() {
    try {
      if (managedPlayer.isPlaying()) {
        managedPlayer.pause();
      }
    } catch (final NullPointerException ex) {
      throw new TtmException(
          ErrorCode.AUDIO_ENGINE_FAILURE, "Environment does not have an Audio Device", ex);
    }
  }

  @Override
  public void resume() {
    try {
      if (managedPlayer.isPaused()) {
        managedPlayer.resume();
      }
    } catch (final NullPointerException ex) {
      throw new TtmException(
          ErrorCode.AUDIO_ENGINE_FAILURE, "Environment does not have an Audio Device", ex);
    }
  }

  @Override
  public void stop() {
    try {
      managedPlayer.finish();
      managedPlayer.reset();
    } catch (final NullPointerException ex) {
      throw new TtmException(
          ErrorCode.AUDIO_ENGINE_FAILURE, "Environment does not have an Audio Device", ex);
    }
  }

  @Override
  public void seek(final int index) {
    try {
      managedPlayer.seek((long) index);
    } catch (final NullPointerException ex) {
      throw new TtmException(
          ErrorCode.AUDIO_ENGINE_FAILURE, "Environment does not have an Audio Device", ex);
    }
  }

  /**
   * Exposes the underlying {@link ManagedPlayer} for fine-grained control and inspection.
   *
   * @return the managed player instance
   */
  public ManagedPlayer getManagedPlayer() {
    return managedPlayer;
  }
}
