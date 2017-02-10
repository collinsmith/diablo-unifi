package com.gmail.collinsmith70.diablo;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Action;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.command.Parameter;
import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.libgdx.CvarSuggestor;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.validator.ValidationException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({ "unused", "ConstantConditions" })
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
          Diablo.client.console.println("<> indicates required, [] indicates optional");
          for (Command cmd : Diablo.client.commands().getCommands()) {
            Diablo.client.console.println(cmd + " : " + cmd.getDescription());
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

  public static final Command cvars = new Command("cvars", "Prints all cvars and values",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          Collection<Cvar> cvars = Diablo.client.cvars().getCvars();
          for (Cvar cvar : cvars) {
            Diablo.client.console.println("%s \"%s\"; %s (Default: \"%s\")",
                cvar.getAlias(), cvar.getValue(), cvar.getDescription(), cvar.getDefaultValue());
          }
        }
      });

  public static final Command get = new Command("get", "Get the value of the specified cvar",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          String alias = instance.getArg(0);
          Cvar cvar = Diablo.client.cvars().get(alias);
          if (cvar == null) {
            throw new ParameterException(
                "Failed to find cvar by alias: %s. For a list of cvars type \"%s\"",
                alias, cvars.getAlias());
          }

          Diablo.client.console.println("%s = %s", cvar.getAlias(), cvar.getValue());
        }
      }, Parameter.of(Cvar.class).processor(CvarSuggestor.INSTANCE));

  public static final Command set = new Command("set", "Sets the value of the specified cvar",
      new Action() {
        @Override
        public void onExecuted(@NonNull Command.Instance instance) {
          String alias = instance.getArg(0);
          String value = instance.getArg(1);
          Cvar cvar = Diablo.client.cvars().get(alias);
          if (cvar == null) {
            throw new ParameterException("Failed to find cvar by alias: " + alias);
          }

          StringSerializer serializer = Diablo.client.cvars().getSerializer(cvar);
          try {
            cvar.setValue(value, serializer);
          } catch (SerializeException e) {
            throw new ParameterException("Invalid value specified: \"%s\". Expected type: %s",
                value, cvar.getType().getName());
          } catch (ValidationException e) {
            throw new ParameterException("Invalid value specified: \"%s\". %s",
                value, e.getMessage());
          }
        }
      }, Parameter.of(Cvar.class).processor(CvarSuggestor.INSTANCE), Parameter.of(String.class));

}
