package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;

public class GoodObject implements IPentahoInitializer {

  public IPentahoSession initSession;
  
  public GoodObject() {
  }
  
  public void init(IPentahoSession session) {
    initSession = session;
  }
  
}
