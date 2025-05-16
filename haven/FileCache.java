package haven;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileCache implements ResCache {
  private final File base;
  
  public FileCache(File base) {
    this.base = base;
  }
  
  public static FileCache foruser() {
    try {
      String path = System.getProperty("user.home", null);
      if (path == null)
        return null; 
      File home = new File(path);
      if (!home.exists() || !home.isDirectory() || !home.canRead() || !home.canWrite())
        return null; 
      File base = new File(new File(home, ".salem"), "cache");
      if (!base.exists() && !base.mkdirs())
        return null; 
      return new FileCache(base);
    } catch (SecurityException e) {
      return null;
    } 
  }
  
  private File forres(String nm) {
    File res = this.base;
    String[] comp = nm.split("/");
    for (int i = 0; i < comp.length - 1; i++)
      res = new File(res, comp[i]); 
    return new File(res, comp[comp.length - 1] + ".cached");
  }
  
  public OutputStream store(String name) throws IOException {
    final File nm = forres(name);
    File dir = nm.getParentFile();
    final File tmp = new File(dir, nm.getName() + ".new");
    dir.mkdirs();
    tmp.delete();
    OutputStream ret = new FilterOutputStream(new BufferedOutputStream(new FileOutputStream(tmp))) {
        public void close() throws IOException {
          super.close();
          if (!tmp.renameTo(nm)) {
            nm.delete();
            tmp.renameTo(nm);
          } 
        }
      };
    return ret;
  }
  
  public InputStream fetch(String name) throws IOException {
    return new BufferedInputStream(new FileInputStream(forres(name)));
  }
  
  public String toString() {
    return "FileCache(" + this.base + ")";
  }
}
