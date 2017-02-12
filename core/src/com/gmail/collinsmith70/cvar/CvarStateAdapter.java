package com.gmail.collinsmith70.cvar;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public abstract class CvarStateAdapter<T> implements Cvar.StateListener<T> {

  @Override
  public void onChanged(@NonNull Cvar<T> cvar, @Nullable T from, @Nullable T to) {}

  @Override
  public void onLoaded(@NonNull Cvar<T> cvar, @Nullable T to) {
    onChanged(cvar, null, to);
  }

}
