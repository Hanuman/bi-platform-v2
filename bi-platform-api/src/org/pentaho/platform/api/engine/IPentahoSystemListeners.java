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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 *
 * @created Jan, 2010 
 * @author James Dixon
 * 
 */
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
