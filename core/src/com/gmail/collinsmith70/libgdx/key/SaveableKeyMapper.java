package com.gmail.collinsmith70.libgdx.key;

import com.google.common.base.Throwables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.SerializeException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@SuppressWarnings({ "SameParameterValue", "WeakerAccess", "unused" })
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
  @SuppressWarnings({ "CaughtExceptionImmediatelyRethrown", "finally", "ReturnInsideFinallyBlock" })
  public boolean add(@NonNull MappedKey key) {
    try {
      int[] assignments = load(key);
      if (assignments != null) {
        switch (assignments.length) {
          case 0:
            key.unassign();
            break;
          case 1:
            key.unassign();
            key.assign(MappedKey.PRIMARY, assignments[0]);
            break;
          default:
            key.assign(assignments);
        }
      }
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, SerializeException.class);
      throw new SerializeException(t);
    } finally {
      return super.add(key);
    }
  }

  @Nullable
  public abstract int[] load(@NonNull MappedKey key);

  public abstract void save(@NonNull MappedKey key);

  public Collection<SerializeException> saveAll() {
    Collection<SerializeException> exceptions = null;
    for (MappedKey key : this) {
      try {
        save(key);
      } catch (SerializeException e) {
        if (exceptions == null) {
          exceptions = new ArrayList<>(1);
        }

        exceptions.add(e);
      }
    }

    if (exceptions == null) {
      exceptions = Collections.emptyList();
    }

    return exceptions;
  }

}
