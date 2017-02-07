package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.cvar.Cvar;

public class CvarProcessor implements Console.Processor {

  @NonNull
  private GdxCvarManager CVARS;

  public CvarProcessor(@NonNull GdxCvarManager cvarManager) {
    this.CVARS = cvarManager;
  }

  @Override
  public boolean process(@NonNull Console console, @NonNull String buffer) {
    String[] args = buffer.split("\\s+");
    Cvar<?> cvar = CVARS.get(args[0]);
    if (cvar != null) {
      switch (args.length) {
        case 1:
          console.println(cvar.getAlias() + " = " + cvar.getValue());
          break;
        case 2:
          String to = args[1];
          console.println(cvar.getAlias() + " = " + to);
          try {
            cvar.setValue(to, CVARS);
          } catch (Exception e) {
            console.println(String.format("Invalid value specified: \"%s\", Expected type: %s",
                to, cvar.getType().getName()));
          }
      }
    }

    return true;
  }
}
