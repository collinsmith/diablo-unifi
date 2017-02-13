package com.gmail.collinsmith70.cvar;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.Validator;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Cvar<T> implements SuggestionProvider {

  @NonNull
  public static <T> Builder<T> builder(@NonNull Class<T> type) {
    return new Builder<>(type);
  }

  @NonNull
  /*package*/ final String ALIAS;

  @NonNull
  /*private*/ final String DESCRIPTION;

  @NonNull
  /*private*/ final Class<T> TYPE;

  @Nullable
  /*private*/ final T DEFAULT_VALUE;

  @Nullable
  /*package*/ final Validator VALIDATOR;

  @Nullable
  /*package*/ final SuggestionProvider SUGGESTIONS;

  @Nullable
  /*package*/ final StringSerializer<T> SERIALIZER;

  /*package*/ final boolean REQUIRES_RESTART;

  @NonNull
  private final Set<StateListener<T>> STATE_LISTENERS;

  @Nullable
  private T value;

  private boolean isLoaded;

  public Cvar(Cvar.Builder<T> builder) {
    this.ALIAS = Strings.nullToEmpty(builder.alias);
    this.DESCRIPTION = Strings.nullToEmpty(builder.description);
    this.TYPE = builder.TYPE;
    this.DEFAULT_VALUE = builder.defaultValue;
    this.VALIDATOR = builder.validator;
    this.SUGGESTIONS = builder.suggestions;
    this.SERIALIZER = builder.serializer;
    this.REQUIRES_RESTART = builder.requiresRestart;
    this.value = DEFAULT_VALUE;
    this.isLoaded = false;

    this.STATE_LISTENERS = new CopyOnWriteArraySet<>();
  }

  @NonNull
  @Override
  public String toString() {
    final T value = this.value;
    if (value == null) {
      return "null";
    }

    return value.toString();
  }

  @NonNull
  public String getAlias() {
    return ALIAS;
  }

  @NonNull
  public String getDescription() {
    return DESCRIPTION;
  }

  @NonNull
  public Class<T> getType() {
    return TYPE;
  }

  @Nullable
  public T getDefault() {
    return DEFAULT_VALUE;
  }

  public boolean requiresRestart() {
    return REQUIRES_RESTART;
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  public boolean hasSerializer() {
    return SERIALIZER != null;
  }

  @Nullable
  public StringSerializer<T> getSerializer() {
    return SERIALIZER;
  }

  @Nullable
  public T get() {
    return value;
  }

  public void set(@Nullable T value) {
    final T prev = this.value;
    if (Objects.equal(prev, value)) {
      return;
    }

    if (VALIDATOR != null) {
      VALIDATOR.validate(value);
    }

    this.value = value;
    if (isLoaded) {
      for (StateListener<T> l : STATE_LISTENERS) {
        l.onChanged(this, prev, value);
      }
    } else {
      this.isLoaded = true;
      for (StateListener<T> l : STATE_LISTENERS) {
        l.onLoaded(this, value);
      }
    }
  }

  public void set(@NonNull String str) {
    try {
      if (SERIALIZER == null) {
        throw new SerializeException(ALIAS + " does not have a serializer attached");
      }

      T value = SERIALIZER.deserialize(str);
      set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, RuntimeException.class);
      throw new RuntimeException(t);
    }
  }

  @SuppressWarnings("unchecked")
  public void set(@NonNull String str, @NonNull StringSerializer deserializer) {
    try {
      T value = ((StringSerializer<T>) deserializer).deserialize(str);
      set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, RuntimeException.class);
      throw new RuntimeException(t);
    }
  }

  public void reset() {
    if (!Objects.equal(value, DEFAULT_VALUE)) {
      final T prev = this.value;
      this.value = DEFAULT_VALUE;
      for (StateListener<T> l : STATE_LISTENERS) {
        l.onChanged(this, prev, value);
      }
    }
  }

  public boolean addStateListener(@NonNull StateListener<T> l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = STATE_LISTENERS.add(l);
    l.onLoaded(this, value);
    return added;
  }

  public boolean containsStateListener(@Nullable StateListener<T> l) {
    return l != null && STATE_LISTENERS.contains(l);

  }

  public boolean removeStateListener(@Nullable StateListener<T> l) {
    return l != null && STATE_LISTENERS.remove(l);
  }

  @Override
  public Collection<String> suggest(@NonNull String str) {
    if (SUGGESTIONS == null) {
      return Collections.emptyList();
    }

    return SUGGESTIONS.suggest(str);
  }

  public interface StateListener<T> {

    void onChanged(@NonNull final Cvar<T> cvar, @Nullable final T from, @Nullable final T to);

    void onLoaded(@NonNull final Cvar<T> cvar, @Nullable final T to);

  }

  public static class Builder<T> {

    @NonNull
    private final Class<T> TYPE;

    @Nullable
    private String alias;

    @Nullable
    private String description;

    @Nullable
    private T defaultValue;

    @Nullable
    private Validator validator;

    @Nullable
    private SuggestionProvider suggestions;

    @Nullable
    private StringSerializer<T> serializer;

    private boolean requiresRestart = false;

    private Builder(@NonNull Class<T> type) {
      Preconditions.checkArgument(type != null, "Type cannot be null");
      this.TYPE = type;
    }

    @NonNull
    public Builder<T> alias(@NonNull String alias) {
      Preconditions.checkArgument(alias != null, "Aliases cannot be null");
      this.alias = alias;
      return this;
    }

    @NonNull
    public Builder<T> description(@NonNull String description) {
      Preconditions.checkArgument(description != null, "Descriptions cannot be null");
      this.description = description;
      return this;
    }

    @NonNull
    public Builder<T> defaultValue(@Nullable T defaultValue) {
      this.defaultValue = defaultValue;
      return this;
    }

    @NonNull
    public Builder<T> validator(@NonNull Validator validator) {
      Preconditions.checkArgument(validator != null, "Validators cannot be null");
      this.validator = validator;
      return this;
    }

    @NonNull
    public Builder<T> suggestions(@NonNull SuggestionProvider suggestions) {
      Preconditions.checkArgument(suggestions != null, "SuggestionProviders cannot be null");
      this.suggestions = suggestions;
      return this;
    }

    @NonNull
    public Builder<T> serializer(@NonNull StringSerializer<T> serializer) {
      Preconditions.checkArgument(serializer != null, "Serializers cannot be null");
      this.serializer = serializer;
      return this;
    }

    @NonNull
    public Builder<T> requiresRestart(boolean b) {
      this.requiresRestart = b;
      return this;
    }

    @NonNull
    public Cvar<T> build() {
      return new Cvar<>(this);
    }

  }

}
