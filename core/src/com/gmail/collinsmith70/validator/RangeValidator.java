package com.gmail.collinsmith70.validator;

import android.support.annotation.NonNull;

/**
 * A {@link Validator} which is designed to validate that an object lies between some arbitrary
 * {@linkplain #getMin() minimum} and {@linkplain #getMax() maximum}
 *
 * @param <T> The type of object which this {@linkplain RangeValidator} is specifically designed to
 *            validate
 */
@SuppressWarnings("unused")
public interface RangeValidator<T extends Comparable<? super T>> extends Validator {

  /**
   * Returns the lowest accepted value.
   *
   * @return The minimum accepted value
   */
  @NonNull
  T getMin();

  /**
   * Returns the highest accepted value.
   *
   * @return The maximum accepted value
   */
  @NonNull
  T getMax();

}
