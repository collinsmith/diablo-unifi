package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;

import java.util.Collection;

public enum CvarSuggestor implements Console.SuggestionProvider {
  INSTANCE;

  @Override
  public boolean suggest(@NonNull Console console, @NonNull CharSequence buffer,
                         @NonNull String[] args) {
    if (buffer.length() == 0) {
      return false;
    }

    Collection<Cvar> cvars = Diablo.client.cvars().search(args[args.length - 1]);
    switch (cvars.size()) {
      case 0:
        return false;
      case 1:
        Cvar singleCvar = cvars.iterator().next();
        console.setBuffer(buffer.toString() + singleCvar.getAlias());
        return true;
      default:
        for (Cvar cvar : cvars) {
          console.println(cvar.getAlias());
        }

        return true;
    }
  }
}
