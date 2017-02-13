package com.gmail.collinsmith70.util;

import android.support.annotation.Nullable;

public class PrimitiveUtils {

  public static boolean unwrap(@Nullable Boolean obj, boolean defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static byte unwrap(@Nullable Byte obj, byte defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static short unwrap(@Nullable Short obj, short defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static int unwrap(@Nullable Integer obj, int defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static long unwrap(@Nullable Long obj, long defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static float unwrap(@Nullable Float obj, float defaultValue) {
    return obj == null ? defaultValue : obj;
  }

  public static double unwrap(@Nullable Double obj, double defaultValue) {
    return obj == null ? defaultValue : obj;
  }
  
  private PrimitiveUtils() {}

}
