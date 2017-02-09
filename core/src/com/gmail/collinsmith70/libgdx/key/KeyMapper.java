package com.gmail.collinsmith70.libgdx.key;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings({ "WeakerAccess", "unused", "ConstantConditions" })
public abstract class KeyMapper implements MappedKey.AssignmentListener, Iterable<MappedKey> {

  protected final IntMap<ObjectSet<MappedKey>> KEYS;

  public KeyMapper() {
    this.KEYS = new IntMap<>();
  }

  @Override
  @NonNull
  // TODO: This method is pretty memory intensive in order to avoid returning duplicates as it is
  //       essentially caching every returned MappedKey and uses 3 iterators.
  public Iterator<MappedKey> iterator() {
    final PeekingIterator<MappedKey> iterator = Iterators.peekingIterator(new Iterator<MappedKey>() {
      final IntMap.Values<ObjectSet<MappedKey>> entries = KEYS.values();
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
    });
    return new Iterator<MappedKey>() {
      Set<MappedKey> alreadyReturned = new HashSet<>();

      @Override
      public boolean hasNext() {
        return iterator.hasNext() && !alreadyReturned.contains(iterator.peek());
      }

      @Override
      public MappedKey next() {
        MappedKey next = iterator.next();
        alreadyReturned.add(next);
        return next;
      }

      @Deprecated
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  public boolean add(@NonNull MappedKey key) {
    return key.addAssignmentListener(this);
  }

  public boolean remove(@NonNull MappedKey key) {
    boolean removed = false;
    for (int keycode : key.assignments) {
      removed = removed || unassign(key, keycode);
    }

    return removed;
  }

  @NonNull
  public Set<MappedKey> get(@MappedKey.Keycode int keycode) {
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      return Collections.emptySet();
    }

    return ImmutableSet.copyOf(keys);
  }

  @Nullable
  protected ObjectSet<MappedKey> lookup(@MappedKey.Keycode int keycode) {
    return KEYS.get(keycode);
  }

  protected void setPressed(@NonNull MappedKey key, @MappedKey.Keycode int keycode,
                            boolean pressed) {
    assert isManaging(key);
    key.setPressed(keycode, pressed);
  }

  public boolean isManaging(@Nullable MappedKey key) {
    return key != null && containsAnyAssignmentsOf(key);
  }

  private boolean containsAnyAssignmentsOf(@NonNull MappedKey key) {
    for (int keycode : key.assignments) {
      ObjectSet<MappedKey> keys = KEYS.get(keycode);
      if (keys != null && keys.contains(key)) {
        return true;
      }
    }

    return false;
  }

  protected final void checkIfManaging(@Nullable MappedKey key) {
    Preconditions.checkArgument(!isManaging(key), "key is not managed by this key mapper");
  }

  private void assign(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                      @MappedKey.Keycode int keycode) {
    Preconditions.checkArgument(key != null, "key cannot be null");
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      keys = new ObjectSet<>();
      KEYS.put(keycode, keys);
    }

    keys.add(key);
  }

  private boolean unassign(@NonNull MappedKey key, @MappedKey.Keycode int keycode) {
    Preconditions.checkArgument(key != null, "key cannot be null");
    ObjectSet<MappedKey> keys = KEYS.get(keycode);
    if (keys == null) {
      return false;
    }

    boolean removed = keys.remove(key);
    if (keys.size == 0) {
      KEYS.remove(keycode);
    }

    return removed;
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
    unassign(key, keycode);
  }
}
