package com.player.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TtmExceptionTest {

  @Test
  void givenValidCodeAndMessage_whenConstructed_thenFieldsAreSet() {
    // Given
    final ErrorCode code = ErrorCode.INVALID_INPUT;
    final String message = "Test error";

    // When
    final TtmException exception = new TtmException(code, message);

    // Then
    assertEquals(code, exception.getCode());
    assertEquals(message, exception.getMessage());
  }

  @Test
  void givenValidCodeMessageAndCause_whenConstructed_thenFieldsAndCauseAreSet() {
    // Given
    final Throwable cause = new RuntimeException("Underlying cause");
    final ErrorCode code = ErrorCode.PERSISTENCE_FAILURE;
    final String message = "Persistence failed";

    // When
    final TtmException exception = new TtmException(code, message, cause);

    // Then
    assertEquals(code, exception.getCode());
    assertEquals(message, exception.getMessage());
    assertEquals(cause, exception.getCause());
  }

  @Test
  void givenNullCode_whenConstructed_thenThrowsNullPointerException() {
    // Given & When & Then
    assertThrows(NullPointerException.class, () -> new TtmException(null, "Message"));
  }

  @Test
  void givenErrorCodes_whenIterated_thenAllValuesAreAccessible() {
    // Given & When & Then
    for (final ErrorCode code : ErrorCode.values()) {
      assertNotNull(code.name());
    }
  }
}