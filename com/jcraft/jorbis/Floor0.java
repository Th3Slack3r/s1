package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;

class Floor0 extends FuncFloor {
  void pack(Object i, Buffer opb) {
    InfoFloor0 info = (InfoFloor0)i;
    opb.write(info.order, 8);
    opb.write(info.rate, 16);
    opb.write(info.barkmap, 16);
    opb.write(info.ampbits, 6);
    opb.write(info.ampdB, 8);
    opb.write(info.numbooks - 1, 4);
    for (int j = 0; j < info.numbooks; j++)
      opb.write(info.books[j], 8); 
  }
  
  Object unpack(Info vi, Buffer opb) {
    InfoFloor0 info = new InfoFloor0(this);
    info.order = opb.read(8);
    info.rate = opb.read(16);
    info.barkmap = opb.read(16);
    info.ampbits = opb.read(6);
    info.ampdB = opb.read(8);
    info.numbooks = opb.read(4) + 1;
    if (info.order < 1 || info.rate < 1 || info.barkmap < 1 || info.numbooks < 1)
      return null; 
    for (int j = 0; j < info.numbooks; j++) {
      info.books[j] = opb.read(8);
      if (info.books[j] < 0 || info.books[j] >= vi.books)
        return null; 
    } 
    return info;
  }
  
  Object look(DspState vd, InfoMode mi, Object i) {
    Info vi = vd.vi;
    InfoFloor0 info = (InfoFloor0)i;
    LookFloor0 look = new LookFloor0(this);
    look.m = info.order;
    look.n = vi.blocksizes[mi.blockflag] / 2;
    look.ln = info.barkmap;
    look.vi = info;
    look.lpclook.init(look.ln, look.m);
    float scale = look.ln / toBARK((float)(info.rate / 2.0D));
    look.linearmap = new int[look.n];
    for (int j = 0; j < look.n; j++) {
      int val = (int)Math.floor((toBARK((float)(info.rate / 2.0D / look.n * j)) * scale));
      if (val >= look.ln)
        val = look.ln; 
      look.linearmap[j] = val;
    } 
    return look;
  }
  
  static float toBARK(float f) {
    return (float)(13.1D * Math.atan(7.4E-4D * f) + 2.24D * Math.atan((f * f) * 1.85E-8D) + 1.0E-4D * f);
  }
  
  Object state(Object i) {
    EchstateFloor0 state = new EchstateFloor0(this);
    InfoFloor0 info = (InfoFloor0)i;
    state.codewords = new int[info.order];
    state.curve = new float[info.barkmap];
    state.frameno = -1L;
    return state;
  }
  
  void free_info(Object i) {}
  
  void free_look(Object i) {}
  
  void free_state(Object vs) {}
  
  int forward(Block vb, Object i, float[] in, float[] out, Object vs) {
    return 0;
  }
  
  float[] lsp = null;
  
  int inverse(Block vb, Object i, float[] out) {
    LookFloor0 look = (LookFloor0)i;
    InfoFloor0 info = look.vi;
    int ampraw = vb.opb.read(info.ampbits);
    if (ampraw > 0) {
      int maxval = (1 << info.ampbits) - 1;
      float amp = ampraw / maxval * info.ampdB;
      int booknum = vb.opb.read(Util.ilog(info.numbooks));
      if (booknum != -1 && booknum < info.numbooks)
        synchronized (this) {
          if (this.lsp == null || this.lsp.length < look.m) {
            this.lsp = new float[look.m];
          } else {
            for (int k = 0; k < look.m; k++)
              this.lsp[k] = 0.0F; 
          } 
          CodeBook b = vb.vd.fullbooks[info.books[booknum]];
          float last = 0.0F;
          int j;
          for (j = 0; j < look.m; j++)
            out[j] = 0.0F; 
          for (j = 0; j < look.m; j += b.dim) {
            if (b.decodevs(this.lsp, j, vb.opb, 1, -1) == -1) {
              for (int k = 0; k < look.n; k++)
                out[k] = 0.0F; 
              return 0;
            } 
          } 
          for (j = 0; j < look.m; ) {
            for (int k = 0; k < b.dim; k++, j++)
              this.lsp[j] = this.lsp[j] + last; 
            last = this.lsp[j - 1];
          } 
          Lsp.lsp_to_curve(out, look.linearmap, look.n, look.ln, this.lsp, look.m, 
              amp, info.ampdB);
          return 1;
        }  
    } 
    return 0;
  }
  
