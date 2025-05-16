package haven;

import java.awt.Color;
import java.awt.font.TextAttribute;
import java.util.LinkedList;
import java.util.List;

public class Textlog extends Widget {
  static Tex texpap = Resource.loadtex("gfx/hud/texpap");
  
  static Tex schain = Resource.loadtex("gfx/hud/schain");
  
  static Tex sflarp = Resource.loadtex("gfx/hud/sflarp");
  
  static RichText.Foundry deffnd = new RichText.Foundry(new Object[] { TextAttribute.FAMILY, "SansSerif", TextAttribute.SIZE, Integer.valueOf(12), TextAttribute.FOREGROUND, Color.BLACK });
  
  List<Text> lines;
  
  int maxy;
  
  int cury;
  
  int margin = 3;
  
  boolean sdrag = false;
  
  boolean quote = true;
  
  public int maxLines = 150;
  
  public RichText.Foundry fnd = deffnd;
  
  @RName("log")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new Textlog(c, (Coord)args[0], parent);
    }
  }
  
  public void draw(GOut g) {
    Coord dc = new Coord();
    for (dc.y = 0; dc.y < this.sz.y; dc.y += (texpap.sz()).y) {
      for (dc.x = 0; dc.x < this.sz.x; dc.x += (texpap.sz()).x)
        g.image(texpap, dc); 
    } 
    g.chcolor();
    int y = -this.cury;
    synchronized (this.lines) {
      for (Text line : this.lines) {
        int dy1 = this.sz.y + y;
        int dy2 = dy1 + (line.sz()).y;
        if (dy2 > 0 && dy1 < this.sz.y)
          g.image(line.tex(), new Coord(this.margin, dy1)); 
        y += (line.sz()).y;
      } 
    } 
    if (this.maxy > this.sz.y) {
      int fx = this.sz.x - (sflarp.sz()).x;
      int cx = fx + (sflarp.sz()).x / 2 - (schain.sz()).x / 2;
      for (y = 0; y < this.sz.y; y += (schain.sz()).y - 1)
        g.image(schain, new Coord(cx, y)); 
      double a = (this.cury - this.sz.y) / (this.maxy - this.sz.y);
      int fy = (int)((this.sz.y - (sflarp.sz()).y) * a);
      g.image(sflarp, new Coord(fx, fy));
    } 
  }
  
  public Textlog(Coord c, Coord sz, Widget parent) {
    super(c, sz, parent);
    this.lines = new LinkedList<>();
    this.maxy = this.cury = 0;
  }
  
  public void append(String line, Color col) {
    Text rl;
    if (this.quote)
      line = RichText.Parser.quote(line); 
    if (col == null) {
      rl = this.fnd.render(line, this.sz.x - this.margin * 2 - (sflarp.sz()).x, new Object[0]);
    } else {
      rl = this.fnd.render(line, this.sz.x - this.margin * 2 - (sflarp.sz()).x, new Object[] { TextAttribute.FOREGROUND, col });
    } 
    synchronized (this.lines) {
      this.lines.add(rl);
      if (this.maxLines > 0 && this.lines.size() > this.maxLines) {
        Text tl = this.lines.remove(0);
        int dy = (tl.sz()).y;
        this.maxy -= dy;
        this.cury -= dy;
      } 
    } 
    if (this.cury == this.maxy)
      this.cury += (rl.sz()).y; 
    this.maxy += (rl.sz()).y;
  }
  
  public void append(String line) {
    append(line, (Color)null);
  }
  
  public void uimsg(String msg, Object... args) {
    if (msg == "apnd")
      append((String)args[0]); 
  }
  
  public boolean mousewheel(Coord c, int amount) {
    this.cury += amount * 20;
    if (this.cury < this.sz.y)
      this.cury = this.sz.y; 
    if (this.cury > this.maxy)
      this.cury = this.maxy; 
    return true;
  }
  
  public boolean mousedown(Coord c, int button) {
    if (button != 1)
      return false; 
    int fx = this.sz.x - (sflarp.sz()).x;
    int cx = fx + (sflarp.sz()).x / 2 - (schain.sz()).x / 2;
    if (this.maxy > this.sz.y && c.x >= fx) {
      this.sdrag = true;
      this.ui.grabmouse(this);
      mousemove(c);
      return true;
    } 
    return false;
  }
  
  public void mousemove(Coord c) {
    if (this.sdrag) {
      double a = (c.y - (sflarp.sz()).y / 2) / (this.sz.y - (sflarp.sz()).y);
      if (a < 0.0D)
        a = 0.0D; 
      if (a > 1.0D)
        a = 1.0D; 
      this.cury = (int)(a * (this.maxy - this.sz.y)) + this.sz.y;
    } 
  }
  
  public boolean mouseup(Coord c, int button) {
    if (button == 1 && this.sdrag) {
      this.sdrag = false;
      this.ui.grabmouse(null);
      return true;
    } 
    return false;
  }
  
  public void setprog(double a) {
    if (a < 0.0D)
      a = 0.0D; 
    if (a > 1.0D)
      a = 1.0D; 
    this.cury = (int)(a * (this.maxy - this.sz.y)) + this.sz.y;
  }
}
