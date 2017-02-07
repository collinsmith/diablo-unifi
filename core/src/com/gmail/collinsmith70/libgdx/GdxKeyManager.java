package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.gmail.collinsmith70.old.Key;
import com.gmail.collinsmith70.old.SaveableKeyManager;
import com.gmail.collinsmith70.libgdx.util.PropagatingInputProcessor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GdxKeyManager extends SaveableKeyManager {

  private static final String TAG = "GdxKeyManager";

  private final Preferences PREFERENCES;

  public GdxKeyManager() {
    this.PREFERENCES = Gdx.app.getPreferences(GdxKeyManager.class.getName());
  }

  @Override
  public Set<Integer> load(@NonNull Key key) {
    String serializedValue = PREFERENCES.getString(key.getAlias());
    if (serializedValue == null) {
      return Collections.emptySet();
    }

    if (!serializedValue.matches("\\[(\\d+,\\s)*\\d+\\]")) {
      Gdx.app.error(TAG, String.format("Error processing saved value for key %s [%s]: \"%s\"",
          key.getName(), key.getAlias(), serializedValue));
      return Collections.emptySet();
    }

    HashSet<Integer> assignments = new HashSet<>();
    assignments.clear();
    serializedValue = serializedValue.substring(1, serializedValue.length() - 1);
    for (String serializedKeycode : serializedValue.split(", ")) {
      assignments.add(Integer.parseInt(serializedKeycode));
    }

    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      String[] keycodeNames = new String[assignments.size()];
      int i = 0;
      for (int keycode : assignments) {
        keycodeNames[i++] = Input.Keys.toString(keycode);
      }

      Gdx.app.debug(TAG, String.format("%s [%s] loaded as %s",
          key.getName(), key.getAlias(), Arrays.toString(keycodeNames)));
    }

    return assignments;
  }

  @Override
  public void save(@NonNull Key key) {
    checkIfManaged(key);
    Set<Integer> assignmentsSet = key.getAssignments();
    Integer[] assignments = key.getAssignments().toArray(new Integer[assignmentsSet.size()]);
    String serializedValue = Arrays.toString(assignments);
    PREFERENCES.putString(key.getAlias(), serializedValue);

    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      String[] keycodeNames = new String[assignments.length];
      int i = 0;
      for (int keycode : assignments) {
        keycodeNames[i++] = Input.Keys.toString(keycode);
      }

      Gdx.app.debug(TAG, String.format("%s [%s] saved as %s",
          key.getName(), key.getAlias(), Arrays.toString(keycodeNames)));
    }
  }

  @Override
  protected void commit(@NonNull Key key) {
    checkIfManaged(key);
    PREFERENCES.flush();
    Gdx.app.debug(TAG, "Committing changes...");
  }

  @Override
  public void onAssigned(@NonNull Key key, int keycode) {
    super.onAssigned(key, keycode);
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, String.format("assigned [%s] to [%s]",
          Input.Keys.toString(keycode), key.getAlias()));
    }
  }

  @Override
  public void onUnassigned(@NonNull Key key, int keycode) {
    super.onUnassigned(key, keycode);
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

  private class InputProcessor extends PropagatingInputProcessor {

    public InputProcessor() {
      super();
    }

    public InputProcessor(@NonNull InputProcessor inputProcessor) {
      super(inputProcessor);
    }

    @Override
    public boolean keyDown(int keycode) {
      Key key = get(keycode);
      if (key != null) {
        key.setPressed(keycode, true);
      }

      return super.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
      Key key = get(keycode);
      if (key != null) {
        key.setPressed(keycode, false);
      }

      return super.keyUp(keycode);
    }

  }
}
