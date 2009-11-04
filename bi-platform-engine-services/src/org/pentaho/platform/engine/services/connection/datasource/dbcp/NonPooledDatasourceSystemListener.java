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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class NonPooledDatasourceSystemListener implements IPentahoSystemListener {
  public static final String DATASOURCE_REGION = "DATASOURCE";//$NON-NLS-1$
  public boolean startup(final IPentahoSession session) {
    try {
      ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
      Logger.debug(this, "NonPooledDatasourceSystemListener: called for startup"); //$NON-NLS-1$
      boolean cachingAvailable = cacheManager != null && cacheManager.cacheEnabled();
      IDatasourceMgmtService datasourceMgmtSvc = (IDatasourceMgmtService)
        PentahoSystem.getObjectFactory().get(IDatasourceMgmtService.class,session);
      if(cachingAvailable) {
        if(!cacheManager.cacheEnabled(IDatasourceService.JDBC_DATASOURCE)) {
          cacheManager.addCacheRegion(IDatasourceService.JDBC_DATASOURCE);
        }
      }
      List<IDatasource> datasources = datasourceMgmtSvc.getDatasources();
      for (IDatasource datasource : datasources) {
        Logger.debug(this, "(storing DataSource under key \"" + IDatasourceService.JDBC_DATASOURCE //$NON-NLS-1$
            + datasource.getName() + "\")"); //$NON-NLS-1$
        cacheManager.putInRegionCache(IDatasourceService.JDBC_DATASOURCE, datasource.getName(), convert(datasource));
       }
      Logger.debug(this, "NonPooledDatasourceSystemListener: done with init"); //$NON-NLS-1$
      return true;
    } catch (ObjectFactoryException objface) {
      Logger.error(this, Messages.getInstance().getErrorString(
          "NonPooledDatasourceSystemListener.ERROR_0001_UNABLE_TO_INSTANTIATE_OBJECT",NonPooledDatasourceSystemListener.class.getName()), objface); //$NON-NLS-1$
      return false;
    } catch (DatasourceMgmtServiceException dmse) {
      Logger.error(this, Messages.getInstance().getErrorString(
          "NonPooledDatasourceSystemListener.ERROR_0002_UNABLE_TO_GET_DATASOURCE",NonPooledDatasourceSystemListener.class.getName()), dmse); //$NON-NLS-1$
      return false;        
    }
  }

  public void shutdown() {
    ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
    Logger.debug(this, "NonPooledDatasourceSystemListener: called for shutdown"); //$NON-NLS-1$
    // Cleaning cache for datasources
    cacheManager.removeRegionCache(IDatasourceService.JDBC_DATASOURCE);      
    
    Logger.debug(this, "NonPooledDatasourceSystemListener: completed shutdown"); //$NON-NLS-1$
    return;
  }
  private DataSource convert(IDatasource datasource) {
    BasicDataSource basicDatasource = new BasicDataSource();
    basicDatasource.setDriverClassName(datasource.getDriverClass());
    basicDatasource.setMaxActive(datasource.getMaxActConn());
    basicDatasource.setMaxIdle(datasource.getIdleConn());
    basicDatasource.setMaxWait(datasource.getWait());
    basicDatasource.setUrl(datasource.getUrl());
    basicDatasource.setUsername(datasource.getUserName());
    basicDatasource.setPassword(datasource.getPassword());
    basicDatasource.setValidationQuery(datasource.getQuery());
    return basicDatasource;
  }
}
