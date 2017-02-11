package com.gmail.collinsmith70.diablo;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.cvar.ValidatableCvar;
import com.gmail.collinsmith70.validator.NonNullSubclassValidator;
import com.gmail.collinsmith70.validator.NumberRangeValidator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class Cvars {

  public static List<Throwable> addTo(CvarManager cvarManager) {
    return addTo(cvarManager, Cvars.class, new ArrayList<Throwable>(0));
  }

  private static List<Throwable> addTo(CvarManager cvarManager, Class<?> clazz,
                                       List<Throwable> throwables) {
    for (Field field : clazz.getFields()) {
      if (Cvar.class.isAssignableFrom(field.getType())) {
        try {
          cvarManager.add((Cvar) field.get(null));
        } catch (Throwable t) {
          throwables.add(t);
        }
      }
    }

    for (Class<?> subclass : clazz.getClasses()) {
      addTo(cvarManager, subclass, throwables);
    }

    return throwables;
  }

  private Cvars() {
  }

  public static class Client {

    private Client() {
    }

    public static final Cvar<Locale> Locale = new ValidatableCvar<>(
        "Client.Locale",
        "Locale for the game client",
        Locale.class,
        java.util.Locale.getDefault(),
        new NonNullSubclassValidator<>(Locale.class));

    public static final Cvar<Boolean> Windowed = new ValidatableCvar<>(
        "Client.Windowed",
        "Whether or not the client is in windowed mode. " +
            "Note: This cvar is ignored when the client " +
            "is started with the -windowed option",
        Boolean.class,
        Boolean.FALSE);

    public static class Console {

      private Console() {
      }

      public static final Cvar<String> Font = new ValidatableCvar<>(
          "Client.Console.Font",
          "Font for the in-game console output stream",
          String.class,
          "default.fnt",
          GdxFileValidator.INTERNAL);

      public static final Cvar<Float> Height = new ValidatableCvar<>(
          "Client.Console.Height",
          "Height of the console in percent of screen height",
          Float.class,
          0.5f,
          new NumberRangeValidator<>(0.25f, 1.0f));

      public static class Color {
        private Color() {
          //...
        }

        public static final Cvar<Float> r = new ValidatableCvar<>(
            "Client.Console.Color.r",
            "Color of the console font",
            Float.class, 1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));
        public static final Cvar<Float> g = new ValidatableCvar<>(
            "Client.Console.Color.g",
            "Color of the console font",
            Float.class, 1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));
        public static final Cvar<Float> b = new ValidatableCvar<>(
            "Client.Console.Color.b",
            "Color of the console font",
            Float.class, 1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));
        public static final Cvar<Float> a = new ValidatableCvar<>(
            "Client.Console.Color.a",
            "Color of the console font",
            Float.class, 1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));
      }

    }

    public static class Sound {

      private Sound() {
        //...
      }

      public static final Cvar<Boolean> Enabled = new ValidatableCvar<>(
          "Client.Sound.Enabled",
          "Controls whether or not sounds will play",
          Boolean.class,
          Boolean.TRUE);

      public static class Effects {

        private Effects() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<>(
            "Client.Sound.Effects.Volume",
            "Controls the volume level for sound effects",
            Float.class,
            1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));

      }

      public static class Environment {

        private Environment() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<>(
            "Client.Sound.Environment.Volume",
            "Controls the volume level for environment effects",
            Float.class,
            1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));

      }

      public static class Voice {

        private Voice() {
          //...
        }

        public static final Cvar<Float> Volume = new ValidatableCvar<>(
            "Client.Sound.Voice.Volume",
            "Controls the volume level for NPC voice dialog",
            Float.class,
            1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));

      }

      public static class Music {

        private Music() {
          //...
        }

        public static final Cvar<Boolean> Enabled = new ValidatableCvar<>(
            "Client.Sound.Music.Enabled",
            "Controls whether or not music will play",
            Boolean.class,
            Boolean.TRUE);

        public static final Cvar<Float> Volume = new ValidatableCvar<>(
            "Client.Sound.Music.Volume",
            "Controls the volume level for music tracks",
            Float.class,
            1.0f,
            new NumberRangeValidator<>(0.0f, 1.0f));

      }
    }

    public static class Display {

      private Display() {
      }

      public static final Cvar<Byte> ShowFPS = new ValidatableCvar<>(
          "Client.Display.ShowFPS",
          "Whether or not to draw the current FPS. " +
              "0=Off, 1=Top Left, 2=Top Right, 3=Bottom Left, 4=Bottom Right",
          Byte.class,
          (byte) 0,
          new NumberRangeValidator<>((byte) 0, (byte) 4));

      public static final Cvar<Short> BackgroundFPSLimit = new ValidatableCvar<>(
          "Client.Display.BackgroundFPSLimit",
          "Limits the FPS of the application when running in the background. " +
              "-1=Won't Render, 0=Unlimited",
          Short.class,
          (short) 10,
          new NumberRangeValidator<>((short) -1, Short.MAX_VALUE));

      public static final Cvar<Short> ForegroundFPSLimit = new ValidatableCvar<>(
          "Client.Display.ForegroundFPSLimit",
          "Limits the FPS of the application when running in the foreground. 0-Unlimited",
          Short.class,
          (short) 0,
          new NumberRangeValidator<>((short) 0, Short.MAX_VALUE));

    }
  }

}
