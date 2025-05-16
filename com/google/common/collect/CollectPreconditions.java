package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;

@GwtCompatible
final class CollectPreconditions {
  static void checkEntryNotNull(Object key, Object value) {
    if (key == null) {
      String str = String.valueOf(String.valueOf(value));
      throw new NullPointerException((new StringBuilder(24 + str.length())).append("null key in entry: null=").append(str).toString());
    } 
    if (value == null) {
      String str = String.valueOf(String.valueOf(key));
      throw new NullPointerException((new StringBuilder(26 + str.length())).append("null value in entry: ").append(str).append("=null").toString());
    } 
  }
  
  static int checkNonnegative(int value, String name) {
    if (value < 0) {
      String str = String.valueOf(String.valueOf(name));
      int i = value;
      throw new IllegalArgumentException((new StringBuilder(40 + str.length())).append(str).append(" cannot be negative but was: ").append(i).toString());
    } 
    return value;
  }
  
  static void checkRemove(boolean canRemove) {
    Preconditions.checkState(canRemove, "no calls to next() since the last call to remove()");
  }
}
