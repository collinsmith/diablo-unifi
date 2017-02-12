package com.gmail.collinsmith70.libgdx;

import com.google.common.base.MoreObjects;

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
  public <T> void save(@NonNull Cvar<T> cvar) {
    final String alias = cvar.getAlias();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG && !isManaging(cvar)) {
      throw new CvarManagerException("%s must be managed by this CvarManager", alias);
    }

    StringSerializer<T> serializer
        = MoreObjects.firstNonNull(cvar.getSerializer(), getSerializer(cvar));
    if (serializer == null) {
      throw new CvarManagerException("%s cannot be saved (no serializer found for %s)",
          alias, cvar.getType().getName());
    }

    final T value = cvar.get();
    final String serialization = serializer.serialize(value);
    PREFERENCES.putString(alias, serialization);
    PREFERENCES.flush();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s saved as \"%s\" (raw: \"%s\")",
          alias, value, serialization));
    }
  }

  @Nullable
  @Override
  public <T> T load(@NonNull Cvar<T> cvar) {
    final String alias = cvar.getAlias();
    StringSerializer<T> serializer
        = MoreObjects.firstNonNull(cvar.getSerializer(), getSerializer(cvar));
    if (serializer == null) {
      try {
        throw new CvarManagerException("%s cannot be loaded (no deserializer found for %s)",
            alias, cvar.getType().getName());
      } finally {
        return cvar.getDefault();
      }
    }

    String serialization = PREFERENCES.getString(alias);
    if (serialization == null) {
      return cvar.getDefault();
    }

    final T deserialization = serializer.deserialize(serialization);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s loaded as \"%s\" [%s] (raw: \"%s\")",
          alias, deserialization, deserialization.getClass().getName(), serialization));
    }

    return deserialization;
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s changed from %s to %s", cvar.getAlias(), from, to));
    }

    super.onChanged(cvar, from, to);
  }

}
