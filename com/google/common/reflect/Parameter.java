package com.google.common.reflect;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import javax.annotation.Nullable;

@Beta
public final class Parameter implements AnnotatedElement {
  private final Invokable<?, ?> declaration;
  
  private final int position;
  
  private final TypeToken<?> type;
  
  private final ImmutableList<Annotation> annotations;
  
  Parameter(Invokable<?, ?> declaration, int position, TypeToken<?> type, Annotation[] annotations) {
    this.declaration = declaration;
    this.position = position;
    this.type = type;
    this.annotations = ImmutableList.copyOf((Object[])annotations);
  }
  
  public TypeToken<?> getType() {
    return this.type;
  }
  
  public Invokable<?, ?> getDeclaringInvokable() {
    return this.declaration;
  }
  
  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
    return (getAnnotation(annotationType) != null);
  }
  
  @Nullable
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    Preconditions.checkNotNull(annotationType);
    for (Annotation annotation : this.annotations) {
      if (annotationType.isInstance(annotation))
        return annotationType.cast(annotation); 
    } 
    return null;
  }
  
  public Annotation[] getAnnotations() {
    return getDeclaredAnnotations();
  }
  
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return getDeclaredAnnotationsByType(annotationType);
  }
  
  public Annotation[] getDeclaredAnnotations() {
    return (Annotation[])this.annotations.toArray((Object[])new Annotation[this.annotations.size()]);
  }
  
  @Nullable
  public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationType) {
    Preconditions.checkNotNull(annotationType);
    return (A)FluentIterable.from((Iterable)this.annotations).filter(annotationType).first().orNull();
  }
  
  public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationType) {
    return (A[])FluentIterable.from((Iterable)this.annotations).filter(annotationType).toArray(annotationType);
  }
  
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof Parameter) {
      Parameter that = (Parameter)obj;
      return (this.position == that.position && this.declaration.equals(that.declaration));
    } 
    return false;
  }
  
  public int hashCode() {
    return this.position;
  }
  
  public String toString() {
    String str = String.valueOf(String.valueOf(this.type));
    int i = this.position;
    return (new StringBuilder(15 + str.length())).append(str).append(" arg").append(i).toString();
  }
}
