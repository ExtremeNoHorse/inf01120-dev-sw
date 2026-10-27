package com.player;

import com.player.audio.AudioEngine;
import com.player.config.PlayerConfig;
import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import com.player.interpreter.Interpreter;
import com.player.interpreter.Timeline;
import com.player.repository.Music;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

/**
 * High-level coordinator that bridges music interpretation, playback engine control, and file
 * export functionality.
 */
public final class Player {
  private final Interpreter interpreter;
  private final AudioEngine audioEngine;
  private PlayerConfig playerConfig;

  /**
   * Constructs a new {@code Player} instance with injected dependencies.
   *
   * @param interpreter the music interpreter
   * @param audioEngine the underlying audio engine
   * @param playerConfig the playback configuration
   */
  public Player(
      final Interpreter interpreter,
      final AudioEngine audioEngine,
      final PlayerConfig playerConfig) {
    this.interpreter = Objects.requireNonNull(interpreter, "Interpreter cannot be null");
    this.audioEngine = Objects.requireNonNull(audioEngine, "AudioEngine cannot be null");
    this.playerConfig = Objects.requireNonNull(playerConfig, "PlayerConfig cannot be null");
  }

  /**
   * Interprets and begins playback of the provided {@link Music}.
   *
   * @param music the music object to play
   * @throws TtmException if music is null
   */
  public void play(final Music music) {
    if (music == null) {
      throw new TtmException(ErrorCode.INVALID_INPUT, "Music cannot be null for playback");
    }
    final String pattern = interpreter.interpret(music.getContent(), playerConfig);
    audioEngine.play(pattern);
  }

  /**
   * Computes when each character of the given text sounds, to follow the playback in the text.
   *
   * @param content the music text
   * @return the timeline of the text
   */
  public Timeline timeline(final String content) {
    return interpreter.timeline(content);
  }

  /** Pauses the currently playing track. */
  public void pause() {
    audioEngine.pause();
  }

  /** Resumes playback from paused state. */
  public void resume() {
    audioEngine.resume();
  }

  /** Stops playback completely and resets audio stream. */
  public void stop() {
    audioEngine.stop();
  }

  /**
   * Moves playback to the given tick position.
   *
   * @param tick the target tick, as reported by {@link #getTickLength()}
   */
  public void seek(final int tick) {
    audioEngine.seek(tick);
  }

  /**
   * Reports the current playback position.
   *
   * @return current tick position
   */
  public long getTickPosition() {
    return audioEngine.getTickPosition();
  }

  /**
   * Reports the length of the music being played.
   *
   * @return total length in ticks
   */
  public long getTickLength() {
    return audioEngine.getTickLength();
  }

  /**
   * Checks whether a playback session is active (playing or paused).
   *
   * @return {@code true} while a music is loaded and not finished
   */
  public boolean isActive() {
    return audioEngine.isActive();
  }

  /**
   * Reconfigures the player playback settings.
   *
   * @param config the new player configuration
   * @throws NullPointerException if config is null
   */
  public void configure(final PlayerConfig config) {
    this.playerConfig = Objects.requireNonNull(config, "PlayerConfig cannot be null");
  }

  /**
   * Exports the interpreted music as a standard MIDI file to the specified target path.
   *
   * @param music the music instance to export
   * @param targetPath the destination path chosen by the user
   * @return the written file path
   * @throws TtmException if writing the MIDI file fails
   */
  public Path export(final Music music, final Path targetPath) {
    if (music == null || targetPath == null) {
      throw new TtmException(
          ErrorCode.INVALID_INPUT, "Music and targetPath cannot be null for export");
    }

    final String patternStr = interpreter.interpret(music.getContent(), playerConfig);
    try {
      final Path parent = targetPath.getParent();
      if (parent != null && !Files.exists(parent)) {
        Files.createDirectories(parent);
      }

      final Pattern pattern = new Pattern(patternStr);
      try (OutputStream out = Files.newOutputStream(targetPath)) {
        MidiFileManager.savePatternToMidi(pattern, out);
      }
      music.setHasRecording(true);
      return targetPath;
    } catch (final IOException e) {
      throw new TtmException(
          ErrorCode.PERSISTENCE_FAILURE, "Falha ao exportar arquivo MIDI: " + targetPath, e);
    }
  }

  /**
   * Retrieves a copy of the current playback configuration.
   *
   * @return a new {@link PlayerConfig} instance with current settings
   */
  public PlayerConfig getPlayerConfig() {
    return new PlayerConfig(
        this.playerConfig.getInstrument(),
        this.playerConfig.getOctave(),
        this.playerConfig.getVolume(),
        this.playerConfig.getBpm(),
        this.playerConfig.getBaseOctave(),
        this.playerConfig.getBaseVolume(),
        this.playerConfig.getBaseInstrument());
  }
}
