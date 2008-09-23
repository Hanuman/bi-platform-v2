/*
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * Created Sep 12, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.services.connections.mondrian;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Query;
import mondrian.olap.Result;
import mondrian.olap.Util;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MDXConnection implements IPentahoLoggingConnection {
  /**
   * Defines the XML element in the component-definition that holds the
   * mondrian-specific MDX Connection string.
   */
  public static final String CONNECTION_STRING_KEY = "mdx-connection-string"; //$NON-NLS-1$

  Connection nativeConnection = null;

  String lastQuery = null;

  IPentahoResultSet resultSet = null;

  ILogger logger = null;

  public MDXConnection() {
    super();
  }

  public void setLogger(final ILogger logger) {
    this.logger = logger;
  }

  public void setProperties(final Properties props) {

    // TODO: consolidate this in the init
    String connectStr = props.getProperty(IPentahoConnection.JNDI_NAME_KEY);
    if (connectStr != null) {
      init(connectStr);
    } else {
      final String connection = props.getProperty(IPentahoConnection.CONNECTION);
      final String provider = props.getProperty(IPentahoConnection.PROVIDER);
      final String userName = props.getProperty(IPentahoConnection.USERNAME_KEY);
      final String password = props.getProperty(IPentahoConnection.PASSWORD_KEY);
      if (connection != null && provider != null) {
        init(connection, provider, userName, password);
      } else {
        init(props);
      }
    }
  }

  /**
   * @param driver - The name of the driver or the connection string
   * @param provider - the provider for MDX usally "mondrian"
   * @param userName - User to connect to the datasource with
   * @param password - Password for the user
   * 
   * @deprecated
   * @see MDXConnection(Properties props, ILogger logger)
   */
  @Deprecated
  public MDXConnection(final String driver, final String provider, final String userName, final String password) {
    super();
    init(driver, provider, userName, password);
  }

  public MDXConnection(final String connectStr, final ILogger logger) {
    super();
    this.logger = logger;
    init(connectStr);
  }

  private void init(final String connectStr) {
    try {
      if (nativeConnection != null) { // Assume we're open
        close();
      }

      // parse the connect string, replace datasource with actual
      // datasource object if possible
      
      Util.PropertyList properties = Util.parseConnectString(connectStr);
      
      String dataSourceName = properties.get("DataSource"); //$NON-NLS-1$
      
      if (dataSourceName != null) {
        IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);
        DataSource dataSourceImpl = datasourceService.getDataSource(dataSourceName);      
        if (dataSourceImpl != null) {
          properties.remove("DataSource"); //$NON-NLS-1$
          nativeConnection = DriverManager.getConnection(properties, null,  dataSourceImpl);
        } else {
          nativeConnection = DriverManager.getConnection(connectStr, null);
        }
      } else {
        nativeConnection = DriverManager.getConnection(connectStr, null);
      }
      
      if (nativeConnection == null) {
        logger.error(Messages.getErrorString(
            "MDXConnection.ERROR_0002_INVALID_CONNECTION", connectStr != null ? connectStr : "null")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } catch (Throwable t) {
      if (logger != null) {
        logger.error(Messages.getErrorString(
            "MDXConnection.ERROR_0002_INVALID_CONNECTION", connectStr != null ? connectStr : "null"), t); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        Logger.error(this.getClass().getName(), Messages.getErrorString(
            "MDXConnection.ERROR_0002_INVALID_CONNECTION", connectStr != null ? connectStr : "null"), t); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  private void init(final Properties properties) {
    try {
      if (nativeConnection != null) { // Assume we're open
        close();
      }

      Util.PropertyList pl = new Util.PropertyList();
      Enumeration enum1 = properties.keys();
      while (enum1.hasMoreElements()) {
        Object key = enum1.nextElement();
        Object value = properties.get(key);
        pl.put(key.toString(), value.toString());
      }
      nativeConnection = DriverManager.getConnection(pl, null);
    } catch (Throwable t) {
      if (logger != null) {
        logger.error(Messages.getErrorString("MDXConnection.ERROR_0002_INVALID_CONNECTION", t.getMessage())); //$NON-NLS-1$
      } else {
        Logger.error(this.getClass().getName(), Messages.getErrorString(
            "MDXConnection.ERROR_0002_INVALID_CONNECTION", t.getMessage()), t); //$NON-NLS-1$ 
      }
    }
  }

  private void init(final String driver, final String provider, final String userName, final String password) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("provider=" + provider); //$NON-NLS-1$
    //
    // MB - This is a hack. Should instead have either a flag or a
    // different method for specifying a datasource instead of this.
    //
    // TODO: Fix for post 1.2 RC 2
    //

    //
    // WES - This hack was fixed up to maintain backward capability.
    // In addition methods were added so that connection info can be passed
    // in via a properties map.

    if (driver.indexOf("dataSource=") >= 0) { //$NON-NLS-1$
      buffer.append("; ").append(driver); //$NON-NLS-1$
    } else {
      buffer.append("; Jdbc=" + driver); //$NON-NLS-1$
    }
    if (userName != null) {
      buffer.append("; JdbcUser=" + userName); //$NON-NLS-1$
    }
    if (password != null) {
      buffer.append("; JdbcPassword=" + password); //$NON-NLS-1$
    }
    init(buffer.toString());
  }

  public boolean initialized() {
    return nativeConnection != null;
  }

  public IPentahoResultSet prepareAndExecuteQuery(final String query, final List parameters) throws Exception {
    throw new UnsupportedOperationException();
  }

  public boolean preparedQueriesSupported() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#close()
   */
  public void close() {
    if (nativeConnection != null) {
      nativeConnection.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#getLastQuery()
   */
  public String getLastQuery() {
    return lastQuery;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#executeQuery(java.lang.String)
   */
  public IPentahoResultSet executeQuery(final String query) {
    Query mdxQuery = nativeConnection.parseQuery(query);
    Result result = nativeConnection.execute(mdxQuery);
    resultSet = new MDXResultSet(result, nativeConnection);
    return resultSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isClosed()
   */
  public boolean isClosed() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#clearWarnings()
   */
  public void clearWarnings() {
    // TODO Auto-generated method stub

  }

  public IPentahoResultSet getResultSet() {
    return resultSet;
  }

  public boolean connect(final Properties props) {
    if (nativeConnection != null) { // Assume we're open
      close();
    }
    init(props);
    String query = props.getProperty(IPentahoConnection.QUERY_KEY);
    if ((query != null) && (query.length() > 0) && (nativeConnection != null)) {
      executeQuery(query);
    }
    return nativeConnection != null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows(final int maxRows) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setFetchSize(int)
   */
  public void setFetchSize(final int fetchSize) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public Connection getConnection() {
    return nativeConnection;
  }

  /**
   * return datasource type MDX
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.MDX_DATASOURCE;
  }
}