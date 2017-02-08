package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.StringSerializer;

public class CvarProcessor implements Console.Processor {

  @NonNull
  private static final String TAG = "CvarProcessor";

  @NonNull
  private final GdxCvarManager CVARS;

  public CvarProcessor(@NonNull GdxCvarManager cvarManager) {
    this.CVARS = cvarManager;
  }

  @Override
  @SuppressWarnings({ "unchecked", "ConstantConditions" })
  public boolean process(@NonNull Console console, @NonNull String buffer) {
    String[] args = buffer.split("\\s+");
    Cvar cvar = CVARS.get(args[0]);
    if (cvar != null) {
      switch (args.length) {
        case 1:
          console.println(cvar.getAlias() + " = " + cvar.getValue());
          break;
        case 2:
          String to = args[1];
          console.println(cvar.getAlias() + " = " + to);
          try {
            StringSerializer serializer = CVARS.getSerializer(cvar);
            cvar.setValue(to, serializer);
          } catch (SerializeException e) {
            console.println(String.format("Invalid value specified: \"%s\", Expected type: %s",
                to, cvar.getType().getName()));
            Gdx.app.error(TAG, e.getCause().getMessage(), e);
          }
      }
    }

    return true;
  }
}
