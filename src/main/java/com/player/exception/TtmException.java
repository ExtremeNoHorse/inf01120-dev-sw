package com.player.exception;

import java.util.Objects;

/** Base domain exception for TTM, encapsulating a specific {@link ErrorCode}. */
public final class TtmException extends RuntimeException {
  private final ErrorCode code;

  /**
   * Constructs a new {@code TtmException} with the specified error code and detail message.
   *
   * @param code the domain error code
   * @param message the detail message
   */
  public TtmException(final ErrorCode code, final String message) {
    super(message);
    this.code = Objects.requireNonNull(code, "ErrorCode cannot be null");
  }

  /**
   * Constructs a new {@code TtmException} with the specified error code, detail message, and cause.
   *
   * @param code the domain error code
   * @param message the detail message
   * @param cause the underlying cause
   */
  public TtmException(final ErrorCode code, final String message, final Throwable cause) {
    super(message, cause);
    this.code = Objects.requireNonNull(code, "ErrorCode cannot be null");
  }

  /**
   * Retrieves the domain error code associated with this exception.
   *
   * @return the {@link ErrorCode}
   */
  public ErrorCode getCode() {
    return code;
  }
}
