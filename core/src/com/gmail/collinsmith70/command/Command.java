package com.gmail.collinsmith70.command;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.validator.ValidationException;
import com.gmail.collinsmith70.validator.Validator;

import org.apache.commons.collections4.iterators.ArrayIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@SuppressWarnings({ "ConstantConditions", "unused" })
public class Command implements Validator {

  @NonNull
  public static <T> Builder builder() {
    return new Builder();
  }

  @NonNull
  private static final String[] EMPTY_ARGS = new String[0];

  @NonNull
  private static final Parameter[] EMPTY_PARAMS = new Parameter[0];

  @NonNull
  /*package*/ final String ALIAS;

  @NonNull
  /*package*/ final String DESCRIPTION;

  @NonNull
  /*package*/ final Parameter[] PARAMS;

  @NonNull
  /*package*/ final Action ACTION;

  @IntRange(from = 0)
  private final int MINIMUM_ARGS;

  @NonNull
  private final Set<AssignmentListener> ASSIGNMENT_LISTENERS;

  @NonNull
  /*package*/ Set<String> aliases;

  public Command(@NonNull Builder builder) {
    this.ALIAS = Preconditions.checkNotNull(builder.alias, "Commands must have at least one alias");
    this.DESCRIPTION = Strings.nullToEmpty(builder.description);
    this.PARAMS = MoreObjects.firstNonNull(builder.params, EMPTY_PARAMS);
    this.ACTION = MoreObjects.firstNonNull(builder.action, Action.DO_NOTHING);
    this.MINIMUM_ARGS = PARAMS == EMPTY_PARAMS ? 0 : calculateMinimumArgs(PARAMS);
    this.ASSIGNMENT_LISTENERS = new CopyOnWriteArraySet<>();
    this.aliases = builder.aliases;
  }

  private int calculateMinimumArgs(@NonNull Parameter[] params) {
    int minimumParams = 0;
    boolean forceOptional = false;
    for (Parameter param : params) {
      if (!(param instanceof OptionalParameter)) {
        minimumParams++;
      } else if (forceOptional) {
        throw new IllegalArgumentException(
            "no required parameters may appear after the first optional parameter");
      } else {
        forceOptional = true;
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
    if (aliases == null) {
      return Collections.emptySet();
    }

    return ImmutableSet.<String>builder().add(ALIAS).addAll(aliases).build();
  }

  @NonNull
  public Parameter getParam(@IntRange(from = 0) int index) {
    return PARAMS[index];
  }

  @IntRange(from = 0)
  public int numArgs() {
    return PARAMS.length;
  }

  @IntRange(from = 0)
  public int minArgs() {
    return MINIMUM_ARGS;
  }

  @NonNull
  private String getParametersHint() {
    StringBuilder sb = new StringBuilder();
    for (Parameter param : PARAMS) {
      sb.append(param);
      sb.append(' ');
    }

    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }

  @Override
  @NonNull
  public String toString() {
    if (MINIMUM_ARGS == 0) {
      return ALIAS;
    }

    return ALIAS + " " + getParametersHint();
  }

  @NonNull
  public Command addAlias(@NonNull String alias) {
    Preconditions.checkArgument(!alias.isEmpty(), "Aliases cannot be empty");
    if (aliases == null) {
      aliases = new CopyOnWriteArraySet<>();
    }

    aliases.add(alias);
    for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
      l.onAssigned(this, alias);
    }

    return this;
  }

  public boolean removeAlias(@NonNull String alias) {
    if (alias == null) {
      return false;
    }

    Preconditions.checkArgument(!alias.equals(ALIAS),
        "The primary alias cannot be removed");
    boolean unassigned = aliases.remove(alias);
    if (unassigned) {
      for (AssignmentListener l : ASSIGNMENT_LISTENERS) {
        l.onUnassigned(this, alias);
      }
    }

    return unassigned;
  }

  public boolean isAlias(@Nullable String alias) {
    return Objects.equal(ALIAS, alias) ||
        (alias != null && aliases != null && aliases.contains(alias));
  }

  public boolean addAssignmentListener(@NonNull AssignmentListener l) {
    Preconditions.checkArgument(l != null, "l cannot be null");
    boolean added = ASSIGNMENT_LISTENERS.add(l);
    if (added) {
      l.onAssigned(this, ALIAS);
      if (aliases != null) {
        for (String alias : aliases) {
          l.onAssigned(this, alias);
        }
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

  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    } else if (!(obj instanceof Instance)) {
      throw new ValidationException("obj is not a subclass of Command.Instance");
    }

    Instance instance = (Instance) obj;
    if (instance.numArgs() < MINIMUM_ARGS) {
      throw new ValidationException("Bad syntax, expected: " + this);
    }

    int numArgs = Math.min(instance.numArgs(), PARAMS.length);
    for (int i = 0; i < numArgs; i++) {
      Parameter param = PARAMS[i];
      if (param.canValidate()) {
        param.validate(instance.getArg(i));
      }
    }
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

  @SuppressWarnings("unused")
  public class Instance implements Iterable<String> {

    private final boolean compressed;

    @NonNull
    private final String ALIAS;

    @NonNull
    private final String[] ARGS;

    private Instance(@NonNull String alias) {
      this(alias, EMPTY_ARGS);
    }

    private Instance(@NonNull String alias, @Nullable String... args) {
      Preconditions.checkArgument(!alias.isEmpty(), "alias cannot be empty");
      this.ALIAS = alias;
      this.ARGS = MoreObjects.firstNonNull(args, EMPTY_ARGS);
      this.compressed = false;
    }

    private Instance(@NonNull String[] args) {
      Preconditions.checkArgument(args.length >= 1,
          "args should at least contain the alias of the command instance as index 0");
      Preconditions.checkArgument(!args[0].isEmpty(), "alias cannot be empty");
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

    @SuppressWarnings("unchecked")
    public <T> T deserializeArg(int i) {
      try {
        String arg = getArg(i);
        return (T) PARAMS[i].deserialize(arg);
      } catch (SerializeException e) {
        throw e;
      } catch (Exception e) {
        throw new SerializeException(e);
      }
    }

    @IntRange(from = 0)
    public int numArgs() {
      return compressed ? ARGS.length - 1 : ARGS.length;
    }

    @Override
    @NonNull
    public Iterator<String> iterator() {
      return new ArrayIterator<>(ARGS, compressed ? 1 : 0);
    }

    public void execute() {
      validate(this);
      ACTION.onExecuted(this);
    }

  }

  public static class Builder {

    @Nullable
    private String alias;

    @Nullable
    private String description;

    @Nullable
    private Set<String> aliases;

    @Nullable
    private Parameter[] params;

    @Nullable
    private Action action;

    private Builder() {}

    @NonNull
    public Builder alias(@NonNull String alias) {
      Preconditions.checkArgument(!alias.isEmpty(), "Aliases cannot be empty");
      if (this.alias == null) {
        this.alias = alias;
      } else {
        if (aliases == null) {
          aliases = new CopyOnWriteArraySet<>();
        }

        aliases.add(alias);
      }

      return this;
    }

    @NonNull
    public Builder description(@NonNull String description) {
      this.description = Preconditions.checkNotNull(description);
      return this;
    }

    @NonNull
    public Builder params(@NonNull Parameter... params) {
      this.params = Preconditions.checkNotNull(params);
      return this;
    }

    @NonNull
    public Builder action(@NonNull Action action) {
      this.action = Preconditions.checkNotNull(action);
      return this;
    }

    @NonNull
    public Command build() {
      return new Command(this);
    }

  }

}
