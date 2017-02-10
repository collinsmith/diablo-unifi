package com.gmail.collinsmith70.command;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.libgdx.Console;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.Validator;

@SuppressWarnings("unused")
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
  public static <T> Parameter<T> of(@NonNull Class<T> type) {
    return new Parameter<>(type, null, Validator.ACCEPT_ALL);
  }

  @NonNull
  /*package*/ final Class<T> TYPE;

  @Nullable
  /*package*/ final StringSerializer<T> SERIALIZER;

  @NonNull
  /*package*/ final Validator VALIDATOR;

  @Nullable
  /*package*/ Console.Processor processor;

  protected Parameter(@NonNull Class<T> type, @Nullable StringSerializer<T> serializer,
                      @NonNull Validator validator) {
    this.TYPE = Preconditions.checkNotNull(type, "type cannot be null");
    this.SERIALIZER = serializer;
    this.VALIDATOR = Preconditions.checkNotNull(validator, "validator cannot be null");
  }

  @NonNull
  public Parameter<T> setProcessor(@NonNull Console.Processor processor) {
    this.processor = Preconditions.checkNotNull(processor, "processor cannot be null");
    return this;
  }

  @Nullable
  public Console.Processor getProcessor() {
    return processor;
  }

  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  @NonNull
  @SuppressWarnings("SameReturnValue")
  public String resolve(@NonNull String arg) {
    return "";
  }

  @NonNull
  @Override
  public String serialize(@NonNull T obj) {
    if (SERIALIZER == null) {
      throw new UnsupportedOperationException("parameter is not serializable");
    }

    return SERIALIZER.serialize(obj);
  }

  @NonNull
  @Override
  public T deserialize(@NonNull String string) {
    if (SERIALIZER == null) {
      throw new UnsupportedOperationException("parameter is not deserializable");
    }

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

  public interface Autocompleteable {

  }
}
