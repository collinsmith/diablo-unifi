package com.gmail.collinsmith70.cvar;

import com.google.common.base.Objects;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;

public class CvarManager implements Cvar.StateListener, Iterable<Cvar> {

  @NonNull
  private final Trie<String, Cvar> CVARS;

  public CvarManager() {
    this.CVARS = new PatriciaTrie<>();
  }

  public Collection<Cvar> getCvars() {
    return CVARS.values();
  }

  @Override
  public Iterator<Cvar> iterator() {
    return CVARS.values().iterator();
  }

  @SuppressWarnings("unchecked")
  public boolean add(@NonNull Cvar cvar) {
    final String alias = cvar.ALIAS;
    final Cvar queriedCvar = CVARS.get(alias);
    if (Objects.equal(queriedCvar, cvar)) {
      return false;
    } else if (queriedCvar != null) {
      throw new CvarManagerException("A Cvar with the alias %s has already been added",
          queriedCvar.ALIAS);
    }

    CVARS.put(alias, cvar);
    cvar.addStateListener(this);
    return true;
  }

  public boolean remove(@Nullable Cvar cvar) {
    if (cvar == null) {
      return false;
    }

    final String alias = cvar.ALIAS;
    final Cvar queriedCvar = CVARS.get(alias);
    return Objects.equal(queriedCvar, cvar)
        && CVARS.remove(alias) != null;
  }

  @SuppressWarnings("unchecked")
  public <T> Cvar<T> get(@Nullable String alias) {
    if (alias == null) {
      return null;
    }

    return (Cvar<T>) CVARS.get(alias);
  }

  @NonNull
  public SortedMap<String, Cvar> prefixMap(@Nullable String alias) {
    return CVARS.prefixMap(alias);
  }

  public boolean isManaging(@Nullable Cvar cvar) {
    if (cvar == null) {
      return false;
    }

    final String alias = cvar.ALIAS;
    final Cvar queriedCvar = CVARS.get(alias);
    return Objects.equal(queriedCvar, cvar);
  }

  public boolean isManaging(@Nullable String alias) {
    return alias != null && CVARS.containsKey(alias);
  }

  @Override
  public void onChanged(@NonNull Cvar cvar, @Nullable Object from, @Nullable Object to) {}

  @Override
  public void onLoaded(@NonNull Cvar cvar, @Nullable Object to) {}

  public static class CvarManagerException extends RuntimeException {

    public CvarManagerException() {
      super();
    }

    public CvarManagerException(@Nullable String reason) {
      super(reason);
    }

    public CvarManagerException(@Nullable String format, @Nullable Object... args) {
      super(String.format(format, args));
    }

  }

}
