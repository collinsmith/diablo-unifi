package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.SortedMap;

public class CommandManager {

  private final Trie<String, Command> COMMANDS;

  public CommandManager() {
    this.COMMANDS = new PatriciaTrie<Command>();
  }

  @NonNull
  public Command create(@NonNull String alias, @Nullable String description,
                        @Nullable Action action, @Nullable Parameter... params) {
    Command command = new Command(alias, description, action, params);
    add(command);
    return command;
  }

  @NonNull
  public Collection<Command> getCommands() {
    return new HashSet<>(COMMANDS.values());
  }

  @NonNull
  public boolean add(@NonNull Command command) {
    if (isManaging(command)) {
      return false;
    } else {
      for (String alias : command.getAliases()) {
        if (isManaging(alias)) {
          throw new DuplicateCommandException(command, String.format(
              "A command with the alias %s is already registered. Command aliases must be unique!",
              alias));
        }
      }
    }

    command.addAssignmentListener(new Command.AssignmentListener() {
      @Override
      public void onAssigned(@NonNull Command command, @NonNull String alias) {
        Gdx.app.debug("CommandManager", "assigning \"" + alias.toLowerCase() + "\" to " + command);
        COMMANDS.put(alias.toLowerCase(), command);
      }

      @Override
      public void onUnassigned(@NonNull Command command, @NonNull String alias) {
        unassign(command, alias);
      }
    });

    return true;
  }

  private boolean unassign(@NonNull Command command, @NonNull String alias) {
    alias = alias.toLowerCase();
    Object curValue = COMMANDS.get(alias);
    if (Objects.equals(curValue, command)) {
      Gdx.app.debug("CommandManager", "unassigning " + alias + " from " + command);
      COMMANDS.remove(alias);
    }

    return true;
  }

  public boolean remove(@Nullable Command command) {
    if (!isManaging(command)) {
      return false;
    }

    for (String alias : command.getAliases()) {
      unassign(command, alias);
    }

    return true;
  }

  @Nullable
  public Command get(@Nullable String alias) {
    alias = alias.toLowerCase();
    return COMMANDS.get(alias);
  }

  @NonNull
  public SortedMap<String, Command> prefixMap(@NonNull String alias) {
    alias = alias.toLowerCase();
    return COMMANDS.prefixMap(alias);
  }

  public boolean isManaging(@Nullable String alias) {
    return alias != null && COMMANDS.containsKey(alias.toLowerCase());
  }

  public boolean isManaging(@Nullable Command command) {
    if (command == null) {
      return false;
    }

    Command value = COMMANDS.get(command.getAlias().toLowerCase());
    return command.equals(value);
  }

  protected final void checkIfManaged(@NonNull Command command) {
    if (!isManaging(command)) {
      throw new UnmanagedCommandException(command, String.format(
          "Command %s is not managed by this CommandManager", command.getAlias()));
    }
  }

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
