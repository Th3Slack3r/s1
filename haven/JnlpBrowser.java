package haven;

import java.lang.reflect.Method;
import java.net.URL;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;

public class JnlpBrowser extends WebBrowser {
  private final BasicService basic;
  
  private JnlpBrowser(BasicService basic) {
    this.basic = basic;
  }
  
  public static JnlpBrowser create() {
    try {
      Class<? extends ServiceManager> cl = Class.forName("javax.jnlp.ServiceManager").asSubclass(ServiceManager.class);
      Method m = cl.getMethod("lookup", new Class[] { String.class });
      BasicService basic = (BasicService)m.invoke(null, new Object[] { "javax.jnlp.BasicService" });
      return new JnlpBrowser(basic);
    } catch (Exception e) {
      return null;
    } 
  }
  
  public void show(URL url) {
    if (!this.basic.showDocument(url))
      throw new WebBrowser.BrowserException("Could not launch browser"); 
  }
}
