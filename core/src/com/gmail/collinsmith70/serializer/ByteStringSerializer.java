package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Byte} object into its {@link String} representation.
 */
public enum ByteStringSerializer implements StringSerializer<Byte> {
  /**
   * @see ByteStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Byte obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Byte deserialize(@NonNull String obj) {
    return Byte.parseByte(obj);
  }

}
