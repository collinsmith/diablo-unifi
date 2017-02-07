package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gmail.collinsmith70.old.Key;
import com.gmail.collinsmith70.old.KeyManager;

import java.lang.reflect.Field;

public class Keys {

  public static <T> void addTo(KeyManager keyManager) {
    addTo(keyManager, Keys.class);
  }

  @SuppressWarnings("unchecked")
  private static <T> void addTo(KeyManager keyManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
      if (Key.class.isAssignableFrom(field.getType())) {
        try {
          Key key = (Key) field.get(null);
          keyManager.add(key);
        } catch (IllegalAccessException e) {
          Gdx.app.error("Keys", "Unable to access key: " + e.getMessage());
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(keyManager, subclass);
    }
  }

  private Keys() {
  }

  public static final Key Console = new Key("Console",
          "console",
          Input.Keys.GRAVE);

}
