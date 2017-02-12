package com.gmail.collinsmith70.cvar.deprecated;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.StringSerializer;

/**
 * <a href="https://en.wikipedia.org/wiki/CVAR">CVAR</a>s are named variables which are used for
 * configuring a client, specifically in video game applications. CVARs support
 * {@linkplain StateListener callbacks} when certain state transitions occur, such as when they
 * are loaded or changed.
 *
 * @param <T> The {@linkplain Class type} of the {@linkplain #getValue variable} being represented
 *
 * @see <a href="https://en.wikipedia.org/wiki/CVAR">Wikipedia article on CVARs</a>
 */
@SuppressWarnings("unused")
public interface Cvar<T> {

  /**
   * Returns the <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">key</a> used
   * to identify this {@code Cvar}.
   *
   * @return The alias used to identify this {@code Cvar}, or {@code null} if no alias has been set
   *
   * @see <a href="https://en.wikipedia.org/wiki/Attribute%E2%80%93value_pair">Wikipedia article on key-value pairs</a>
   */
  @NonNull
  String getAlias();

  /**
   * Returns the default value of this {@code Cvar}. The default value of this {@code Cvar} is the
   * assigned value upon instantiation, as well as when this {@code Cvar} is {@linkplain #reset}.
   *
   * @return The default value of this {@code Cvar}, or {@code null} if no default value has been
   *         set
   * 
   * @see #reset
   */
  @Nullable
  T getDefaultValue();

  /**
   * Returns a brief description explaining the purpose and function of this {@code Cvar} along with
   * the values it permits.
   *
   * @return A brief description of this {@code Cvar}, or {@code ""} if no description was given
   */
  @NonNull
  String getDescription();

  /**
   * Returns the class {@linkplain T type} of the {@linkplain #getValue variable} being represented.
   *
   * @return The class {@linkplain T type} of the {@linkplain #getValue variable} being represented
   */
  @NonNull
  Class<T> getType();

  /**
   * Returns the value of the variable represented by this {@code Cvar}.
   *
   * @return The value of the variable represented by this {@code Cvar}, or {@code null} if no value
   *         has been set (i.e., the {@code Cvar} is unassigned)
   */
  @Nullable
  T getValue();

  /**
   * Assigns the value of this {@code Cvar} to {@code value}.
   * <p>
   * Note: Setting the value of this {@code Cvar} to {@code null} will not change its state to
   *       {@linkplain #isLoaded unloaded}.
   * <p>
   * Note: Calling this method will invoke either {@link StateListener#onChanged} or
   *       {@link StateListener#onLoaded}, depending on whether or not this {@code Cvar} has been
   *       {@linkplain #isLoaded loaded}.
   *
   * @param value The value to assign the variable represented by this {@code Cvar} to, or
   *              {@code null} to mark this {@code Cvar} as having no value, and thus be unassigned
   */
  void setValue(@Nullable T value);

  /**
   * {@linkplain StringSerializer#deserialize Deserializes} the specified (@code string} using the
   * passed {@code serializer}. This implementation is provided to assist with assigning
   * {@code Cvar} instances string representations of their values, when type erasure has taken
   * place, so it is expected that {@code serializer} can deserialize objects of type {@link T}.
   * <p>
   * Note: This implementation will call {@link #setValue} to perform the actual assignment.
   *
   * @param string     Serialized value to assign
   * @param serializer {@code StringSerializer} to use to deserialize {@code string}
   *
   * @throws SerializeException if {@code serializer} cannot handle deserializing {@code string}
   *     (e.g., ClassCastException), or if there was some other problem deserializing {@code
   *     string}. For more details on the specific cause, see {@link SerializeException#getCause()}.
   *
   * @see #setValue
   */
  @SuppressWarnings("unchecked")
  void setValue(@NonNull String string, @NonNull StringSerializer<?> serializer);

  /**
   * Returns whether or not the {@linkplain #getValue value} of this {@code Cvar} is {@code null}.
   *
   * @return {@code true} if the value of this {@code Cvar} is {@code null}, otherwise {@code false}
   *
   * @see #getValue
   */
  boolean isEmpty();

  /**
   * Returns whether or not this {@code Cvar} has has its first {@linkplain #setValue assignment}
   * (i.e., been initialized).
   *
   * @return {@code true} if this {@link Cvar} has been initialized, otherwise {@code false}
   */
  boolean isLoaded();

  /**
   * Resets this {@code Cvar} to its {@linkplain #getDefaultValue default value}.
   * <p>
   * Note: Calling this method does not set the state of this {@code Cvar} as
   *       {@linkplain #isLoaded loaded}
   *
   * @see #getDefaultValue
   */
  void reset();

  /**
   * Associates a {@code StateListener} with this {@code Cvar} to receive callbacks when its state
   * changes.
   *
   * @param l The {@code StateListener} to register
   *
   * @return {@code true} if {@code l} was added, otherwise {@code false} if it is already
   *         registered to receive callbacks
   *
   * @see #containsStateListener
   * @see #removeStateListener
   */
  boolean addStateListener(@NonNull StateListener<T> l);

  /**
   * Returns whether or not the specified {@code StateListener} {@code l} is registered to receive
   * callback events regarding the state of this {@code Cvar}.
   * <p>
   * Note: If {@code l} is {@code null}, {@code false} will be returned.
   *
   * @param l The {@code StateListener} to check
   *
   * @return {@code true} if {@code l} is registered to receive callbacks regarding the state of
   *         this {@code Cvar}, otherwise {@code false}
   *
   * @see #addStateListener
   * @see #removeStateListener
   */
  boolean containsStateListener(@Nullable StateListener<T> l);

  /**
   * Unregisters the specified {@code StateListener} {@code l} from receiving callbacks regarding
   * the state changes of this {@code Cvar}.
   * <p>
   * Note: If {@code l} is {@code null} or not a registered listener, {@code false} will be
   *       returned.
   *
   * @param l The {@code StateListener} to remove
   *
   * @return {@code true} if {@code l} was removed by this operation, otherwise {@code false}
   *
   * @see #addStateListener
   * @see #containsStateListener
   */
  boolean removeStateListener(@Nullable StateListener<T> l);

  /**
   * Interface for representing the various <a href="https://en.wikipedia.org/wiki/Callback_(computer_programming)">
   * callbacks</a> {@link Cvar} instances will send during state transitions.
   *
   * @param <T> The {@linkplain Class type} of the {@linkplain #getValue variable} being represented
   *
   * @see <a href="https://en.wikipedia.org/wiki/Callback_(computer_programming)">Wikipedia article on callbacks</a>
   */
  interface StateListener<T> {

    /**
     * Called synchronously when the value of a {@link Cvar} changes.
     * <p>
     * Note: This callback may not be called when a {@code Cvar} is {@linkplain #isLoaded loaded},
     *       as {@link #onLoaded} is designed specifically for that purpose and this callback may
     *       not apply in all cases.
     *
     * @param cvar The {@code Cvar} where the event occurred
     * @param from The previous value of the {@code Cvar}
     * @param to   The current value of the {@code Cvar}
     */
    void onChanged(@NonNull final Cvar<T> cvar, @Nullable final T from, @Nullable final T to);

    /**
     * Called synchronously when a {@link Cvar} is {@linkplain #isLoaded loaded}.
     *
     * @param cvar {@code Cvar} where the event occurred
     * @param to   The current value of the {@code Cvar}
     */
    void onLoaded(@NonNull final Cvar<T> cvar, @Nullable final T to);

  }

}