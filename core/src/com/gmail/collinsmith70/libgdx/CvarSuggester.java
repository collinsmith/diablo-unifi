package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Strings;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;

import java.util.Collection;

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

        if (commonPrefix != null && !commonPrefix.isEmpty()) {
          append = commonPrefix.substring(arg.length());
          console.appendToBuffer(append);
        } else {
          for (Cvar cvar : cvars) {
            console.println(cvar.getAlias());
          }
        }

        return cvars.size();
    }
  }
}
