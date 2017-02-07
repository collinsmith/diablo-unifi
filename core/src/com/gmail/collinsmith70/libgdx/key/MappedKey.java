package com.gmail.collinsmith70.libgdx.key;

import com.google.common.base.Preconditions;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.badlogic.gdx.Input;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.badlogic.gdx.Input.Keys.*;

public class MappedKey implements Iterable<Integer> {

  public static final int NOT_MAPPED = 0;

  public static final int PRIMARY_ASSIGNMENT = 0;
  public static final int SECONDARY_ASSIGNMENT = 1;

  @IntDef({PRIMARY_ASSIGNMENT, SECONDARY_ASSIGNMENT})
  @Retention(RetentionPolicy.SOURCE)
  @interface Assignment {}

  @IntDef({NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, A,
      ALT_LEFT, ALT_RIGHT, APOSTROPHE, AT, B, BACK, BACKSLASH, C, CALL, CAMERA, CLEAR, COMMA, D,
      /*DEL,*/ BACKSPACE, FORWARD_DEL, DPAD_CENTER, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_UP,
      /*CENTER, DOWN, LEFT, RIGHT, UP,*/ E, ENDCALL, ENTER, ENVELOPE, EQUALS, EXPLORER, F, FOCUS, G,
      GRAVE, H, HEADSETHOOK, HOME, I, J, K, L, LEFT_BRACKET, M, MEDIA_FAST_FORWARD, MEDIA_NEXT,
      MEDIA_PLAY_PAUSE, MEDIA_PREVIOUS, MEDIA_REWIND, MEDIA_STOP, MENU, MINUS, MUTE, N,
      NOTIFICATION, NUM, O, P, PERIOD, PLUS, POUND, POWER, Q, R, RIGHT_BRACKET, S, SEARCH,
      SEMICOLON, SHIFT_LEFT, SHIFT_RIGHT, SLASH, SOFT_LEFT, SOFT_RIGHT, SPACE, STAR, SYM, T, TAB, U,
      UNKNOWN, V, VOLUME_DOWN, VOLUME_UP, W, X, Y, Z, /*META_ALT_LEFT_ON, META_ALT_ON,
      META_ALT_RIGHT_ON, META_SHIFT_LEFT_ON, META_SHIFT_ON,*/ META_SHIFT_RIGHT_ON, /*META_SYM_ON,*/
      CONTROL_LEFT, CONTROL_RIGHT, ESCAPE, END, INSERT, PAGE_UP, PAGE_DOWN, PICTSYMBOLS,
      SWITCH_CHARSET, /*BUTTON_CIRCLE,*/ BUTTON_A, BUTTON_B, BUTTON_C, BUTTON_X, BUTTON_Y, BUTTON_Z,
      BUTTON_L1, BUTTON_R1, BUTTON_L2, BUTTON_R2, BUTTON_THUMBL, BUTTON_THUMBR, BUTTON_START,
      BUTTON_SELECT, BUTTON_MODE, NUMPAD_0, NUMPAD_1, NUMPAD_2, NUMPAD_3, NUMPAD_4, NUMPAD_5,
      NUMPAD_6, NUMPAD_7, NUMPAD_8, NUMPAD_9, COLON, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11,
      F12})
  @Retention(RetentionPolicy.SOURCE)
  @interface Keycode {}

  private static final int NONE = 0;
  private static final int PRIMARY = 1;
  private static final int SECONDARY = 2;

  @IntDef(flag = true, value = {PRIMARY, SECONDARY})
  @Retention(RetentionPolicy.SOURCE)
  @interface AssignmentFlag {}

  @NonNull
  private final String NAME;

  @NonNull
  private final String ALIAS;

  @NonNull
  private final int[] ASSIGNMENTS;

  @AssignmentFlag
  private int pressedKeycode;

  @NonNull
  private final Set<StateListener> STATE_LISTENERS;

  @NonNull
  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS;

  public MappedKey(@NonNull String name, @NonNull String alias, @Keycode int primary) {
    this(name, alias, primary, Input.Keys.ANY_KEY);
  }

  public MappedKey(@NonNull String name, @NonNull String alias,
                   @Keycode int primary, @Keycode int secondary) {
    Preconditions.checkArgument(!name.isEmpty(), "key names cannot be empty");
    Preconditions.checkArgument(!name.isEmpty(), "key aliases cannot be empty");

    this.NAME = name;
    this.ALIAS = alias;
    this.ASSIGNMENTS = new int[] {primary, secondary};

    this.STATE_LISTENERS = new CopyOnWriteArraySet<StateListener>();
    this.ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<AssignmentListener>();

    this.pressedKeycode = NONE;
  }

  /**
   * Returns the user-friendly name of this {@code MappedKey}.
   *
   * @return The name of this {@code MappedKey}
   */
  @NonNull
  public String getName() {
    return NAME;
  }

  /**
   * Returns the alias associated with this {@code MappedKey}. The alias of a {@code MappedKey}
   * is used as a unique identifier.
   *
   * @return The alias of this {@code MappedKey}
   */
  @NonNull
  public String getAlias() {
    return ALIAS;
  }

  /**
   * Returns the {@linkplain #PRIMARY_ASSIGNMENT primary} keycode mapped to this {@code MappedKey}.
   *
   * @return The primary keycode mapped to this {@code MappedKey}
   */
  @Keycode
  public int getPrimaryAssignment() {
    return ASSIGNMENTS[PRIMARY_ASSIGNMENT];
  }

  /**
   * Returns the {@linkplain #SECONDARY_ASSIGNMENT secondary} keycode mapped to this {@code MappedKey}.
   *
   * @return The secondary keycode mapped to this {@code MappedKey}
   */
  @Keycode
  public int getSecondaryAssignment() {
    return ASSIGNMENTS[SECONDARY_ASSIGNMENT];
  }

