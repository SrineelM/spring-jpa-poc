package com.example.demo.exception;

/**
 * Checked variant for recoverable business flow errors where calling code is expected to make a
 * decision (retry, alternative path, user feedback). Carries an error code plus optional context
 * object for structured diagnostics.
 */
public class CustomCheckedException extends Exception {

  /** Machine readable code (e.g. used for i18n mapping or client branching). */
  private final String errorCode;

  /** Optional contextual payload (could be an ID, DTO snapshot, etc.). */
  private final Object context;

  public CustomCheckedException(String message) {
    super(message);
    this.errorCode = "BUSINESS_ERROR"; // default category
    this.context = null;
  }

  public CustomCheckedException(String message, String errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.context = null;
  }

  public CustomCheckedException(String message, String errorCode, Object context) {
    super(message);
    this.errorCode = errorCode;
    this.context = context; // attachments for improved troubleshooting
  }

  public CustomCheckedException(String message, Throwable cause) {
    super(message, cause); // preserve original stack for root cause analysis
    this.errorCode = "BUSINESS_ERROR";
    this.context = null;
  }

  public CustomCheckedException(String message, String errorCode, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.context =
        null; // context omitted when cause given – could add overloaded variant if needed
  }

  public String getErrorCode() {
    return errorCode;
  }

  public Object getContext() {
    return context;
  }
}
