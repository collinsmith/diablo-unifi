package com.gmail.collinsmith70.libgdx;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;
import com.gmail.collinsmith70.libgdx.util.ConsoleUtils;
import com.gmail.collinsmith70.util.StringUtils;

import java.util.Set;
import java.util.SortedMap;

public enum CvarSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                     @NonNull String[] args, @IntRange(from = 0) int targetArg) {
    String arg = targetArg == args.length ? "" : args[targetArg];
    SortedMap<String, Cvar> cvars = Diablo.client.cvars().prefixMap(arg);
    switch (cvars.size()) {
      case 0:
        return 0;
      case 1:
        String alias = cvars.firstKey();
        console.buffer.append(alias, arg.length());
        return 1;
      default:
        Set<String> aliases = cvars.keySet();
        String commonPrefix = StringUtils.commonPrefix(aliases);
        if (commonPrefix.length() > arg.length()) {
          console.buffer.append(commonPrefix, arg.length());
        } else {
          ConsoleUtils.printList(console, aliases, 4, 36);
        }

        return aliases.size();
    }
  }
}
