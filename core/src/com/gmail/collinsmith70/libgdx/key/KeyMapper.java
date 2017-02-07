package com.gmail.collinsmith70.libgdx.key;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.IntMap;

public abstract class KeyMapper implements MappedKey.AssignmentListener {

  private final IntMap<MappedKey> KEYS;

  public KeyMapper() {
    this.KEYS = new IntMap<MappedKey>();
  }

  public boolean add(@NonNull MappedKey key) {
    if (isManaging(key)) {

    }

    key.addAssignmentListener(this);
    return true;
  }

  public boolean isManaging(@Nullable MappedKey key) {
    return key != null && containsAnyAssignmentsOf(key);
  }

  private boolean containsAnyAssignmentsOf(@NonNull MappedKey key) {
    for (int keycode : key) {
      if (KEYS.containsKey(keycode)) {
        return true;
      }
    }

    return false;
  }

  private void mapAllAssignmentsOf(@NonNull MappedKey key) {
    for (int keycode : key) {
      KEYS.put(keycode, key);
    }
  }

  @Override
  public void onAssigned(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                         @MappedKey.Keycode int keycode) {
    KEYS.put(keycode, key);
  }
}
