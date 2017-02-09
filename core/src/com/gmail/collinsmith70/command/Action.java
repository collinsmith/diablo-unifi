package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;

@SuppressWarnings("UnusedParameters")
public interface Action {

  Action DO_NOTHING = new Action() {
    @Override
    public void onExecuted(@NonNull Command.Instance instance) {}
  };

  void onExecuted(@NonNull Command.Instance instance);

}
