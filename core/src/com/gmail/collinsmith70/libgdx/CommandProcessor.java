package com.gmail.collinsmith70.libgdx;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.command.Parameter;
import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.libgdx.util.ConsoleUtils;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.util.StringUtils;
import com.gmail.collinsmith70.validator.ValidationException;

import java.util.Set;
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
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                     @NonNull String[] args, @IntRange(from = 0) int targetArg) {
    switch (args.length) {
      case 0:
        return 0;
      case 1: // Command /w no args
        String arg0 = args[0];
        SortedMap<String, Command> commands = COMMANDS.prefixMap(arg0);
        switch (commands.size()) {
          case 0:
            return 0;
          case 1:
            // check if current command is completed (' ' after it)
            char ch = buffer.charAt(buffer.length() - 1);
            if (ch == ' ') {
              // break into default (i.e., the command alias is complete, handle args)
              break;
            }

            String alias = commands.firstKey();
            console.buffer.append(alias, arg0.length());
            console.buffer.append(' ');
            return 1; // suggestion provided
          default:
            Set<String> aliases = commands.keySet();
            String commonPrefix = StringUtils.commonPrefix(aliases);
            if (commonPrefix.length() > arg0.length()) {
              console.buffer.append(commonPrefix, arg0.length());
            } else {
              ConsoleUtils.printList(console, aliases, 6, 12);
            }

            return commands.size();
        }
      default: // Command /w args (suggest args)
        Command command = COMMANDS.get(args[0]);
        if (command == null) {
          return 0;
        }

        // args are offset +1, so current arg is:
        int targetParam = args.length - 2;

        // check if current param is completed (' ' after it), in which case, our target is
        // really the next param
        char ch = buffer.charAt(buffer.length() - 1);
        if (ch == ' ' && command.hasParam(targetParam + 1)) {
          targetParam += 1;
        }

        Parameter param = command.getParam(targetParam);
        if (!param.canSuggest()) {
          return 0;
        }

        // apply args[] offset (targetParam is represented by args[targetParam + 1])
        int suggestions = param.suggest(console, buffer, args, targetParam + 1);
        if (suggestions == 1) {
          console.buffer.append(' ');
        }

        return suggestions;
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
