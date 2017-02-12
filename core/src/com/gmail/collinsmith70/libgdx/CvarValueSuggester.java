package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Strings;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.command.ParameterException;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;

import java.util.Collection;
import java.util.Iterator;

public enum CvarValueSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                     @NonNull String[] args, @IntRange(from = 0) int targetArg) {
    if (buffer.length() == 0) {
      return 0;
    }/* else if (buffer.charAt(buffer.length() - 1) == ' ') {
      return 0;
    }*/

    String alias = args[targetArg - 1];
    Cvar cvar = Diablo.client.cvars().get(alias);
    if (cvar == null) {
      throw new ParameterException("A parameter of type %s must precede a CvarValueSuggester",
          Cvar.class.getName());
    }

    String str = targetArg == args.length ? "" : args[targetArg];
    Collection<String> suggestions = cvar.suggest(str);
    switch (suggestions.size()) {
      case 0:
        return 0;
      case 1:
        String onlySuggestion = suggestions.iterator().next();
        String append = onlySuggestion.substring(str.length());
        console.appendToBuffer(append);
        return 1;
      default:
        String commonPrefix = null;
        for (String suggestion : suggestions) {
          if (commonPrefix == null) {
            commonPrefix = suggestion;
          } else if (commonPrefix.isEmpty()) {
            break;
          } else {
            commonPrefix = Strings.commonPrefix(commonPrefix, suggestion);
          }
        }

        if (commonPrefix != null && commonPrefix.length() > str.length()) {
          append = commonPrefix.substring(str.length());
          console.appendToBuffer(append);
        } else {
          int i = 0;
          StringBuilder sb = new StringBuilder(64);
          for (Iterator<String> it = suggestions.iterator(); it.hasNext(); ) {
            String suggestion = it.next();
            if (++i % 4 == 0) {
              sb.append(suggestion);
              console.println(sb.toString());
              sb.setLength(0);
            } else if (it.hasNext()) {
              sb.append(Strings.padEnd(suggestion, 32, ' '));
            } else {
              sb.append(suggestion);
            }
          }

          if (sb.length() > 0) {
            console.println(sb.toString());
          }
        }

        return suggestions.size();
    }
  }
}
