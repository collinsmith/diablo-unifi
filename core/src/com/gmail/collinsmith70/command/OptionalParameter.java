package com.gmail.collinsmith70.command;

import com.google.common.base.Preconditions;

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
    this.serializer = Preconditions.checkNotNull(serializer, "serializer cannot be null");
    return this;
  }

  @NonNull
  public OptionalParameter<T> validator(@NonNull Validator validator) {
    this.validator = Preconditions.checkNotNull(validator, "validator cannot be null");
    return this;
  }

  @NonNull
  public OptionalParameter<T> processor(@NonNull Console.Processor processor) {
    this.processor = Preconditions.checkNotNull(processor, "processor cannot be null");
    return this;
  }

  @Override
  public String toString() {
    return "[" + TYPE.getSimpleName() + "]";
  }
}
