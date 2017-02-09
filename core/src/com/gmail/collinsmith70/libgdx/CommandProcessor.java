package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.validator.ValidationException;

public class CommandProcessor implements Console.Processor {

  private static final String TAG = "CommandProcessor";

  @NonNull
  private final CommandManager COMMANDS;

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

    try {
      cmd.newInstance(args).execute();
    } catch (ValidationException e) {
      String message = e.getMessage();
      if (message != null) {
        console.println(message);
      }
    } catch (Exception e) {
      Gdx.app.error(TAG, e.getMessage(), e);
    }

    return true;
  }

  @Override
  public void onUnprocessed(@NonNull Console console, @NonNull String buffer) {}
}
