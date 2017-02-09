package com.gmail.collinsmith70.libgdx.key;

import android.support.annotation.NonNull;

@SuppressWarnings({ "unused", "WeakerAccess" })
public abstract class MappedKeyStateAdapter implements MappedKey.StateListener {

  @Override
  public void onPressed(@NonNull MappedKey key, @MappedKey.Keycode int keycode) {}

  @Override
  public void onDepressed(@NonNull MappedKey key, @MappedKey.Keycode int keycode) {}

}
