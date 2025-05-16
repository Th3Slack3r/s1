package haven.glsl;

import haven.Coord;
import haven.Coord3f;
import haven.GLState;
import haven.GOut;
import haven.Glob;
import haven.PView;

public abstract class MiscLib {
  private static final AutoVarying frageyen = new AutoVarying(Type.VEC3, "s_eyen") {
      protected Expression root(VertexContext vctx) {
        return vctx.eyen.depref();
      }
    };
  
  public static ValBlock.Value frageyen(FragmentContext fctx) {
    return fctx.mainvals.ext(frageyen, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            vals.getClass();
            ValBlock.Value ret = new ValBlock.Value(vals, Type.VEC3, new Symbol.Gen("eyen")) {
                public Expression root() {
                  return MiscLib.frageyen.ref();
                }
              };
            ret.mod(new Macro1<Expression>() {
                  public Expression expand(Expression in) {
                    return Cons.normalize(in);
                  }
                },  0);
            return ret;
          }
        });
  }
  
  public static final AutoVarying fragobjv = new AutoVarying(Type.VEC3, "s_objv") {
      protected Expression root(VertexContext vctx) {
        return Cons.pick(vctx.objv.depref(), "xyz");
      }
    };
  
  public static final AutoVarying fragmapv = new AutoVarying(Type.VEC3, "s_mapv") {
      protected Expression root(VertexContext vctx) {
        return Cons.pick(vctx.mapv.depref(), "xyz");
      }
    };
  
  public static final AutoVarying frageyev = new AutoVarying(Type.VEC3, "s_eyev") {
      protected Expression root(VertexContext vctx) {
        return Cons.pick(vctx.eyev.depref(), "xyz");
      }
    };
  
  private static final Object vertedir_id = new Object();
  
  public static ValBlock.Value vertedir(final VertexContext vctx) {
    return vctx.mainvals.ext(vertedir_id, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            vals.getClass();
            return new ValBlock.Value(vals, Type.VEC3, new Symbol.Gen("edir")) {
                public Expression root() {
                  return Cons.neg(Cons.normalize(Cons.pick(vctx.eyev.depref(), "xyz")));
                }
              };
          }
        });
  }
  
  private static final Object fragedir_id = new Object();
  
  public static ValBlock.Value fragedir(FragmentContext fctx) {
    return fctx.mainvals.ext(fragedir_id, new ValBlock.Factory() {
          public ValBlock.Value make(ValBlock vals) {
            vals.getClass();
            return new ValBlock.Value(vals, Type.VEC3, new Symbol.Gen("edir")) {
                public Expression root() {
                  return Cons.neg(Cons.normalize(MiscLib.frageyev.ref()));
                }
              };
          }
        });
  }
  
  public static final Uniform maploc = new Uniform.AutoApply(Type.VEC3, new GLState.Slot[] { PView.loc }) {
      public void apply(GOut g, int loc) {
        Coord3f orig = g.st.wxf.mul4(Coord3f.o);
        orig.z = (((PView.RenderContext)g.st.get(PView.ctx)).glob()).map.getcz(orig.x, -orig.y);
        g.gl.glUniform3f(loc, orig.x, orig.y, orig.z);
      }
    };
  
  public static final Uniform time = new Uniform.AutoApply(Type.FLOAT, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        g.gl.glUniform1f(loc, (float)(System.currentTimeMillis() % 3000000L) / 1000.0F);
      }
    };
  
  public static final Uniform globtime = new Uniform.AutoApply(Type.FLOAT, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        Glob glob = ((PView.RenderContext)g.st.cur(PView.ctx)).glob();
        g.gl.glUniform1f(loc, (float)(glob.globtime() % 10000000L) / 1000.0F);
      }
    };
  
  public static Coord ssz(GOut g) {
    PView.RenderState wnd = (PView.RenderState)g.st.cur(PView.wnd);
    if (wnd == null)
      return g.sz; 
    return wnd.sz();
  }
  
  public static final Uniform pixelpitch = new Uniform.AutoApply(Type.VEC2, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        Coord sz = MiscLib.ssz(g);
        g.gl.glUniform2f(loc, 1.0F / sz.x, 1.0F / sz.y);
      }
    };
  
  public static final Uniform screensize = new Uniform.AutoApply(Type.VEC2, new GLState.Slot[0]) {
      public void apply(GOut g, int loc) {
        Coord sz = MiscLib.ssz(g);
        g.gl.glUniform2f(loc, sz.x, sz.y);
      }
    };
}
