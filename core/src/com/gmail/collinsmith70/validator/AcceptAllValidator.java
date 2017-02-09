package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * A {@link SimpleValidator} which accepts all values. This class is intended to be used to
 * represent {@linkplain Validator validators} for structures where the value does not require
 * validation.
 */
@SuppressWarnings("unused")
public final class AcceptAllValidator extends SimpleValidator {

  /**
   * Evaluates the passed object as valid.
   *
   * @param obj {@inheritDoc}
   */
  @Override
  public void validate(@Nullable Object obj) {}

}
