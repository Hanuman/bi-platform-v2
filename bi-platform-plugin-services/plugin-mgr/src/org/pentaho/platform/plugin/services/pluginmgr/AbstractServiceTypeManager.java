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
package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IServiceTypeManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.WebServiceConfig;

public abstract class AbstractServiceTypeManager implements IServiceTypeManager {
  
  protected Collection<WebServiceConfig> registeredServiceConfigs = new ArrayList<WebServiceConfig>();
  protected Map<String, Class<?>> serviceClassMap = new HashMap<String, Class<?>>();
  protected Map<String, Object> serviceInstanceMap = new HashMap<String, Object>();
  
  public void registerService(WebServiceConfig wsConfig) {
    serviceClassMap.put(wsConfig.getId(), wsConfig.getServiceClass());
    registeredServiceConfigs.add(wsConfig);
  }
  
  public WebServiceConfig getServiceConfig(String serviceId) {
    for(WebServiceConfig config : registeredServiceConfigs) {
      if(config.getId().equals(serviceId)) {
        return config;
      }
    }
    return null;
  }

  public Object getServiceBean(String serviceId) throws ServiceException {
    Object serviceInstance = serviceInstanceMap.get(serviceId);
    if (serviceInstance == null) {
      try {
        serviceInstance = serviceClassMap.get(serviceId).newInstance();
      } catch (InstantiationException e) {
        throw new ServiceException(e);
      } catch (IllegalAccessException e) {
        throw new ServiceException(e);
      }
    }
    return serviceInstance;
  }
}