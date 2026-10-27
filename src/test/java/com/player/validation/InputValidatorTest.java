package com.player.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import org.junit.jupiter.api.Test;

class InputValidatorTest {
  private static final int MIN_LENGTH = 5;

  @Test
  void givenDefaultValidator_whenValidNonEmptyInput_thenPasses() {
    // Given
    final InputValidator validator = new InputValidator();

    // When & Then
    assertDoesNotThrow(() -> validator.validate("A:3 B:3"));
  }

  @Test
  void givenDefaultValidator_whenNullOrBlank_thenThrowsTtmException() {
    // Given
    final InputValidator validator = new InputValidator();

    // When & Then
    final TtmException exNull = assertThrows(TtmException.class, () -> validator.validate(null));
    assertEquals(ErrorCode.INVALID_INPUT, exNull.getCode());

    final TtmException exBlank = assertThrows(TtmException.class, () -> validator.validate("   "));
    assertEquals(ErrorCode.INVALID_INPUT, exBlank.getCode());
  }

  @Test
  void givenCustomRule_whenRuleFails_thenThrowsTtmException() {
    // Given
    final InputValidator validator = new InputValidator();
    validator.addRule(input -> input.length() >= MIN_LENGTH);

    // When & Then
    assertDoesNotThrow(() -> validator.validate("12345"));

    final TtmException ex = assertThrows(TtmException.class, () -> validator.validate("1234"));
    assertEquals(ErrorCode.INVALID_INPUT, ex.getCode());
  }
}
