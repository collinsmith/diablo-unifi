package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Serializer which (de)serializes an {@link Object} into its {@link String} representation.
 */
public enum ObjectStringSerializer implements StringSerializer<Object> {
  /**
   * @see ObjectStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@Nullable Object obj) {
    return obj.toString();
  }

  @Override
  @Nullable
  public Object deserialize(@NonNull String obj) {
    return obj;
  }

}