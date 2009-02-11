package org.pentaho.webservice.core;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Stores the IPentahoSession session object for the current thread so that a
 * web service bean can get to it without requiring it to be passed to its
 * methods
 * @author jamesdixon
 *
 */
public class SessionHandler {

  private static ThreadLocal<IPentahoSession> sessions = new ThreadLocal<IPentahoSession>();
  
  /**
   * Sets an IPentahoSession for the current thread
   * @param session
   */
  public static void setSession( IPentahoSession session ) {
    sessions.set( session );
  }
  
  /**
   * Returns the IPentahoSession for the current thread
   * @return thread session
   */
  public static IPentahoSession getSession() {
    return sessions.get();
  }
  
  /**
   * Removes the IPentahoSession for the current thread.
   * It is important that the framework calls this to prevent session bleed-
   * through between requests as threads are re-used by the server.
   */
  public static void removeSession() {
    sessions.remove();
  }
  
}
