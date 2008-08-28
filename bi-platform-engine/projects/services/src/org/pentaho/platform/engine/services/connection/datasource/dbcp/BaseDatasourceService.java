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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public abstract class BaseDatasourceService implements IDatasourceService {
  ICacheManager cacheManager;
  
  public BaseDatasourceService() {
	  cacheManager = PentahoSystem.getCacheManager(null);
	  // if no cache manager implementation is available we'll use the simple one
  }
  
  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   *
   */
  public void clearCache() {
      cacheManager.removeRegionCache(IDatasourceService.JDBC_DATASOURCE);
  }

  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   *
   */
  public void clearDataSource(String dsName) {
     cacheManager.removeFromRegionCache(IDatasourceService.JDBC_DATASOURCE, dsName);
  }
}
