package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SaveableCvarManager;

public class GdxCvarManager extends SaveableCvarManager {

  private static final String TAG = "GdxCvarManager";

  private final Preferences PREFERENCES;

  public GdxCvarManager() {
    super();
    this.PREFERENCES = Gdx.app.getPreferences(GdxCvarManager.class.getName());
  }

  @Override
  public <T> void save(@NonNull Cvar<T> cvar) {
    if (!isManaging(cvar)) {
      throw new IllegalArgumentException(String.format("Cvar %s is not managed by this CvarManager",
          cvar.getAlias()));
    }

    if (!containsSerializer(cvar)) {
      Gdx.app.error(TAG, String.format("Cvar %s cannot be saved (no serializer found)",
          cvar.getAlias()));
      return;
    }

    PREFERENCES.putString(cvar.getAlias(), getSerializer(cvar).serialize(cvar.getValue()));
    PREFERENCES.flush();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s saved as %s [%s]",
          cvar.getAlias(), cvar.getValue(), cvar.getType().getName()));
    }
  }

  @Override
  public <T> T load(@NonNull Cvar<T> cvar) {
    if (!isManaging(cvar)) {
      throw new IllegalArgumentException(String.format("Cvar %s is not managed by this CvarManager",
          cvar.getAlias()));
    }

    String serializedValue = PREFERENCES.getString(cvar.getAlias());
    if (serializedValue == null || serializedValue.isEmpty()) {
      serializedValue = getSerializer(cvar).serialize(cvar.getDefaultValue());
    }

    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("%s loaded as %s [%s]",
          cvar.getAlias(), serializedValue, cvar.getType().getName()));
    }

    return getSerializer(cvar).deserialize(serializedValue);
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {
    super.onChanged(cvar, from, to);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("Changed %s from %s to %s", cvar.getAlias(), from, to));
    }
  }

}
