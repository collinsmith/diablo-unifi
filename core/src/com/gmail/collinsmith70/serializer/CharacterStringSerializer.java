package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

/**
 * Serializer which (de)serializes a {@link Character} object into its {@link String} representation.
 */
public enum CharacterStringSerializer implements StringSerializer<Character> {
  /**
   * @see CharacterStringSerializer
   */
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Character obj) {
    return obj.toString();
  }

  @Override
  @NonNull
  public Character deserialize(@NonNull String obj) {
    if (obj.length() != 1) {
      throw new IllegalArgumentException("Character serializations should have a length of 1");
    }

    return obj.charAt(0);
  }

}
