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

import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.NonPooledDatasourceService;

public class JndiDatasourceServiceTest extends TestCase {

  public void testGetDatasource() {
    Connection connection = null;
    try {
      JndiDatasourceService service = new JndiDatasourceService();
      DataSource ds = service.getDataSource("SampleData");
      connection = ds.getConnection();
      assertTrue("Datasource connection", ds != null);
    } catch (Exception e) {
      fail("Not expected to throw exception");
      e.printStackTrace();
    } finally {
      try {
        connection.close();
      } catch (Exception ee) {
        ee.printStackTrace();
      }
    }

  }
  
  public static void main(String[] args) {
    JndiDatasourceServiceTest test = new JndiDatasourceServiceTest();
    try {
      test.testGetDatasource();
    } finally {
    }
  }
}
