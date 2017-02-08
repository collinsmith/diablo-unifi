package com.gmail.collinsmith70.diablo;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Action;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;

import java.lang.reflect.Field;

public class Commands {

  private static final String TAG = "Commands";

  public static void addTo(CommandManager commandManager) {
    addTo(commandManager, Commands.class);
  }

  private static void addTo(CommandManager commandManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
      if (Command.class.isAssignableFrom(field.getType())) {
        try {
          commandManager.add((Command) field.get(null));
        } catch (IllegalAccessException e) {
          Gdx.app.error(TAG, "Unable to access command: " + e.getMessage());
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(commandManager, subclass);
    }
  }

  private Commands() {
  }

  public static final Command help = new Command("help", "Displays this message",
      new Action() {
        @Override
        public void onActionExecuted(@NonNull Command.Instance instance) {
          for (Command cmd : Diablo.client.commands.getCommands()) {
            Diablo.client.console.println(cmd.getAlias() + " : " + cmd.getDescription());
          }
        }
      })
      .assign("?");

  public static final Command clear = new Command("clear", "Clears the console output",
      new Action() {
        @Override
        public void onActionExecuted(@NonNull Command.Instance instance) {
          Diablo.client.console.clear();
        }
      })
      .assign("cls");

  public static final Command exit = new Command("exit", "Exits the application",
      new Action() {
        @Override
        public void onActionExecuted(@NonNull Command.Instance instance) {
          Gdx.app.exit();
        }
      });

  public static final Command assign = new Command("assign", "Assigns the specified key",
      new Action() {
        @Override
        public void onActionExecuted(@NonNull Command.Instance instance) {
          throw new UnsupportedOperationException();
        }
      });

}
