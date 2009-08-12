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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Mar 21, 2005 
 * @author Marc Batchelor
 */

package org.pentaho.platform.engine.services.audit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;

/**
 * @author mbatchel
 * 
 */
public class AuditConnection {

  private DataSource auditDs;

  private boolean initialized;

  private static String DRIVER_URL;

  private static String DRIVER_CLASS;
    
  private static String DRIVER_USERID; 

  private static String DRIVER_PASSWORD; 
    
  private static String AUDIT_JNDI; 
    
  private static final Log logger = LogFactory.getLog(AuditConnection.class);

  private boolean useNewDatasourceService = false;
  
  private static final String auditConfigFile = "audit_sql.xml";
  

  public void initialize() {

    if (!initialized) {
      retrieveParameters();
      try {
        IDatasourceService datasourceService = getDatasourceService(); 
        auditDs = datasourceService.getDataSource(AuditConnection.AUDIT_JNDI);
        if (auditDs != null) {
          AuditConnection.logger.debug(Messages.getString(
              "AUDCONN.DEBUG_LOOKUP_FOUND_CLASS", auditDs.getClass().getName())); //$NON-NLS-1$
        }
      } catch (Exception dsException) {
        AuditConnection.logger.error(
            Messages.getErrorString("AUDCONN.ERROR_0001_COULD_NOT_GET_DATASOURCE"), dsException); //$NON-NLS-1$
      }
      if (auditDs != null) {
        initialized = true;
      } else {
        try {
          AuditConnection.logger.warn(Messages.getString("AUDCONN.WARN_FALLING_BACK_TO_DRIVERMGR")); //$NON-NLS-1$
          Class.forName(DRIVER_CLASS).newInstance();
          initialized = true;
        } catch (IllegalAccessException ex) {
          AuditConnection.logger.error(Messages.getErrorString("AUDCONN.ERROR_0002_INSTANCE_DRIVER"), ex); //$NON-NLS-1$
        } catch (ClassNotFoundException cfe) {
          AuditConnection.logger.error(Messages.getErrorString("AUDCONN.ERROR_0002_INSTANCE_DRIVER"), cfe); //$NON-NLS-1$          
        } catch (InstantiationException ie) {
          AuditConnection.logger.error(Messages.getErrorString("AUDCONN.ERROR_0002_INSTANCE_DRIVER"), ie); //$NON-NLS-1$          
        }
      }
    }
  }

  /**
   * This ugliness exists because of bug http://jira.pentaho.com/browse/BISERVER-3478. Once this 
   * is fixed, we can move this initialization into a one liner for each setting in the class construction. 
   * 
   * The logic needs to be that if the config file does not exist, we can fall over to the
   * pentaho.xml file for the attribute value (for backward compatibility). 
   */
  private void retrieveParameters(){
    
    String tmp = PentahoSystem.getSystemSetting(auditConfigFile, "auditConnection/driverURL", null);//$NON-NLS-1$ //$NON-NLS-2$
    DRIVER_URL = (tmp !=null) ? tmp:
      PentahoSystem.getSystemSetting("auditConnection/driverURL", Messages.getString("AUDCONN.CODE_DEFAULT_CONNECT_URL"));//$NON-NLS-1$ //$NON-NLS-2$

    tmp = PentahoSystem.getSystemSetting(auditConfigFile, "auditConnection/driverCLASS", null);//$NON-NLS-1$ //$NON-NLS-2$
    DRIVER_CLASS = (tmp !=null) ? tmp: 
      PentahoSystem.getSystemSetting("auditConnection/driverCLASS", Messages.getString("AUDCONN.CODE_DEFAULT_CONNECT_DRIVER"));//$NON-NLS-1$ //$NON-NLS-2$
      
    tmp = PentahoSystem.getSystemSetting(auditConfigFile, "auditConnection/userid", null);//$NON-NLS-1$ //$NON-NLS-2$
    DRIVER_USERID = (tmp !=null) ? tmp:  
      PentahoSystem.getSystemSetting("auditConnection/userid", "sa");//$NON-NLS-1$ //$NON-NLS-2$

    tmp = PentahoSystem.getSystemSetting(auditConfigFile, "auditConnection/password", null);//$NON-NLS-1$ //$NON-NLS-2$
    DRIVER_PASSWORD = (tmp !=null) ? tmp:   
      PentahoSystem.getSystemSetting("auditConnection/password", "");//$NON-NLS-1$ //$NON-NLS-2$
      
    tmp = PentahoSystem.getSystemSetting(auditConfigFile, "auditConnection/JNDI", null);//$NON-NLS-1$ //$NON-NLS-2$
    AUDIT_JNDI = (tmp !=null) ? tmp:   
          PentahoSystem.getSystemSetting("auditConnection/JNDI", "Hibernate");//$NON-NLS-1$ //$NON-NLS-2$
    
  }

  public void setUseNewDatasourceService(boolean useNewService) {
    //
    // The platform should not be calling this method. But, in case someone really 
    // really wants to use the new datasource service features to hook up
    // a core service like Hibernate, this is now toggle-able.
    //
    useNewDatasourceService = useNewService;
  }

  
  private IDatasourceService getDatasourceService() throws ObjectFactoryException {
    //
    // Our new datasource stuff is provided for running queries and acquiring data. It is
    // NOT there for the inner workings of the platform. So, the Hibernate datasource should ALWAYS
    // be provided by JNDI. However, the class could be twiddled so that it will use the factory. 
    //
    // And, since the default shipping condition should be to NOT use the factory (and force JNDI), 
    // I've reversed the logic in the class to have the negative condition first (the default execution
    // path).
    //
    // Marc - BISERVER-2004
    //
    if (!useNewDatasourceService) {
      return new JndiDatasourceService();
    } else {
      IDatasourceService datasourceService =  PentahoSystem.getObjectFactory().get(IDatasourceService.class, null);
      return datasourceService;
    }
  }
  
  public DataSource getAuditDatasource() {
    initialize();
    return auditDs;
  }

  protected void waitFor(final int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      // ignore the interrupted exception, if it happens
    }
  }

  // Handle JNDI being unavailable
  private Connection getConnection() throws SQLException {
    return (auditDs != null ? auditDs.getConnection() : DriverManager.getConnection(AuditConnection.DRIVER_URL,
        AuditConnection.DRIVER_USERID, AuditConnection.DRIVER_PASSWORD));
  }

  public Connection getAuditConnection() throws SQLException {
    Connection con = null;
    int[] sleepTime = { 0, 200, 500, 2000 };
    for (int i = 0; i <= 3; i++) {
      waitFor(sleepTime[i]);
      try {
        con = getConnection();
        try {
          con.clearWarnings();
        } catch (SQLException ex) {
        }
        return con;
      } catch (SQLException ex) {
        AuditConnection.logger.warn(Messages.getString("AuditConnection.WARN_0001_CONNECTION_ATTEMPT_FAILED", "" //$NON-NLS-1$ //$NON-NLS-2$
            + sleepTime[i]));
      }
    }
    return con;
  }

}
