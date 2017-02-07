package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * A {@code RuntimeException} which is thrown when {@link Validator#validate(Object)} invalidates
 * a specified object.
 */
public class ValidationException extends RuntimeException {

  /**
   * Constructs a new {@code ValidationException} instance with no reason.
   */
  public ValidationException() {
    super((String) null);
  }

  /**
   * Constructs a new {@code ValidationException} instance with the specified {@code reason}.
   *
   * @param reason The reason why the object was not valid
   */
  public ValidationException(@Nullable String reason) {
    super(reason);
  }

}
