package com.gmail.collinsmith70.libgdx.util;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.InputProcessor;

public class PropagatingInputProcessor implements InputProcessor {
  @Nullable
  private final InputProcessor PROPAGATOR;

  public PropagatingInputProcessor() {
    this.PROPAGATOR = null;
  }

  public PropagatingInputProcessor(@NonNull InputProcessor inputProcessor) {
    this.PROPAGATOR = Preconditions.checkNotNull(inputProcessor);
  }

  @Override
  public boolean keyDown(int keycode) {
    return PROPAGATOR != null && PROPAGATOR.keyDown(keycode);
  }

  @Override
  public boolean keyUp(int keycode) {
    return PROPAGATOR != null && PROPAGATOR.keyUp(keycode);
  }

  @Override
  public boolean keyTyped(char character) {
    return PROPAGATOR != null && PROPAGATOR.keyTyped(character);
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    return PROPAGATOR != null && PROPAGATOR.touchDown(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    return PROPAGATOR != null && PROPAGATOR.touchUp(screenX, screenY, pointer, button);
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    return PROPAGATOR != null && PROPAGATOR.touchDragged(screenX, screenY, pointer);
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY) {
    return PROPAGATOR != null && PROPAGATOR.mouseMoved(screenX, screenY);
  }

  @Override
  public boolean scrolled(int amount) {
    return PROPAGATOR != null && PROPAGATOR.scrolled(amount);
  }

}
