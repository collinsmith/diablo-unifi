package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Strings;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.command.Parameter;
import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.validator.ValidationException;

import java.util.Iterator;
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
    if (buffer.length() == 0) {
      return 0;
    }

    char lastCh = buffer.charAt(buffer.length() - 1);
    if (args.length == 0) {
      return 0;
    } else if (args.length > 1) {
      Command command = COMMANDS.get(args[targetArg]);
      if (command == null) {
        return 0;
      }

      // args[0] is command, so params are offset by 1
      int targetParam = args.length - 2;
      if (lastCh == ' ' && command.hasParam(targetParam + 1)) {
        targetParam += 1;
      }

      Parameter param = command.getParam(targetParam);
      if (!param.canSuggest()) {
        return 0;
      }

      int suggestionsProvided = param.suggest(console, buffer, args, targetParam + 1);
      if (suggestionsProvided == 1) {
        console.buffer.append(' ');
      }

      return suggestionsProvided;
    } else if (lastCh == ' ') {
      return 0;
    }

    String arg = args[targetArg];
    SortedMap<String, Command> commands = COMMANDS.prefixMap(arg);
    switch (commands.size()) {
      case 0:
        return 0;
      case 1:
        String cmdAlias = commands.firstKey();
        console.buffer.append(cmdAlias, arg.length());
        console.buffer.append(' ');
        return 1;
      default:
        final Set<String> commandAliases = commands.keySet();

        String commonPrefix = null;
        for (String alias : commandAliases) {
          if (commonPrefix == null) {
            commonPrefix = alias;
          } else if (commonPrefix.isEmpty()) {
            break;
          } else {
            commonPrefix = Strings.commonPrefix(commonPrefix, alias);
          }
        }

        if (commonPrefix != null && commonPrefix.length() > arg.length()) {
          console.buffer.append(commonPrefix, arg.length());
        } else {
          int i = 0;
          StringBuilder sb = new StringBuilder(64);
          for (Iterator<String> it = commandAliases.iterator(); it.hasNext(); ) {
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
          }
        }

        return commands.size();
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
