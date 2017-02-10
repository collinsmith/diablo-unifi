package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.libgdx.Console;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.Validator;

public class OptionalParameter<T> extends Parameter<T> {

  @NonNull
  public static <T> OptionalParameter<T> of(@NonNull Class<T> type) {
    return new OptionalParameter<>(type);
  }

  OptionalParameter(@NonNull Class<T> type) {
    super(type);
  }

  @NonNull
  public OptionalParameter<T> serializer(@NonNull StringSerializer<T> serializer) {
    super.serializer(serializer);
    return this;
  }

  @NonNull
  public OptionalParameter<T> validator(@NonNull Validator validator) {
    super.validator(validator);
    return this;
  }

  @NonNull
  public OptionalParameter<T> processor(@NonNull Console.SuggestionProvider suggestionProvider) {
    super.processor(suggestionProvider);
    return this;
  }

  @Override
  public String toString() {
    return "[" + TYPE.getSimpleName() + "]";
  }
}
