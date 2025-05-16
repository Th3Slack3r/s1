package com.jcraft.jorbis;

class Drft {
  int n;
  
  float[] trigcache;
  
  int[] splitcache;
  
  void backward(float[] data) {
    if (this.n == 1)
      return; 
    drftb1(this.n, data, this.trigcache, this.trigcache, this.n, this.splitcache);
  }
  
  void init(int n) {
    this.n = n;
    this.trigcache = new float[3 * n];
    this.splitcache = new int[32];
    fdrffti(n, this.trigcache, this.splitcache);
  }
  
  void clear() {
    if (this.trigcache != null)
      this.trigcache = null; 
    if (this.splitcache != null)
      this.splitcache = null; 
  }
  
  static int[] ntryh = new int[] { 4, 2, 3, 5 };
  
  static float tpi = 6.2831855F;
  
  static float hsqt2 = 0.70710677F;
  
  static float taui = 0.8660254F;
  
  static float taur = -0.5F;
  
  static float sqrt2 = 1.4142135F;
  
  static void drfti1(int n, float[] wa, int index, int[] ifac) {
    int ntry = 0, j = -1;
    int nl = n;
    int nf = 0;
    int state = 101;
    while (true) {
      int i;
      int nq;
      int nr;
      switch (state) {
        case 101:
          j++;
          if (j < 4) {
            ntry = ntryh[j];
          } else {
            ntry += 2;
          } 
        case 104:
          nq = nl / ntry;
          nr = nl - ntry * nq;
          if (nr != 0) {
            state = 101;
            continue;
          } 
          nf++;
          ifac[nf + 1] = ntry;
          nl = nq;
          if (ntry != 2) {
            state = 107;
            continue;
          } 
          if (nf == 1) {
            state = 107;
            continue;
          } 
          for (i = 1; i < nf; i++) {
            int ib = nf - i + 1;
            ifac[ib + 1] = ifac[ib];
          } 
          ifac[2] = 2;
        case 107:
          if (nl != 1) {
            state = 104;
            continue;
          } 
          break;
      } 
    } 
    ifac[0] = n;
    ifac[1] = nf;
    float argh = tpi / n;
    int is = 0;
    int nfm1 = nf - 1;
    int l1 = 1;
    if (nfm1 == 0)
      return; 
    for (int k1 = 0; k1 < nfm1; k1++) {
      int ip = ifac[k1 + 2];
      int ld = 0;
      int l2 = l1 * ip;
      int ido = n / l2;
      int ipm = ip - 1;
      for (j = 0; j < ipm; j++) {
        ld += l1;
        int i = is;
        float argld = ld * argh;
        float fi = 0.0F;
        for (int ii = 2; ii < ido; ii += 2) {
          fi++;
          float arg = fi * argld;
          wa[index + i++] = (float)Math.cos(arg);
          wa[index + i++] = (float)Math.sin(arg);
        } 
        is += ido;
      } 
      l1 = l2;
    } 
  }
  
  static void fdrffti(int n, float[] wsave, int[] ifac) {
    if (n == 1)
      return; 
    drfti1(n, wsave, n, ifac);
  }
  
  static void dradf2(int ido, int l1, float[] cc, float[] ch, float[] wa1, int index) {
    int t1 = 0;
    int t2 = l1 * ido, t0 = t2;
    int t3 = ido << 1;
    int k;
    for (k = 0; k < l1; k++) {
      ch[t1 << 1] = cc[t1] + cc[t2];
      ch[(t1 << 1) + t3 - 1] = cc[t1] - cc[t2];
      t1 += ido;
      t2 += ido;
    } 
    if (ido < 2)
      return; 
    if (ido != 2) {
      t1 = 0;
      t2 = t0;
      for (k = 0; k < l1; k++) {
        t3 = t2;
        int t4 = (t1 << 1) + (ido << 1);
        int t5 = t1;
        int t6 = t1 + t1;
        for (int i = 2; i < ido; i += 2) {
          t3 += 2;
          t4 -= 2;
          t5 += 2;
          t6 += 2;
          float tr2 = wa1[index + i - 2] * cc[t3 - 1] + wa1[index + i - 1] * cc[t3];
          float ti2 = wa1[index + i - 2] * cc[t3] - wa1[index + i - 1] * cc[t3 - 1];
          ch[t6] = cc[t5] + ti2;
          ch[t4] = ti2 - cc[t5];
          ch[t6 - 1] = cc[t5 - 1] + tr2;
          ch[t4 - 1] = cc[t5 - 1] - tr2;
        } 
        t1 += ido;
        t2 += ido;
      } 
      if (ido % 2 == 1)
        return; 
    } 
    t3 = t2 = (t1 = ido) - 1;
    t2 += t0;
    for (k = 0; k < l1; k++) {
      ch[t1] = -cc[t2];
      ch[t1 - 1] = cc[t3];
      t1 += ido << 1;
      t2 += ido;
      t3 += ido;
    } 
  }
  
