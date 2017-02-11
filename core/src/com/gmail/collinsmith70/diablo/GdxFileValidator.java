package com.gmail.collinsmith70.diablo;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.gmail.collinsmith70.libgdx.Console;
import com.gmail.collinsmith70.validator.ValidationException;
import com.gmail.collinsmith70.validator.Validator;

import java.util.ArrayList;
import java.util.List;

public enum GdxFileValidator implements Validator, Console.SuggestionProvider {
  INTERNAL(new InternalFileHandleResolver());

  @NonNull
  private final FileHandleResolver RESOLVER;

  GdxFileValidator(@NonNull FileHandleResolver resolver) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    } else if (!(obj instanceof String)) {
      throw new ValidationException("obj must be a String");
    }

    String file = (String) obj;
    FileHandle handle = RESOLVER.resolve(file);
    if (!handle.exists()) {
      throw new ValidationException("File not found!");
    }
  }

  @Override
  public boolean isValid(@Nullable Object obj) {
    try {
      validate(obj);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public int suggest(@NonNull Console console, @NonNull CharSequence buffer,
                     @NonNull String[] args) {
    String arg = args[args.length - 1];
    FileHandle handle = RESOLVER.resolve(Gdx.files.getLocalStoragePath());
    FileHandle[] children = handle.list(".fnt");
    List<FileHandle> matching = new ArrayList();
    for (FileHandle child : children) {
      if (child.name().startsWith(arg)) {
        matching.add(child);
      }
    }

    for (FileHandle match : matching) {
      console.println(match.name());
    }

    return 0;
  }
}
