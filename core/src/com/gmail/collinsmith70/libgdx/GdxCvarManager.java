package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SaveableCvarManager;
import com.gmail.collinsmith70.serializer.StringSerializer;

public class GdxCvarManager extends SaveableCvarManager {

  private static final String TAG = "GdxCvarManager";

  private final Preferences PREFERENCES;

  public GdxCvarManager() {
    super();
    this.PREFERENCES = Gdx.app.getPreferences(GdxCvarManager.class.getName());
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public <T> void save(@NonNull Cvar<T> cvar) {
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG && !isManaging(cvar)) {
      Gdx.app.debug(TAG, String.format("cvar %s is being saved by a cvar manager not managing it",
          cvar));
    }

    StringSerializer<T> serializer = getSerializer(cvar);
    if (serializer == null) {
      Gdx.app.error(TAG, String.format("cvar %s cannot be saved (no serializer found for %s)",
          cvar.getAlias(), cvar.getType().getName()));
      return;
    }

    PREFERENCES.putString(cvar.getAlias(), serializer.serialize(cvar.getValue()));
    PREFERENCES.flush();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s saved as \"%s\" [%s]",
          cvar.getAlias(), cvar.getValue(), cvar.getType().getName()));
    }
  }

  @Nullable
  @Override
  public <T> T load(@NonNull Cvar<T> cvar) {
    StringSerializer<T> serializer = getSerializer(cvar);
    if (serializer == null) {
      Gdx.app.error(TAG, String.format("cvar %s cannot be loaded (no deserializer found for %s)",
          cvar.getAlias(), cvar.getType().getName()));
      return cvar.getDefaultValue();
    }

    String serializedValue = PREFERENCES.getString(cvar.getAlias());
    if (serializedValue == null) {
      return cvar.getDefaultValue();
    }

    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s loaded as \"%s\" [%s]",
          cvar.getAlias(), serializedValue, cvar.getType().getName()));
    }

    return serializer.deserialize(serializedValue);
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {
    super.onChanged(cvar, from, to);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("changed %s from %s to %s", cvar.getAlias(), from, to));
    }
  }

}
