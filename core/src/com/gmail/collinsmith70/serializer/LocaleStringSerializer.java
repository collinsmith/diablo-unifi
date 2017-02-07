package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;

import java.util.Locale;

public enum LocaleStringSerializer implements StringSerializer<Locale> {
  INSTANCE;

  @Override
  @NonNull
  public String serialize(@NonNull Locale obj) {
    return obj.toLanguageTag();
  }

  @Override
  @NonNull
  public Locale deserialize(@NonNull String obj) {
    return Locale.forLanguageTag(obj);
  }

}
