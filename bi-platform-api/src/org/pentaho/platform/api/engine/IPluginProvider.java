package org.pentaho.platform.api.engine;

import java.util.List;


public interface IPluginProvider {

  public List<IPlatformPlugin> getPlugins();

  public void reload(IPentahoSession session, List<String> comments);

}
