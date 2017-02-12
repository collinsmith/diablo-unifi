package com.gmail.collinsmith70.cvar.deprecated;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Basic implementation of a {@link com.gmail.collinsmith70.cvar.Cvar.StateListener} which propagates all
 * {@link com.gmail.collinsmith70.cvar.Cvar.StateListener#onLoaded} calls to {@link com.gmail.collinsmith70.cvar.Cvar.StateListener#onChanged} with
 * {@code from} set to {@code null}.
 *
 * @param <T> {@linkplain Class type} of the {@linkplain com.gmail.collinsmith70.cvar.Cvar#getValue variable} which the
 *            {@link com.gmail.collinsmith70.cvar.Cvar} represents
 */
@Deprecated
public abstract class SimpleCvarStateAdapter<T> implements com.gmail.collinsmith70.cvar.Cvar.StateListener<T> {

  /**
   * Called synchronously when the value of a {@link com.gmail.collinsmith70.cvar.Cvar} changes.
   * <p>
   * Note: This implementation will always have {@code from} set to {@code null} if it is being
   *       propagated to via {@link #onLoaded}.
   *
   * @param cvar The {@code Cvar} where the event occurred
   * @param from The previous value of the {@code Cvar}
   * @param to   The current value of the {@code Cvar}
   */
  @Override
  public void onChanged(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar, @Nullable T from, @Nullable T to) {}

  /**
   * Called synchronously when a {@link com.gmail.collinsmith70.cvar.Cvar} is {@linkplain com.gmail.collinsmith70.cvar.Cvar#isLoaded loaded}, and propagates
   * that call to {@link #onChanged} instead, passing a {@code null} reference as the {@code from}
   * argument.
   *
   * @param cvar The {@code Cvar} where the event occurred
   * @param to   The current value of the {@code Cvar}
   */
  @Override
  public void onLoaded(@NonNull com.gmail.collinsmith70.cvar.Cvar<T> cvar, @Nullable T to) {
    onChanged(cvar, null, to);
  }

}
