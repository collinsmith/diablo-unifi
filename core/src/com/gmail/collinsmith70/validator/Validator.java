package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * An extension of {@link Validatable} with the addition of {@link #validate}, which behaves
 * similarly to {@link #isValid}, except it will throw {@link ValidationException} if the passed
 * object is not valid.
 */
@SuppressWarnings("unused")
public interface Validator extends Validatable {

  /**
   * Constant reference to a {@code Validator} which accepts all values
   */
  Validator ACCEPT_ALL = new AcceptAllValidator();

  /**
   * Constant reference to a {@code Validator} which rejects all values
   */
  Validator REJECT_ALL = new RejectAllValidator();

  /**
   * Constant reference to a {@code Validator} which accepts all non-null values
   */
  Validator ACCEPT_NON_NULL = new NonNullValidator();

  /**
   * Constant reference to a {@code Validator} which accepts all strings which are both non-null and
   * non-empty.
   */
  Validator ACCEPT_NON_NULL_NON_EMPTY_STRING = new NonNullNonEmptyStringValidator();

  /**
   * Validates the specified object {@code obj}, and throws a {@link ValidationException} if it is
   * not valid.
   *
   * @param obj The object to validate
   *
   * @throws ValidationException if {@code obj} is not valid
   */
  void validate(@Nullable Object obj);

}
