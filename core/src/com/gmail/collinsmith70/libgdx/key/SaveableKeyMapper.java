package com.gmail.collinsmith70.libgdx.key;

import android.support.annotation.NonNull;

public abstract class SaveableKeyMapper extends KeyMapper {

  private boolean autosaving;

  public SaveableKeyMapper() {
    this(true);
  }

  public SaveableKeyMapper(boolean autosave) {
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
  public boolean add(@NonNull MappedKey key) {
    int[] loadedAssignments = load(key);
    key.assign(loadedAssignments);
    return super.add(key);
  }

  public abstract int[] load(MappedKey key);

  public abstract void save(MappedKey key);

  protected abstract void commit(MappedKey key);

  public void saveAll() {
    for (MappedKey key : this) {
      save(key);
      commit(key);
    }
  }

}
