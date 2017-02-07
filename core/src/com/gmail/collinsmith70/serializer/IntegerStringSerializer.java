package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Integer} object into its {@link String} representation.
 */
public enum IntegerStringSerializer implements StringSerializer<Integer> {
  /**
   * @see IntegerStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Integer obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Integer deserialize(@NonNull String obj) {
    return Integer.parseInt(obj);
  }

}
