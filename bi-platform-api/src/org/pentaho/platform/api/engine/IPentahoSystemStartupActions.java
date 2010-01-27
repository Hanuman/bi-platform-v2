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