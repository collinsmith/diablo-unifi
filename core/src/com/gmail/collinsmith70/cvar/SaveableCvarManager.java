package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.serializer.ByteStringSerializer;
import com.gmail.collinsmith70.serializer.CharacterStringSerializer;
import com.gmail.collinsmith70.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.serializer.FloatStringSerializer;
import com.gmail.collinsmith70.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.serializer.LocaleStringSerializer;
import com.gmail.collinsmith70.serializer.LongStringSerializer;
import com.gmail.collinsmith70.serializer.ObjectStringSerializer;
import com.gmail.collinsmith70.serializer.ShortStringSerializer;
import com.gmail.collinsmith70.serializer.StringSerializer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of a {@link CvarManager} which adds the ability to {@linkplain #save save} and
 * {@linkplain #load load} {@link Cvar} instances so that their values can persist across multiple
 * executions. In addition to the contract set forth within {@code CvarManager}, a
 * {@code SaveableCvarManager} contains mappings between class types and {@link StringSerializer}
 * instances which can be used to (de)serialize them for saving/loading.
 */
public abstract class SaveableCvarManager extends CvarManager {

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
          = new HashMap<Class, StringSerializer>();

  static {
    DEFAULT_SERIALIZERS.put(Character.class, CharacterStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(String.class, ObjectStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Byte.class, ByteStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Short.class, ShortStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Long.class, LongStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Float.class, FloatStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
    DEFAULT_SERIALIZERS.put(Locale.class, LocaleStringSerializer.INSTANCE);
  }

  /**
   * {@linkplain Map Mapping} of {@link Class} instances to their default {@link StringSerializer}
   * instances for which they can be ({@linkplain StringSerializer#deserialize de})
   * {@linkplain StringSerializer#serialize serialized} with.
   */
  @NonNull
  private final Map<Class, StringSerializer> SERIALIZERS;

  /**
   * {@code true} represents that this {@code CvarManager} will automatically save and commit
   * {@linkplain Cvar#setValue changes} made to a {@link Cvar} to the underlying data structure
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
   *                 otherwise {@code false}. Autosaving will cause {@link Cvar} changes to be
   *                 {@linkplain #save saved} as soon as they occur.
   */
  public SaveableCvarManager(boolean autosave) {
    super();
    this.autosaving = autosave;
    this.SERIALIZERS = new ConcurrentHashMap<Class, StringSerializer>(DEFAULT_SERIALIZERS);
  }

  /**
   * Returns whether or not this {@code SaveableCvarManager} will automatically
   * {@linkplain #save save} {@linkplain Cvar#setValue changes} made to a {@link Cvar} to the
   * underlying data structure (typically some implementation of a
   * {@link java.util.prefs.Preferences}) instance.
   *
   * @return {@code true} if this {@link CvarManager} will automatically {@linkplain #save save}
   *         {@linkplain Cvar#setValue changes}, otherwise {@code false}
   *
   * @see #setAutosaving
   */
  public boolean isAutosaving() {
    return autosaving;
  }

  /**
   * Controls whether or not {@link Cvar} instances will have their values serialized and saved
   * whenever a modification to their value is made.
   * <p>
   * Note: If {@linkplain #isAutosaving autosaving} transitions from disabled to enabled, then
   *       {@link #saveAll()} will automatically be called to {@linkplain #save save} all current
   *       {@linkplain Cvar#getValue values}.
   *
   * @param autosave {@code true} to enable {@linkplain #isAutosaving autosaving} of {@code Cvar}
   *                 instances {@linkplain #isManaging managed} by this {@link SaveableCvarManager}
   *                 whenever they are {@linkplain Cvar#setValue changed}, otherwise {@code false}
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
   */
  @Override
  public <T> boolean add(@NonNull Cvar<T> cvar) {
    boolean added = super.add(cvar);
    load(cvar);
    return added;
  }

  /**
   * Loads the specified {@link Cvar}.
   * <p>
   * Note: It is the responsibility of implementing classes to determine how the value should be
   *       loaded, as well as actually {@linkplain Cvar#setValue setting that value} on the
   *       {@code Cvar} itself.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to load
   *
   * @return The {@linkplain StringSerializer#deserialize deserialized} {@linkplain Cvar#getValue
   *         value} which was loaded from a persistent backend
   *
   * @see Cvar#setValue
   */
  @Nullable
  public abstract <T> T load(@NonNull Cvar<T> cvar);

  /**
   * {@inheritDoc}
   * <p>
   * Additionally, this will {@linkplain #save} the specified {@link Cvar} if
   * {@linkplain #isAutosaving autosaving} is enabled.
   *
   * @param cvar {@inheritDoc}
   * @param from {@inheritDoc}
   * @param to   {@inheritDoc}
   *
   * @see #isAutosaving
   * @see #setAutosaving
   */
  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {
    if (autosaving) {
      save(cvar);
    }
  }

  /**
   * Serializes and saves the specified {@link Cvar} to a persistent backend (typically some
   * implementation of a {@link java.util.prefs.Preferences}).
   * <p>
   * Note: It is the responsibility of implementing classes to determine how the value should be
   * saved.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to save
   */
  public abstract <T> void save(@NonNull Cvar<T> cvar);

  /**
   * Aggregate operation which {@linkplain #save saves} all {@link Cvar} instances
   * {@linkplain #isManaging managed} by this {@link SaveableCvarManager}.
   *
   * @see #save
   */
  public void saveAll() {
    for (Cvar cvar : getCvars()) {
      save(cvar);
    }
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
   * @see #getSerializer(Cvar)
   */
  @Nullable
  public <T> StringSerializer<T> getSerializer(@NonNull Class<T> type) {
    if (type == null) {
      throw new IllegalArgumentException("type is not allowed to be null");
    }

    return (StringSerializer<T>) SERIALIZERS.get(type);
  }

  /**
   * Retrieves the {@link StringSerializer} associated with the specified {@link Cvar}.
   *
   * @param <T>  The {@linkplain Class type} of the {@linkplain Cvar#getValue variable} which the
   *             {@code Cvar} represents
   * @param cvar The {@code Cvar} instance to look-up
   *
   * @return A {@code StringSerializer} instance which can be used to (de)serialize the specified
   *         {@link Cvar}, or {@code null} if no {@code StringSerializer} has been set yet
   *
   * @see #getSerializer(Class)
   */
  @Nullable
  public <T> StringSerializer<T> getSerializer(@NonNull Cvar<T> cvar) {
    return getSerializer(cvar.getType());
  }

  /**
   * Associates the specified {@link StringSerializer} as the {@code StringSerializer} to use when
   * (de)serializing {@link Cvar} instances of the specified {@link Class} type.
   *
   * @param <T>        The type which the {@code StringSerializer} is designed to handle
   * @param type       The {@code Class} which the {@code StringSerializer} is designed to handle,
   *                   and which {@code Cvar} instances must {@linkplain Cvar#getType match} in
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
   * Returns whether or not the specified {@link Cvar} {@linkplain Cvar#getType() type} is
   * associated with any {@link StringSerializer}.
   *
   * @param cvar The {@code Cvar} instance whose type to check if there is an associated
   *             {@code StringSerializer} for
   *
   * @return {@code true} if there is a {@code StringSerializer} associated with the specified
   *         {@code Cvar} instance's {@linkplain Cvar#getType() type}, otherwise {@code false}
   */
  public boolean containsSerializer(@NonNull Cvar cvar) {
    return containsSerializer(cvar.getType());
  }

  /**
   * Returns whether or not the specified {@link Class} is associated with any
   * {@link StringSerializer}.
   *
   * @param type The {@linkplain Class type} to check if there is an associated
   *             {@code StringSerializer} for
   *
   * @return {@code true} if there is a {@code StringSerializer} assocaited with the specified
   *         type, otherwise {@code false}
   */
  public boolean containsSerializer(@NonNull Class type) {
    if (type == null) {
      throw new IllegalArgumentException("type is not allowed to be null");
    }

    return SERIALIZERS.containsKey(type);
  }

}
