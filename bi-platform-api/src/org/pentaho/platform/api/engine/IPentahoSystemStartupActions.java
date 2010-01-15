package org.pentaho.platform.api.engine;

import java.util.List;

import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISessionStartupAction;

public interface IPentahoSystemStartupActions {

  public void sessionStartup(final IPentahoSession session);
  
  public void sessionStartup(final IPentahoSession session, IParameterProvider sessionParameters);
  
  public void globalStartup();
  
  public void globalStartup(final IPentahoSession session);
  
  /**
   * Registers server actions that will be invoked when a session is created.
   * NOTE: it is completely up to the {@link IPentahoSession} implementation whether
   * to advise the system of it's creation via 
   * {@link PentahoSystem#sessionStartup(IPentahoSession)}.
   * 
   * @param actions the server actions to execute on session startup
   */
  public void setSessionStartupActions(List<ISessionStartupAction> actions);
  
  public void clearGlobals();

  public Object putInGlobalAttributesMap(final Object key, final Object value);

  public Object removeFromGlobalAttributesMap(final Object key);

  public IParameterProvider getGlobalParameters();

}
