package com.gmail.collinsmith70.serializer;

import android.support.annotation.Nullable;

public class SerializeException extends RuntimeException {

  public SerializeException() {
    super();
  }

  public SerializeException(@Nullable String message) {
    super(message);
  }

}