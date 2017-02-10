package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.command.Parameter;
import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.validator.ValidationException;

import java.util.SortedMap;

import static com.gmail.collinsmith70.util.StringUtils.parseArgs;

public class CommandProcessor implements Console.Processor, Console.SuggestionProvider {

  private static final String TAG = "CommandProcessor";

  @NonNull
  private final CommandManager COMMANDS;

  public CommandProcessor(@NonNull CommandManager commandManager) {
    this.COMMANDS = commandManager;
  }

  @Override
  public boolean suggest(@NonNull Console console, @NonNull CharSequence buffer,
                         @NonNull String[] args) {
    if (buffer.length() == 0) {
      return false;
    }

    if (args.length == 0) {
      return false;
    } else if (args.length > 1) {
      Command command = COMMANDS.get(args[0]);
      if (command == null) {
        return false;
      }

      Parameter param = command.getParam(args.length - 2);
      if (!param.canSuggest()) {
        return false;
      }

      return param.suggest(console, buffer, args);
    }

    SortedMap<String, Command> commands = COMMANDS.prefixMap(args[0]);
    switch (commands.size()) {
      case 0:
        return false;
      case 1:
        console.setBuffer(commands.firstKey());
        console.keyTyped(' ');
        return true;
      default:
        for (String alias : commands.keySet()) {
          console.println(alias);
        }

        /*int i = 0;
        StringBuilder sb = new StringBuilder(64);
        for (Iterator<String> it = commands.keySet().iterator(); it.hasNext();) {
          String alias = it.next();
          if (++i % 6 == 0) {
            sb.append(alias);
            console.println(sb.toString());
            sb.setLength(0);
          } else if (it.hasNext()) {
            sb.append(Strings.padEnd(alias, 12, ' '));
          } else {
            sb.append(alias);
          }
        }

        if (sb.length() > 0) {
          console.println(sb.toString());
        }*/

        return true;
    }
  }

  @Override
  public boolean process(@NonNull Console console, @NonNull String buffer) {
    String[] args = parseArgs(buffer);
    Command cmd = COMMANDS.get(args[0]);
    if (cmd == null) {
      return false;
    }

    try {
      cmd.newInstance(args).execute();
    } catch (SerializeException|ValidationException|ParameterException e) {
      String message = e.getMessage();
      if (message != null) {
        console.println(message);
      }

      //Gdx.app.error(TAG, e.getClass().getName() + ": " + e.getMessage(), e);
    } catch (Exception e) {
      Gdx.app.error(TAG, e.getClass().getName() + ": " + e.getMessage(), e);
    }

    return true;
  }

  @Override
  public void onUnprocessed(@NonNull Console console, @NonNull String buffer) {}
}