  static void dradf4(int ido, int l1, float[] cc, float[] ch, float[] wa1, int index1, float[] wa2, int index2, float[] wa3, int index3) {
    int t0 = l1 * ido;
    int t1 = t0;
    int t4 = t1 << 1;
    int t2 = t1 + (t1 << 1);
    int t3 = 0;
    int k;
    for (k = 0; k < l1; k++) {
      float tr1 = cc[t1] + cc[t2];
      float tr2 = cc[t3] + cc[t4];
      int i;
      ch[i = t3 << 2] = tr1 + tr2;
      ch[(ido << 2) + i - 1] = tr2 - tr1;
      ch[(i += ido << 1) - 1] = cc[t3] - cc[t4];
      ch[i] = cc[t2] - cc[t1];
      t1 += ido;
      t2 += ido;
      t3 += ido;
      t4 += ido;
    } 
    if (ido < 2)
      return; 
    if (ido != 2) {
      t1 = 0;
      for (k = 0; k < l1; k++) {
        t2 = t1;
        t4 = t1 << 2;
        int m, j = (m = ido << 1) + t4;
        for (int i = 2; i < ido; i += 2) {
          t3 = t2 += 2;
          t4 += 2;
          j -= 2;
          t3 += t0;
          float cr2 = wa1[index1 + i - 2] * cc[t3 - 1] + wa1[index1 + i - 1] * cc[t3];
          float ci2 = wa1[index1 + i - 2] * cc[t3] - wa1[index1 + i - 1] * cc[t3 - 1];
          t3 += t0;
          float cr3 = wa2[index2 + i - 2] * cc[t3 - 1] + wa2[index2 + i - 1] * cc[t3];
          float ci3 = wa2[index2 + i - 2] * cc[t3] - wa2[index2 + i - 1] * cc[t3 - 1];
          t3 += t0;
          float cr4 = wa3[index3 + i - 2] * cc[t3 - 1] + wa3[index3 + i - 1] * cc[t3];
          float ci4 = wa3[index3 + i - 2] * cc[t3] - wa3[index3 + i - 1] * cc[t3 - 1];
          float tr1 = cr2 + cr4;
          float tr4 = cr4 - cr2;
          float ti1 = ci2 + ci4;
          float ti4 = ci2 - ci4;
          float ti2 = cc[t2] + ci3;
          float ti3 = cc[t2] - ci3;
          float tr2 = cc[t2 - 1] + cr3;
          float tr3 = cc[t2 - 1] - cr3;
          ch[t4 - 1] = tr1 + tr2;
          ch[t4] = ti1 + ti2;
          ch[j - 1] = tr3 - ti4;
          ch[j] = tr4 - ti3;
          ch[t4 + m - 1] = ti4 + tr3;
          ch[t4 + m] = tr4 + ti3;
          ch[j + m - 1] = tr2 - tr1;
          ch[j + m] = ti1 - ti2;
        } 
        t1 += ido;
      } 
      if ((ido & 0x1) != 0)
        return; 
    } 
    t2 = (t1 = t0 + ido - 1) + (t0 << 1);
    t3 = ido << 2;
    t4 = ido;
    int t5 = ido << 1;
    int t6 = ido;
    for (k = 0; k < l1; k++) {
      float ti1 = -hsqt2 * (cc[t1] + cc[t2]);
      float tr1 = hsqt2 * (cc[t1] - cc[t2]);
      ch[t4 - 1] = tr1 + cc[t6 - 1];
      ch[t4 + t5 - 1] = cc[t6 - 1] - tr1;
      ch[t4] = ti1 - cc[t1 + t0];
      ch[t4 + t5] = ti1 + cc[t1 + t0];
      t1 += ido;
      t2 += ido;
      t4 += t3;
      t6 += ido;
    } 
  }
  
