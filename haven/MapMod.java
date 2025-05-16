package haven;

public class MapMod extends Window implements MapView.Grabber {
  MapView mv;
  
  MapView.GrabXL grab;
  
  MCache.Overlay ol;
  
  MCache map;
  
  boolean walkmod;
  
  CheckBox cbox;
  
  Button btn;
  
  Label text;
  
  Coord sc;
  
  Coord c1;
  
  Coord c2;
  
  TextEntry tilenm;
  
  public static final String fmt = "Selected: %d×%d";
  
  @RName("mapmod")
  public static class $_ implements Widget.Factory {
    public Widget create(Coord c, Widget parent, Object[] args) {
      return new MapMod(c, parent);
    }
  }
  
  public MapMod(Coord c, Widget parent) {
    super(c, new Coord(200, 100), parent, "Kartlasskostning");
    this.map = this.ui.sess.glob.map;
    this.mv = ((GameUI)getparent((Class)GameUI.class)).map;
    this.mv.getClass();
    this.grab = new MapView.GrabXL(this.mv, this);
    this.walkmod = false;
    this.mv.enol(new int[] { 17 });
    this.mv.grab(this.grab);
    this.cbox = new CheckBox(Coord.z, this, "Walk drawing");
    this.cbox.canactivate = true;
    this.btn = new Button(this.asz.add(-50, -30), Integer.valueOf(40), this, "Change");
    this.text = new Label(Coord.z, this, String.format("Selected: %d×%d", new Object[] { Integer.valueOf(0), Integer.valueOf(0) }));
    this.tilenm = new TextEntry(new Coord(0, 40), new Coord(50, 17), this, "");
    this.tilenm.canactivate = true;
  }
  
  public void destroy() {
    this.mv.disol(new int[] { 17 });
    if (!this.walkmod)
      this.mv.release(this.grab); 
    if (this.ol != null)
      this.ol.destroy(); 
    super.destroy();
  }
  
  public boolean mmousedown(Coord mc, int button) {
    if (button != 1)
      return false; 
    Coord tc = mc.div(MCache.tilesz);
    if (this.ol != null)
      this.ol.destroy(); 
    this.map.getClass();
    this.ol = new MCache.Overlay(this.map, tc, tc, 131072);
    this.sc = tc;
    this.grab.mv = true;
    this.ui.grabmouse(this.mv);
    return true;
  }
  
  public boolean mmousewheel(Coord mc, int amount) {
    return false;
  }
  
  public boolean mmouseup(Coord mc, int button) {
    this.grab.mv = false;
    this.ui.grabmouse(null);
    return true;
  }
  
  public void mmousemove(Coord mc) {
    Coord tc = mc.div(MCache.tilesz);
    Coord c1 = new Coord(0, 0), c2 = new Coord(0, 0);
    if (tc.x < this.sc.x) {
      c1.x = tc.x;
      c2.x = this.sc.x;
    } else {
      c1.x = this.sc.x;
      c2.x = tc.x;
    } 
    if (tc.y < this.sc.y) {
      c1.y = tc.y;
      c2.y = this.sc.y;
    } else {
      c1.y = this.sc.y;
      c2.y = tc.y;
    } 
    this.ol.update(c1, c2);
    this.c1 = c1;
    this.c2 = c2;
    this.text.settext(String.format("Selected: %d×%d", new Object[] { Integer.valueOf(c2.x - c1.x + 1), Integer.valueOf(c2.y - c1.y + 1) }));
  }
  
  public void wdgmsg(Widget sender, String msg, Object... args) {
    if (sender == this.btn) {
      if (this.c1 != null && this.c2 != null)
        wdgmsg("mod", new Object[] { this.c1, this.c2 }); 
      return;
    } 
    if (sender == this.cbox) {
      this.walkmod = ((Boolean)args[0]).booleanValue();
      if (!this.walkmod) {
        this.mv.grab(this.grab);
      } else {
        if (this.ol != null)
          this.ol.destroy(); 
        this.ol = null;
        this.mv.release(this.grab);
      } 
      wdgmsg("wm", new Object[] { Integer.valueOf(this.walkmod ? 1 : 0) });
      return;
    } 
    if (sender == this.tilenm) {
      wdgmsg("tilenm", new Object[] { this.tilenm.text });
      return;
    } 
    super.wdgmsg(sender, msg, args);
  }
}
