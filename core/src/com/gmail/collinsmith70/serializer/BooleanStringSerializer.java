package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Boolean} object into its {@link String} representation.
 */
public enum BooleanStringSerializer implements StringSerializer<Boolean> {
  /**
   * @see BooleanStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Boolean obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Boolean deserialize(@NonNull String obj) {
    if (obj.equalsIgnoreCase("true")) {
      return Boolean.TRUE;
    } else if (obj.equalsIgnoreCase("yes")) {
      return Boolean.TRUE;
    } else {
      try {
        int i = Integer.parseInt(obj);
        return i > 0;
      } catch (NumberFormatException e) {
        return Boolean.FALSE;
      }
    }
  }

}
