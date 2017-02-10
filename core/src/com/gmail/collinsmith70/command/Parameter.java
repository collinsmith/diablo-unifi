package com.gmail.collinsmith70.command;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.libgdx.Console;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.Validator;

public class Parameter<T> implements StringSerializer<T>, Validator, Console.SuggestionProvider {

  @NonNull
  public static <T> Parameter<T> of(@NonNull Class<T> type) {
    return new Parameter<>(type);
  }

  @NonNull
  /*package*/ final Class<T> TYPE;

  @Nullable
  private StringSerializer<T> serializer;

  @Nullable
  private Validator validator;

  @Nullable
  private Console.SuggestionProvider suggestionProvider;

  Parameter(@NonNull Class<T> type) {
    this.TYPE = Preconditions.checkNotNull(type, "type cannot be null");
  }

  @NonNull
  public Parameter<T> serializer(@NonNull StringSerializer<T> serializer) {
    this.serializer = Preconditions.checkNotNull(serializer, "serializer cannot be null");
    return this;
  }

  @NonNull
  public Parameter<T> validator(@NonNull Validator validator) {
    this.validator = Preconditions.checkNotNull(validator, "validator cannot be null");
    return this;
  }

  @NonNull
  public Parameter<T> processor(@NonNull Console.SuggestionProvider suggestionProvider) {
    this.suggestionProvider
        = Preconditions.checkNotNull(suggestionProvider, "suggestionProvider cannot be null");
    return this;
  }

  public boolean canSerialize() {
    return serializer != null;
  }

  public boolean canValidate() {
    return validator != null;
  }

  public boolean canSuggest() {
    return suggestionProvider != null;
  }

  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  @Override
  public String toString() {
    return "<" + TYPE.getSimpleName() + ">";
  }

  @NonNull
  @Override
  public String serialize(@NonNull T obj) {
    if (serializer == null) {
      throw new UnsupportedOperationException(this + " is not serializable");
    }

    return serializer.serialize(obj);
  }

  @NonNull
  @Override
  public T deserialize(@NonNull String string) {
    if (serializer == null) {
      throw new UnsupportedOperationException(this + " is not deserializable");
    }

    return serializer.deserialize(string);
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (validator == null) {
      throw new UnsupportedOperationException(this + " is not validatable");
    }

    validator.validate(obj);
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    if (validator == null) {
      throw new UnsupportedOperationException(this + " is not validatable");
    }

    return validator.isValid(obj);
  }

  @Override
  public boolean suggest(@NonNull Console console, @NonNull CharSequence buffer,
                         @NonNull String[] args) {
    if (suggestionProvider == null) {
      throw new UnsupportedOperationException(this + " cannot process console input");
    }

    return suggestionProvider.suggest(console, buffer, args);
  }
}
