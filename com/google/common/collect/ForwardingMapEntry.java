package com.google.common.collect;

import com.google.common.annotations.Beta;
import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Objects;
import java.util.Map;
import javax.annotation.Nullable;

@GwtCompatible
public abstract class ForwardingMapEntry<K, V> extends ForwardingObject implements Map.Entry<K, V> {
  public K getKey() {
    return delegate().getKey();
  }
  
  public V getValue() {
    return delegate().getValue();
  }
  
  public V setValue(V value) {
    return delegate().setValue(value);
  }
  
  public boolean equals(@Nullable Object object) {
    return delegate().equals(object);
  }
  
  public int hashCode() {
    return delegate().hashCode();
  }
  
  protected boolean standardEquals(@Nullable Object object) {
    if (object instanceof Map.Entry) {
      Map.Entry<?, ?> that = (Map.Entry<?, ?>)object;
      return (Objects.equal(getKey(), that.getKey()) && Objects.equal(getValue(), that.getValue()));
    } 
    return false;
  }
  
  protected int standardHashCode() {
    K k = getKey();
    V v = getValue();
    return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
  }
  
  @Beta
  protected String standardToString() {
    String str1 = String.valueOf(String.valueOf(getKey())), str2 = String.valueOf(String.valueOf(getValue()));
    return (new StringBuilder(1 + str1.length() + str2.length())).append(str1).append("=").append(str2).toString();
  }
  
  protected abstract Map.Entry<K, V> delegate();
}