  static void dradfg(int ido, int ip, int l1, int idl1, float[] cc, float[] c1, float[] c2, float[] ch, float[] ch2, float[] wa, int index) {
    int t2 = 0;
    float dcp = 0.0F, dsp = 0.0F;
    float arg = tpi / ip;
    dcp = (float)Math.cos(arg);
    dsp = (float)Math.sin(arg);
    int ipph = ip + 1 >> 1;
    int ipp2 = ip;
    int idp2 = ido;
    int nbd = ido - 1 >> 1;
    int t0 = l1 * ido;
    int t10 = ip * ido;
    int state = 100;
    while (true) {
      int i;
      int m;
      int k;
      int l;
      int ik;
      int is;
      int n;
      int i1;
      int i2;
      int i3;
      float ai1;
      float ar1;
      switch (state) {
        case 101:
          if (ido == 1) {
            state = 119;
            continue;
          } 
          for (ik = 0; ik < idl1; ik++)
            ch2[ik] = c2[ik]; 
          n = 0;
          for (m = 1; m < ip; m++) {
            n += t0;
            t2 = n;
            for (int i4 = 0; i4 < l1; i4++) {
              ch[t2] = c1[t2];
              t2 += ido;
            } 
          } 
          is = -ido;
          n = 0;
          if (nbd > l1) {
            for (m = 1; m < ip; m++) {
              n += t0;
              is += ido;
              t2 = -ido + n;
              for (int i4 = 0; i4 < l1; i4++) {
                int idij = is - 1;
                t2 += ido;
                int i6 = t2;
                for (int i5 = 2; i5 < ido; i5 += 2) {
                  idij += 2;
                  i6 += 2;
                  ch[i6 - 1] = wa[index + idij - 1] * c1[i6 - 1] + wa[index + idij] * c1[i6];
                  ch[i6] = wa[index + idij - 1] * c1[i6] - wa[index + idij] * c1[i6 - 1];
                } 
              } 
            } 
          } else {
            for (m = 1; m < ip; m++) {
              is += ido;
              int idij = is - 1;
              n += t0;
              t2 = n;
              for (int i4 = 2; i4 < ido; i4 += 2) {
                idij += 2;
                t2 += 2;
                int i6 = t2;
                for (int i5 = 0; i5 < l1; i5++) {
                  ch[i6 - 1] = wa[index + idij - 1] * c1[i6 - 1] + wa[index + idij] * c1[i6];
                  ch[i6] = wa[index + idij - 1] * c1[i6] - wa[index + idij] * c1[i6 - 1];
                  i6 += ido;
                } 
              } 
            } 
          } 
          n = 0;
          t2 = ipp2 * t0;
          if (nbd < l1) {
            for (m = 1; m < ipph; m++) {
              n += t0;
              t2 -= t0;
              int i5 = n;
              int i6 = t2;
              for (int i4 = 2; i4 < ido; i4 += 2) {
                i5 += 2;
                i6 += 2;
                int i8 = i5 - ido;
                int t6 = i6 - ido;
                for (int i7 = 0; i7 < l1; i7++) {
                  i8 += ido;
                  t6 += ido;
                  c1[i8 - 1] = ch[i8 - 1] + ch[t6 - 1];
                  c1[t6 - 1] = ch[i8] - ch[t6];
                  c1[i8] = ch[i8] + ch[t6];
                  c1[t6] = ch[t6 - 1] - ch[i8 - 1];
                } 
              } 
            } 
          } else {
            for (m = 1; m < ipph; m++) {
              n += t0;
              t2 -= t0;
              int i5 = n;
              int i6 = t2;
              for (int i4 = 0; i4 < l1; i4++) {
                int i8 = i5;
                int t6 = i6;
                for (int i7 = 2; i7 < ido; i7 += 2) {
                  i8 += 2;
                  t6 += 2;
                  c1[i8 - 1] = ch[i8 - 1] + ch[t6 - 1];
                  c1[t6 - 1] = ch[i8] - ch[t6];
                  c1[i8] = ch[i8] + ch[t6];
                  c1[t6] = ch[t6 - 1] - ch[i8 - 1];
                } 
                i5 += ido;
                i6 += ido;
              } 
            } 
          } 
        case 119:
          for (ik = 0; ik < idl1; ik++)
            c2[ik] = ch2[ik]; 
          n = 0;
          t2 = ipp2 * idl1;
          for (m = 1; m < ipph; m++) {
            n += t0;
            t2 -= t0;
            int i5 = n - ido;
            int i6 = t2 - ido;
            for (int i4 = 0; i4 < l1; i4++) {
              i5 += ido;
              i6 += ido;
              c1[i5] = ch[i5] + ch[i6];
              c1[i6] = ch[i6] - ch[i5];
            } 
          } 
          ar1 = 1.0F;
          ai1 = 0.0F;
          n = 0;
          t2 = ipp2 * idl1;
          i1 = (ip - 1) * idl1;
          for (l = 1; l < ipph; l++) {
            n += idl1;
            t2 -= idl1;
            float ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            int i4 = n;
            int i5 = t2;
            int t6 = i1;
            int t7 = idl1;
            for (ik = 0; ik < idl1; ik++) {
              ch2[i4++] = c2[ik] + ar1 * c2[t7++];
              ch2[i5++] = ai1 * c2[t6++];
            } 
            float dc2 = ar1;
            float ds2 = ai1;
            float ar2 = ar1;
            float ai2 = ai1;
            i4 = idl1;
            i5 = (ipp2 - 1) * idl1;
            for (m = 2; m < ipph; m++) {
              i4 += idl1;
              i5 -= idl1;
              float ar2h = dc2 * ar2 - ds2 * ai2;
              ai2 = dc2 * ai2 + ds2 * ar2;
              ar2 = ar2h;
              t6 = n;
              t7 = t2;
              int t8 = i4;
              int t9 = i5;
              for (ik = 0; ik < idl1; ik++) {
                ch2[t6++] = ch2[t6++] + ar2 * c2[t8++];
                ch2[t7++] = ch2[t7++] + ai2 * c2[t9++];
              } 
            } 
          } 
          n = 0;
          for (m = 1; m < ipph; m++) {
            n += idl1;
            t2 = n;
            for (ik = 0; ik < idl1; ik++)
              ch2[ik] = ch2[ik] + c2[t2++]; 
          } 
          if (ido < l1) {
            state = 132;
            continue;
          } 
          n = 0;
          t2 = 0;
          for (k = 0; k < l1; k++) {
            i1 = n;
            int i5 = t2;
            for (int i4 = 0; i4 < ido; i4++)
              cc[i5++] = ch[i1++]; 
            n += ido;
            t2 += t10;
          } 
          state = 135;
        case 132:
          for (i = 0; i < ido; i++) {
            n = i;
            t2 = i;
            for (k = 0; k < l1; k++) {
              cc[t2] = ch[n];
              n += ido;
              t2 += t10;
            } 
          } 
        case 135:
          n = 0;
          t2 = ido << 1;
          i1 = 0;
          i2 = ipp2 * t0;
          for (m = 1; m < ipph; m++) {
            n += t2;
            i1 += t0;
            i2 -= t0;
            int i4 = n;
            int t6 = i1;
            int t7 = i2;
            for (k = 0; k < l1; k++) {
              cc[i4 - 1] = ch[t6];
              cc[i4] = ch[t7];
              i4 += t10;
              t6 += ido;
              t7 += ido;
            } 
          } 
          if (ido == 1)
            return; 
          if (nbd < l1) {
            state = 141;
            continue;
          } 
          n = -ido;
          i1 = 0;
          i2 = 0;
          i3 = ipp2 * t0;
          for (m = 1; m < ipph; m++) {
            n += t2;
            i1 += t2;
            i2 += t0;
            i3 -= t0;
            int t6 = n;
            int t7 = i1;
            int t8 = i2;
            int t9 = i3;
            for (k = 0; k < l1; k++) {
              for (i = 2; i < ido; i += 2) {
                int ic = idp2 - i;
                cc[i + t7 - 1] = ch[i + t8 - 1] + ch[i + t9 - 1];
                cc[ic + t6 - 1] = ch[i + t8 - 1] - ch[i + t9 - 1];
                cc[i + t7] = ch[i + t8] + ch[i + t9];
                cc[ic + t6] = ch[i + t9] - ch[i + t8];
              } 
              t6 += t10;
              t7 += t10;
              t8 += ido;
              t9 += ido;
            } 
          } 
          return;
        case 141:
          break;
      } 
    } 
    int t1 = -ido;
    int t3 = 0;
    int t4 = 0;
    int t5 = ipp2 * t0;
    for (int j = 1; j < ipph; j++) {
      t1 += t2;
      t3 += t2;
      t4 += t0;
      t5 -= t0;
      for (int i = 2; i < ido; i += 2) {
        int t6 = idp2 + t1 - i;
        int t7 = i + t3;
        int t8 = i + t4;
        int t9 = i + t5;
        for (int k = 0; k < l1; k++) {
          cc[t7 - 1] = ch[t8 - 1] + ch[t9 - 1];
          cc[t6 - 1] = ch[t8 - 1] - ch[t9 - 1];
          cc[t7] = ch[t8] + ch[t9];
          cc[t6] = ch[t9] - ch[t8];
          t6 += t10;
          t7 += t10;
          t8 += ido;
          t9 += ido;
        } 
      } 
    } 
  }
  
