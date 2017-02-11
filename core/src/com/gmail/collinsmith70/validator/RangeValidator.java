package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * A {@link Validator} which is designed to validate that an object lies between some arbitrary
 * {@linkplain #getMin() minimum} and {@linkplain #getMax() maximum}. If either the minimum or
 * maximum bounds are {@code null}, then this implies that there is no bound for that limit.
 *
 * @param <T> The type of object which this {@linkplain RangeValidator} validates
 */
@SuppressWarnings("unused")
public interface RangeValidator<T extends Comparable<? super T>> extends Validator {

  /**
   * Returns the lowest accepted value.
   *
   * @return The minimum accepted value
   */
  @Nullable
  T getMin();

  /**
   * Returns the highest accepted value.
   *
   * @return The maximum accepted value
   */
  @Nullable
  T getMax();

}