  Object inverse1(Block vb, Object i, Object memo) {
    LookFloor0 look = (LookFloor0)i;
    InfoFloor0 info = look.vi;
    float[] lsp = (float[])null;
    if (memo instanceof float[])
      lsp = (float[])memo; 
    int ampraw = vb.opb.read(info.ampbits);
    if (ampraw > 0) {
      int maxval = (1 << info.ampbits) - 1;
      float amp = ampraw / maxval * info.ampdB;
      int booknum = vb.opb.read(Util.ilog(info.numbooks));
      if (booknum != -1 && booknum < info.numbooks) {
        CodeBook b = vb.vd.fullbooks[info.books[booknum]];
        float last = 0.0F;
        if (lsp == null || lsp.length < look.m + 1) {
          lsp = new float[look.m + 1];
        } else {
          for (int k = 0; k < lsp.length; k++)
            lsp[k] = 0.0F; 
        } 
        int j;
        for (j = 0; j < look.m; j += b.dim) {
          if (b.decodev_set(lsp, j, vb.opb, b.dim) == -1)
            return null; 
        } 
        for (j = 0; j < look.m; ) {
          for (int k = 0; k < b.dim; k++, j++)
            lsp[j] = lsp[j] + last; 
          last = lsp[j - 1];
        } 
        lsp[look.m] = amp;
        return lsp;
      } 
    } 
    return null;
  }
  
  int inverse2(Block vb, Object i, Object memo, float[] out) {
    LookFloor0 look = (LookFloor0)i;
    InfoFloor0 info = look.vi;
    if (memo != null) {
      float[] lsp = (float[])memo;
      float amp = lsp[look.m];
      Lsp.lsp_to_curve(out, look.linearmap, look.n, look.ln, lsp, look.m, amp, 
          info.ampdB);
      return 1;
    } 
    for (int j = 0; j < look.n; j++)
      out[j] = 0.0F; 
    return 0;
  }
  
  static float fromdB(float x) {
    return (float)Math.exp(x * 0.11512925D);
  }
  
  static void lsp_to_lpc(float[] lsp, float[] lpc, int m) {
    int m2 = m / 2;
    float[] O = new float[m2];
    float[] E = new float[m2];
    float[] Ae = new float[m2 + 1];
    float[] Ao = new float[m2 + 1];
    float[] Be = new float[m2];
    float[] Bo = new float[m2];
    int i;
    for (i = 0; i < m2; i++) {
      O[i] = (float)(-2.0D * Math.cos(lsp[i * 2]));
      E[i] = (float)(-2.0D * Math.cos(lsp[i * 2 + 1]));
    } 
    int j;
    for (j = 0; j < m2; j++) {
      Ae[j] = 0.0F;
      Ao[j] = 1.0F;
      Be[j] = 0.0F;
      Bo[j] = 1.0F;
    } 
    Ao[j] = 1.0F;
    Ae[j] = 1.0F;
    for (i = 1; i < m + 1; i++) {
      float B = 0.0F, A = B;
      for (j = 0; j < m2; j++) {
        float temp = O[j] * Ao[j] + Ae[j];
        Ae[j] = Ao[j];
        Ao[j] = A;
        A += temp;
        temp = E[j] * Bo[j] + Be[j];
        Be[j] = Bo[j];
        Bo[j] = B;
        B += temp;
      } 
      lpc[i - 1] = (A + Ao[j] + B - Ae[j]) / 2.0F;
      Ao[j] = A;
      Ae[j] = B;
    } 
  }
  
  static void lpc_to_curve(float[] curve, float[] lpc, float amp, LookFloor0 l, String name, int frameno) {
    float[] lcurve = new float[Math.max(l.ln * 2, l.m * 2 + 2)];
    if (amp == 0.0F) {
      for (int j = 0; j < l.n; j++)
        curve[j] = 0.0F; 
      return;
    } 
    l.lpclook.lpc_to_curve(lcurve, lpc, amp);
    for (int i = 0; i < l.n; i++)
      curve[i] = lcurve[l.linearmap[i]]; 
  }
  
  class InfoFloor0 {
    int order;
    
    int rate;
    
    int barkmap;
    
    int ampbits;
    
    int ampdB;
    
    int numbooks;
    
    int[] books;
    
    final Floor0 this$0;
    
    InfoFloor0(Floor0 this$0) {
      this.this$0 = this$0;
      this.books = new int[16];
    }
  }
  
  class LookFloor0 {
    int n;
    
    int ln;
    
    int m;
    
    int[] linearmap;
    
    Floor0.InfoFloor0 vi;
    
    Lpc lpclook;
    
    final Floor0 this$0;
    
    LookFloor0(Floor0 this$0) {
      this.this$0 = this$0;
      this.lpclook = new Lpc();
    }
  }
  
  class EchstateFloor0 {
    int[] codewords;
    
    float[] curve;
    
    long frameno;
    
    long codes;
    
    final Floor0 this$0;
    
    EchstateFloor0(Floor0 this$0) {
      this.this$0 = this$0;
    }
  }
}
