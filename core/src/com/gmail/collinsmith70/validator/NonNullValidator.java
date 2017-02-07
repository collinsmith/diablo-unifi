package com.gmail.collinsmith70.validator;

import android.support.annotation.NonNull;

/**
 * A {@link SimpleValidator} which evaluates whether or not the passed reference is non-null.
 */
public class NonNullValidator extends SimpleValidator {

  /**
   * Validates that the passed object is not null.
   *
   * @param obj {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    }
  }

}
