package com.player.audio;

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
    if (pattern == null || pattern.trim().isEmpty()) {
      return;
    }
    final Pattern jfuguePattern = new Pattern(pattern);
    new Thread(() -> jfuguePlayer.play(jfuguePattern)).start();
  }

  @Override
  public void pause() {
    if (managedPlayer.isPlaying()) {
      managedPlayer.pause();
    }
  }

  @Override
  public void resume() {
    if (managedPlayer.isPaused()) {
      managedPlayer.resume();
    }
  }

  @Override
  public void stop() {
    managedPlayer.finish();
    managedPlayer.reset();
  }

  @Override
  public void seek(final int index) {
    managedPlayer.seek((long) index);
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
