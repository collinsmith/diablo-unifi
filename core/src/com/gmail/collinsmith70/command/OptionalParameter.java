package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.Validator;

@SuppressWarnings("unused")
public class OptionalParameter<T> extends Parameter<T> {

  @NonNull
  public static <T> OptionalParameter<T> of(@NonNull Class<T> type,
                                            @NonNull StringSerializer<T> serializer,
                                            @NonNull Validator validator) {
    return new OptionalParameter<>(type, serializer, validator);
  }

  @NonNull
  public static <T> OptionalParameter<T> of(@NonNull Class<T> type,
                                            @NonNull StringSerializer<T> serializer) {
    return new OptionalParameter<>(type, serializer, Validator.ACCEPT_ALL);
  }

  @NonNull
  public static <T> OptionalParameter<T> of(@NonNull Class<T> type) {
    return new OptionalParameter<>(type, null, Validator.ACCEPT_ALL);
  }

  protected OptionalParameter(@NonNull Class<T> type, @Nullable StringSerializer<T> serializer,
                              @NonNull Validator validator) {
    super(type, serializer, validator);
  }

  @Override
  public String toString() {
    return "[" + TYPE.getSimpleName() + "]";
  }
}
