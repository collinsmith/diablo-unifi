package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Short} object into its {@link String} representation.
 */
public enum ShortStringSerializer implements StringSerializer<Short> {
  /**
   * @see ShortStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Short obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Short deserialize(@NonNull String obj) {
    return Short.parseShort(obj);
  }

}
