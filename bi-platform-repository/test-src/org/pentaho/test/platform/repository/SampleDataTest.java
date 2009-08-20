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
 * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 9, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.test.platform.repository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class SampleDataTest extends BaseTest {
  private static final String SOLUTION_PATH = "projects/actions/test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH);
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH);
      return ALT_SOLUTION_PATH;
    }
  }
  public void testSampleData() {
    startTest();
    String driver = "org.hsqldb.jdbcDriver"; //$NON-NLS-1$
    String userId = "PENTAHO_USER"; //$NON-NLS-1$
    String password = "PASSWORD"; //$NON-NLS-1$
    String connectionInfo = "jdbc:hsqldb:file:test-src/solution/system/data/sampledata"; //$NON-NLS-1$
    try {
      Class.forName(driver).newInstance();
    } catch (ClassNotFoundException e) {
      assertNotNull(e.getMessage(), null);
    } catch (IllegalAccessException e) {
      assertNotNull(e.getMessage(), null);
    } catch (InstantiationException e) {
      assertNotNull(e.getMessage(), null);
    }
    try {
      Connection con = DriverManager.getConnection(connectionInfo, userId, password);
      try {
        Statement stmt = con.createStatement();
        try {
          ResultSet rs = stmt.executeQuery("select count(*) from QUADRANT_ACTUALS"); //$NON-NLS-1$
          try {
            rs.next();
            int result = rs.getInt(1);
            assertTrue(
                Messages
                    .getErrorString(
                        "SampleDataTest.ERROR_0001_TEST_FAILED", "sampledata.QUADRANT_ACTUALS", Integer.toString(148), Integer.toString(result)), result == 148); //$NON-NLS-1$ //$NON-NLS-2$
          } finally {
            rs.close();
          }
        } finally {
          stmt.close();
        }
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      assertNotNull(e.getMessage(), null);
    }
    finishTest();

  }

  public void testQuartzData() {
    startTest();
    String driver = "org.hsqldb.jdbcDriver"; //$NON-NLS-1$
    String userId = "sa"; //$NON-NLS-1$
    String password = ""; //$NON-NLS-1$
    String connectionInfo = "jdbc:hsqldb:file:test-src/solution/system/data/quartz"; //$NON-NLS-1$
    try {
      Class.forName(driver).newInstance();
    } catch (ClassNotFoundException e) {
      assertNotNull(e.getMessage(), null);
    } catch (IllegalAccessException e) {
      assertNotNull(e.getMessage(), null);
    } catch (InstantiationException e) {
      assertNotNull(e.getMessage(), null);
    }
    try {
      Connection con = DriverManager.getConnection(connectionInfo, userId, password);
      try {
        Statement stmt = con.createStatement();
        try {
          ResultSet rs = stmt.executeQuery("select count(*) from QRTZ_LOCKS"); //$NON-NLS-1$
          try {
            rs.next();
            int result = rs.getInt(1);
            assertTrue(
                Messages
                    .getErrorString(
                        "SampleDataTest.ERROR_0001_TEST_FAILED", "QRTZ_TRIGGERS", Integer.toString(5), Integer.toString(result)), result == 5); //$NON-NLS-1$ //$NON-NLS-2$
          } finally {
            rs.close();
          }
        } finally {
          stmt.close();
        }
      } finally {
        con.close();
      }
    } catch (SQLException e) {
      assertNotNull(e.getMessage(), null);
    }
    finishTest();

  }

  public static void main(String[] args) {
    SampleDataTest test = new SampleDataTest();
    test.setUp();

    try {
      test.testSampleData();
      test.testQuartzData();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

}