  static void drftf1(int n, float[] c, float[] ch, float[] wa, int[] ifac) {
    int nf = ifac[1];
    int na = 1;
    int l2 = n;
    int iw = n;
    for (int k1 = 0; k1 < nf; k1++) {
      int kh = nf - k1;
      int ip = ifac[kh + 1];
      int l1 = l2 / ip;
      int ido = n / l2;
      int idl1 = ido * l1;
      iw -= (ip - 1) * ido;
      na = 1 - na;
      int state = 100;
      while (true) {
        int ix2;
        int ix3;
        switch (state) {
          case 100:
            if (ip != 4) {
              state = 102;
              continue;
            } 
            ix2 = iw + ido;
            ix3 = ix2 + ido;
            if (na != 0) {
              dradf4(ido, l1, ch, c, wa, iw - 1, wa, ix2 - 1, wa, ix3 - 1);
            } else {
              dradf4(ido, l1, c, ch, wa, iw - 1, wa, ix2 - 1, wa, ix3 - 1);
            } 
            state = 110;
          case 102:
            if (ip != 2) {
              state = 104;
              continue;
            } 
            if (na != 0) {
              state = 103;
              continue;
            } 
            dradf2(ido, l1, c, ch, wa, iw - 1);
            state = 110;
          case 103:
            dradf2(ido, l1, ch, c, wa, iw - 1);
          case 104:
            if (ido == 1)
              na = 1 - na; 
            if (na != 0) {
              state = 109;
              continue;
            } 
            dradfg(ido, ip, l1, idl1, c, c, c, ch, ch, wa, iw - 1);
            na = 1;
            state = 110;
          case 109:
            dradfg(ido, ip, l1, idl1, ch, ch, ch, c, c, wa, iw - 1);
            na = 0;
            break;
          case 110:
            break;
        } 
      } 
      l2 = l1;
    } 
    if (na == 1)
      return; 
    for (int i = 0; i < n; i++)
      c[i] = ch[i]; 
  }
  
