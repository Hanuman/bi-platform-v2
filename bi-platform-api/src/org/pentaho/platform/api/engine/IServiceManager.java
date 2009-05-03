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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created May 1, 2009
 * @author aphillips
 */

package org.pentaho.platform.api.engine;

/**
 * A service manager allows a Java bean to be exposed as a web service by constructing 
 * a simple {@link WebServiceDefinition} and calling {@link #defineService(WebServiceDefinition)}.
 * Currently, these services will then become invokable through the GenericServlet servlet
 * proxying system.  Your new webservice will be available through {base_url}/content/ws-run/{service_name}/<operation>.
 * 
 * @author aphillips
 *
 */
public interface IServiceManager {

  /**
   * Registers a service with the service manager.  The service may not become active until
   * {@link #initServices(IPentahoSession)} has been called.
   * @param wsDefinition the web service definition
   */
  public void defineService(WebServiceDefinition wsDefinition);

  /**
   * Activates the services that have been registered with the service manager.
   * @param session the current session
   * @throws ServiceInitializationException 
   */
  public void initServices(IPentahoSession session) throws ServiceInitializationException;

}