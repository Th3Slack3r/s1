package dolda.jglob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class Loader {
  private final Class<? extends Annotation> an;
  
  private final ClassLoader cl;
  
  private Loader(Class<? extends Annotation> annotation, ClassLoader loader) {
    this.an = annotation;
    this.cl = loader;
  }
  
  public Iterable<String> names() {
    return new Iterable<String>() {
        public Iterator<String> iterator() {
          return new Iterator<String>() {
              private Enumeration<URL> rls;
              
              private Iterator<String> cur = null;
              
              private Iterator<String> parse(URL url) {
                try {
                  List<String> buf = new LinkedList<String>();
                  InputStream in = url.openStream();
                  try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(in, "utf-8"));
                    String ln;
                    while ((ln = r.readLine()) != null) {
                      ln = ln.trim();
                      if (ln.length() < 1)
                        continue; 
                      buf.add(ln);
                    } 
                    return buf.iterator();
                  } finally {
                    in.close();
                  } 
                } catch (IOException e) {
                  throw new GlobAccessException(e);
                } 
              }
              
              public boolean hasNext() {
                if (this.cur == null || !this.cur.hasNext()) {
                  if (this.rls == null)
                    try {
                      this.rls = Loader.this.cl.getResources("META-INF/glob/" + Loader.this.an.getName());
                    } catch (IOException e) {
                      throw new GlobAccessException(e);
                    }  
                  if (!this.rls.hasMoreElements())
                    return false; 
                  URL u = this.rls.nextElement();
                  this.cur = parse(u);
                } 
                return true;
              }
              
              public String next() {
                if (!hasNext())
                  throw new NoSuchElementException(); 
                String ret = this.cur.next();
                return ret;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
  
  public Iterable<Class<?>> classes() {
    return new Iterable<Class<?>>() {
        public Iterator<Class<?>> iterator() {
          return new Iterator<Class<?>>() {
              private final Iterator<String> names = Loader.this.names().iterator();
              
              private Class<?> n = null;
              
              public boolean hasNext() {
                while (this.n == null) {
                  Class<?> c;
                  if (!this.names.hasNext())
                    return false; 
                  String nm = this.names.next();
                  try {
                    c = Loader.this.cl.loadClass(nm);
                  } catch (ClassNotFoundException e) {
                    continue;
                  } 
                  if (c.getAnnotation((Class)Loader.this.an) == null)
                    continue; 
                  this.n = c;
                } 
                return true;
              }
              
              public Class<?> next() {
                if (!hasNext())
                  throw new NoSuchElementException(); 
                Class<?> r = this.n;
                this.n = null;
                return r;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
  
  public <T> Iterable<T> instances(final Class<T> cast) {
    return new Iterable<T>() {
        public Iterator<T> iterator() {
          return new Iterator() {
              private final Iterator<Class<?>> classes = Loader.this.classes().iterator();
              
              private T n = null;
              
              public boolean hasNext() {
                while (this.n == null) {
                  T inst;
                  if (!this.classes.hasNext())
                    return false; 
                  Class<?> cl = this.classes.next();
                  try {
                    inst = cast.cast(cl.newInstance());
                  } catch (InstantiationException e) {
                    throw new GlobInstantiationException(e);
                  } catch (IllegalAccessException e) {
                    throw new GlobInstantiationException(e);
                  } 
                  this.n = inst;
                } 
                return true;
              }
              
              public T next() {
                if (!hasNext())
                  throw new NoSuchElementException(); 
                T r = this.n;
                this.n = null;
                return r;
              }
              
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
        }
      };
  }
  
  public Iterable<?> instances() {
    return instances(Object.class);
  }
  
  public static Loader get(Class<? extends Annotation> annotation, ClassLoader loader) {
    return new Loader(annotation, loader);
  }
  
  public static Loader get(Class<? extends Annotation> annotation) {
    return get(annotation, annotation.getClassLoader());
  }
}
