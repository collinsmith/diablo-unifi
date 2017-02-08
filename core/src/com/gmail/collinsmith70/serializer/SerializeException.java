package com.gmail.collinsmith70.serializer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings({ "WeakerAccess", "unused", "SameParameterValue" })
public class SerializeException extends RuntimeException {

  public SerializeException() {
    super();
  }

  public SerializeException(@Nullable String message) {
    super(message);
  }

  public SerializeException(@NonNull Throwable cause) {
    super(cause);
  }

}
