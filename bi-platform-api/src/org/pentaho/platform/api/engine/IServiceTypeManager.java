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
 * Created June 17 2009
 * @author aphillips
 */
package org.pentaho.platform.api.engine;

import org.pentaho.platform.api.engine.WebServiceConfig.ServiceType;


public interface IServiceTypeManager {

  public void registerService(final WebServiceConfig wsDefinition) throws ServiceException;

  public Object getServiceBean(String serviceId) throws ServiceException;
  
  public ServiceType getSupportedServiceType();

  public void initServices() throws ServiceInitializationException;

  public WebServiceConfig getServiceConfig(String serviceId);

}