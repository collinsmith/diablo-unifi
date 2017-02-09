package com.gmail.collinsmith70.libgdx.key;

import com.google.common.base.Preconditions;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
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

@SuppressWarnings({ "ConstantConditions", "unused", "UnusedReturnValue" })
public class MappedKey implements Iterable<Integer> {

  /**
   * Represents that a {@link Keycode} is not mapped to any valid keycode.
   */
  public static final int NOT_MAPPED = 0;

  /**
   * Index of the {@linkplain #getPrimaryAssignment primary keycode}.
   *
   * @see #getPrimaryAssignment
   * @see #getAssignments
   */
  public static final int PRIMARY = 0;

  /**
   * Index of the {@linkplain #getSecondaryAssignment secondary keycode}.
   *
   * @see #getSecondaryAssignment
   * @see #getAssignments
   */
  public static final int SECONDARY = 1;

  /**
   * An assignment represents named indeces of the {@linkplain #getAssignments mappings} of a {@code
   * MappedKey}. A value annotated with {@code @Assignment} will be equal to one of the below
   * values.
   *
   * @see #getAssignments
   * @see #getPrimaryAssignment
   * @see #getSecondaryAssignment
   */
  @IntDef({ PRIMARY, SECONDARY })
  @Retention(RetentionPolicy.SOURCE)
  public @interface Assignment {}

  /**
   * A keycode represents a unique identifier for an input. E.g., the letter {@code L} has a unique
   * identifier different from the letter {@code K}. A value annotated with {@code @Keycode} will be
   * equal to one of the below values, which are inherited from {@link Input.Keys}.
   */
  @IntDef({ NOT_MAPPED, NUM_0, NUM_1, NUM_2, NUM_3, NUM_4, NUM_5, NUM_6, NUM_7, NUM_8, NUM_9, A,
      ALT_LEFT, ALT_RIGHT, APOSTROPHE, AT, B, BACK, BACKSLASH, C, CALL, CAMERA, CLEAR, COMMA, D,
      /*DEL,*/ BACKSPACE, FORWARD_DEL, DPAD_CENTER, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT, DPAD_UP,
      /*CENTER, DOWN, LEFT, RIGHT, UP,*/ E, ENDCALL, ENTER, ENVELOPE, EQUALS, EXPLORER, F, FOCUS, G,
      GRAVE, H, HEADSETHOOK, HOME, I, J, K, L, LEFT_BRACKET, M, MEDIA_FAST_FORWARD, MEDIA_NEXT,
      MEDIA_PLAY_PAUSE, MEDIA_PREVIOUS, MEDIA_REWIND, MEDIA_STOP, MENU, MINUS, MUTE, N,
      NOTIFICATION, NUM, O, P, PERIOD, PLUS, POUND, POWER, Q, R, RIGHT_BRACKET, S, SEARCH,
      SEMICOLON, SHIFT_LEFT, SHIFT_RIGHT, SLASH, SOFT_LEFT, SOFT_RIGHT, SPACE, STAR, SYM, T, TAB, U,
      /*UNKNOWN,*/ V, VOLUME_DOWN, VOLUME_UP, W, X, Y, Z, /*META_ALT_LEFT_ON, META_ALT_ON,
      META_ALT_RIGHT_ON, META_SHIFT_LEFT_ON, META_SHIFT_ON,*/ META_SHIFT_RIGHT_ON, /*META_SYM_ON,*/
      CONTROL_LEFT, CONTROL_RIGHT, ESCAPE, END, INSERT, PAGE_UP, PAGE_DOWN, PICTSYMBOLS,
      SWITCH_CHARSET, /*BUTTON_CIRCLE,*/ BUTTON_A, BUTTON_B, BUTTON_C, BUTTON_X, BUTTON_Y, BUTTON_Z,
      BUTTON_L1, BUTTON_R1, BUTTON_L2, BUTTON_R2, BUTTON_THUMBL, BUTTON_THUMBR, BUTTON_START,
      BUTTON_SELECT, BUTTON_MODE, NUMPAD_0, NUMPAD_1, NUMPAD_2, NUMPAD_3, NUMPAD_4, NUMPAD_5,
      NUMPAD_6, NUMPAD_7, NUMPAD_8, NUMPAD_9, COLON, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11,
      F12 })
  @Retention(RetentionPolicy.SOURCE)
  public @interface Keycode {}