  static void dradb2(int ido, int l1, float[] cc, float[] ch, float[] wa1, int index) {
    int t0 = l1 * ido;
    int t1 = 0;
    int t2 = 0;
    int t3 = (ido << 1) - 1;
    int k;
    for (k = 0; k < l1; k++) {
      ch[t1] = cc[t2] + cc[t3 + t2];
      ch[t1 + t0] = cc[t2] - cc[t3 + t2];
      t2 = (t1 += ido) << 1;
    } 
    if (ido < 2)
      return; 
    if (ido != 2) {
      t1 = 0;
      t2 = 0;
      for (k = 0; k < l1; k++) {
        t3 = t1;
        int t4, t5 = (t4 = t2) + (ido << 1);
        int t6 = t0 + t1;
        for (int i = 2; i < ido; i += 2) {
          t3 += 2;
          t4 += 2;
          t5 -= 2;
          t6 += 2;
          ch[t3 - 1] = cc[t4 - 1] + cc[t5 - 1];
          float tr2 = cc[t4 - 1] - cc[t5 - 1];
          ch[t3] = cc[t4] - cc[t5];
          float ti2 = cc[t4] + cc[t5];
          ch[t6 - 1] = wa1[index + i - 2] * tr2 - wa1[index + i - 1] * ti2;
          ch[t6] = wa1[index + i - 2] * ti2 + wa1[index + i - 1] * tr2;
        } 
        t2 = (t1 += ido) << 1;
      } 
      if (ido % 2 == 1)
        return; 
    } 
    t1 = ido - 1;
    t2 = ido - 1;
    for (k = 0; k < l1; k++) {
      ch[t1] = cc[t2] + cc[t2];
      ch[t1 + t0] = -(cc[t2 + 1] + cc[t2 + 1]);
      t1 += ido;
      t2 += ido << 1;
    } 
  }
  
  static void dradb3(int ido, int l1, float[] cc, float[] ch, float[] wa1, int index1, float[] wa2, int index2) {
    int t0 = l1 * ido;
    int t1 = 0;
    int t2 = t0 << 1;
    int t3 = ido << 1;
    int t4 = ido + (ido << 1);
    int t5 = 0;
    int k;
    for (k = 0; k < l1; k++) {
      float tr2 = cc[t3 - 1] + cc[t3 - 1];
      float cr2 = cc[t5] + taur * tr2;
      ch[t1] = cc[t5] + tr2;
      float ci3 = taui * (cc[t3] + cc[t3]);
      ch[t1 + t0] = cr2 - ci3;
      ch[t1 + t2] = cr2 + ci3;
      t1 += ido;
      t3 += t4;
      t5 += t4;
    } 
    if (ido == 1)
      return; 
    t1 = 0;
    t3 = ido << 1;
    for (k = 0; k < l1; k++) {
      int t7 = t1 + (t1 << 1);
      int t6 = t5 = t7 + t3;
      int t8 = t1;
      int t9, t10 = (t9 = t1 + t0) + t0;
      for (int i = 2; i < ido; i += 2) {
        t5 += 2;
        t6 -= 2;
        t7 += 2;
        t8 += 2;
        t9 += 2;
        t10 += 2;
        float tr2 = cc[t5 - 1] + cc[t6 - 1];
        float cr2 = cc[t7 - 1] + taur * tr2;
        ch[t8 - 1] = cc[t7 - 1] + tr2;
        float ti2 = cc[t5] - cc[t6];
        float ci2 = cc[t7] + taur * ti2;
        ch[t8] = cc[t7] + ti2;
        float cr3 = taui * (cc[t5 - 1] - cc[t6 - 1]);
        float ci3 = taui * (cc[t5] + cc[t6]);
        float dr2 = cr2 - ci3;
        float dr3 = cr2 + ci3;
        float di2 = ci2 + cr3;
        float di3 = ci2 - cr3;
        ch[t9 - 1] = wa1[index1 + i - 2] * dr2 - wa1[index1 + i - 1] * di2;
        ch[t9] = wa1[index1 + i - 2] * di2 + wa1[index1 + i - 1] * dr2;
        ch[t10 - 1] = wa2[index2 + i - 2] * dr3 - wa2[index2 + i - 1] * di3;
        ch[t10] = wa2[index2 + i - 2] * di3 + wa2[index2 + i - 1] * dr3;
      } 
      t1 += ido;
    } 
  }
  
