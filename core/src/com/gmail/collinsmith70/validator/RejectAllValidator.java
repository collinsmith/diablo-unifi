package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * A {@link SimpleValidator} which rejects everything. This class is intended to be used to
 * represent {@linkplain Validator validators} for immutable structures.
 */
@SuppressWarnings("unused")
public final class RejectAllValidator extends SimpleValidator {

  /**
   * Rejects the passed object and throws a {@link ValidationException}.
   *
   * @param obj {@inheritDoc}
   */
  @Override
  public void validate(@Nullable Object obj) {
    throw new ValidationException();
  }

}
