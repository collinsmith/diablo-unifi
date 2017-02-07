package com.gmail.collinsmith70.old;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * A MappableKey is a named action that can be assigned to multiple keycodes.
 */
public class Key {

  /**
   * The name of this {@code MappableKey}.
   *
   * @see #getName
   */
  @NonNull
  private final String NAME;

  /**
   * The alias assigned to this {@code MappableKey}.
   *
   * @see #getAlias
   */
  @NonNull
  private final String ALIAS;

  @NonNull
  private final Set<Integer> ASSIGNMENTS;

  @NonNull
  private final Set<StateListener> STATE_LISTENERS;

  @NonNull
  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS;

  private boolean pressed;

  private Key(@NonNull String name, @NonNull String alias) {
    Preconditions.checkArgument(!name.isEmpty(), "key names cannot be empty");
    Preconditions.checkArgument(!name.isEmpty(), "key aliases cannot be empty");

    this.STATE_LISTENERS = new CopyOnWriteArraySet<StateListener>();
    this.ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<AssignmentListener>();

    this.NAME = name;
    this.ALIAS = alias;
    this.ASSIGNMENTS = new CopyOnWriteArraySet<Integer>();

    this.pressed = false;
  }

  public Key(@NonNull String name, @NonNull String alias, int keycode) {
    this(name, alias);
    assign(keycode);
  }

  public Key(@NonNull String name, @NonNull String alias, int keycode, int alternateKeycode) {
    this(name, alias, keycode);
    assign(alternateKeycode);
  }

  public Key(@NonNull String name, @NonNull String alias, int... keycodes) {
    this(name, alias);
    for (int keycode : keycodes) {
      assign(keycode);
    }
  }

  public String getName() {
    return NAME;
  }

  public String getAlias() {
    return ALIAS;
  }

  public Set<Integer> getAssignments() {
    return ImmutableSet.copyOf(ASSIGNMENTS);
  }

  public boolean isPressed() {
    return pressed;
  }

  public void setPressed(boolean b) {
    setPressed(0, b);
  }

  public void setPressed(int keycode, boolean b) {
    if (this.pressed == b) {
      return;
    }

    this.pressed = b;
    if (b) {
      for (StateListener l : STATE_LISTENERS) {
        l.onPressed(this, keycode);
      }
    } else {
      for (StateListener l : STATE_LISTENERS) {
        l.onDepressed(this, keycode);
      }
    }
  }

  public boolean assign(int keycode) {
    boolean assigned = ASSIGNMENTS.add(keycode);
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      l.onAssigned(this, keycode);
    }

    return assigned;
  }

  public boolean isAssigned(int keycode) {
    return ASSIGNMENTS.contains(keycode);
  }

  public boolean unassign(int keycode) {
    boolean unassigned = ASSIGNMENTS.remove(keycode);
    if (unassigned) {
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, keycode);
      }
    }

    return unassigned;
  }

  void setAssignments(@NonNull Set<Integer> assignments) {
    ASSIGNMENTS.clear();
    ASSIGNMENTS.addAll(assignments);
  }

  public boolean addStateListener(@NonNull StateListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    return STATE_LISTENERS.add(l);
  }

  public boolean containsStateListener(@Nullable StateListener l) {
    return l != null && STATE_LISTENERS.contains(l);
  }

  public boolean removeStateListener(@Nullable StateListener l) {
    return l != null && STATE_LISTENERS.remove(l);
  }

  public boolean addAssignmentListener(@NonNull AssignmentListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = ASSIGNMENT_LISTENERS.add(l);
    if (added) {
      for (int keycode : ASSIGNMENTS) {
        l.onFirstAssignment(this, keycode);
      }
    }

    return added;
  }

  public boolean containsAssignmentListener(@Nullable AssignmentListener l) {
    return l != null && ASSIGNMENT_LISTENERS.contains(l);
  }

  public boolean removeAssignmentListener(@Nullable AssignmentListener l) {
    return l != null && ASSIGNMENT_LISTENERS.remove(l);
  }

  public interface StateListener {

    void onPressed(@NonNull Key key, int keycode);

    void onDepressed(@NonNull Key key, int keycode);

  }

  public interface AssignmentListener {

    void onAssigned(@NonNull Key key, int keycode);

    void onUnassigned(@NonNull Key key, int keycode);

    void onFirstAssignment(@NonNull Key key, int keycode);

  }

}
