package com.gmail.collinsmith70.old;

import android.support.annotation.NonNull;

import java.util.Set;

public abstract class SaveableKeyManager extends KeyManager {

  private boolean autosaving;

  public SaveableKeyManager() {
    this(true);
  }

  public SaveableKeyManager(boolean autosave) {
    super();
    this.autosaving = autosave;
  }

  public boolean isAutosaving() {
    return autosaving;
  }

  public void setAutosaving(boolean autosave) {
    if (this.autosaving == autosave) {
      return;
    }

    this.autosaving = autosave;
    if (autosave) {
      saveAll();
    }
  }

  @Override
  public Key add(@NonNull Key key) {
    Set<Integer> loadedAssignments = load(key);
    if (loadedAssignments.isEmpty()) {
      super.add(key);
    }

    return key;
  }

  public abstract Set<Integer> load(Key key);

  public abstract void save(Key key);

  protected abstract void commit(Key key);

  public void saveAll() {
    for (Key key : getKeys()) {
      save(key);
      commit(key);
    }
  }

}
