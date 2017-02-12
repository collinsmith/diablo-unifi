package com.gmail.collinsmith70.diablo;

import com.gmail.collinsmith70.cvar.Cvar;
import com.gmail.collinsmith70.cvar.CvarManager;
import com.gmail.collinsmith70.libgdx.GdxFileSuggester;
import com.gmail.collinsmith70.libgdx.GdxFileValidator;
import com.gmail.collinsmith70.libgdx.util.GdxFileHandleResolvers;
import com.gmail.collinsmith70.serializer.LocaleStringSerializer;
import com.gmail.collinsmith70.validator.NonNullSubclassValidator;
import com.gmail.collinsmith70.validator.NumberRangeValidator;

import java.io.File;
import java.io.FilenameFilter;
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

    private Client() {}

    public static final Cvar<Locale> Locale = Cvar.builder(Locale.class)
        .alias("Client.Locale")
        .description("Locale of the game client")
        .defaultValue(java.util.Locale.getDefault())
        .validator(new NonNullSubclassValidator<>(Locale.class))
        .serializer(LocaleStringSerializer.INSTANCE)
        .build();

    public static final Cvar<Boolean> Windowed = Cvar.builder(Boolean.class)
        .alias("Client.Windowed")
        .description("Whether or not the client is in windowed mode. Note: This cvar is ignored " +
            "when the client is started with the -windowed option")
        .defaultValue(Boolean.FALSE)
        .build();

    public static class Console {

      private Console() {}

      public static final Cvar<String> Font = Cvar.builder(String.class)
          .alias("Client.Console.Font")
          .description("Font file for the console")
          .defaultValue("default.fnt")
          .validator(new GdxFileValidator(GdxFileHandleResolvers.INTERNAL, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
              return name.endsWith(".fnt");
            }
          }))
          .suggestions(new GdxFileSuggester(GdxFileHandleResolvers.INTERNAL, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
              return name.endsWith(".fnt");
            }
          }))
          .build();

      public static final Cvar<Float> Height = Cvar.builder(Float.class)
          .alias("Client.Console.Height")
          .description("Height of the console in percent of screen height")
          .defaultValue(0.5f)
          .validator(NumberRangeValidator.of(Float.class, 0.25f, 1.0f))
          .build();

      public static class Color {

        private Color() {}

        public static final Cvar<Float> r = Cvar.builder(Float.class)
            .alias("Client.Console.Color.r")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        public static final Cvar<Float> g = Cvar.builder(Float.class)
            .alias("Client.Console.Color.g")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        public static final Cvar<Float> b = Cvar.builder(Float.class)
            .alias("Client.Console.Color.b")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();

        public static final Cvar<Float> a = Cvar.builder(Float.class)
            .alias("Client.Console.Color.a")
            .description("Color of the console font")
            .defaultValue(1.0f)
            .validator(NumberRangeValidator.of(Float.class, 0.0f, 1.0f))
            .build();
      }

    }

    public static class Display {

      private Display() {}

      public static final Cvar<Byte> ShowFPS = Cvar.builder(Byte.class)
          .alias("Client.Display.ShowFPS")
          .description("Whether or not to draw the current FPS. " +
              "0=Off, 1=Top Left, 2=Top Right, 3=Bottom Left, 4=Bottom Right")
          .defaultValue((byte) 0)
          .validator(NumberRangeValidator.of(Byte.class, (byte) 0, (byte) 4))
          .build();

      public static final Cvar<Short> BackgroundFPSLimit = Cvar.builder(Short.class)
          .alias("Client.Display.BackgroundFPSLimit")
          .description("Limits the FPS of the application when running in the background. " +
              "-1=Won't Render, 0=Unlimited")
          .defaultValue((short) 10)
          .validator(NumberRangeValidator.of(Byte.class, (byte) -1, null))
          .build();

      public static final Cvar<Short> ForegroundFPSLimit = Cvar.builder(Short.class)
          .alias("Client.Display.ForegroundFPSLimit")
          .description("Limits the FPS of the application when running in the foreground. " +
              "0-Unlimited")
          .defaultValue((short) 0)
          .validator(NumberRangeValidator.of(Byte.class, (byte) 0, null))
          .build();

    }
  }

}
