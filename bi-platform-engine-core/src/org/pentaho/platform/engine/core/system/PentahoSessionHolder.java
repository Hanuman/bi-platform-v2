/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jun 23, 2005 
 * @author James Dixon
 * 
 */
package org.pentaho.platform.engine.core.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.messages.Messages;

/**
 * Stores the IPentahoSession session object for the current thread so that a
 * web service bean can get to it without requiring it to be passed to its
 * methods
 * @author jamesdixon
 *
 */
public class PentahoSessionHolder {

  private static final ThreadLocal<IPentahoSession> perThreadSession = new InheritableThreadLocal<IPentahoSession>();
  
  private static Log logger = LogFactory.getLog(PentahoSessionHolder.class);

  /**
   * Sets an IPentahoSession for the current thread
   * @param session
   */
  public static void setSession(IPentahoSession session) {
    perThreadSession.set(session);
  }

  /**
   * Returns the IPentahoSession for the current thread
   * @return thread session
   */
  public static IPentahoSession getSession() {
    IPentahoSession sess = perThreadSession.get();
    if (sess == null) {
      //In a perfect world, the platform should never be in a state where session is null, but we are not there yet.  Not all places
      //that instance sessions use the PentahoSessionHolder yet, so we will not make a fuss here if session is null.  When PentahoSessionHolder
      //is fully integrated with all sessions, then we should probably throw an exception here since in that case a null session means 
      //the system is in an illegal state.
      logger.debug(Messages.getString("PentahoSessionHolder.WARN_THREAD_SESSION_NULL", Thread.currentThread().getName())); //$NON-NLS-1$
    }
    return sess;
  }

  /**
   * Removes the IPentahoSession for the current thread.
   * It is important that the framework calls this to prevent session bleed-
   * through between requests as threads are re-used by the server.
   */
  public static void removeSession() {
    IPentahoSession sess = perThreadSession.get();
    if (sess != null) {
      //If the session is a custom/stand-alone session, we need to remove references
      //to it from other objects which may be holding on to it.  We do this to prevent
      //memory leaks.  In the future, this should not be necessary since objects
      //should not need to have setSesssion methods, but instead use PentahoSessionHolder.getSession()
      if (sess instanceof StandaloneSession) {
        ((StandaloneSession) sess).destroy();
      }

      perThreadSession.remove();
    }
  }
}
