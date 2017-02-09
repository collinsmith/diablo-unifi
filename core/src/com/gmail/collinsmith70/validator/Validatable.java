package com.gmail.collinsmith70.validator;

import android.support.annotation.Nullable;

/**
 * Functional interface declaring that a class' instances should be validatable, using
 * {@link #isValid} to determine whether or not certain values are correct in "setter" methods.
 * <p>
 * <p>
 * For example:
 * <pre>
 * {@code
 * class Name implements Validatable<String> {
 *   private String firstName;
 *
 *   boolean isValid(Object obj) {
 *     if (obj == null) {
 *       return false;
 *     } else if (!(obj instanceof String)) {
 *       return false;
 *     }
 *
 *     return !((String)obj).isEmpty();
 *   }
 *
 *   void setFirstName(String firstName) {
 *     if (!isValid(firstName)) {
 *       throw new IllegalArgumentException("First names must be non-null non-empty strings");
 *     }
 *
 *     this.firstName = firstName;
 *   }
 * }
 * }
 * </pre>
 */
@SuppressWarnings("unused")
public interface Validatable {

  /**
   * Returns whether or not the specified object {@code obj} is valid.
   *
   * @param obj The object to validate
   *
   * @return {@code true} if the object is valid, otherwise {@code false}
   */
  boolean isValid(@Nullable Object obj);

}
