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

  public static final Command exit = new Command("exit", "Exits the application", new Action() {
    @Override
    public void onActionExecuted(@NonNull Command.Instance instance) {
      Gdx.app.exit();
    }
  });

}
