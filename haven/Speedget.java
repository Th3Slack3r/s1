package haven;

public class Speedget extends Widget {
  public static final Tex[][] imgs = new Tex[4][3];
  
  public static final Coord tsz;
  
  private int cur;
  
  private int max;
  
  static {
    String[] names = { "crawl", "walk", "run", "sprint" };
    String[] vars = { "dis", "off", "on" };
    int w = 0;
    for (int i = 0; i < 4; i++) {
      for (int o = 0; o < 3; o++)
        imgs[i][o] = Resource.loadtex("gfx/hud/meter/rmeter/" + names[i] + "-" + vars[o]); 
      w += (imgs[i][0].sz()).x;
    } 
    tsz = new Coord(w, (imgs[0][0].sz()).y);
  }
  
  @RName("speedget")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      int cur = ((Integer)args[0]).intValue();
      int max = ((Integer)args[1]).intValue();
      return new Speedget(c, parent, cur, max);
    }
  }
  
  public Speedget(Coord c, Widget parent, int cur, int max) {
    super(c, tsz, parent);
    this.cur = cur;
    this.max = max;
  }
  
  public void draw(GOut g) {
    int x = 0;
    for (int i = 0; i < 4; i++) {
      Tex t;
      if (i == this.cur) {
        t = imgs[i][2];
      } else if (i > this.max) {
        t = imgs[i][0];
      } else {
        t = imgs[i][1];
      } 
      g.image(t, new Coord(x, 0));
      x += (t.sz()).x;
    } 
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "cur") {
      this.cur = ((Integer)args[0]).intValue();
    } else if (msg == "max") {
      this.max = ((Integer)args[0]).intValue();
    } 
  }
  
  public boolean mousedown(Coord c, int button) {
    int x = 0;
    for (int i = 0; i < 4; i++) {
      x += (imgs[i][0].sz()).x;
      if (c.x < x) {
        wdgmsg("set", new Object[] { Integer.valueOf(i) });
        break;
      } 
    } 
    return true;
  }
  
  public boolean mousewheel(Coord c, int amount) {
    if (this.max >= 0)
      wdgmsg("set", new Object[] { Integer.valueOf((this.cur + this.max + 1 + amount) % (this.max + 1)) }); 
    return true;
  }
}
