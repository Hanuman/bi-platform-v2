package org.pentaho.platform.api.engine;

import java.util.List;
import java.util.Map;

import org.pentaho.platform.api.engine.IApplicationContext;

public interface IPentahoSystemInitializer {

  public boolean init(final IApplicationContext pApplicationContext, final Map listenerMap);
  
  public boolean getInitializedOK();

  public int getInitializedStatus();

  public List<String> getInitializationFailureMessages();
  
  public void addInitializationFailureMessage(final int failureBit, final String message);

}
