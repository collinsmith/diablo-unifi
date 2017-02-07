package com.gmail.collinsmith70.command;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Command {

  private static final String[] EMPTY_ARGS = new String[0];

  private final String ALIAS;
  private final String DESCRIPTION;
  private final Set<String> ALIASES;
  private final Parameter[] PARAMS;
  private final Action ACTION;
  private final int MINIMUM_ARGS;

  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS;

  public Command(@NonNull String alias, @Nullable String description,
                 @Nullable Action action, @Nullable Parameter... params) {
    this.ALIAS = alias;
    this.DESCRIPTION = Strings.nullToEmpty(description);
    this.ALIASES = new CopyOnWriteArraySet<String>();
    this.ACTION = MoreObjects.firstNonNull(action, Action.DO_NOTHING);
    this.PARAMS = params;
    this.MINIMUM_ARGS = calculateMinimumArgs(params);

    this.ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<AssignmentListener>();

    ALIASES.add(alias);
  }

  private int calculateMinimumArgs(@NonNull Parameter[] params) {
    int minimumParams = 0;
    for (Parameter param : params) {
      if (!(param instanceof OptionalParameter)) {
        minimumParams++;
      }
    }

    return minimumParams;
  }

  @NonNull
  public String getAlias() {
    return ALIAS;
  }

  @NonNull
  public String getDescription() {
    return DESCRIPTION;
  }

  @NonNull
  public Set<String> getAliases() {
    return ImmutableSet.copyOf(ALIASES);
  }

  public int minArgs() {
    return MINIMUM_ARGS;
  }

  private String getParametersHint() {
    StringBuilder sb = new StringBuilder();
    for (Parameter param : PARAMS) {
      if (param instanceof OptionalParameter) {
        sb.append(String.format("[%s] ", param));
      } else {
        sb.append(String.format("<%s> ", param));
      }
    }

    return sb.toString().trim();
  }

  @Override
  public String toString() {
    if (MINIMUM_ARGS == 0) {
      return ALIAS;
    }

    return ALIAS + " " + getParametersHint();
  }

  public Command assign(@NonNull String alias) {
    Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be null");
    ALIASES.add(alias);
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      l.onAssigned(this, alias);
    }

    return this;
  }

  public boolean unassign(@Nullable String alias) {
    if (alias == null) {
      return false;
    }

    Preconditions.checkArgument(!alias.equals(ALIAS),
        "cannot unassign the primary alias of a command");
    boolean unassigned = ALIASES.remove(alias);
    if (unassigned) {
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, alias);
      }
    }

    return unassigned;
  }

  public boolean isAlias(@Nullable String alias) {
    return alias != null && ALIASES.contains(alias);
  }

  public boolean addAssignmentListener(@NonNull AssignmentListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = ASSIGNMENT_LISTENERS.add(l);
    if (added) {
      for (String alias : ALIASES) {
        l.onAssigned(this, alias);
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

  @NonNull
  public Instance newInstance(@NonNull String alias) {
    return new Instance(alias);
  }

  @NonNull
  public Instance newInstance(@NonNull String alias, @Nullable String... args) {
    return new Instance(alias, args);
  }

  @NonNull
  public Instance newInstance(@NonNull String[] args) {
    return new Instance(args);
  }

  public interface AssignmentListener {

    void onAssigned(@NonNull Command command, @NonNull String alias);

    void onUnassigned(@NonNull Command command, @NonNull String alias);

  }

  public class Instance implements Iterable<String> {

    private final boolean compressed;

    @NonNull
    private final String ALIAS;

    @NonNull
    private final String[] ARGS;

    private Instance(@NonNull String alias) {
      Preconditions.checkArgument(!alias.isEmpty(), "Instance alias cannot be empty");
      this.ALIAS = alias;
      this.ARGS = EMPTY_ARGS;
      this.compressed = false;
    }

    private Instance(@NonNull String alias, @Nullable String... args) {
      Preconditions.checkArgument(!alias.isEmpty(), "Instance alias cannot be empty");
      this.ALIAS = alias;
      this.ARGS = MoreObjects.firstNonNull(args, EMPTY_ARGS);
      this.compressed = false;
    }

    private Instance(@NonNull String[] args) {
      Preconditions.checkArgument(args.length >= 1);
      this.ALIAS = args[0];
      this.ARGS = args;
      this.compressed = true;
    }

    @NonNull
    public String getAlias() {
      return ALIAS;
    }

    @NonNull
    public String getArg(int i) {
      return ARGS[compressed ? i + 1 : i];
    }

    public int numArgs() {
      return compressed ? ARGS.length - 1 : ARGS.length;
    }

    @Override
    public Iterator<String> iterator() {
      return new ArrayIterator<String>(ARGS);
    }

    @NonNull
    public void execute() {
      ACTION.onActionExecuted(this);
    }

  }

}
