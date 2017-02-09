package com.gmail.collinsmith70.command;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.serializer.StringStringSerializer;
import com.gmail.collinsmith70.validator.Validator;

public class Parameter<T> implements StringSerializer<T>, Validator {

  @NonNull
  public static <T> Parameter<T> of(@NonNull Class<T> type,
                                    @NonNull StringSerializer<T> serializer,
                                    @NonNull Validator validator) {
    return new Parameter<>(type, serializer, validator);
  }

  @NonNull
  public static <T> Parameter<T> of(@NonNull Class<T> type,
                                    @NonNull StringSerializer<T> serializer) {
    return new Parameter<>(type, serializer, Validator.ACCEPT_ALL);
  }

  @NonNull
  public static Parameter<String> forStrings() {
    return new Parameter<>(String.class, StringStringSerializer.INSTANCE, Validator.ACCEPT_ALL);
  }

  @NonNull
  /*package*/ Class<T> TYPE;

  @NonNull
  /*package*/ StringSerializer<T> SERIALIZER;

  @NonNull
  /*package*/ Validator VALIDATOR;

  protected Parameter(@NonNull Class<T> type, @NonNull StringSerializer<T> serializer,
                      @NonNull Validator validator) {
    this.TYPE = Preconditions.checkNotNull(type, "type cannot be null");
    this.SERIALIZER = Preconditions.checkNotNull(serializer, "serializer cannot be null");
    this.VALIDATOR = Preconditions.checkNotNull(validator, "validator cannot be null");
  }

  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  @NonNull
  public String resolve(@NonNull String arg) {
    return "";
  }

  @NonNull
  @Override
  public String serialize(@NonNull T obj) {
    return SERIALIZER.serialize(obj);
  }

  @NonNull
  @Override
  public T deserialize(@NonNull String string) {
    return SERIALIZER.deserialize(string);
  }

  @Override
  public void validate(@Nullable Object obj) {
    VALIDATOR.validate(obj);
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    return VALIDATOR.isValid(obj);
  }

  @Override
  public String toString() {
    return "<" + TYPE.getSimpleName() + ">";
  }
}
