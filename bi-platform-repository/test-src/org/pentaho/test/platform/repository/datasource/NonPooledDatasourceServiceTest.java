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
package org.pentaho.test.platform.repository.datasource;

import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

import org.pentaho.platform.engine.services.connection.datasource.dbcp.NonPooledDatasourceService;
import org.pentaho.test.platform.engine.core.BaseTest;

public class NonPooledDatasourceServiceTest extends BaseTest {

	public static final String SOLUTION_PATH = "test-src/solution";
	  private static final String ALT_SOLUTION_PATH = "test-src/solution";
	  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
	  final String SYSTEM_FOLDER = "/system";
	  private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";

		  public String getSolutionPath() {
		      File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
		      if(file.exists()) {
		        System.out.println("File exist returning " + SOLUTION_PATH);
		        return SOLUTION_PATH;  
		      } else {
		        System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);      
		        return ALT_SOLUTION_PATH;
		      }
		  }

  public void testGetDatasource() {
    Connection connection = null;
    try {
      NonPooledDatasourceService service = new NonPooledDatasourceService();
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

  public void testDataSource() {
    Connection connection = null;
    try {
      NonPooledDatasourceService service = new NonPooledDatasourceService();
      DataSource ds = service.getDataSource("SampleData");
      assertNotNull( "DataSource is null", ds );
      connection = ds.getConnection();
      assertNotNull( "Connection is null", ds );
      String dsBoundName = service.getDSBoundName("SampleData");
      assertNotNull( "Bound name is null", dsBoundName );
      String dsUnBoundName = service.getDSUnboundName("SampleData");
      assertNotNull( "Unbound name is null", dsUnBoundName );
    } catch (Exception e) {
      fail("Not expected to reach here");
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
    NonPooledDatasourceServiceTest test = new NonPooledDatasourceServiceTest();
    try {
      test.testGetDatasource();
      test.testDataSource();
    } finally {
    }
  }
}
