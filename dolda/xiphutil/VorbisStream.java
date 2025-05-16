package dolda.xiphutil;

import com.jcraft.jogg.Packet;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VorbisStream {
  private final PacketStream in;
  
  private final Info info = new Info();
  
  private final Comment cmt = new Comment();
  
  private final DspState dsp = new DspState();
  
  private final Block blk = new Block(this.dsp);
  
  private final float[][][] pcmp;
  
  private final int[] idxp;
  
  public final Map<String, String> uc;
  
  public final String vnd;
  
  public final int chn;
  
  public final int rate;
  
  public VorbisStream(PacketStream in) throws IOException {
    this.in = in;
    this.info.init();
    this.cmt.init();
    for (int i = 0; i < 3; i++) {
      Packet pkt = in.packet();
      if (pkt == null)
        throw new VorbisException(); 
      if (this.info.synthesis_headerin(this.cmt, pkt) < 0)
        throw new VorbisException(); 
    } 
    this.vnd = new String(this.cmt.vendor, 0, this.cmt.vendor.length - 1, "UTF-8");
    HashMap<String, String> uc = new HashMap<>();
    for (int j = 0; j < this.cmt.user_comments.length - 1; j++) {
      byte[] cb = this.cmt.user_comments[j];
      String cs = new String(cb, 0, cb.length - 1, "UTF-8");
      int ep;
      if ((ep = cs.indexOf('=')) < 1)
        throw new VorbisException(); 
      uc.put(cs.substring(0, ep).toLowerCase().intern(), cs.substring(ep + 1));
    } 
    this.uc = Collections.unmodifiableMap(uc);
    this.chn = this.info.channels;
    this.rate = this.info.rate;
    this.dsp.synthesis_init(this.info);
    this.blk.init(this.dsp);
    this.pcmp = new float[1][][];
    this.idxp = new int[this.chn];
  }
  
  public VorbisStream(InputStream in) throws IOException {
    this(new PacketStream(new PageStream(in)));
  }
  
  public float[][] decode() throws IOException {
    Packet pkt;
    do {
      int len = this.dsp.synthesis_pcmout(this.pcmp, this.idxp);
      if (len > 0) {
        float[][] ret = new float[this.chn][];
        for (int i = 0; i < this.chn; i++) {
          ret[i] = new float[len];
          System.arraycopy(this.pcmp[0][i], this.idxp[i], ret[i], 0, len);
        } 
        this.dsp.synthesis_read(len);
        return ret;
      } 
      pkt = this.in.packet();
      if (pkt == null)
        return null; 
    } while (this.blk.synthesis(pkt) == 0 && this.dsp.synthesis_blockin(this.blk) == 0);
    throw new VorbisException();
  }
  
  public InputStream pcmstream() {
    return new InputStream() {
        private byte[] buf;
        
        private int bufp;
        
        private boolean convert() throws IOException {
          float[][] inb = VorbisStream.this.decode();
          if (inb == null) {
            this.buf = new byte[0];
            return false;
          } 
          this.buf = new byte[2 * VorbisStream.this.chn * (inb[0]).length];
          int p = 0;
          for (int i = 0; i < (inb[0]).length; i++) {
            for (int c = 0; c < VorbisStream.this.chn; c++) {
              int s = (int)(inb[c][i] * 32767.0F);
              this.buf[p++] = (byte)s;
              this.buf[p++] = (byte)(s >> 8);
            } 
          } 
          this.bufp = 0;
          return true;
        }
        
        public int read() throws IOException {
          byte[] rb = new byte[1];
          while (true) {
            int ret = read(rb);
            if (ret < 0)
              return -1; 
            if (ret != 0)
              return rb[0]; 
          } 
        }
        
        public int read(byte[] dst, int off, int len) throws IOException {
          if (this.buf == null && !convert())
            return -1; 
          if (this.buf.length - this.bufp < len)
            len = this.buf.length - this.bufp; 
          System.arraycopy(this.buf, this.bufp, dst, off, len);
          if ((this.bufp += len) == this.buf.length)
            this.buf = null; 
          return len;
        }
        
        public void close() throws IOException {
          VorbisStream.this.close();
        }
      };
  }
  
  public String toString() {
    return String.format("Vorbis Stream (encoded by `%s', %d comments, %d channels, sampled at %d Hz)", new Object[] { this.vnd, Integer.valueOf(this.uc.size()), Integer.valueOf(this.chn), Integer.valueOf(this.rate) });
  }
  
  public static void main(String[] args) throws Exception {
    VorbisStream vs = new VorbisStream(new FileInputStream(args[0]));
    InputStream pcm = vs.pcmstream();
    byte[] buf = new byte[4096];
    int ret;
    while ((ret = pcm.read(buf)) >= 0)
      System.out.write(buf); 
  }
  
  public void close() throws IOException {
    this.in.close();
  }
}
