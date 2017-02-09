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
  @SuppressWarnings({ "ConstantConditions", "NullableProblems" })
  // The inspection is complaining of this method overriding obj with @NonNull, however this is
  // a special case, where null values are expected to fail
  public void validate(@NonNull Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    }
  }

}
