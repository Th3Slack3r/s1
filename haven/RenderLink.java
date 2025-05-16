package haven;

public interface RenderLink {
  Rendered make();
  
  @LayerName("rlink")
  public static class Res extends Resource.Layer {
    public final transient RenderLink l;
    
    public Res(Resource res, byte[] buf) {
      super(res);
      int t = buf[0];
      int[] off = { 1 };
      if (t == 0) {
        String meshnm = Utils.strd(buf, off);
        int meshver = Utils.uint16d(buf, off[0]);
        off[0] = off[0] + 2;
        final int meshid = Utils.int16d(buf, off[0]);
        off[0] = off[0] + 2;
        String matnm = Utils.strd(buf, off);
        int matver = Utils.uint16d(buf, off[0]);
        off[0] = off[0] + 2;
        final int matid = Utils.int16d(buf, off[0]);
        off[0] = off[0] + 2;
        final Resource mesh = Resource.load(meshnm, meshver);
        final Resource mat = Resource.load(matnm, matver);
        this.l = new RenderLink() {
            Rendered res = null;
            
            public Rendered make() {
              if (this.res == null) {
                FastMesh m = null;
                for (FastMesh.MeshRes mr : mesh.<FastMesh.MeshRes>layers(FastMesh.MeshRes.class)) {
                  if (meshid < 0 || mr.id == meshid) {
                    m = mr.m;
                    break;
                  } 
                } 
                Material M = null;
                for (Material.Res mr : mat.<Material.Res>layers(Material.Res.class)) {
                  if (matid < 0 || mr.id == matid) {
                    M = mr.get();
                    break;
                  } 
                } 
                this.res = M.apply(m);
              } 
              return this.res;
            }
          };
      } else if (t == 1) {
        String nm = Utils.strd(buf, off);
        int ver = Utils.uint16d(buf, off[0]);
        off[0] = off[0] + 2;
        final Resource amb = Resource.load(nm, ver);
        this.l = new RenderLink() {
            public Rendered make() {
              return new ActAudio.Ambience(amb);
            }
          };
      } else {
        throw new Resource.LoadException("Invalid renderlink type: " + t, getres());
      } 
    }
    
    public void init() {}
  }
}
