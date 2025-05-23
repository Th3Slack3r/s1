package com.google.common.io;

import com.google.common.base.Preconditions;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

public abstract class ByteSink {
  public CharSink asCharSink(Charset charset) {
    return new AsCharSink(charset);
  }
  
  public abstract OutputStream openStream() throws IOException;
  
  public OutputStream openBufferedStream() throws IOException {
    OutputStream out = openStream();
    return (out instanceof BufferedOutputStream) ? out : new BufferedOutputStream(out);
  }
  
  public void write(byte[] bytes) throws IOException {
    Preconditions.checkNotNull(bytes);
    Closer closer = Closer.create();
    try {
      OutputStream out = closer.<OutputStream>register(openStream());
      out.write(bytes);
      out.flush();
    } catch (Throwable e) {
      throw closer.rethrow(e);
    } finally {
      closer.close();
    } 
  }
  
  public long writeFrom(InputStream input) throws IOException {
    Preconditions.checkNotNull(input);
    Closer closer = Closer.create();
    try {
      OutputStream out = closer.<OutputStream>register(openStream());
      long written = ByteStreams.copy(input, out);
      out.flush();
      return written;
    } catch (Throwable e) {
      throw closer.rethrow(e);
    } finally {
      closer.close();
    } 
  }
  
  private final class AsCharSink extends CharSink {
    private final Charset charset;
    
    private AsCharSink(Charset charset) {
      this.charset = (Charset)Preconditions.checkNotNull(charset);
    }
    
    public Writer openStream() throws IOException {
      return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
    }
    
    public String toString() {
      String str1 = String.valueOf(String.valueOf(ByteSink.this.toString())), str2 = String.valueOf(String.valueOf(this.charset));
      return (new StringBuilder(13 + str1.length() + str2.length())).append(str1).append(".asCharSink(").append(str2).append(")").toString();
    }
  }
}