  /**
   * The user-friendly name of this {@code MappedKey}.
   *
   * @see #getName
   */
  @NonNull
  private final String NAME;

  /**
   * The alias associated with this {@code MappedKey}. The alias of a {@code MappedKey} can be
   * used as a unique identifier for saving.
   *
   * @see #getAlias
   */
  @NonNull
  private final String ALIAS;

  /**
   * Key mapping assignment event callback receivers.
   */
  @NonNull
  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS;

  /**
   * Key event callback receivers.
   */
  @NonNull
  private final Set<StateListener> STATE_LISTENERS;

  /**
   * List of inputs mapped to this {@code MappedKey}.
   * <p>
   * Invariant: No key mappings may appear after the first {@link #NOT_MAPPED unmapped} index.
   *
   * @see #assign(int[])
   * @see #assign(int, int)
   * @see #unassign()
   * @see #unassign(int)
   * @see #getAssignments()
   * @see #isAssigned()
   * @see #isAssigned(int)
   */
  @NonNull
  @Keycode
  /*package*/ int[] assignments;

  /**
   * Counts the number of pressed inputs mapped in this {@code MappedKey}. This number is
   * incremented each time {@link #setPressed} is called where {@code pressed = true}, and
   * inversely, decremented each time it is called where {@code pressed = false}. When {@code
   * pressed > 0}, this {@code MappedKey} is pressed.
   *
   * @see #setPressed
   * @see #isPressed
   */
  @IntRange(from = 0)
  private int pressed;

  /**
   * Constructs a {@code MappedKey} with the specified {@code name}, {@code alias}, and {@code
   * primary} key mapping. The {@linkplain #getSecondaryAssignment secondary} key mapping will
   * automatically be set to {@linkplain #NOT_MAPPED unmapped}.
   *
   * @param name    The name of the {@code MappedKey}
   * @param alias   The alias of the {@code MappedKey} -- should be unique.
   * @param primary The primary {@link Keycode} to map to the {@code MappedKep}
   *
   * @throws IllegalArgumentException if either {@code name} or {@code alias} is empty, or {@code
   *     primary} is {@linkplain #NOT_MAPPED unmapped}.
   */
  public MappedKey(@NonNull String name, @NonNull String alias, @Keycode int primary) {
    this(name, alias, primary, NOT_MAPPED);
  }

  /**
   * Constructs a {@code MappedKey} with the specified {@code name}, {@code alias}, and {@code
   * primary} and {@code secondary} key mappings.
   *
   * @param name      The name of the {@code MappedKey}
   * @param alias     The alias of the {@code MappedKey} -- should be unique.
   * @param primary   The primary {@link Keycode} to map to the {@code MappedKep}
   * @param secondary The secondary {@link Keycode} to map to the {@code MappedKep}
   *
   * @throws IllegalArgumentException if either {@code name} or {@code alias} is empty, {@code
   *     primary} is {@linkplain #NOT_MAPPED unmapped}, or {@code primary == secondary}.
   */
  public MappedKey(@NonNull String name, @NonNull String alias,
                   @Keycode int primary, @Keycode int secondary) {
    Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");
    Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
    Preconditions.checkArgument(primary != NOT_MAPPED, "primary key mapping must be mapped");
    Preconditions.checkArgument(primary != secondary, "key mappings must be unique");

    this.NAME = name;
    this.ALIAS = alias;
    this.assignments = new int[] { primary, secondary };
    this.pressed = 0;

    this.ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<>();
    this.STATE_LISTENERS = new CopyOnWriteArraySet<>();
  }

  @Override
  @NonNull
  public String toString() {
    return getAlias();
  }

