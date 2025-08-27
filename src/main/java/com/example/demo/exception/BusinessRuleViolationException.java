package com.example.demo.exception;

/**
 * Thrown when a domain/business invariant is violated (e.g., attempting to create a user with a
 * duplicate email when uniqueness is required). Using a dedicated type allows targeted handling
 * separate from generic validation or infrastructure errors.
 */
public class BusinessRuleViolationException extends RuntimeException {

  /** Categorical identifier (e.g. "UNIQUE_EMAIL", "MAX_LIMIT_EXCEEDED"). */
  private final String ruleType;

  /** The value that caused the violation – useful for logging / client feedback. */
  private final Object violatedValue;

  public BusinessRuleViolationException(String message, String ruleType, Object violatedValue) {
    super(message); // human readable description of the broken rule
    this.ruleType = ruleType;
    this.violatedValue = violatedValue;
  }

  public BusinessRuleViolationException(String message) {
    super(message);
    this.ruleType = "UNKNOWN"; // fallback when no explicit category supplied
    this.violatedValue = null;
  }

  public String getRuleType() {
    return ruleType;
  }

  public Object getViolatedValue() {
    return violatedValue;
  }
}