  static void dradb4(int ido, int l1, float[] cc, float[] ch, float[] wa1, int index1, float[] wa2, int index2, float[] wa3, int index3) {
    int t0 = l1 * ido;
    int t1 = 0;
    int t2 = ido << 2;
    int t3 = 0;
    int t6 = ido << 1;
    int k;
    for (k = 0; k < l1; k++) {
      int i = t3 + t6;
      int t5 = t1;
      float tr3 = cc[i - 1] + cc[i - 1];
      float tr4 = cc[i] + cc[i];
      float tr1 = cc[t3] - cc[(i += t6) - 1];
      float tr2 = cc[t3] + cc[i - 1];
      ch[t5] = tr2 + tr3;
      ch[t5 += t0] = tr1 - tr4;
      ch[t5 += t0] = tr2 - tr3;
      ch[t5 += t0] = tr1 + tr4;
      t1 += ido;
      t3 += t2;
    } 
    if (ido < 2)
      return; 
    if (ido != 2) {
      t1 = 0;
      for (k = 0; k < l1; k++) {
        int j, t5 = (j = t3 = (t2 = t1 << 2) + t6) + t6;
        int t7 = t1;
        for (int i = 2; i < ido; i += 2) {
          t2 += 2;
          t3 += 2;
          j -= 2;
          t5 -= 2;
          t7 += 2;
          float ti1 = cc[t2] + cc[t5];
          float ti2 = cc[t2] - cc[t5];
          float ti3 = cc[t3] - cc[j];
          float tr4 = cc[t3] + cc[j];
          float tr1 = cc[t2 - 1] - cc[t5 - 1];
          float tr2 = cc[t2 - 1] + cc[t5 - 1];
          float ti4 = cc[t3 - 1] - cc[j - 1];
          float tr3 = cc[t3 - 1] + cc[j - 1];
          ch[t7 - 1] = tr2 + tr3;
          float cr3 = tr2 - tr3;
          ch[t7] = ti2 + ti3;
          float ci3 = ti2 - ti3;
          float cr2 = tr1 - tr4;
          float cr4 = tr1 + tr4;
          float ci2 = ti1 + ti4;
          float ci4 = ti1 - ti4;
          int t8;
          ch[(t8 = t7 + t0) - 1] = wa1[index1 + i - 2] * cr2 - wa1[index1 + i - 1] * ci2;
          ch[t8] = wa1[index1 + i - 2] * ci2 + wa1[index1 + i - 1] * cr2;
          ch[(t8 += t0) - 1] = wa2[index2 + i - 2] * cr3 - wa2[index2 + i - 1] * ci3;
          ch[t8] = wa2[index2 + i - 2] * ci3 + wa2[index2 + i - 1] * cr3;
          ch[(t8 += t0) - 1] = wa3[index3 + i - 2] * cr4 - wa3[index3 + i - 1] * ci4;
          ch[t8] = wa3[index3 + i - 2] * ci4 + wa3[index3 + i - 1] * cr4;
        } 
        t1 += ido;
      } 
      if (ido % 2 == 1)
        return; 
    } 
    t1 = ido;
    t2 = ido << 2;
    t3 = ido - 1;
    int t4 = ido + (ido << 1);
    for (k = 0; k < l1; k++) {
      int t5 = t3;
      float ti1 = cc[t1] + cc[t4];
      float ti2 = cc[t4] - cc[t1];
      float tr1 = cc[t1 - 1] - cc[t4 - 1];
      float tr2 = cc[t1 - 1] + cc[t4 - 1];
      ch[t5] = tr2 + tr2;
      ch[t5 += t0] = sqrt2 * (tr1 - ti1);
      ch[t5 += t0] = ti2 + ti2;
      ch[t5 += t0] = -sqrt2 * (tr1 + ti1);
      t3 += ido;
      t1 += t2;
      t4 += t2;
    } 
  }
  
