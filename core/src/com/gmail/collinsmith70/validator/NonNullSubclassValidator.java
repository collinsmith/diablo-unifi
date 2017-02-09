package com.gmail.collinsmith70.validator;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;

/**
 * A {@link NonNullValidator} which validates that the passed value is both non-null and a subclass
 * of {@link T}
 *
 * @param <T> The type of object which this {@linkplain NonNullSubclassValidator} is specifically
 *            designed to validate if objects are a subclass of
 */
@SuppressWarnings("unused")
public class NonNullSubclassValidator<T> extends NonNullValidator {

  /**
   * Reference the {@link Class} this {@linkplain NonNullSubclassValidator} is basing its
   * {@linkplain #validate(Object) validations} on.
   */
  @NonNull
  private final Class<T> TYPE;

  /**
   * Constructs a new {@code NonNullSubclassValidator} instance.
   *
   * @param type The {@linkplain T type} to check if passed objects are subclasses of
   */
  public NonNullSubclassValidator(@NonNull Class<T> type) {
    this.TYPE = Preconditions.checkNotNull(type, "type cannot be null");
  }

  /**
   * Returns the {@linkplain T type} used when checking subclasses.
   *
   * @return The type to check if passed objects are subclasses of
   */
  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  /**
   * Validates that the passed object {@code obj} is a subclass of the {@linkplain #getType() type}
   * this instance represents. {@inheritDoc}
   *
   * @param obj {@inheritDoc}
   */
  @Override
  public void validate(@NonNull Object obj) {
    super.validate(obj);
    if (!TYPE.isAssignableFrom(obj.getClass())) {
      throw new ValidationException("passed reference is not a subclass of " + TYPE.getName());
    }
  }

}
