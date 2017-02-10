package com.gmail.collinsmith70.diablo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.SimpleCvarStateAdapter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DesktopLauncher {
  public static void main(String[] args) {
    Options options = new Options()
        .addOption("help", false, "prints this message")
        .addOption("w", "windowed", false, "forces windowed mode")
        .addOption("fps", "drawFps", false, "force draws an FPS counter")
        .addOption("logLevel", true, "verbosity of LibGDX log")
        .addOption("allowSoftwareMode", false,
            "allows software OpenGL rendering if hardware acceleration was not available.");

    CommandLine cmd = null;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      System.out.println("For usage, use -help option");
    } finally {
      if (cmd != null) {
        if (cmd.hasOption("help")) {
          HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp("diablo", options);
          System.exit(0);
          return;
        }
      }
    }

    final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
    config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
    config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
    config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
    config.resizable = false;
    config.width = 1280;
    config.height = 720;
    if (cmd != null) {
      config.allowSoftwareMode = cmd.hasOption("allowSoftwareMode");
    }

    final Client client = new Client(config.width, config.height);
    if (cmd != null) {
      client.setWindowedForced(cmd.hasOption("w"));
      client.setDrawFpsForced(cmd.hasOption("fps"));
    }

    new LwjglApplication(client, config);
    if (cmd != null) {
      String logLevel = cmd.getOptionValue("logLevel", "info");
      if (logLevel.equalsIgnoreCase("none")) {
        Gdx.app.setLogLevel(Application.LOG_NONE);
      } else if (logLevel.equalsIgnoreCase("debug")) {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
      } else if (logLevel.equalsIgnoreCase("info")) {
        Gdx.app.setLogLevel(Application.LOG_INFO);
      } else if (logLevel.equalsIgnoreCase("error")) {
        Gdx.app.setLogLevel(Application.LOG_ERROR);
      }
    }

    Cvars.Client.Windowed.addStateListener(new SimpleCvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar, @Nullable Boolean from,
                            @Nullable Boolean to) {
        if (!client.isWindowedForced()) {
          if (to) {
            Gdx.graphics.setWindowedMode(client.width(), client.height());
          } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
          }
        }
      }
    });

    Cvars.Client.Display.BackgroundFPSLimit.addStateListener(new SimpleCvarStateAdapter<Short>() {
      @Override
      public void onChanged(@NonNull Cvar<Short> cvar, @Nullable Short from, @Nullable Short to) {
        config.backgroundFPS = to;
      }
    });

    Cvars.Client.Display.ForegroundFPSLimit.addStateListener(new SimpleCvarStateAdapter<Short>() {
      @Override
      public void onChanged(@NonNull Cvar<Short> cvar, @Nullable Short from, @Nullable Short to) {
        config.foregroundFPS = to;
      }
    });
  }
}
