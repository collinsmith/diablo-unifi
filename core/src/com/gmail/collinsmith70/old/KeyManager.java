package com.gmail.collinsmith70.old;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class KeyManager implements Key.AssignmentListener {

  private final Map<Integer, Key> KEYS;

  public KeyManager() {
    this.KEYS = new ConcurrentHashMap<Integer, Key>();
  }

  @Override
  public void onAssigned(@NonNull Key key, int keycode) {
    KEYS.put(keycode, key);
  }

  @Override
  public void onUnassigned(@NonNull Key key, int keycode) {
    Object curValue = KEYS.get(key);
    if (!Objects.equals(curValue, key) || (curValue == null && !KEYS.containsKey(key))) {
      return;
    }

    KEYS.remove(keycode);
  }

  @Override
  public void onFirstAssignment(Key key, int keycode) {
    onAssigned(key, keycode);
  }

  @NonNull
  public Collection<Key> getKeys() {
    return KEYS.values();
  }

  public Key add(@NonNull Key key) {
    if (isManaging(key)) {
      return key;
    }

    key.addAssignmentListener(this);
    for (int keycode : key.getAssignments()) {
      KEYS.put(keycode, key);
    }

    return key;
  }

  public boolean remove(@Nullable Key key) {
    if (!isManaging(key)) {
      return false;
    }

    for (int keycode : key.getAssignments()) {
      Object curValue = KEYS.get(keycode);
      if (!Objects.equals(curValue, key)) {
        continue;
      }

      KEYS.remove(keycode);
    }

    return true;
  }

  public Key get(int keycode) {
    return KEYS.get(keycode);
  }

  public boolean isManaging(@Nullable Key key) {
    return key != null && KEYS.containsValue(key);
  }

  protected final void checkIfManaged(@NonNull Key key) {
    Preconditions.checkNotNull(key);
    if (!isManaging(key)) {
      throw new UnmanagedKeyException(key, key.getAlias() + " is not managed by this KeyManager");
    }
  }

  public static abstract class KeyManagerException extends RuntimeException {

    @Nullable
    private final Key KEY;

    private KeyManagerException() {
      this(null, null);
    }

    private KeyManagerException(@Nullable Key key) {
      this(key, null);
    }

    private KeyManagerException(@Nullable String message) {
      this(null, message);
    }

    private KeyManagerException(@Nullable Key key, @Nullable String message) {
      super(message);
      this.KEY = key;
    }

    @Nullable
    public Key getKey() {
      return KEY;
    }

  }

  public static class DuplicateKeyException extends KeyManagerException {

    private DuplicateKeyException() {
      this(null, null);
    }

    private DuplicateKeyException(@Nullable Key key) {
      this(key, null);
    }

    private DuplicateKeyException(@Nullable String message) {
      this(null, message);
    }

    private DuplicateKeyException(@Nullable Key key, @Nullable String message) {
      super(key, message);
    }

  }

  public static class UnmanagedKeyException extends KeyManagerException {

    private UnmanagedKeyException() {
      this(null, null);
    }

    private UnmanagedKeyException(@Nullable Key key) {
      this(key, null);
    }

    private UnmanagedKeyException(@Nullable String message) {
      this(null, message);
    }

    private UnmanagedKeyException(@Nullable Key key, @Nullable String message) {
      super(key, message);
    }

  }

}
