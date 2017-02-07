package com.gmail.collinsmith70.command;

import android.support.annotation.NonNull;

public interface Action {

  Action DO_NOTHING = new Action() {
    @Override
    public void onActionExecuted(@NonNull Command.Instance instance) {}
  };

  void onActionExecuted(@NonNull Command.Instance instance);

}
