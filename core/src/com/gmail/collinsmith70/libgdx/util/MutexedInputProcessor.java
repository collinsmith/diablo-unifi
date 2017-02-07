package com.gmail.collinsmith70.libgdx.util;

import com.google.common.base.Supplier;

import android.support.annotation.NonNull;

import com.badlogic.gdx.InputProcessor;

public class MutexedInputProcessor extends PropagatingInputProcessor {

  private final Supplier<Boolean> condition;
  private final InputProcessor inputProcessor;

  public MutexedInputProcessor(@NonNull InputProcessor inputProcessor,
                               @NonNull Supplier<Boolean> condition) {
    super();
    this.condition = condition;
    this.inputProcessor = inputProcessor;
  }

  public MutexedInputProcessor(@NonNull InputProcessor a,
                               @NonNull InputProcessor b,
                               @NonNull Supplier<Boolean> condition) {
    super(b);
    this.condition = condition;
    this.inputProcessor = a;
  }

  @Override
  public boolean keyDown(int keycode) {
    if (condition.get()) {
      inputProcessor.keyDown(keycode);
    }
    
    return super.keyDown(keycode);
  }

  @Override
  public boolean keyUp(int keycode) {
    if (condition.get()) {
      return inputProcessor.keyUp(keycode);
    }

    return super.keyUp(keycode);
  }

  @Override
  public boolean keyTyped(char ch) {
    if (condition.get()) {
      return inputProcessor.keyTyped(ch);
    }

    return super.keyTyped(ch);
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    if (condition.get()) {
      return inputProcessor.touchDown(screenX, screenY, pointer, button);
    }

    return super.touchDown(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    if (condition.get()) {
      return inputProcessor.touchUp(screenX, screenY, pointer, button);
    }

    return super.touchUp(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (condition.get()) {
      return inputProcessor.touchDragged(screenX, screenY, pointer);
    }

    return super.touchDragged(screenX, screenY, pointer);
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    if (condition.get()) {
      return inputProcessor.mouseMoved(screenX, screenY);
    }

    return super.mouseMoved(screenX, screenY);
  }

  @Override
  public boolean scrolled(int amount) {
    if (condition.get()) {
      return inputProcessor.scrolled(amount);
    }

    return super.scrolled(amount);
  }

}
