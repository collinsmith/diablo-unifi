package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Strings;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.validator.ValidationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandProcessor implements Console.Processor {

  private static final String TAG = "CommandProcessor";

  private static final Pattern PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  @NonNull
  private final CommandManager COMMANDS;

  public CommandProcessor(@NonNull CommandManager commandManager) {
    this.COMMANDS = commandManager;
  }

  @Override
  public boolean hint(@NonNull Console console, @NonNull CharSequence buffer) {
    if (buffer.length() == 0) {
      return false;
    }

    String[] args = parseArgs(buffer);
    if (args.length == 0) {
      return false;
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
        int i = 0;
        StringBuilder sb = new StringBuilder(64);
        for (Iterator<String> it = commands.keySet().iterator(); it.hasNext();) {
          String alias = it.next();
          if (++i % 6 == 0) {
            sb.append(alias);
            sb.append('\n');
          } else if (it.hasNext()) {
            sb.append(Strings.padEnd(alias, 12, ' '));
          } else {
            sb.append(alias);
          }
        }

        console.println(sb.toString());
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
    } catch (SerializeException|ValidationException e) {
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

  private String[] parseArgs(@NonNull CharSequence buffer) {
    String tmp;
    Collection<String> args = new ArrayList<>(8);
    Matcher matcher = PATTERN.matcher(buffer);
    while (matcher.find()) {
      if ((tmp = matcher.group(1)) != null) {
        // Add double-quoted string without the quotes
        args.add(tmp);
      } else if ((tmp = matcher.group(2)) != null) {
        // Add single-quoted string without the quotes
        args.add(tmp);
      } else {
        // Add unquoted word
        args.add(matcher.group());
      }
    }

    return args.toArray(new String[args.size()]);
  }

  @Override
  public void onUnprocessed(@NonNull Console console, @NonNull String buffer) {}
}
