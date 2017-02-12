package com.gmail.collinsmith70.cvar.deprecated;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.serializer.ByteStringSerializer;
import com.gmail.collinsmith70.serializer.CharacterStringSerializer;
import com.gmail.collinsmith70.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.serializer.FloatStringSerializer;
import com.gmail.collinsmith70.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.serializer.LocaleStringSerializer;
import com.gmail.collinsmith70.serializer.LongStringSerializer;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.ShortStringSerializer;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.serializer.StringStringSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a {@link com.gmail.collinsmith70.cvar.CvarManager} which adds the ability to {@linkplain #save save} and
 * {@linkplain #load load} {@link com.gmail.collinsmith70.cvar.Cvar} instances so that their values can persist across multiple
 * executions. In addition to the contract set forth within {@code CvarManager}, a
 * {@code SaveableCvarManager} contains mappings between class types and {@link StringSerializer}
 * instances which can be used to (de)serialize them for saving/loading.
 */
@SuppressWarnings({ "SameParameterValue", "WeakerAccess", "unused", "ConstantConditions" })
@Deprecated
public abstract class SaveableCvarManager extends com.gmail.collinsmith70.cvar.CvarManager {

  /**
   * {@linkplain Map Mapping} of {@link Class} instances to their default {@link StringSerializer}
   * instances for which they can be ({@linkplain StringSerializer#deserialize de})
   * {@linkplain StringSerializer#serialize serialized} with. This static reference is specifically
   * used when {@code CvarManager} instances are constructed, so that they have a default mapping of
   * {@code StringSerializer} instances.
   *
   * @see #SERIALIZERS
   */
  @NonNull
  private static final Map<Class, StringSerializer> DEFAULT_SERIALIZERS
      = ImmutableMap.<Class, StringSerializer>builder()
      .put(Character.class, CharacterStringSerializer.INSTANCE)
      .put(Boolean.class, BooleanStringSerializer.INSTANCE)
      .put(Byte.class, ByteStringSerializer.INSTANCE)
      .put(Short.class, ShortStringSerializer.INSTANCE)
      .put(Integer.class, IntegerStringSerializer.INSTANCE)
      .put(Long.class, LongStringSerializer.INSTANCE)
      .put(Float.class, FloatStringSerializer.INSTANCE)
      .put(Double.class, DoubleStringSerializer.INSTANCE)
      .put(String.class, StringStringSerializer.INSTANCE)
      .put(Locale.class, LocaleStringSerializer.INSTANCE)
      .build();

  /**
   * {@linkplain Map Mapping} of {@link Class} instances to their default {@link StringSerializer}
   * instances for which they can be ({@linkplain StringSerializer#deserialize de})
   * {@linkplain StringSerializer#serialize serialized} with.
   */
  @NonNull
  private final Map<Class, StringSerializer> SERIALIZERS;

  /**
   * {@code true} represents that this {@code CvarManager} will automatically save and commit
   * {@linkplain com.gmail.collinsmith70.cvar.Cvar#setValue changes} made to a {@link com.gmail.collinsmith70.cvar.Cvar} to the underlying data structure
   * (typically some implementation of a {@link java.util.prefs.Preferences}) instance.
   *
   * @see #save
   * @see #isAutosaving
   * @see #setAutosaving
   */
  private boolean autosaving;

  /**
   * Constructs a new {@code SaveableCvarManager} instance with
   * {@linkplain #isAutosaving() autosaving} enabled.
   */
  public SaveableCvarManager() {
    this(true);
  }

  /**
   * Constructs a new {@code SaveableCvarManager} instance with {@code autosave} specified.
   *
   * @param autosave {@code true} to enable {@linkplain #isAutosaving() autosaving},
   *                 otherwise {@code false}. Autosaving will cause {@link com.gmail.collinsmith70.cvar.Cvar} changes to be
   *                 {@linkplain #save saved} as soon as they occur.
   */
  public SaveableCvarManager(boolean autosave) {
    super();
    this.autosaving = autosave;
    this.SERIALIZERS = new ConcurrentHashMap<>(DEFAULT_SERIALIZERS);
  }

  /**
   * Returns whether or not this {@code SaveableCvarManager} will automatically
   * {@linkplain #save save} {@linkplain com.gmail.collinsmith70.cvar.Cvar#setValue changes} made to a {@link com.gmail.collinsmith70.cvar.Cvar} to the
   * underlying data structure (typically some implementation of a
   * {@link java.util.prefs.Preferences}) instance.
   *
   * @return {@code true} if this {@link com.gmail.collinsmith70.cvar.CvarManager} will automatically {@linkplain #save save}
   *         {@linkplain com.gmail.collinsmith70.cvar.Cvar#setValue changes}, otherwise {@code false}
   *
   * @see #setAutosaving
   */
  public boolean isAutosaving() {
    return autosaving;
  }

  /**
   * Controls whether or not {@link com.gmail.collinsmith70.cvar.Cvar} instances will have their values serialized and saved
   * whenever a modification to their value is made.
   * <p>
   * Note: If {@linkplain #isAutosaving autosaving} transitions from disabled to enabled, then
   *       {@link #saveAll()} will automatically be called to {@linkplain #save save} all current
   *       {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue values}.
   *
   * @param autosave {@code true} to enable {@linkplain #isAutosaving autosaving} of {@code Cvar}
   *                 instances {@linkplain #isManaging managed} by this {@link SaveableCvarManager}
   *                 whenever they are {@linkplain com.gmail.collinsmith70.cvar.Cvar#setValue changed}, otherwise {@code false}
   *
   * @see #isAutosaving
   */
  public void setAutosaving(boolean autosave) {
    if (this.autosaving == autosave) {
      return;
    }

    this.autosaving = autosave;
    if (autosave) {
      saveAll();
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Note: This operation will also attempt to {@linkplain #load load} any persistent serialized
   *       data.
   *
   * @param cvar {@inheritDoc}
   *
   * @return {@inheritDoc}
   *
   * @throws SerializeException if there was an issue deserializing the saved value of {@code
   *     cvar}. For more details on the specific cause, see {@link SerializeException#getCause()}.
   *     If a {@code SerializeException} is thrown, the {@code cvar} will still be added normally.
   */
  @Override
  @SuppressWarnings({ "CaughtExceptionImmediatelyRethrown", "finally", "ReturnInsideFinallyBlock" })
  public <T> boolean add(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar) {
    try {
      T value = load(cvar);
      cvar.setValue(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, SerializeException.class);
      throw new SerializeException(t);
    } finally {
      return super.add(cvar);
    }
  }

  /**
   * Loads the persistent value of the specified {@link com.gmail.collinsmith70.cvar.Cvar} from storage.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to load
   *
   * @return The {@linkplain StringSerializer#deserialize deserialized} {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue
   *         value} which was loaded from a persistent backend
   *
   * @throws SerializeException if there was an issue deserializing the saved value of {@code
   *     cvar}. For more details on the specific cause, see {@link SerializeException#getCause()}.
   *
   * @see com.gmail.collinsmith70.cvar.Cvar#setValue
   * @see #save
   */
  @Nullable
  public abstract <T> T load(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar);

  /**
   * Serializes and saves the specified {@link com.gmail.collinsmith70.cvar.Cvar} to a persistent backend (typically some
   * implementation of a {@link java.util.prefs.Preferences}). <p> Note: It is the responsibility of
   * implementing classes to determine how the value should be saved.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to save
   *
   * @throws SerializeException if there was an issue serializing the value of {@code cvar}. For
   *     more details on the specific cause, see {@link SerializeException#getCause()}.
   *
   * @see #load
   */
  public abstract <T> void save(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar);

  /**
   * {@inheritDoc}
   * <p>
   * Additionally, this will {@linkplain #save} the specified {@link com.gmail.collinsmith70.cvar.Cvar} if
   * {@linkplain #isAutosaving autosaving} is enabled.
   *
   * @param cvar {@inheritDoc}
   * @param from {@inheritDoc}
   * @param to   {@inheritDoc}
   *
   * @throws SerializeException if there was an issue serializing the value of {@code cvar}. For
   *     more details on the specific cause, see {@link SerializeException#getCause()}.
   *
   * @see #isAutosaving
   * @see #setAutosaving
   */
  @Override
  public void onChanged(@NonNull com.gmail.collinsmith70.cvar.Cvar cvar, @Nullable Object from, @Nullable Object to) {
    if (autosaving) {
      save(cvar);
    }
  }

  /**
   * Aggregate operation which {@linkplain #save saves} all {@link com.gmail.collinsmith70.cvar.Cvar} instances
   * {@linkplain #isManaging managed} by this {@link SaveableCvarManager}.
   *
   * @return A list of {@link SerializeException} instances, if any, that were thrown when
   *         serializing the values of the {@code Cvar} instances managed by this {@code
   *         SaveableCvarManager}. For more details on the specific causes of the exceptions, see
   *         {@link SerializeException#getCause()}.
   *
   * @see #save
   */
  @NonNull
  public Collection<SerializeException> saveAll() {
    Collection<SerializeException> exceptions = null;
    for (com.gmail.collinsmith70.cvar.Cvar cvar : getCvars()) {
      try {
        save(cvar);
      } catch (SerializeException e) {
        if (exceptions == null) {
          exceptions = new ArrayList<>(1);
        }

        exceptions.add(e);
      }
    }

    if (exceptions == null) {
      exceptions = Collections.emptyList();
    }

    return exceptions;
  }

  /**
   * Retrieves the {@link StringSerializer} associated with the specified {@link Class} type.
   *
   * @param <T>  The type which the {@link StringSerializer} should handle
   * @param type The {@link Class} which the {@link StringSerializer} is designed to handle
   *
   * @return A {@code StringSerializer} which can be used to (de)serialize objects of the specified
   *         type {@link T}, or {@code null} if no {@code StringSerializer} exists yet for that type
   *
   * @see #getSerializer(com.gmail.collinsmith70.cvar.Cvar)
   */
  @Nullable
  @SuppressWarnings("unchecked")
  public <T> StringSerializer<T> getSerializer(@NonNull Class<T> type) {
    Preconditions.checkArgument(type != null, "type is not allowed to be null");
    return (StringSerializer<T>) SERIALIZERS.get(type);
  }

  /**
   * Retrieves the {@link StringSerializer} associated with the specified {@link com.gmail.collinsmith70.cvar.Cvar}.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to look-up
   *
   * @return A {@code StringSerializer} instance which can be used to (de)serialize the specified
   *         {@link com.gmail.collinsmith70.cvar.Cvar}, or {@code null} if no {@code StringSerializer} has been set yet
   *
   * @see #getSerializer(Class)
   */
  @Nullable
  public <T> StringSerializer<T> getSerializer(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar) {
    return getSerializer(cvar.getType());
  }

  /**
   * Associates the specified {@link StringSerializer} as the {@code StringSerializer} to use when
   * (de)serializing {@link com.gmail.collinsmith70.cvar.Cvar} instances of the specified {@link Class} type.
   *
   * @param <T>        The type which the {@code StringSerializer} is designed to handle
   * @param type       The {@code Class} which the {@code StringSerializer} is designed to handle,
   *                   and which {@code Cvar} instances must {@linkplain com.gmail.collinsmith70.cvar.Cvar#getType match} in
   *                   order for the specified {@code StringSerializer} to be used when
   *                   {@linkplain #save saving} and {@linkplain #load loading}
   * @param serializer The {@code StringSerializer} to associate with the specified {@link Class},
   *                   or {@code null} to {@linkplain #removeSerializer disassociate} that
   *                   {@code Class} with any {@code StringSerializer}
   */
  public <T> void putSerializer(@NonNull Class<T> type, @Nullable StringSerializer<T> serializer) {
    if (serializer == null) {
      removeSerializer(type);
      return;
    }

    SERIALIZERS.put(type, serializer);
  }

  /**
   * Disassociates the specified {@link Class} from any {@link StringSerializer}.
   * <p>
   * Note: Passing a {@code null} reference will do nothing to the underlying
   *       {@link StringSerializer} associations.
   *
   * @param type The {@code Class} to disassociate
   */
  public void removeSerializer(@Nullable Class type) {
    if (type == null) {
      return;
    }

    SERIALIZERS.remove(type);
  }

  /**
   * Returns whether or not the specified {@link com.gmail.collinsmith70.cvar.Cvar} {@linkplain com.gmail.collinsmith70.cvar.Cvar#getType() type} is
   * associated with any {@link StringSerializer}.
   *
   * @param cvar The {@code Cvar} instance whose type to check if there is an associated
   *             {@code StringSerializer} for
   *
   * @return {@code true} if there is a {@code StringSerializer} associated with the specified
   *         {@code Cvar} instance's {@linkplain com.gmail.collinsmith70.cvar.Cvar#getType() type}, otherwise {@code false}
   */
  public boolean canSerialize(@NonNull Cvar cvar) {
    return canSerialize(cvar.getType());
  }

  /**
   * Returns whether or not the specified {@link Class} is associated with any
   * {@link StringSerializer}.
   *
   * @param type The {@linkplain Class type} to check if there is an associated
   *             {@code StringSerializer} for
   *
   * @return {@code true} if there is a {@code StringSerializer} associated with the specified
   *         type, otherwise {@code false}
   */
  public boolean canSerialize(@Nullable Class type) {
    return type != null && SERIALIZERS.containsKey(type);
  }

}
