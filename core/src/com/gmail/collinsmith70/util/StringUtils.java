package com.gmail.collinsmith70.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

  private static final Pattern PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

  @NonNull
  public static String[] parseArgs(@NonNull CharSequence buffer) {
    String tmp;
    Collection<String> args = new ArrayList<>(8);
    Matcher matcher = PATTERN.matcher(buffer);
    while (matcher.find()) {
      if ((tmp = matcher.group(1)) != null) {
        // Add double-quoted string without the quotes
        args.add(tmp);
      } else if ((tmp = matcher.group(2)) != null) {
        // Add single-quoted string without the quotes
        args.add(tmp);
      } else {
        // Add unquoted word
        args.add(matcher.group());
      }
    }

    return args.toArray(new String[args.size()]);
  }

  private StringUtils() {}

}
