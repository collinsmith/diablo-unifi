package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ParameterException extends RuntimeException {

  public ParameterException() {
    super((String) null);
  }

  public ParameterException(@Nullable String reason) {
    super(reason);
  }

  public ParameterException(@NonNull String format, @Nullable Object... args) {
    super(String.format(format, args));
  }

}
