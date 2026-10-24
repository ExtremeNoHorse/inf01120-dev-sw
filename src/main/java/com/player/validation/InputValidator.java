package com.player.validation;

import com.player.exception.ErrorCode;
import com.player.exception.TtmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Validates text inputs against a customizable list of predicates before interpretation. */
public final class InputValidator {
  private final List<Predicate<String>> rules;

  /** Constructs an {@code InputValidator} with standard non-null and non-empty rules. */
  public InputValidator() {
    this.rules = new ArrayList<>();
    this.rules.add(Objects::nonNull);
    this.rules.add(s -> !s.trim().isEmpty());
  }

  /**
   * Constructs an {@code InputValidator} with custom validation rules.
   *
   * @param rules list of string predicates that must all pass
   */
  public InputValidator(final List<Predicate<String>> rules) {
    this.rules = new ArrayList<>(Objects.requireNonNull(rules, "Rules list cannot be null"));
  }

  /**
   * Adds an additional validation rule predicate.
   *
   * @param rule the predicate to add
   */
  public void addRule(final Predicate<String> rule) {
    this.rules.add(Objects.requireNonNull(rule, "Rule predicate cannot be null"));
  }

  /**
   * Validates the provided input against all configured rules.
   *
   * @param input the string input to validate
   * @throws TtmException with {@link ErrorCode#INVALID_INPUT} if any rule is violated
   */
  public void validate(final String input) {
    for (final Predicate<String> rule : rules) {
      if (!rule.test(input)) {
        throw new TtmException(
            ErrorCode.INVALID_INPUT, "Entrada inválida detectada pelo validador");
      }
    }
  }
}
