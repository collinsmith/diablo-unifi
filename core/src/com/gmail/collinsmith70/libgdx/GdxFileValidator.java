package com.gmail.collinsmith70.libgdx;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.gmail.collinsmith70.validator.ValidationException;
import com.gmail.collinsmith70.validator.Validator;

import java.io.File;
import java.io.FilenameFilter;

public class GdxFileValidator implements Validator {

  @NonNull
  private final FileHandleResolver RESOLVER;

  @Nullable
  private final FilenameFilter FILTER;

  public GdxFileValidator(@NonNull FileHandleResolver resolver) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = null;
  }

  public GdxFileValidator(@NonNull FileHandleResolver resolver, @NonNull FilenameFilter filter) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "resolver cannot be null");
    this.FILTER = Preconditions.checkNotNull(filter, "filter cannot be null");
  }

  @Override
  public void validate(@Nullable Object obj) {
    if (obj == null) {
      throw new ValidationException("obj cannot be null");
    } else if (!(obj instanceof String)) {
      throw new ValidationException("obj must be a String");
    }

    String fileName = (String) obj;
    FileHandle handle = RESOLVER.resolve(fileName);
    if (!handle.exists()) {
      throw new ValidationException("File not found!");
    }

    File file = handle.parent() != null ? handle.parent().file() : null;
    if (FILTER != null && !FILTER.accept(file, handle.name())) {
      throw new ValidationException("File not accepted!");
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
}
