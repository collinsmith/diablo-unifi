package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Strings;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;

import java.util.Collection;
import java.util.Iterator;

public enum CvarSuggester implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                         @NonNull String[] args) {
    if (buffer.length() == 0) {
      return 0;
    }

    String arg = args[args.length - 1];
    Collection<Cvar> cvars = Diablo.client.cvars().search(arg);
    switch (cvars.size()) {
      case 0:
        return 0;
      case 1:
        Cvar singleCvar = cvars.iterator().next();
        String append = singleCvar.getAlias().substring(arg.length());
        console.appendToBuffer(append);
        return 1;
      default:
        String commonPrefix = null;
        for (Cvar cvar : cvars) {
          if (commonPrefix == null) {
            commonPrefix = cvar.getAlias();
          } else if (commonPrefix.isEmpty()) {
            break;
          } else {
            commonPrefix = Strings.commonPrefix(commonPrefix, cvar.getAlias());
          }
        }

        if (commonPrefix != null && commonPrefix.length() > arg.length()) {
          append = commonPrefix.substring(arg.length());
          console.appendToBuffer(append);
        } else {
          int i = 0;
          StringBuilder sb = new StringBuilder(64);
          for (Iterator<Cvar> it = cvars.iterator(); it.hasNext(); ) {
            Cvar cvar = it.next();
            String alias = cvar.getAlias();
            if (++i % 4 == 0) {
              sb.append(alias);
              console.println(sb.toString());
              sb.setLength(0);
            } else if (it.hasNext()) {
              sb.append(Strings.padEnd(alias, 36, ' '));
            } else {
              sb.append(alias);
            }
          }

          if (sb.length() > 0) {
            console.println(sb.toString());
          }
        }

        return cvars.size();
    }
  }
}
