package com.gmail.collinsmith70.validator;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * A {@link ValidationException} which is thrown when a {@link RangeValidator} invalidates that an
 * object is outside of the specified range.
 */
public class RangeValidationException extends ValidationException {

  /**
   * The {@linkplain RangeValidator#getMin() minimum} acceptable value.
   */
  private final Object MIN;

  /**
   * The {@linkplain RangeValidator#getMax() maximum} acceptable value.
   */
  private final Object MAX;

  /**
   * Constructs a new {@code RangeValidationException} instance with a {@code null} minimum and
   * maximum. The reason should reflect how the value was outside of the acceptable range.
   *
   * @param reason The reason why the object was not valid
   */
  public RangeValidationException(@Nullable String reason) {
    super(reason);
    this.MIN = null;
    this.MAX = null;
  }

  /**
   * Constructs a new {@code RangeValidationException} instance with an automatically generated
   * reason.
   *
   * @param min The minimum value of the {@link RangeValidator}
   * @param max The maximum value of the {@link RangeValidator}
   */
  public RangeValidationException(@NonNull Object min, @NonNull Object max) {
    super(String.format("passed reference must lie between %s and %s (inclusive)", min, max));
    this.MIN = Preconditions.checkNotNull(min);
    this.MAX = Preconditions.checkNotNull(max);
  }

  /**
   * Returns the smallest acceptable value of the {@link RangeValidator}.
   *
   * @return The minimum acceptable value
   */
  public Object getMin() {
    return MIN;
  }

  /**
   * Returns the highest acceptable value of the {@link RangeValidator}.
   *
   * @return The maximum acceptable value
   */
  public Object getMax() {
    return MAX;
  }

}
