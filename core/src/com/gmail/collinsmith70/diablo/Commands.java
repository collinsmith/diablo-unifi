package com.gmail.collinsmith70.diablo;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Action;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class Commands {

  public static List<Throwable> addTo(CommandManager commandManager) {
    return addTo(commandManager, Commands.class, new ArrayList<Throwable>(0));
  }

  private static List<Throwable> addTo(CommandManager commandManager, Class<?> clazz,
                                       List<Throwable> throwables) {
    for (Field field : clazz.getFields()) {
      if (Command.class.isAssignableFrom(field.getType())) {
        try {
          commandManager.add((Command) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(commandManager, subclass, throwables);
    }

    return throwables;
  }

  private Commands() {
  }

  public static final Command help = new Command("help", "Displays this message",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          for (Command cmd : Diablo.client.commands.getCommands()) {
            Diablo.client.console.println(cmd.getAlias() + " : " + cmd.getDescription());
          }
        }
      })
      .addAlias("?");

  public static final Command clear = new Command("clear", "Clears the console output",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          Diablo.client.console.clear();
        }
      })
      .addAlias("cls");

  public static final Command exit = new Command("exit", "Exits the application",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          Gdx.app.exit();
        }
      });

  public static final Command assign = new Command("assign", "Assigns the specified key",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          throw new UnsupportedOperationException();
        }
      });

}
