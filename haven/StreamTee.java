package haven;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class StreamTee extends InputStream {
  private final InputStream in;
  
  private final List<OutputStream> forked = new LinkedList<>();
  
  private boolean readeof = false;
  
  private boolean ncwe = false;
  
  public StreamTee(InputStream in) {
    this.in = in;
  }
  
  public int available() throws IOException {
    return this.in.available();
  }
  
  public void close() throws IOException {
    this.in.close();
    if (!this.ncwe || this.readeof)
      synchronized (this.forked) {
        for (OutputStream s : this.forked)
          s.close(); 
      }  
  }
  
  public void setncwe() {
    this.ncwe = true;
  }
  
  public void flush() throws IOException {
    synchronized (this.forked) {
      for (OutputStream s : this.forked)
        s.flush(); 
    } 
  }
  
  public void mark(int limit) {}
  
  public boolean markSupported() {
    return false;
  }
  
  public int read() throws IOException {
    int rv = this.in.read();
    if (rv >= 0) {
      synchronized (this.forked) {
        for (OutputStream s : this.forked)
          s.write(rv); 
      } 
    } else {
      this.readeof = true;
    } 
    return rv;
  }
  
  public int read(byte[] buf, int off, int len) throws IOException {
    int rv = this.in.read(buf, off, len);
    if (rv > 0) {
      synchronized (this.forked) {
        for (OutputStream s : this.forked)
          s.write(buf, off, rv); 
      } 
    } else {
      this.readeof = true;
    } 
    return rv;
  }
  
  public void reset() throws IOException {
    throw new IOException("Mark not supported on StreamTee");
  }
  
  public void attach(OutputStream s) {
    synchronized (this.forked) {
      this.forked.add(s);
    } 
  }
}
