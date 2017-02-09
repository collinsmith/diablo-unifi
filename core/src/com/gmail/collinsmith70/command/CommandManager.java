package com.gmail.collinsmith70.command;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.HashSet;
import java.util.SortedMap;

@SuppressWarnings({ "ConstantConditions", "unused", "SameReturnValue", "UnusedReturnValue",
    "WeakerAccess" })
public class CommandManager implements Command.AssignmentListener {

  @NonNull
  private final Trie<String, Command> COMMANDS;

  public CommandManager() {
    this.COMMANDS = new PatriciaTrie<>();
  }

  @NonNull
  public Command create(@NonNull String alias, @NonNull String description,
                        @Nullable Action action, @Nullable Parameter... params) {
    Command command = new Command(alias, description, action, params);
    add(command);
    return command;
  }

  @NonNull
  public Collection<Command> getCommands() {
    return new HashSet<>(COMMANDS.values());
  }

  public boolean add(@NonNull Command command) {
    final Command queriedCommand = COMMANDS.get(command.getAlias());
    if (Objects.equal(command, queriedCommand)) {
      return false;
    } else if (queriedCommand != null) {
      throw new IllegalArgumentException(String.format(
          "A command with the alias %s is already being managed by this CommandManager",
          command.getAlias()));
    }

    for (String alias : command.ALIASES) {
      if (COMMANDS.containsKey(alias)) {
        throw new DuplicateCommandException(command, String.format(
            "A command with the alias %s is already registered. Command aliases must be unique!",
            alias));
      }
    }

    return command.addAssignmentListener(this);
  }

  @Override
  public void onAssigned(@NonNull Command command, @NonNull String alias) {
    COMMANDS.put(alias, command);
  }

  @Override
  public void onUnassigned(@NonNull Command command, @NonNull String alias) {
    unassign(alias);
  }

  private boolean unassign(@NonNull String alias) {
    Preconditions.checkArgument(alias != null, "alias cannot be null");
    return COMMANDS.remove(alias) != null;
  }

  private boolean unassign(@NonNull Command command, @NonNull String alias) {
    Preconditions.checkArgument(command != null, "command cannot be null");
    Preconditions.checkArgument(alias != null, "alias cannot be null");
    Command queriedCommand = COMMANDS.get(alias);
    if (Objects.equal(queriedCommand, command)) {
      COMMANDS.remove(alias);
    }

    return true;
  }

  public boolean remove(@Nullable Command command) {
    if (command == null) {
      return false;
    }

    boolean unassigned = false;
    for (String alias : command.ALIASES) {
      unassigned = unassigned || unassign(command, alias);
    }

    return unassigned;
  }

  @Nullable
  public Command get(@Nullable String alias) {
    return COMMANDS.get(alias);
  }

  @NonNull
  public SortedMap<String, Command> prefixMap(@NonNull String alias) {
    return COMMANDS.prefixMap(alias);
  }

  public boolean isManaging(@Nullable String alias) {
    return alias != null && COMMANDS.containsKey(alias);
  }

  public boolean isManaging(@Nullable Command command) {
    if (command == null) {
      return false;
    }

    Command value = COMMANDS.get(command.getAlias());
    return command.equals(value);
  }

  protected final void checkIfManaged(@NonNull Command command) {
    if (!isManaging(command)) {
      throw new UnmanagedCommandException(command, String.format(
          "Command %s is not managed by this CommandManager", command.getAlias()));
    }
  }

  @SuppressWarnings("unused")
  public static abstract class CommandManagerException extends RuntimeException {

    public final Command COMMAND;

    private CommandManagerException() {
      this(null, null);
    }

    private CommandManagerException(Command command) {
      this(command, null);
    }

    private CommandManagerException(String message) {
      this(null, message);
    }

    private CommandManagerException(Command command, String message) {
      super(message);
      this.COMMAND = command;
    }

    private Command getCommand() {
      return COMMAND;
    }

  }

  @SuppressWarnings("unused")
  public static class DuplicateCommandException extends CommandManagerException {

    private DuplicateCommandException() {
      this(null, null);
    }

    private DuplicateCommandException(@Nullable Command command) {
      this(command, null);
    }

    private DuplicateCommandException(@Nullable String message) {
      this(null, message);
    }

    private DuplicateCommandException(@Nullable Command command, @Nullable String message) {
      super(command, message);
    }

  }

  @SuppressWarnings("unused")
  public static class UnmanagedCommandException extends CommandManagerException {

    private UnmanagedCommandException() {
      this(null, null);
    }

    private UnmanagedCommandException(@Nullable Command command) {
      this(command, null);
    }

    private UnmanagedCommandException(@Nullable String message) {
      this(null, message);
    }

    private UnmanagedCommandException(@Nullable Command command, @Nullable String message) {
      super(command, message);
    }

  }

}