  /**
   * Returns an array consisting of at the keycodes mapped to this {@code MappedKey}. This array
   * will contain at least {@code 2} keycodes ({@link #PRIMARY_ASSIGNMENT primary} and
   * {@link #SECONDARY_ASSIGNMENT secondary}), however a larger array is possible if more keycodes
   * are mapped to this {@code MappedKey}. If an assignment is not mapped, then its value will be
   * set as {@link #NOT_MAPPED}.
   * <p>
   * Note: The returned array is only a copy of the assignments, so changes will not be propagated
   *       to the key mappings of this {@code MappedKey}.
   * <p>
   * Note: If an entire copy is not needed and only iteration is desired, then {@link #iterator}
   *       should be used instead.
   *
   * @return An array of the keycodes associated with this {@code MappedKey}
   *
   * @see #NOT_MAPPED
   * @see #PRIMARY_ASSIGNMENT
   * @see #SECONDARY_ASSIGNMENT
   */
  @NonNull
  @Size(min = 2)
  public int[] getAssignments() {
    return Arrays.copyOf(ASSIGNMENTS, ASSIGNMENTS.length);
  }

  /**
   * Returns an iterator over the {@linkplain #getAssignments assignments} of this {@code
   * MappedKey}.
   * <p>
   * Note: Keycodes are returned in order of {@linkplain #PRIMARY_ASSIGNMENT primary},
   *       {@linkplain #SECONDARY_ASSIGNMENT secondary}, etc.
   *
   * @return An iterator over the kep mappings of this {@code MappedKey}
   */
  @Override
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private int nextIndex = 0;

      @Override
      public boolean hasNext() {
        return nextIndex < ASSIGNMENTS.length;
      }

      @Override
      public Integer next() {
        return ASSIGNMENTS[nextIndex++];
      }

      @Deprecated
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns whether or not this {@code MappedKey} is pressed. A {@code MappedKey} is pressed if
   * any of its {@linkplain #getAssignments mapped keycodes} are pressed.
   *
   * @return {@code true} if this {@code MappedKey} is pressed, otherwise {@code false}
   *
   * @see #setPressed(boolean)
   */
  public boolean isPressed() {
    return pressedKeycode != NONE;
  }

  /**
   * Sets whether or not this {@code MappedKey} is pressed. A {@code MappedKey} is pressed if
   * any of its {@linkplain #getAssignments mapped keys} are pressed. In this case, this
   * method will forward the {@link StateListener} events as if the {@link #getPrimaryAssignment
   * primary} mapped key were pressed.
   *
   * @param b {@code true} to set this {@code MappedKey} as pressed, otherwise {@code false}
   *
   * @see #isPressed
   */
  public void setPressed(boolean b) {
    setPressed(ASSIGNMENTS[PRIMARY_ASSIGNMENT], b);
  }

  /**
   * Sets whether or not this {@code MappedKey} is pressed. A {@code MappedKey} is pressed if
   * any of its {@linkplain #getAssignments assigned keycodes} are pressed. In this case, this
   * method will forward the {@link StateListener} events with {@code keycode} as the pressed
   * assignment.
   * <p>
   * Note: No validation is performed to check if {@code keycode} is {@linkplain #getAssignments
   *       assigned} to this {@code MappedKey}, that is the responsibility of the caller.
   * <p>
   * Note: If this {@code MappedKey} is pressed and {@code b} is {@code false} and {@code keycode}
   *       did not initiate the last press event, then this {@code MappedKey} will remain pressed.
   *
   * @param keycode The assigned keycode being pressed
   * @param b       {@code true} to set this {@code MappedKey} as pressed, otherwise {@code false}
   *
   * @see #isPressed
   * @see #setPressed(boolean)
   */
  void setPressed(@Keycode int keycode, boolean b) {
    if (keycode == ASSIGNMENTS[PRIMARY_ASSIGNMENT]) {
      if (b) {
        this.pressedKeycode |= PRIMARY;
      } else {
        this.pressedKeycode &= ~PRIMARY;
      }
    } else if (keycode == ASSIGNMENTS[SECONDARY_ASSIGNMENT]) {
      if (b) {
        this.pressedKeycode |= SECONDARY;
      } else {
        this.pressedKeycode &= ~SECONDARY;
      }
    }
  }

  @Keycode
  public int assign(@Assignment int assignment, @Keycode int keycode) {
    @Keycode
    int previous = ASSIGNMENTS[assignment];
    ASSIGNMENTS[assignment] = keycode;
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      l.onAssigned(this, assignment, keycode);
    }

    return previous;
  }

  public boolean unassign(@Assignment int assignment) {
    @Keycode
    int unassigned = ASSIGNMENTS[assignment];
    if (unassigned != NOT_MAPPED) {
      ASSIGNMENTS[assignment] = NOT_MAPPED;
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, assignment, unassigned);
      }

      return true;
    }

    return false;
  }

  public boolean isAssigned(@Keycode int keycode) {
    for (int mapped : ASSIGNMENTS) {
      if (mapped == keycode) {
        return true;
      }
    }

    return false;
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
      for (int i = 0; i < ASSIGNMENTS.length; i++) {
        l.onFirstAssignment(this, i, ASSIGNMENTS[i]);
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

  interface StateListener {

    void onPressed(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

    void onDepressed(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

  }

  interface AssignmentListener {

    void onAssigned(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

    void onUnassigned(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

    void onFirstAssignment(@NonNull MappedKey key, @Assignment int assignment,
                           @Keycode int keycode);

  }

}