  /**
   * Validates that assignments is valid (i.e., {@code assignments.length >= 2} and assignments
   * indeces are unique. This is a relatively expensive operation, however with only 2-3 elements
   * expected at most, it should not be an issue.
   *
   * @param keycodes The array of assignments to validate
   *
   * @return The passed array ({@code assignments})
   *
   * @throws IllegalArgumentException if {@code assignments} is invalid
   */
  @NonNull
  private int[] validateAssignments(@NonNull @Size(min = 2) int[] keycodes) {
    Preconditions.checkArgument(keycodes.length >= 2, "keycodes.length must be >= 2");
    boolean forceUnmapped = false;
    for (int i = 0; i < keycodes.length; i++) {
      @Keycode int keycode = keycodes[i];
      if (keycode == NOT_MAPPED) {
        forceUnmapped = true;
      }

      for (int j = i + 1; j < keycodes.length; j++) {
        @Keycode int anotherKeycode = keycodes[j];
        if (anotherKeycode == NOT_MAPPED) {
          forceUnmapped = true;
        } else if (forceUnmapped || keycode == anotherKeycode) {
          throw new IllegalArgumentException("mapped keys cannot contain any duplicates or " +
              "mappings after the last unmapped index. Key: "
              + getName() + " [" + getAlias() + "]");
        }
      }

      if (forceUnmapped) {
        break;
      }
    }

    return keycodes;
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
   * should be a unique identifier.
   * 
   * @return The alias of this {@code MappedKey}
   */
  @NonNull
  public String getAlias() {
    return ALIAS;
  }

  /**
   * Returns the {@linkplain #PRIMARY primary} keycode mapped to this {@code MappedKey}.
   *
   * @return The primary keycode mapped to this {@code MappedKey}
   */
  @Keycode
  public int getPrimaryAssignment() {
    return assignments[PRIMARY];
  }

  /**
   * Returns the {@linkplain #SECONDARY secondary} keycode mapped to this {@code
   * MappedKey}.
   * <p>
   * Invariant: If there exists a secondary mapping to this {@code MappedKey} (i.e., the return
   *            value of this is not equal to {@link #NOT_MAPPED}), then it is given that the 
   *            {@link #PRIMARY primary} assignment must also be mapped to a valid keycode.
   *
   * @return The secondary keycode mapped to this {@code MappedKey}
   */
  @Keycode
  public int getSecondaryAssignment() {
    return assignments[SECONDARY];
  }

  /**
   * Returns an array consisting of all the keycodes mapped to this {@code MappedKey}. This array
   * will contain at least {@code 2} keycodes ({@link #PRIMARY primary} and {@link
   * #SECONDARY secondary}), however a larger array is possible if more keycodes are
   * mapped to this {@code MappedKey}. If an index is not mapped, then its value will be set as
   * {@link #NOT_MAPPED}.
   * <p>
   * Note: The returned array is only a copy of the assignments, so changes will not be
   *       propagated to the key mappings of this {@code MappedKey}.
   * <p>
   * Note: If an entire copy is not needed and only iteration is desired, then {@link #iterator}
   *       should be used instead.
   *
   * @return An array of the keycodes associated with this {@code MappedKey}
   *
   * @see #NOT_MAPPED
   * @see #PRIMARY
   * @see #SECONDARY
   * @see #getPrimaryAssignment
   * @see #getSecondaryAssignment
   */
  @NonNull
  @Size(min = 2)
  public int[] getAssignments() {
    return Arrays.copyOf(assignments, assignments.length);
  }

  /**
   * Returns an iterator over the {@linkplain #getAssignments assignments} of this {@code
   * MappedKey}. This implementation is optimized for speed with minimal validation checks, so use
   * with caution or within a for-each loop only, e.g., ({@code for (int keycode : key)}).
   * <p>
   * Note: Kep mappings are returned in order of {@linkplain #PRIMARY primary},
   *       {@linkplain #SECONDARY secondary}, etc. No valid key mappings may appear after an
   *       {@link #NOT_MAPPED unmapped} assignment.
   *
   * @return An iterator over the kep mappings of this {@code MappedKey}
   */
  @Override
  @NonNull
  public Iterator<Integer> iterator() {
    return new Iterator<Integer>() {
      private int nextIndex = 0;

      @Override
      public boolean hasNext() {
        return nextIndex < assignments.length;
      }

      @Override
      @NonNull
      public Integer next() {
        return assignments[nextIndex++];
      }

      @Deprecated
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Remaps the assignments for this {@code MappedKey} to the values within {@code keycodes}. This
   * is intended as a load operation.
   *
   * @param keycodes The new assignments for this {@code MappedKey}
   *
   * @throws IllegalArgumentException if {@code keycodes} is invalid ({@code keycodes.length < 2},
   *     or contains duplicates, or contains mappings after last {@linkplain #NOT_MAPPED unmapped}
   *     value)
   *
   * @see #assign(int, int)
   * @see #unassign(int)
   * @see #unassign()
   */
  public void assign(@Keycode @Size(min = 2) int[] keycodes) {
    validateAssignments(keycodes);
    int[] assignments = this.assignments;
    this.assignments = Arrays.copyOf(keycodes, keycodes.length);
    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      if (keycode == NOT_MAPPED) {
        break;
      }

      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, i, keycode);
      }
    }

    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onAssigned(this, i, keycode);
      }
    }
  }

  /**
   * Maps {@code keycode} to the specified {@code assignment} and returns the previously assigned
   * keycode for that assignment.
   *
   * @param assignment The {@link Assignment} to map {@code keycode} to
   * @param keycode    The {@link Keycode} to map
   *
   * @return The previously assigned keycode to the specified assignment
   *
   * @throws IllegalArgumentException if {@code keycode} is equal to {@link #NOT_MAPPED}
   *     ({@link #unassign(int)} should be used to unmap values), or if there is already a
   *     {@linkplain #isAssigned(int) mapping} for the specified {@code keycode}.
   *
   * @see #assign(int[])
   * @see #unassign(int)
   * @see #unassign()
   */
  @Keycode
  public int assign(@Assignment int assignment, @Keycode int keycode) {
    Preconditions.checkArgument(keycode != NOT_MAPPED,
        "cannot unmap using this method, use unassign(int) instead");
    @Keycode int previous = assignments[assignment];
    if (previous == keycode) {
      return previous;
    }

    Preconditions.checkArgument(!isAssigned(keycode),
        "duplicate keycodes are not allowed. Keycode:" + keycode + " Key:" + this);
    assignments[assignment] = keycode;
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      if (previous != NOT_MAPPED) {
        l.onUnassigned(this, assignment, keycode);
      }

      l.onAssigned(this, assignment, keycode);
    }

    return previous;
  }

  /**
   * Removes the mapping to the specified {@code assignment} and shifts all assignments to the
   * start. E.g., if {@code assignment} equals {@link #PRIMARY}, then the new primary assignment
   * will be set as {@link #SECONDARY} (shifted left).
   *
   * @param assignment The {@link Assignment} to unassign
   *
   * @return {@code true} if the specified {@code assignment} was {@linkplain #NOT_MAPPED unmapped}
   *         and is now unassigned.
   *
   * @see #assign(int, int)
   * @see #assign(int[])
   * @see #unassign()
   */
  public boolean unassign(@Assignment int assignment) {
    @Keycode int unassigned = assignments[assignment];
    if (unassigned != NOT_MAPPED) {
      System.arraycopy(assignments, assignment + 1, assignments, assignment,
          assignments.length - assignment - 1);
      assignments[assignments.length - 1] = NOT_MAPPED;
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, assignment, unassigned);
      }

      return true;
    }

    return false;
  }

  /**
   * Removes the mappings to all assignments of this {@code MappedKey}.
   *
   * @return {@code true} if this {@code MappedKey} contained any valid mappings before this
   *         operation
   *
   * @see #assign(int, int)
   * @see #assign(int[])
   * @see #unassign(int)
   */
  public boolean unassign() {
    boolean unassigned = false;
    for (@Assignment int i = 0; i < assignments.length; i++) {
      @Keycode int keycode = assignments[i];
      if (keycode != NOT_MAPPED) {
        unassigned = true;
        assignments[i] = NOT_MAPPED;
        for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
          l.onUnassigned(this, i, keycode);
        }

        continue;
      }

      break;
    }

    return unassigned;
  }

  /**
   * Returns whether or not there are any mappings for this {@code MappedKey}.
   *
   * @return {@code true} if any mappings exist, otherwise {@code false}
   *
   * @see #isAssigned(int)
   * @see #assign(int, int)
   * @see #assign(int[])
   * @see #unassign(int)
   * @see #unassign()
   */
  public boolean isAssigned() {
    return assignments[PRIMARY] != NOT_MAPPED;
  }

  /**
   * Returns whether or not the specified {@code keycode} is mapped to any of the assignments of
   * this {@code MappedKey}.
   *
   * @param keycode The {@link Keycode} to check
   *
   * @return {@code true} if it is mapped to an assignment, otherwise {@code false}
   *
   * @see #isAssigned()
   * @see #assign(int, int)
   * @see #assign(int[])
   * @see #unassign(int)
   * @see #unassign()
   */
  public boolean isAssigned(@Keycode int keycode) {
    for (int assignedKeycode : assignments) {
      switch (assignedKeycode) {
        case NOT_MAPPED:
          return false;
        default:
          if (keycode == assignedKeycode) {
            return true;
          }
      }
    }

    return false;
  }

  /**
   * Returns whether or not any of the {@linkplain #getAssignments() mappings} for this {@code
   * MappedKey} are pressed.
   *
   * @return {@code true} if any of the mappings are pressed, otherwise {@code false}
   */
  public boolean isPressed() {
    return pressed > 0;
  }

  /**
   * Sets whether or not this {@code MappedKey} is pressed. A {@code MappedKey} is pressed if any
   * of its {@linkplain #getAssignments mapped keys} are pressed. In this case, this method
   * will forward the {@link StateListener} events with {@code keycode} as the pressed assignment.
   * <p>
   * Note: No validation is performed to check if {@code keycode} is {@linkplain #getAssignments
   *       mapped} to this {@code MappedKey}, that is the responsibility of the caller.
   * <p>
   * Note: For each pressing of {@code keycode} there must also be a corresponding depress. Failing
   *       to due so will invalidate the state of this {@code MappedKey}.
   *
   * @param keycode The assigned {@link Keycode} being pressed
   * @param pressed {@code true} to set this {@code MappedKey} as pressed, otherwise {@code false}
   *
   * @see #isPressed
   * @see #isAssigned(int)
   */
  /*package*/ void setPressed(@Keycode int keycode, boolean pressed) {
    assert isAssigned(keycode);
    if (pressed) {
      this.pressed++;
      for (StateListener l : STATE_LISTENERS) {
        l.onPressed(this, keycode);
      }
    } else {
      this.pressed--;
      for (StateListener l : STATE_LISTENERS) {
        l.onDepressed(this, keycode);
      }
    }
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
      for (@Assignment int i = 0; i < assignments.length; i++) {
        @Keycode int keycode = assignments[i];
        if (keycode == NOT_MAPPED) {
          break;
        }

        l.onFirstAssignment(this, i, keycode);
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

  /**
   * Interface used as a callback for assignment events.
   *
   * @see #assign(int, int)
   * @see #assign(int[])
   * @see #unassign()
   * @see #unassign(int)
   * @see #addAssignmentListener
   * @see #removeAssignmentListener
   * @see #containsAssignmentListener
   */
  interface AssignmentListener {

    /**
     * Called when {@code keycode} is assigned to the {@code assignment} for the referenced
     * {@link MappedKey}.
     *
     * @param key        The {@code MappedKey} being assigned to
     * @param assignment The {@link Assignment} being assigned
     * @param keycode    The {@link Keycode} assigned
     *
     * @see #assign(int, int)
     * @see #assign(int[])
     */
    void onAssigned(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

    /**
     * Called when {@code keycode} is unassigned from the {@code assignment} for the referenced
     * {@link MappedKey}.
     *
     * @param key        The {@code MappedKey} being unassigned from
     * @param assignment The {@link Assignment} being unassigned
     * @param keycode    The {@link Keycode} unassigned
     *
     * @see #unassign(int)
     * @see #unassign()
     */
    void onUnassigned(@NonNull MappedKey key, @Assignment int assignment, @Keycode int keycode);

    /**
     * Called when this {@code AssignmentListener} is being initialized, once for each
     * {@link #getAssignments() assignment} for the referenced {@link MappedKey}.
     *
     * @param key        The {@code MappedKey} being assigned to
     * @param assignment The {@link Assignment} being assigned
     * @param keycode    The {@link Keycode} assigned
     *
     * @see #addAssignmentListener
     */
    void onFirstAssignment(@NonNull MappedKey key, @Assignment int assignment,
                           @Keycode int keycode);

  }

  /**
   * Interface used as a callback for key state change events.
   */
  @SuppressWarnings("EmptyMethod")
  interface StateListener {

    /**
     * Called when the referenced {@link MappedKey} is pressed.
     *
     * @param key     The {@code MappedKey} where the event took place
     * @param keycode The {@link Keycode} being pressed
     */
    void onPressed(@NonNull MappedKey key, @Keycode int keycode);

    /**
     * Called when the referenced {@link MappedKey} is depressed.
     *
     * @param key     The {@code MappedKey} where the event took place
     * @param keycode The {@link Keycode} being depressed
     */
    void onDepressed(@NonNull MappedKey key, @Keycode int keycode);

  }

}
