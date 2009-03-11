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

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.IPentahoSession;

public interface IWebServiceConfigurator extends AxisConfigurator {

  /** 
   * Sets the session used for initialization
   */
  public void setSession( IPentahoSession session );

  /**
   * Unloads and then re-loads the web services
   */
  public void reloadServices() throws AxisFault;

  /**
   * Sets the enabled state of a named web service and returns a boolean
   * indicating whether any change of state was successful
   * @param name The name of the web service
   * @param enabled The new state of the web service
   * @return Success of state change attempt
   * @throws AxisFault
   */
  public boolean setEnabled( String name, boolean enabled ) throws AxisFault;
  
  /**
   * Returns a web service wrapper using the service name as the key
   * @param name Web service name
   * @return Web service wrapper for the specific service
   */
  public IWebServiceWrapper getServiceWrapper( String name );
  
  /**
   * Unloads the web services.
   */
  public void unloadServices() throws AxisFault;

  /**
   * Returns the logger for this class
   * @return Log instance
   */
  public Log getLogger();
}
