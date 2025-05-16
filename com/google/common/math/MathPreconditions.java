package com.google.common.math;

import com.google.common.annotations.GwtCompatible;
import java.math.BigInteger;
import javax.annotation.Nullable;

@GwtCompatible
final class MathPreconditions {
  static int checkPositive(@Nullable String role, int x) {
    if (x <= 0) {
      String str = String.valueOf(String.valueOf(role));
      int i = x;
      throw new IllegalArgumentException((new StringBuilder(26 + str.length())).append(str).append(" (").append(i).append(") must be > 0").toString());
    } 
    return x;
  }
  
  static long checkPositive(@Nullable String role, long x) {
    if (x <= 0L) {
      String str = String.valueOf(String.valueOf(role));
      long l = x;
      throw new IllegalArgumentException((new StringBuilder(35 + str.length())).append(str).append(" (").append(l).append(") must be > 0").toString());
    } 
    return x;
  }
  
  static BigInteger checkPositive(@Nullable String role, BigInteger x) {
    if (x.signum() <= 0) {
      String str1 = String.valueOf(String.valueOf(role)), str2 = String.valueOf(String.valueOf(x));
      throw new IllegalArgumentException((new StringBuilder(15 + str1.length() + str2.length())).append(str1).append(" (").append(str2).append(") must be > 0").toString());
    } 
    return x;
  }
  
  static int checkNonNegative(@Nullable String role, int x) {
    if (x < 0) {
      String str = String.valueOf(String.valueOf(role));
      int i = x;
      throw new IllegalArgumentException((new StringBuilder(27 + str.length())).append(str).append(" (").append(i).append(") must be >= 0").toString());
    } 
    return x;
  }
  
  static long checkNonNegative(@Nullable String role, long x) {
    if (x < 0L) {
      String str = String.valueOf(String.valueOf(role));
      long l = x;
      throw new IllegalArgumentException((new StringBuilder(36 + str.length())).append(str).append(" (").append(l).append(") must be >= 0").toString());
    } 
    return x;
  }
  
  static BigInteger checkNonNegative(@Nullable String role, BigInteger x) {
    if (x.signum() < 0) {
      String str1 = String.valueOf(String.valueOf(role)), str2 = String.valueOf(String.valueOf(x));
      throw new IllegalArgumentException((new StringBuilder(16 + str1.length() + str2.length())).append(str1).append(" (").append(str2).append(") must be >= 0").toString());
    } 
    return x;
  }
  
  static double checkNonNegative(@Nullable String role, double x) {
    if (x < 0.0D) {
      String str = String.valueOf(String.valueOf(role));
      double d = x;
      throw new IllegalArgumentException((new StringBuilder(40 + str.length())).append(str).append(" (").append(d).append(") must be >= 0").toString());
    } 
    return x;
  }
  
  static void checkRoundingUnnecessary(boolean condition) {
    if (!condition)
      throw new ArithmeticException("mode was UNNECESSARY, but rounding was necessary"); 
  }
  
  static void checkInRange(boolean condition) {
    if (!condition)
      throw new ArithmeticException("not in range"); 
  }
  
  static void checkNoOverflow(boolean condition) {
    if (!condition)
      throw new ArithmeticException("overflow"); 
  }
}
