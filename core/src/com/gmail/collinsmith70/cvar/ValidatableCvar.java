package com.gmail.collinsmith70.cvar;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.validator.Validatable;
import com.gmail.collinsmith70.validator.ValidationException;
import com.gmail.collinsmith70.validator.Validator;

/**
 * Implementation of a {@link SimpleCvar} which performs {@link #isValid validations} upon
 * {@linkplain #setValue assigned} values.
 *
 * @param <T> The {@linkplain Class type} of the {@linkplain #getValue variable} which this
 *            {@code SimpleCvar} represents
 */
public class ValidatableCvar<T> extends SimpleCvar<T> implements Validatable {

  /**
   * {@code Validator} used to evaluate the validity of the values being
   * {@linkplain #setValue assigned} to this {@code ValidatableCvar}.
   *
   * @see #setValue
   * @see #isValid
   */
  @NonNull
  private final Validator VALIDATOR;

  /**
   * Constructs a new {@code ValidatableCvar} instance with a {@link Validator} that accepts all
   * non-null values.
   *
   * @param alias        The {@linkplain #getAlias name} of the {@code Cvar}
   * @param description  A brief {@linkplain #getDescription description} explaining the purpose and
   *                     function of the {@code Cvar} and values it permits
   * @param type         The {@linkplain T type} of the {@linkplain #getValue variable} that the
   *                     {@code Cvar} represents
   * @param defaultValue The {@linkplain #getDefaultValue default value} which will be assigned to
   *                     the {@code Cvar} now and whenever it is {@linkplain #reset}.
   *
   * @see Validator#ACCEPT_NON_NULL
   */
  public ValidatableCvar(@Nullable String alias, @Nullable String description,
                         @NonNull Class<T> type, @Nullable T defaultValue) {
    this(alias, description, type, defaultValue, Validator.ACCEPT_NON_NULL);
  }

  /**
   * Constructs a new {@code ValidatableCvar} instance with a custom {@link Validator}.
   *
   * @param alias        The {@linkplain #getAlias name} of the {@code Cvar}
   * @param description  A brief {@linkplain #getDescription description} explaining the purpose and
   *                     function of the {@code Cvar} and values it permits
   * @param type         The {@linkplain T type} of the {@linkplain #getValue variable} that the
   *                     {@code Cvar} represents
   * @param defaultValue The {@linkplain #getDefaultValue default value} which will be assigned to
   *                     the {@code Cvar} now and whenever it is {@linkplain #reset}.
   * @param validator    The {@code Validator} to use when {@linkplain #isValid validating}
   *                     {@linkplain #setValue assignments}
   */
  public ValidatableCvar(@Nullable String alias, @Nullable String description,
                         @NonNull Class<T> type, @Nullable T defaultValue,
                         @NonNull Validator validator) {
    super(alias, description, type, defaultValue);
    Preconditions.checkArgument(validator != null, "validator cannot be null");

    this.VALIDATOR = validator;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note: This operation will {@linkplain #isValid validate} the specified {@code value} and may
   *       throw a {@link ValidationException} even if it is the
   *       {@link #getDefaultValue default value} (i.e., {@code isValid(getDefaultValue())}
   *       evaluates as {@code false}). If this is the case, and setting to the default value is
   *       required, then use {@link #reset} instead.
   *
   * @throws ValidationException if {@code value} is {@linkplain #isValid not valid} according to
   *     the {@linkplain Validator} used by this {@code ValidatableCvar}.
   *
   * @see #reset
   */
  @Override
  public void setValue(@Nullable final T value) {
    VALIDATOR.validate(value);
    super.setValue(value);
  }

  /**
   * {@inheritDoc}
   *
   * @param obj {@inheritDoc}
   *
   * @return {@code true} if the {@link Validator} used by this {@code ValidatableCvar} determines
   *          that {@code obj} is {@linkplain Validatable#isValid valid}, otherwise {@code false}
   */
  @Override
  public boolean isValid(@Nullable final Object obj) {
    return VALIDATOR.isValid(obj);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note: This operation will not {@linkplain #isValid validate} the
   *       {@link #getDefaultValue default value}.
   */
  @Override
  public void reset() {
    super.setValue(getDefaultValue());
  }

}
