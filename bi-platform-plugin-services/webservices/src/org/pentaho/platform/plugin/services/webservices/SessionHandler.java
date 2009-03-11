/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.services.webservices;

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
