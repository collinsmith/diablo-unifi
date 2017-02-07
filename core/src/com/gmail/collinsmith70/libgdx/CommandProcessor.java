package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;

public class CommandProcessor implements Console.Processor {

  private static final String TAG = "CommandProcessor";

  @NonNull
  private CommandManager COMMANDS;

  public CommandProcessor(@NonNull CommandManager commandManager) {
    this.COMMANDS = commandManager;
  }

  @Override
  public boolean process(@NonNull Console console, @NonNull String buffer) {
    String[] args = buffer.split("\\s+");
    Command cmd = COMMANDS.get(args[0]);
    if (cmd == null) {
      return false;
    }

    if (cmd.minArgs() <= args.length - 1) {
      cmd.newInstance(args).execute();
    } else {
      console.println(String.format("Bad syntax, expected \"%s\"", cmd));
    }

    return true;
  }
}
