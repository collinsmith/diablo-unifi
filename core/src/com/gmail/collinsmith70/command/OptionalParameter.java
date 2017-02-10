package com.gmail.collinsmith70.command;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.libgdx.Console;
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

  @NonNull
  public OptionalParameter<T> setProcessor(@NonNull Console.Processor processor) {
    this.processor = Preconditions.checkNotNull(processor, "processor cannot be null");
    return this;
  }

  @Override
  public String toString() {
    return "[" + TYPE.getSimpleName() + "]";
  }
}
