package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Float} object into its {@link String} representation.
 */
public enum FloatStringSerializer implements StringSerializer<Float> {
  /**
   * @see FloatStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Float obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Float deserialize(@NonNull String obj) {
    return Float.parseFloat(obj);
  }

}
