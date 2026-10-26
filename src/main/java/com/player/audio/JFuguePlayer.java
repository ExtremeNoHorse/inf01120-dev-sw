package com.player.audio;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.util.Objects;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import org.jfugue.midi.MidiParserListener;
import org.jfugue.player.ManagedPlayer;
import org.jfugue.player.Player;
import org.jfugue.player.SequencerManager;
import org.staccato.StaccatoParser;

/** Concrete {@link AudioEngine} implementation wrapping JFugue's {@link ManagedPlayer}. */
public final class JFuguePlayer implements AudioEngine {
  private final ManagedPlayer managedPlayer;

  /** Constructs a new {@code JFuguePlayer} with standard managed player settings. */
  public JFuguePlayer() {
    this(createPlayer());
  }

  /**
   * Constructs a new {@code JFuguePlayer} wrapping the provided JFugue {@link Player}.
   *
   * @param player the JFugue player instance
   */
  public JFuguePlayer(final Player player) {
    this.managedPlayer = Objects.requireNonNull(player, "Player cannot be null").getManagedPlayer();
  }

  @Override
  public void play(final String pattern) {
    if (pattern == null || pattern.trim().isEmpty()) {
      return;
    }
    // Starts on the caller's thread and returns at once: overlapping restarts cannot race.
    try {
      managedPlayer.start(toSequence(pattern));
    } catch (final InvalidMidiDataException | MidiUnavailableException ex) {
      throw new TtmException(ErrorCode.AUDIO_UNAVAILABLE, "Unable to start playback", ex);
    }
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
    managedPlayer.seek(index);
  }

  @Override
  public long getTickPosition() {
    return managedPlayer.getTickPosition();
  }

  @Override
  public long getTickLength() {
    return managedPlayer.getTickLength();
  }

  @Override
  public boolean isActive() {
    return managedPlayer.isStarted() && !managedPlayer.isFinished();
  }

  /**
   * Builds the MIDI sequence with a fresh parser. JFugue's shared parser keeps the time of voices
   * other than V0 between calls, so a second multi-voice pattern would start late and off tempo.
   */
  private static Sequence toSequence(final String pattern) {
    final StaccatoParser parser = new StaccatoParser();
    final MidiParserListener listener = new MidiParserListener();
    parser.addParserListener(listener);
    parser.parse(pattern);
    return listener.getSequence();
  }

  private static Player createPlayer() {
    try {
      SequencerManager.getInstance();
      return new Player();
    } catch (final MidiUnavailableException ex) {
      throw new TtmException(ErrorCode.AUDIO_UNAVAILABLE, "No audio device is available", ex);
    }
  }
}
