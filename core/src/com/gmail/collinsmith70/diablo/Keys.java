package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Input;
import com.gmail.collinsmith70.libgdx.key.KeyMapper;
import com.gmail.collinsmith70.libgdx.key.MappedKey;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class Keys {

  public static List<Throwable> addTo(KeyMapper keyManager) {
    return addTo(keyManager, Keys.class, new ArrayList<Throwable>(0));
  }

  private static List<Throwable> addTo(KeyMapper keyManager, Class<?> clazz,
                                       List<Throwable> throwables) {
    for (Field field : clazz.getFields()) {
      if (MappedKey.class.isAssignableFrom(field.getType())) {
        try {
          keyManager.add((MappedKey) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(keyManager, subclass, throwables);
    }

    return throwables;
  }

  private Keys() {
  }

  public static final MappedKey Console = new MappedKey("Console",
          "console",
          Input.Keys.GRAVE);

}
