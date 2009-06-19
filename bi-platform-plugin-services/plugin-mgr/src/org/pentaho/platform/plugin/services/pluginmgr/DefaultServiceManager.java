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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.IServiceTypeManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.api.engine.WebServiceConfig;
import org.pentaho.platform.api.engine.WebServiceConfig.ServiceType;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class DefaultServiceManager implements IServiceManager {

  public Map<ServiceType, IServiceTypeManager> serviceManagerMap = new HashMap<ServiceType, IServiceTypeManager>();

  public void setServiceTypeManagers(Collection<IServiceTypeManager> serviceTypeManagers) {
    for (IServiceTypeManager handler : serviceTypeManagers) {
      ServiceType type = handler.getSupportedServiceType();
      if (type == null) {
        throw new IllegalArgumentException(
            Messages.getErrorString("DefaultServiceManager.ERROR_0001")); //$NON-NLS-1$
      }
      serviceManagerMap.put(type, handler);
      Logger.info(getClass().toString(),
          "registered service manager to handle services of type '" + handler.getSupportedServiceType() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void registerService(final WebServiceConfig config) throws ServiceException {
    validate(config);
    ServiceType type = config.getServiceType();
    IServiceTypeManager mgr = serviceManagerMap.get(type);
    if (mgr == null) {
      String availableTypes = StringUtils.join(serviceManagerMap.keySet().iterator(), Messages.getString(",")); //$NON-NLS-1$
      throw new ServiceException(Messages.getErrorString("DefaultServiceManager.ERROR_0002", config.getId(), type.toString(), availableTypes)); //$NON-NLS-1$
    }
    serviceManagerMap.get(config.getServiceType()).registerService(config);
  }

  private static void validate(WebServiceConfig config) {
    if(StringUtils.isEmpty(config.getId())) {
      throw new IllegalStateException("web service id not set"); //$NON-NLS-1$
    }
    if(config.getServiceClass() == null) {
      throw new IllegalStateException("service class not set"); //$NON-NLS-1$
    }
    if(config.getServiceType() == null) {
      throw new IllegalStateException("service type not set"); //$NON-NLS-1$
    }
  }

  public Object getServiceBean(ServiceType serviceType, String serviceId) throws ServiceException {
    return serviceManagerMap.get(serviceType).getServiceBean(serviceId);
  }

  public WebServiceConfig getServiceConfig(ServiceType serviceType, String serviceId) {
    return serviceManagerMap.get(serviceType).getServiceConfig(serviceId);
  }

  public void initServices() throws ServiceInitializationException {
    for (IServiceTypeManager handler : serviceManagerMap.values()) {
      handler.initServices();
    }
  }
}