  static void dradbg(int ido, int ip, int l1, int idl1, float[] cc, float[] c1, float[] c2, float[] ch, float[] ch2, float[] wa, int index) {
    int ipph = 0, t0 = 0, t10 = 0;
    int nbd = 0;
    float dcp = 0.0F, dsp = 0.0F;
    int ipp2 = 0;
    int state = 100;
    while (true) {
      int i;
      int m;
      int k;
      int l;
      int ik;
      int n;
      int i1;
      int t2;
      int t3;
      int t5;
      int t7;
      int t9;
      float ai1;
      float ar1;
      float arg;
      switch (state) {
        case 100:
          t10 = ip * ido;
          t0 = l1 * ido;
          arg = tpi / ip;
          dcp = (float)Math.cos(arg);
          dsp = (float)Math.sin(arg);
          nbd = ido - 1 >>> 1;
          ipp2 = ip;
          ipph = ip + 1 >>> 1;
          if (ido < l1) {
            state = 103;
            continue;
          } 
          i1 = 0;
          t2 = 0;
          for (k = 0; k < l1; k++) {
            int i3 = i1;
            int t4 = t2;
            for (int i2 = 0; i2 < ido; i2++) {
              ch[i3] = cc[t4];
              i3++;
              t4++;
            } 
            i1 += ido;
            t2 += t10;
          } 
          state = 106;
        case 103:
          i1 = 0;
          for (i = 0; i < ido; i++) {
            t2 = i1;
            int i2 = i1;
            for (k = 0; k < l1; k++) {
              ch[t2] = cc[i2];
              t2 += ido;
              i2 += t10;
            } 
            i1++;
          } 
        case 106:
          i1 = 0;
          t2 = ipp2 * t0;
          t7 = t5 = ido << 1;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            int i2 = i1;
            int t4 = t2;
            int t6 = t5;
            for (k = 0; k < l1; k++) {
              ch[i2] = cc[t6 - 1] + cc[t6 - 1];
              ch[t4] = cc[t6] + cc[t6];
              i2 += ido;
              t4 += ido;
              t6 += t10;
            } 
            t5 += t7;
          } 
          if (ido == 1) {
            state = 116;
            continue;
          } 
          if (nbd < l1) {
            state = 112;
            continue;
          } 
          i1 = 0;
          t2 = ipp2 * t0;
          t7 = 0;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            int i2 = i1;
            int t4 = t2;
            t7 += ido << 1;
            int t8 = t7;
            for (k = 0; k < l1; k++) {
              t5 = i2;
              int t6 = t4;
              int i3 = t8;
              int t11 = t8;
              for (i = 2; i < ido; i += 2) {
                t5 += 2;
                t6 += 2;
                i3 += 2;
                t11 -= 2;
                ch[t5 - 1] = cc[i3 - 1] + cc[t11 - 1];
                ch[t6 - 1] = cc[i3 - 1] - cc[t11 - 1];
                ch[t5] = cc[i3] - cc[t11];
                ch[t6] = cc[i3] + cc[t11];
              } 
              i2 += ido;
              t4 += ido;
              t8 += t10;
            } 
          } 
          state = 116;
        case 112:
          i1 = 0;
          t2 = ipp2 * t0;
          t7 = 0;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            int i2 = i1;
            int t4 = t2;
            t7 += ido << 1;
            int t8 = t7;
            int i3 = t7;
            for (i = 2; i < ido; i += 2) {
              i2 += 2;
              t4 += 2;
              t8 += 2;
              i3 -= 2;
              t5 = i2;
              int t6 = t4;
              int t11 = t8;
              int t12 = i3;
              for (k = 0; k < l1; k++) {
                ch[t5 - 1] = cc[t11 - 1] + cc[t12 - 1];
                ch[t6 - 1] = cc[t11 - 1] - cc[t12 - 1];
                ch[t5] = cc[t11] - cc[t12];
                ch[t6] = cc[t11] + cc[t12];
                t5 += ido;
                t6 += ido;
                t11 += t10;
                t12 += t10;
              } 
            } 
          } 
        case 116:
          ar1 = 1.0F;
          ai1 = 0.0F;
          i1 = 0;
          t9 = t2 = ipp2 * idl1;
          t3 = (ip - 1) * idl1;
          for (l = 1; l < ipph; l++) {
            i1 += idl1;
            t2 -= idl1;
            float ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            int t4 = i1;
            t5 = t2;
            int t6 = 0;
            t7 = idl1;
            int t8 = t3;
            int i2;
            for (i2 = 0; i2 < idl1; i2++) {
              c2[t4++] = ch2[t6++] + ar1 * ch2[t7++];
              c2[t5++] = ai1 * ch2[t8++];
            } 
            float dc2 = ar1;
            float ds2 = ai1;
            float ar2 = ar1;
            float ai2 = ai1;
            t6 = idl1;
            t7 = t9 - idl1;
            for (m = 2; m < ipph; m++) {
              t6 += idl1;
              t7 -= idl1;
              float ar2h = dc2 * ar2 - ds2 * ai2;
              ai2 = dc2 * ai2 + ds2 * ar2;
              ar2 = ar2h;
              t4 = i1;
              t5 = t2;
              int t11 = t6;
              int t12 = t7;
              for (i2 = 0; i2 < idl1; i2++) {
                c2[t4++] = c2[t4++] + ar2 * ch2[t11++];
                c2[t5++] = c2[t5++] + ai2 * ch2[t12++];
              } 
            } 
          } 
          i1 = 0;
          for (m = 1; m < ipph; m++) {
            i1 += idl1;
            t2 = i1;
            for (int i2 = 0; i2 < idl1; i2++)
              ch2[i2] = ch2[i2] + ch2[t2++]; 
          } 
          i1 = 0;
          t2 = ipp2 * t0;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            t3 = i1;
            int t4 = t2;
            for (k = 0; k < l1; k++) {
              ch[t3] = c1[t3] - c1[t4];
              ch[t4] = c1[t3] + c1[t4];
              t3 += ido;
              t4 += ido;
            } 
          } 
          if (ido == 1) {
            state = 132;
            continue;
          } 
          if (nbd < l1) {
            state = 128;
            continue;
          } 
          i1 = 0;
          t2 = ipp2 * t0;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            t3 = i1;
            int t4 = t2;
            for (k = 0; k < l1; k++) {
              t5 = t3;
              int t6 = t4;
              for (i = 2; i < ido; i += 2) {
                t5 += 2;
                t6 += 2;
                ch[t5 - 1] = c1[t5 - 1] - c1[t6];
                ch[t6 - 1] = c1[t5 - 1] + c1[t6];
                ch[t5] = c1[t5] + c1[t6 - 1];
                ch[t6] = c1[t5] - c1[t6 - 1];
              } 
              t3 += ido;
              t4 += ido;
            } 
          } 
          state = 132;
        case 128:
          i1 = 0;
          t2 = ipp2 * t0;
          for (m = 1; m < ipph; m++) {
            i1 += t0;
            t2 -= t0;
            t3 = i1;
            int t4 = t2;
            for (i = 2; i < ido; i += 2) {
              t3 += 2;
              t4 += 2;
              t5 = t3;
              int t6 = t4;
              for (k = 0; k < l1; k++) {
                ch[t5 - 1] = c1[t5 - 1] - c1[t6];
                ch[t6 - 1] = c1[t5 - 1] + c1[t6];
                ch[t5] = c1[t5] + c1[t6 - 1];
                ch[t6] = c1[t5] - c1[t6 - 1];
                t5 += ido;
                t6 += ido;
              } 
            } 
          } 
        case 132:
          if (ido == 1)
            return; 
          for (ik = 0; ik < idl1; ik++)
            c2[ik] = ch2[ik]; 
          i1 = 0;
          for (m = 1; m < ip; m++) {
            t2 = i1 += t0;
            for (k = 0; k < l1; k++) {
              c1[t2] = ch[t2];
              t2 += ido;
            } 
          } 
          if (nbd > l1) {
            state = 139;
            continue;
          } 
          n = -ido - 1;
          i1 = 0;
          for (m = 1; m < ip; m++) {
            n += ido;
            i1 += t0;
            int idij = n;
            t2 = i1;
            for (i = 2; i < ido; i += 2) {
              t2 += 2;
              idij += 2;
              t3 = t2;
              for (k = 0; k < l1; k++) {
                c1[t3 - 1] = wa[index + idij - 1] * ch[t3 - 1] - wa[index + idij] * ch[t3];
                c1[t3] = wa[index + idij - 1] * ch[t3] + wa[index + idij] * ch[t3 - 1];
                t3 += ido;
              } 
            } 
          } 
          return;
        case 139:
          break;
      } 
    } 
    int is = -ido - 1;
    int t1 = 0;
    for (int j = 1; j < ip; j++) {
      is += ido;
      t1 += t0;
      int t2 = t1;
      for (int k = 0; k < l1; k++) {
        int idij = is;
        int t3 = t2;
        for (int i = 2; i < ido; i += 2) {
          idij += 2;
          t3 += 2;
          c1[t3 - 1] = wa[index + idij - 1] * ch[t3 - 1] - wa[index + idij] * ch[t3];
          c1[t3] = wa[index + idij - 1] * ch[t3] + wa[index + idij] * ch[t3 - 1];
        } 
        t2 += ido;
      } 
    } 
  }
  
  static void drftb1(int n, float[] c, float[] ch, float[] wa, int index, int[] ifac) {
    int l2 = 0;
    int ip = 0, ido = 0, idl1 = 0;
    int nf = ifac[1];
    int na = 0;
    int l1 = 1;
    int iw = 1;
    for (int k1 = 0; k1 < nf; k1++) {
      int state = 100;
      while (true) {
        int ix2;
        int ix3;
        switch (state) {
          case 100:
            ip = ifac[k1 + 2];
            l2 = ip * l1;
            ido = n / l2;
            idl1 = ido * l1;
            if (ip != 4) {
              state = 103;
              continue;
            } 
            ix2 = iw + ido;
            ix3 = ix2 + ido;
            if (na != 0) {
              dradb4(ido, l1, ch, c, wa, index + iw - 1, wa, index + ix2 - 1, wa, index + 
                  ix3 - 1);
            } else {
              dradb4(ido, l1, c, ch, wa, index + iw - 1, wa, index + ix2 - 1, wa, index + 
                  ix3 - 1);
            } 
            na = 1 - na;
            state = 115;
          case 103:
            if (ip != 2) {
              state = 106;
              continue;
            } 
            if (na != 0) {
              dradb2(ido, l1, ch, c, wa, index + iw - 1);
            } else {
              dradb2(ido, l1, c, ch, wa, index + iw - 1);
            } 
            na = 1 - na;
            state = 115;
          case 106:
            if (ip != 3) {
              state = 109;
              continue;
            } 
            ix2 = iw + ido;
            if (na != 0) {
              dradb3(ido, l1, ch, c, wa, index + iw - 1, wa, index + ix2 - 1);
            } else {
              dradb3(ido, l1, c, ch, wa, index + iw - 1, wa, index + ix2 - 1);
            } 
            na = 1 - na;
            state = 115;
          case 109:
            if (na != 0) {
              dradbg(ido, ip, l1, idl1, ch, ch, ch, c, c, wa, index + iw - 1);
            } else {
              dradbg(ido, ip, l1, idl1, c, c, c, ch, ch, wa, index + iw - 1);
            } 
            if (ido == 1)
              na = 1 - na; 
            break;
          case 115:
            break;
        } 
      } 
      l1 = l2;
      iw += (ip - 1) * ido;
    } 
    if (na == 0)
      return; 
    for (int i = 0; i < n; i++)
      c[i] = ch[i]; 
  }
}
