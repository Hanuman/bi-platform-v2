package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.engine.IContentOutputHandler;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;

public interface IPentahoSystemHelper {

  public IContentOutputHandler getOutputDestinationFromContentRef(final String contentTag,
      final IPentahoSession session);

  public String getSystemName();

  @Deprecated  //use get(...) to retrieve pentaho system objects
  public Object createObject(final String className, final ILogger logger);

  @Deprecated  //use get(...) to retrieve pentaho system objects
  public Object createObject(final String className);

  public void registerHostnameVerifier();

}
