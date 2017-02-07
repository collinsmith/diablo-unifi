package com.gmail.collinsmith70.validator;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Implementation of a {@link RangeValidator} which validates that objects lie in some
 * {@link Number} range (inclusively).
 *
 * @param <T> The type of {@link Number} subclass to validate
 */
public class NumberRangeValidator<T extends Number & Comparable<? super T>>
        implements RangeValidator<T> {

  /**
   * Minimum value
   */
  private final T MIN;

  /**
   * Maximum value
   */
  private final T MAX;

  /**
   * Constructs a new {@code NumberRangeValidator} instance with the specified minimum and maximum
   * values.
   *
   * @param min The minimum value of the {@code NumberRangeValidator}
   * @param max The maximum value of the {@code NumberRangeValidator}
   */
  public NumberRangeValidator(@NonNull T min, @NonNull T max) {
    this.MIN = Preconditions.checkNotNull(min);
    this.MAX = Preconditions.checkNotNull(max);
  }

  @Override
  public T getMin() {
    return MIN;
  }

  @Override
  public T getMax() {
    return MAX;
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (ValidationException e) {
      return false;
    }
  }

  /**
   * Validates that the passed object {@code obj} is a {@link Number} which lies within the
   * {@linkplain RangeValidator#getMin() minimum} and {@linkplain RangeValidator#getMax() maximum}
   * values of this {@code NumberRangeValidator}.
   *
   * @param obj {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("passed reference cannot be null");
    }

    if (!MIN.getClass().isAssignableFrom(obj.getClass())) {
      throw new ValidationException(
              "passed reference is not a subclass of " + MIN.getClass().getName());
    }

    T castedObj = (T) obj;
    if (MIN.compareTo(castedObj) > 0 && MAX.compareTo(castedObj) < 0) {
      throw new RangeValidationException(MIN, MAX);
    }
  }

}
