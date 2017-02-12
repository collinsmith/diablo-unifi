package com.gmail.collinsmith70.cvar;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gmail.collinsmith70.serializer.BooleanStringSerializer;
import com.gmail.collinsmith70.serializer.ByteStringSerializer;
import com.gmail.collinsmith70.serializer.CharacterStringSerializer;
import com.gmail.collinsmith70.serializer.DoubleStringSerializer;
import com.gmail.collinsmith70.serializer.FloatStringSerializer;
import com.gmail.collinsmith70.serializer.IntegerStringSerializer;
import com.gmail.collinsmith70.serializer.LongStringSerializer;
import com.gmail.collinsmith70.serializer.SerializeException;
import com.gmail.collinsmith70.serializer.ShortStringSerializer;
import com.gmail.collinsmith70.serializer.StringSerializer;
import com.gmail.collinsmith70.serializer.StringStringSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SaveableCvarManager extends CvarManager {

  @NonNull
  private final Map<Class, StringSerializer> SERIALIZERS;

  private boolean autosave;

  public SaveableCvarManager() {
    this(true);
  }

  public SaveableCvarManager(boolean autosave) {
    this.autosave = autosave;
    this.SERIALIZERS = new ConcurrentHashMap<>();
    SERIALIZERS.put(Character.class, CharacterStringSerializer.INSTANCE);
    SERIALIZERS.put(Boolean.class, BooleanStringSerializer.INSTANCE);
    SERIALIZERS.put(Byte.class, ByteStringSerializer.INSTANCE);
    SERIALIZERS.put(Short.class, ShortStringSerializer.INSTANCE);
    SERIALIZERS.put(Integer.class, IntegerStringSerializer.INSTANCE);
    SERIALIZERS.put(Long.class, LongStringSerializer.INSTANCE);
    SERIALIZERS.put(Float.class, FloatStringSerializer.INSTANCE);
    SERIALIZERS.put(Double.class, DoubleStringSerializer.INSTANCE);
    SERIALIZERS.put(String.class, StringStringSerializer.INSTANCE);
  }

  public boolean isAutosaving() {
    return autosave;
  }

  public void setAutosaving(boolean autosave) {
    if (this.autosave != autosave) {
      this.autosave = autosave;
      if (autosave) {
        saveAll();
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean add(@NonNull Cvar cvar) {
    //noinspection finally
    try {
      Object value = load(cvar);
      cvar.set(value);
    } catch (Throwable t) {
      Throwables.propagateIfPossible(t, SerializeException.class);
      throw new SerializeException(t);
    } finally {
      //noinspection ReturnInsideFinallyBlock
      return super.add(cvar);
    }
  }

  @Nullable
  public abstract <T> T load(@NonNull Cvar<T> cvar);

  public abstract <T> void save(@NonNull Cvar<T> cvar);

  @NonNull
  public Collection<RuntimeException> saveAll() {
    Collection<RuntimeException> exceptions = null;
    for (Cvar cvar : this) {
      try {
        save(cvar);
      } catch (RuntimeException e) {
        if (exceptions == null) {
          exceptions = new ArrayList<>(1);
        }

        exceptions.add(e);
      }
    }

    return MoreObjects.firstNonNull(exceptions,
        Collections.<RuntimeException>emptyList());
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {
    if (autosave) {
      save(cvar);
    }
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> StringSerializer<T> getSerializer(@NonNull Class<T> type) {
    Preconditions.checkArgument(type != null, "type cannot be null");
    return SERIALIZERS.get(type);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  public <T> StringSerializer<T> getSerializer(@NonNull Cvar<T> cvar) {
    return SERIALIZERS.get(cvar.TYPE);
  }

  public void putSerializer(@NonNull Class type, @Nullable StringSerializer serializer) {
    if (serializer == null) {
      SERIALIZERS.remove(type);
      return;
    }

    SERIALIZERS.put(type, serializer);
  }

  public void removeSerializer(@Nullable Class type) {
    SERIALIZERS.remove(type);
  }
}
