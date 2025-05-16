package haven.resutil;

import haven.Coord;
import haven.Coord3f;
import haven.GLState;
import haven.MapMesh;
import haven.Material;
import haven.MeshBuf;
import haven.Resource;
import haven.Tex;
import haven.Tiler;
import java.util.Random;

public class CaveTile extends Tiler {
  public final Resource.Tileset set;
  
  public final int h;
  
  public final Material wtex;
  
  public CaveTile(int id, Resource.Tileset set, int h, Tex wtex) {
    super(id);
    this.set = set;
    this.h = h;
    this.wtex = new Material(wtex);
  }
  
  public void lay(MapMesh m, Random rnd, Coord lc, Coord gc) {
    Resource.Tile g = (Resource.Tile)this.set.ground.pick(rnd);
    m.getClass();
    new MapMesh.Plane(m, m.gnd(), lc, 0, g);
  }
  
  private void wall(MeshBuf buf, MapMesh.SPoint s1, MapMesh.SPoint s2, Coord3f nrm) {
    MeshBuf.Tex ta = (MeshBuf.Tex)buf.layer(MeshBuf.tex);
    buf.getClass();
    MeshBuf.Vertex v1 = new MeshBuf.Vertex(buf, s1.pos, nrm);
    buf.getClass();
    MeshBuf.Vertex v2 = new MeshBuf.Vertex(buf, s2.pos, nrm);
    buf.getClass();
    MeshBuf.Vertex v3 = new MeshBuf.Vertex(buf, s2.pos.add(0.0F, 0.0F, this.h), nrm);
    buf.getClass();
    MeshBuf.Vertex v4 = new MeshBuf.Vertex(buf, s1.pos.add(0.0F, 0.0F, this.h), nrm);
    ta.set(v1, new Coord3f(0.0F, 1.0F, 0.0F));
    ta.set(v2, new Coord3f(1.0F, 1.0F, 0.0F));
    ta.set(v3, new Coord3f(1.0F, 0.0F, 0.0F));
    ta.set(v4, new Coord3f(0.0F, 0.0F, 0.0F));
    buf.getClass();
    new MeshBuf.Face(buf, v1, v3, v4);
    buf.getClass();
    new MeshBuf.Face(buf, v1, v2, v3);
  }
  
  public void trans(MapMesh m, Random rnd, Tiler gt, Coord lc, Coord gc, int z, int bmask, int cmask) {
    int cid = m.map.gettile(gc);
    if (cid <= this.id || m.map.tiler(cid) instanceof CaveTile)
      return; 
    if (bmask == 0)
      return; 
    MeshBuf buf = MapMesh.Models.get(m, (GLState)this.wtex);
    MapMesh.Surface gnd = m.gnd();
    if ((bmask & 0x1) != 0)
      wall(buf, gnd.spoint(lc.add(0, 1)), gnd.spoint(lc), new Coord3f(1.0F, 0.0F, 0.0F)); 
    if ((bmask & 0x2) != 0)
      wall(buf, gnd.spoint(lc), gnd.spoint(lc.add(1, 0)), new Coord3f(0.0F, -1.0F, 0.0F)); 
    if ((bmask & 0x4) != 0)
      wall(buf, gnd.spoint(lc.add(1, 0)), gnd.spoint(lc.add(1, 1)), new Coord3f(-1.0F, 0.0F, 0.0F)); 
    if ((bmask & 0x8) != 0)
      wall(buf, gnd.spoint(lc.add(1, 1)), gnd.spoint(lc.add(0, 1)), new Coord3f(0.0F, 1.0F, 0.0F)); 
  }
}
