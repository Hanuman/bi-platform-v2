/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.test.platform.plugin.services.cache;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.apache.commons.dbcp.PoolingDataSource;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleMapCacheManager;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;
import org.pentaho.test.platform.engine.core.BaseTest;

public class DatasourceSystemListenerWithoutCacheEnabledTest extends BaseTest {

  private static final String SOLUTION_PATH = "cache/test-src/solution1";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";  
  final String SYSTEM_FOLDER = "/system";
  public String getSolutionPath() {
      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
      if(file.exists()) {
        return SOLUTION_PATH;  
      } else {
        return ALT_SOLUTION_PATH;
      }
  }

  public void testListener() {
    Connection connection = null;
    PoolingDataSource ds = null;
    try {
      ICacheManager simpleMapCacheManager = SimpleMapCacheManager.getInstance();
      StandaloneSession session = new StandaloneSession("TestSession");
      ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
      PooledDatasourceSystemListener listener = new PooledDatasourceSystemListener();
      listener.startup(session);
      boolean cachingAvailable = cacheManager != null && cacheManager.cacheEnabled();
      List datasourceList = null;
      List poolsList = null;
      if(cachingAvailable) {
        datasourceList = cacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_DATASOURCE);
        poolsList = cacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_POOL);
      } else {
        datasourceList = simpleMapCacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_DATASOURCE);
        poolsList = simpleMapCacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_POOL);
      }
      assertNotNull(datasourceList);
      assertNotNull(poolsList);
      assertTrue("Size is not zero", datasourceList.size() > 0);
      assertTrue("Size is not zero", poolsList.size() > 0);      
      listener.shutdown();
      if(cachingAvailable) {
        datasourceList = cacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_DATASOURCE);
        poolsList = cacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_POOL);
      } else {
        datasourceList = simpleMapCacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_DATASOURCE);
        poolsList = simpleMapCacheManager.getAllValuesFromRegionCache(IDatasourceService.JDBC_POOL);
      }
      assertSame(0, datasourceList.size());
      assertSame(0, poolsList.size());
    } catch (Exception e) {
      fail("Not Expected the exception to be thrown");
      e.printStackTrace();
    }
  }
}
