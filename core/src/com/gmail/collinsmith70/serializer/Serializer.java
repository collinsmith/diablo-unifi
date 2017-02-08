package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Abstract representation of a <a href="https://en.wikipedia.org/wiki/Serialization">Serializer</a>
 * which can both serialize and deserialize objects from type {@link T1} into and from type
 * {@link T2}.
 *
 * @param <T1> The class which this {@code Serializer} accepts
 * @param <T2> The class this {@code Serializer} (de)serializes {@link T1} into.
 */
@SuppressWarnings("WeakerAccess")
public interface Serializer<T1, T2> {

  /**
   * Serializes an object into its {@link T2} representation.
   *
   * @param obj The object to serialize
   *
   * @return {@link T2} representation of the passed object
   */
  @Nullable
  T2 serialize(@NonNull T1 obj);

  /**
   * Deserializes an object from its {@link T2} representation.
   *
   * @param obj The object to serialize
   *
   * @return {@link T1} representation of the passed object
   */
  @Nullable
  T1 deserialize(@NonNull T2 obj);

}
