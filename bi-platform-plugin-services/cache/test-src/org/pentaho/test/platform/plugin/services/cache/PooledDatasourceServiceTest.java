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

import java.sql.Connection;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceService;

public class PooledDatasourceServiceTest extends TestCase {

  public void setUp() {
  //  super.setUp();
  //  StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(TestSettings.SOLUTION_PATH, ""); //$NON-NLS-1$
 //   PentahoSystem.init(applicationContext, getRequiredListeners());

  }

  public void testConnectionPoolWhenExhausted() {
    //startTest();
 //   info("Testing Connecton Pooling"); //$NON-NLS-1$
    Connection connection = null;
    try {
      PooledDatasourceService service = new PooledDatasourceService();
      DataSource ds = service.getDataSource("SampleData");
      for (int i = 0; i < 10; i++) {
        connection = ds.getConnection();
        System.out.println("Got the " + (i+1) + " Connection");
      }
      fail("Not expected to reach here");
    } catch (Exception e) {
      assertTrue("Expected the exception to be thrown", true);
      e.printStackTrace();
    } finally {
      try {
        connection.close();
      } catch (Exception ee) {
        ee.printStackTrace();
      }
    }

  }

}
