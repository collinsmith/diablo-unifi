package com.gmail.collinsmith70.diablo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.gmail.collinsmith70.libgdx.key.KeyMapper;
import com.gmail.collinsmith70.libgdx.key.MappedKey;

import java.lang.reflect.Field;

public class Keys {

  public static <T> void addTo(KeyMapper keyManager) {
    addTo(keyManager, Keys.class);
  }

  @SuppressWarnings("unchecked")
  private static <T> void addTo(KeyMapper keyManager, Class<?> clazz) {
    for (Field field : clazz.getFields()) {
      if (MappedKey.class.isAssignableFrom(field.getType())) {
        try {
          MappedKey key = (MappedKey) field.get(null);
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

  public static final MappedKey Console = new MappedKey("Console",
          "console",
          Input.Keys.GRAVE);

}
