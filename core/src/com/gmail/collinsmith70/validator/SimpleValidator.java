package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * Partial implementation of a {@link Validator} which determines the return value of
 * {@link #isValid} by checking whether or not {@link #validate} threw an exception.
 */
public abstract class SimpleValidator implements Validator {

  /**
   * {@inheritDoc}
   *
   * @param obj {@inheritDoc}
   *
   * @return {@code true} if {@link #validate} will not throw an exception, otherwise {@code false}
   */
  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

}
