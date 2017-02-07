package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Double} object into its {@link String} representation.
 */
public enum DoubleStringSerializer implements StringSerializer<Double> {
  /**
   * @see DoubleStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Double obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Double deserialize(@NonNull String obj) {
    return Double.parseDouble(obj);
  }

}
