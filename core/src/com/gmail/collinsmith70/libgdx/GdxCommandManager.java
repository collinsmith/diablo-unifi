package com.gmail.collinsmith70.libgdx;

import android.support.annotation.NonNull;

import com.badlogic.gdx.Gdx;
import com.gmail.collinsmith70.command.Command;
import com.gmail.collinsmith70.command.CommandManager;

public class GdxCommandManager extends CommandManager {

  public GdxCommandManager() {
    super();
  }

  @Override
  public void onAssigned(@NonNull Command command, @NonNull String alias) {
    super.onAssigned(command, alias);
    Gdx.app.debug("CommandManager", "assigning \"" + alias.toLowerCase() + "\" to " + command);
  }

  @Override
  public void onUnassigned(@NonNull Command command, @NonNull String alias) {
    super.onUnassigned(command, alias);
    Gdx.app.debug("CommandManager", "unassigning " + alias + " from " + command);
  }
}
