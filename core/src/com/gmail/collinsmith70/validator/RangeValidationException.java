package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * A {@link ValidationException} which is thrown when a {@link RangeValidator} invalidates that an
 * object is outside of the specified range.
 */
@SuppressWarnings("unused")
public class RangeValidationException extends ValidationException {

  /**
   * The {@linkplain RangeValidator#getMin() minimum} acceptable value.
   */
  @Nullable
  private final Object MIN;

  /**
   * The {@linkplain RangeValidator#getMax() maximum} acceptable value.
   */
  @Nullable
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
  public RangeValidationException(@Nullable Object min, @Nullable Object max) {
    super(formatMessage(min, max));
    this.MIN = min;
    this.MAX = max;
  }

  private static String formatMessage(@Nullable Object min, @Nullable Object max) {
    if (min == null && max != null) {
      return String.format("Value must be less than or equal to %s", max);
    } else if (min != null && max == null) {
      return String.format("Value must be greater than or equal to %s", min);
    } else {
      return String.format("Value must be between %s and %s (inclusive)", min, max);
    }
  }

  /**
   * Returns the smallest acceptable value of the {@link RangeValidator}.
   *
   * @return The minimum acceptable value
   */
  @Nullable
  public Object getMin() {
    return MIN;
  }

  /**
   * Returns the highest acceptable value of the {@link RangeValidator}.
   *
   * @return The maximum acceptable value
   */
  @Nullable
  public Object getMax() {
    return MAX;
  }

}
