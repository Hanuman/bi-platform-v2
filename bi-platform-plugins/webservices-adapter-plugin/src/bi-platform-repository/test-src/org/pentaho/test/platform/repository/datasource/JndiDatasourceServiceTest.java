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
 * 
 * Copyright 2006-2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.repository.datasource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.test.platform.engine.core.BaseTest;

public class JndiDatasourceServiceTest extends BaseTest {

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
		  
  public void testGetDatasource() throws DatasourceServiceException, SQLException {
    Connection connection = null;
    try {
      JndiDatasourceService service = new JndiDatasourceService();
      DataSource ds = service.getDataSource("SampleData");
      assertNotNull( "DataSource is null", ds );
      String dsBoundName = service.getDSBoundName("SampleData");
      assertNotNull( "Bound name is null", dsBoundName );
      String dsUnBoundName = service.getDSUnboundName("SampleData");
      assertNotNull( "Unbound name is null", dsUnBoundName );
    } finally {
    	if(connection != null) {
    		connection.close();	
    	}
    }
  }
  
  public static void main(String[] args) throws DatasourceServiceException, SQLException {
	  JndiDatasourceServiceTest test = new JndiDatasourceServiceTest();
    try {
      test.testGetDatasource();
    } finally {
    }
  }
}
