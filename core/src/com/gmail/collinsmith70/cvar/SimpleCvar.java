package com.gmail.collinsmith70.cvar;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.StringSerializer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Basic implementation of a {@link Cvar}.
 *
 * @param <T> {@inheritDoc}
 */
@SuppressWarnings("ConstantConditions")
public class SimpleCvar<T> implements Cvar<T> {

  /**
   * <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">MappableKey</a> used to identify
   * this {@code Cvar}, or {@code null} if no alias has been set.
   *
   * @see <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">Wikipedia article on key-value pairs</a>
   * @see #getAlias
   */
  @NonNull
  private final String ALIAS;

  /**
   * Default value of this {@code Cvar}, which is assigned upon instantiation and when this
   * {@code Cvar} is {@linkplain #reset}.
   *
   * @see #reset
   * @see #getDefaultValue
   */
  @Nullable
  private final T DEFAULT_VALUE;

  /**
   * A brief description explaining the purpose and function of this {@code Cvar}, including the
   * values it permits.
   *
   * @see #getDescription
   */
  @NonNull
  private final String DESCRIPTION;

  /**
   * Class {@linkplain T type} of the {@linkplain #getValue variable} being represented.
   *
   * @see #getType
   */
  @NonNull
  private final Class<T> TYPE;

  /**
   * {@link Set} of {@link Cvar.StateListener} instances which will receive callbacks during state
   * transitions of this {@code Cvar}.
   *
   * @see #addStateListener
   * @see #containsStateListener
   * @see #removeStateListener
   */
  @NonNull
  private final Set<StateListener<T>> STATE_LISTENERS;

  /**
   * Value of the {@linkplain #getValue variable} represented by this {@code Cvar}.
   *
   * @see #getValue
   * @see #setValue
   */
  @Nullable
  private T value;

  /**
   * {@code true} if this {@code Cvar} has had its first {@linkplain #setValue assignment},
   * otherwise {@code false}.
   *
   * @see #isLoaded
   */
  private boolean isLoaded;

  /**
   * Constructs a new {@code SimpleCvar} instance.
   *
   * @param alias        The {@linkplain #getAlias name} of the {@code Cvar}
   * @param description  A brief {@linkplain #getDescription description} explaining the purpose and
   *                     function of the {@code Cvar} and values it permits
   * @param type         The {@linkplain T type} of the {@linkplain #getValue variable} that the
   *                     {@code Cvar} represents
   * @param defaultValue The {@linkplain #getDefaultValue default value} which will be assigned to
   *                     the {@code Cvar} now and whenever it is {@linkplain #reset}
   */
  public SimpleCvar(@NonNull String alias, @NonNull String description,
                    @NonNull Class<T> type, @Nullable T defaultValue) {
    Preconditions.checkArgument(alias != null, "alias is not allowed to be null");
    Preconditions.checkArgument(description != null, "description is not allowed to be null");
    Preconditions.checkArgument(type != null, "type is not allowed to be null");

    this.ALIAS = alias;
    this.DESCRIPTION = description;
    this.DEFAULT_VALUE = defaultValue;
    this.TYPE = type;
    this.STATE_LISTENERS = new CopyOnWriteArraySet<>();

    this.value = DEFAULT_VALUE;
    this.isLoaded = false;
  }

  @NonNull
  @Override
  public String getAlias() {
    return ALIAS;
  }

  @Nullable
  @Override
  public T getDefaultValue() {
    return DEFAULT_VALUE;
  }

  @NonNull
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @NonNull
  @Override
  public Class<T> getType() {
    return TYPE;
  }

  @Override
  @Nullable
  public T getValue() {
    return value;
  }

  @Override
  public boolean isEmpty() {
    return value == null;
  }

  @Override
  public boolean isLoaded() {
    return isLoaded;
  }

  @Override
  public void reset() {
    setValue(DEFAULT_VALUE);
  }

  @Override
  public void setValue(@Nullable T value) {
    final T oldValue = this.value;
    if (Objects.equal(oldValue, value)) {
      return;
    }

    this.value = value;
    if (isLoaded) {
      for (Cvar.StateListener<T> stateListener : STATE_LISTENERS) {
        stateListener.onChanged(this, oldValue, this.value);
      }
    } else {
      for (Cvar.StateListener<T> stateListener : STATE_LISTENERS) {
        stateListener.onLoaded(this, this.value);
      }

      this.isLoaded = true;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(@NonNull String str, @NonNull StringSerializer serializer) {
    try {
      StringSerializer<T> castedSerializer = (StringSerializer<T>) serializer;
      T value = castedSerializer.deserialize(str);
      setValue(value);
    } catch (Throwable t) {
      throw new SerializeException(t);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note: Calls {@link Cvar.StateListener#onLoaded} for {@code l} so that it can perform any
   *       necessary setup.
   */
  @Override
  public boolean addStateListener(@NonNull StateListener<T> l) {
    if (l == null) {
      throw new IllegalArgumentException("state listener is not allowed to be null");
    }

    boolean added = STATE_LISTENERS.add(l);
    l.onLoaded(this, value);
    return added;
  }

  @Override
  public boolean containsStateListener(@Nullable StateListener<T> l) {
    return l != null && STATE_LISTENERS.contains(l);

  }

  @Override
  public boolean removeStateListener(@Nullable StateListener<T> l) {
    return l != null && STATE_LISTENERS.remove(l);

  }

  /**
   * Returns a string representation of the {@linkplain #getValue value} of this {@code Cvar}.
   *
   * @return The value of this {@code Cvar}, or {@code "null"} if it is {@code null}.
   */
  @Override
  @NonNull
  public String toString() {
    final T value = this.value;
    if (value == null) {
      return "null";
    } else if (value instanceof String) {
      return "\"" + value.toString() + "\"";
    }

    return value.toString();
  }

}
