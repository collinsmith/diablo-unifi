package com.gmail.collinsmith70.libgdx;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;
import com.gmail.collinsmith70.libgdx.util.ConsoleUtils;
import com.gmail.collinsmith70.util.StringUtils;

import java.util.Collection;

public enum CvarValueSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                     @NonNull String[] args, @IntRange(from = 0) int targetArg) {
    String alias = args[targetArg - 1];
    Cvar cvar = Diablo.client.cvars().get(alias);
    if (cvar == null) {
      throw new ParameterException(
          "A parameter of type %s must precede a parameter using CvarValueSuggester",
          Cvar.class.getName());
    }

    String arg = targetArg == args.length ? "" : args[targetArg];
    Collection<String> suggestions = cvar.suggest(arg);
    switch (suggestions.size()) {
      case 0:
        return 0;
      case 1:
        String suggestion = suggestions.iterator().next();
        console.buffer.append(suggestion, arg.length());
        return 1;
      default:
        String commonPrefix = StringUtils.commonPrefix(suggestions);
        if (commonPrefix.length() > arg.length()) {
          console.buffer.append(commonPrefix, arg.length());
        } else {
          ConsoleUtils.printList(console, suggestions, 4, 32);
        }

        return suggestions.size();
    }
  }
}
