package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Long} object into its {@link String} representation.
 */
public enum LongStringSerializer implements StringSerializer<Long> {
  /**
   * @see LongStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Long obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Long deserialize(@NonNull String obj) {
    return Long.parseLong(obj);
  }

}
