package com.gmail.collinsmith70.libgdx.key;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Iterator;

public abstract class KeyMapper implements MappedKey.AssignmentListener, Iterable<MappedKey> {

  private final IntMap<ObjectSet<MappedKey>> KEYS;

  public KeyMapper() {
    this.KEYS = new IntMap<>();
  }

  @Override
  public Iterator<MappedKey> iterator() {
    return new Iterator<MappedKey>() {
      IntMap.Values<ObjectSet<MappedKey>> entries = KEYS.values();
      ObjectSet.ObjectSetIterator<MappedKey> keys = null;

      @Override
      public boolean hasNext() {
        return entries.hasNext || (keys != null && keys.hasNext);
      }

      @Override
      public MappedKey next() {
        if (keys == null || !keys.hasNext) {
          keys = entries.next().iterator();
        }

        return keys.next();
      }

      @Deprecated
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public boolean add(@NonNull MappedKey key) {
    if (isManaging(key)) {
      return false;
    }

    key.addAssignmentListener(this);
    return true;
  }

  public boolean remove(@NonNull MappedKey key) {
    if (!isManaging(key)) {
      return false;
    }

    @MappedKey.Assignment int i = MappedKey.PRIMARY;
    for (int keycode : key) {
      unassign(key, i++, keycode);
    }

    return true;
  }

  public ObjectSet<MappedKey> get(@MappedKey.Keycode int keycode) {
    return KEYS.get(keycode);
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

  protected final void checkIfManaging(@Nullable MappedKey key) {
    if (!isManaging(key)) {
      throw new IllegalArgumentException("key is not managed by this key mapper");
    }
  }

  private void assign(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                      @MappedKey.Keycode int keycode) {
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      keys = new ObjectSet<>();
      KEYS.put(keycode, keys);
    }

    keys.add(key);
  }

  private void unassign(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                        @MappedKey.Keycode int keycode) {
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      throw new IllegalStateException();
    }

    keys.remove(key);
    if (keys.size == 0) {
      KEYS.remove(keycode);
    }
  }

  @Override
  public void onAssigned(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                         @MappedKey.Keycode int keycode) {
    assign(key, assignment, keycode);
  }

  @Override
  public void onFirstAssignment(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                                @MappedKey.Keycode int keycode) {
    onAssigned(key, assignment, keycode);
  }

  @Override
  public void onUnassigned(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                           @MappedKey.Keycode int keycode) {
    unassign(key, assignment, keycode);
  }
}
