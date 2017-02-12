package com.gmail.collinsmith70.libgdx.util;

import com.google.common.base.Strings;

import android.support.annotation.NonNull;

import com.gmail.collinsmith70.libgdx.Console;

import java.util.Iterator;

public class ConsoleUtils {

  public static void printList(@NonNull Console console, @NonNull Iterable<String> it,
                               int columns, int width) {
    printList(console, it.iterator(), columns, width);
  }

  public static void printList(@NonNull Console console, @NonNull Iterator<String> it,
                               int columns, int width) {
    int i = 0;
    StringBuilder sb = new StringBuilder(columns * width);
    while (it.hasNext()) {
      String text = it.next();
      if (++i % columns == 0) {
        sb.append(text);
        console.println(sb.toString());
        sb.setLength(0);
      } else if (it.hasNext()) {
        sb.append(Strings.padEnd(text, width, ' '));
      } else {
        sb.append(text);
      }
    }

    if (sb.length() > 0) {
      console.println(sb.toString());
    }
  }

  private ConsoleUtils() {}

}
