package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.diablo.Diablo;
import com.gmail.collinsmith70.util.StringUtils;

import java.util.Collection;

public enum CvarProcessor implements Console.Processor {
  INSTANCE;

  @Override
  public boolean hint(@NonNull Console console, @NonNull CharSequence buffer) {
    if (buffer.length() == 0) {
      return false;
    }

    String[] args = StringUtils.parseArgs(buffer);
    Collection<Cvar> cvars = Diablo.client.cvars().search(args[args.length - 1]);
    switch (cvars.size()) {
      case 0:
        return false;
      case 1:
        Cvar singleCvar = cvars.iterator().next();
        console.setBuffer(buffer + singleCvar.getAlias());
        return true;
      default:
        for (Cvar cvar : cvars) {
          console.println(cvar.getAlias());
        }

        return true;
    }
  }

  @Override
  public boolean process(@NonNull Console console, @NonNull String buffer) {
    return false;
  }

  @Override
  public void onUnprocessed(@NonNull Console console, @NonNull String buffer) {}
}
