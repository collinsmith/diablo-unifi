package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.gmail.collinsmith70.cvar.SuggestionProvider;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;

public class GdxFileSuggester implements SuggestionProvider {

  @NonNull
  private final FileHandleResolver RESOLVER;

  @Nullable
  private final FilenameFilter FILTER;

  public GdxFileSuggester(@NonNull FileHandleResolver resolver) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = null;
  }

  public GdxFileSuggester(@NonNull FileHandleResolver resolver, @NonNull FilenameFilter filter) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = Preconditions.checkNotNull(filter, "filter cannot be null");
  }

  @Override
  public Collection<String> suggest(@NonNull String str) {
    FileHandle handle = RESOLVER.resolve(Gdx.files.getLocalStoragePath());
    FileHandle[] children = handle.list();
    Collection<String> matching = new ArrayList<>(children.length);
    for (FileHandle child : children) {
      String fileName = child.name();
      if (!fileName.startsWith(str)) {
        continue;
      }

      if (FILTER == null || FILTER.accept(child.parent().file(), fileName)) {
        matching.add(fileName);
      }
    }

    return matching;
  }
}
