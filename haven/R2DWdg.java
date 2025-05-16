package haven;

public class R2DWdg extends Widget {
  private final PView target;
  
  public R2DWdg(PView target) {
    super(target.c, target.sz, target.parent);
    this.target = target;
  }
  
  public void draw(GOut g) {
    super.draw(g);
    g.chcolor();
    if (this.target.rls == null)
      return; 
    for (RenderList.Slot s : this.target.rls.slots()) {
      if (s.r instanceof PView.Render2D)
        ((PView.Render2D)s.r).draw2d(g); 
    } 
  }
}
