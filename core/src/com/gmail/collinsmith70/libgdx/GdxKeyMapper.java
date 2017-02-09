package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.ObjectSet;
import com.gmail.collinsmith70.libgdx.key.MappedKey;
import com.gmail.collinsmith70.libgdx.key.SaveableKeyMapper;
import com.gmail.collinsmith70.libgdx.util.PropagatingInputProcessor;
import com.gmail.collinsmith70.serializer.IntArrayStringSerializer;
import com.gmail.collinsmith70.serializer.SerializeException;

import java.util.Arrays;

@SuppressWarnings("unused")
public class GdxKeyMapper extends SaveableKeyMapper {

  private static final String TAG = "GdxKeyMapper";

  @NonNull
  private final Preferences PREFERENCES;

  public GdxKeyMapper() {
    this.PREFERENCES = Gdx.app.getPreferences(GdxKeyMapper.class.getName());
  }

  @Override
  @Nullable
  public int[] load(@NonNull MappedKey key) {
    String alias = key.getAlias();
    String serializedValue = PREFERENCES.getString(alias);
    if (serializedValue == null) {
      return null;
    }

    int[] assignments;
    try {
      assignments = IntArrayStringSerializer.INSTANCE.deserialize(serializedValue);
    } catch (SerializeException t) {
      Gdx.app.error(TAG, String.format("removing %s from preferences (invalid save format)",
          alias));
      PREFERENCES.remove(alias);
      throw t;
    }

    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      String[] keycodeNames = getKeycodeNames(assignments);
      Gdx.app.debug(TAG, String.format("%s [%s] loaded as %s (raw: %s)",
          key.getName(), key.getAlias(), Arrays.toString(keycodeNames)));
    }

    return assignments;
  }

  @Override
  public void save(@NonNull MappedKey key) {
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG && !isManaging(key)) {
      Gdx.app.debug(TAG, String.format("key %s is being saved by a key mapper not managing it",
          key));
    }

    int[] assignments = key.getAssignments();
    String serializedValue = IntArrayStringSerializer.INSTANCE.serialize(assignments);
    PREFERENCES.putString(key.getAlias(), serializedValue);
    PREFERENCES.flush();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      String[] keycodeNames = getKeycodeNames(assignments);
      Gdx.app.debug(TAG, String.format("%s [%s] saved as %s (raw: \"%s\")",
          key.getName(), key.getAlias(), Arrays.toString(keycodeNames), serializedValue));
    }
  }

  @NonNull
  private String[] getKeycodeNames(@NonNull int[] keycodes) {
    int i = 0;
    String[] keycodeNames = new String[keycodes.length];
    for (int keycode : keycodes) {
      if (keycode == MappedKey.NOT_MAPPED) {
        keycodeNames[i++] = "null(0)";
      } else {
        keycodeNames[i++] = Input.Keys.toString(keycode) + "(" + keycode + ")";
      }
    }

    return keycodeNames;
  }

  @Override
  public void onAssigned(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                         @MappedKey.Keycode int keycode) {
    super.onAssigned(key, assignment, keycode);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("assigned [%s] to [%s]",
          Input.Keys.toString(keycode), key.getAlias()));
    }
  }

  @Override
  public void onUnassigned(@NonNull MappedKey key, @MappedKey.Assignment int assignment,
                           @MappedKey.Keycode int keycode) {
    super.onUnassigned(key, assignment, keycode);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("unassigned [%s] from [%s]",
          Input.Keys.toString(keycode), key.getAlias()));
    }
  }

  @NonNull
  public com.badlogic.gdx.InputProcessor newInputProcessor() {
    return new InputProcessor();
  }

  @NonNull
  public com.badlogic.gdx.InputProcessor newInputProcessor(@NonNull InputProcessor inputProcessor) {
    return new InputProcessor(inputProcessor);
  }

  @SuppressWarnings("unused")
  private class InputProcessor extends PropagatingInputProcessor {

    public InputProcessor() {
      super();
    }

    public InputProcessor(@NonNull InputProcessor inputProcessor) {
      super(inputProcessor);
    }

    @Override
    public boolean keyDown(int keycode) {
      ObjectSet<MappedKey> keys = lookup(keycode);
      if (keys != null) {
        for (MappedKey key : keys) {
          setPressed(key, keycode, true);
        }
      }

      return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
      ObjectSet<MappedKey> keys = lookup(keycode);
      if (keys != null) {
        for (MappedKey key : keys) {
          setPressed(key, keycode, false);
        }
      }

      return super.keyUp(keycode);
    }

  }
}
