package org.pentaho.platform.api.engine;

import java.util.List;

import org.pentaho.platform.api.engine.ILogoutListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.PentahoSystemException;

public interface IPentahoSystemListeners {

  public void notifySystemListenersOfStartup() throws PentahoSystemException;

  public void shutdown();

  public void addLogoutListener(final ILogoutListener listener);

  public ILogoutListener remove(final ILogoutListener listener);

  public void invokeLogoutListeners(final IPentahoSession session);

  /**
   * Registers custom handlers that are notified of both system startup and 
   * system shutdown events.
   * 
   * @param systemListeners the system event handlers
   */
  public void setSystemListeners(List<IPentahoSystemListener> systemListeners);
  
}
