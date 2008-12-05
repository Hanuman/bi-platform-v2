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
 * Created Aug 30, 2005 
 * @author wseyler
 */
package org.pentaho.platform.plugin.services.connections.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PentahoSystemException;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class SQLConnection implements IPentahoLoggingConnection {
  Connection nativeConnection;
  //Added by Arijit Chatterjee
  /**
   * The timeout value for the connection (in seconds)
   */
  private int timeOut = 0; // in seconds

  /*
   * private static int connectionCtr = 0;
   */
  // private int myCtr;
  /** keep track of any created statements for closing at the end */
  ArrayList<Statement> stmts = new ArrayList<Statement>();

  /** keep track of any created result sets for closing at the end */
  ArrayList<IPentahoResultSet> resultSets = new ArrayList<IPentahoResultSet>();

  IPentahoResultSet sqlResultSet = null;

  ILogger logger = null;

  int maxRows = -1;

  int fetchSize = -1;

  public static final int RESULTSET_SCROLLABLE = ResultSet.TYPE_SCROLL_INSENSITIVE;

  public static final int RESULTSET_FORWARDONLY = ResultSet.TYPE_FORWARD_ONLY;

  public static final int CONCUR_READONLY = ResultSet.CONCUR_READ_ONLY;

  public static final int CONCUR_UPDATABLE = ResultSet.CONCUR_UPDATABLE;

  /*
   * private synchronized void bump() { connectionCtr++; }
   */
  String lastQuery = null;

  public SQLConnection() {
    super();
  }

  public void setLogger(final ILogger logger) {
    this.logger = logger;
  }

  public void setProperties(Properties props) {
    // TODO: consolidate this into connect()
    String jndiName = props.getProperty(IPentahoConnection.JNDI_NAME_KEY);
    if (jndiName != null) {
      initWithJNDI(jndiName);
    } else {
      connect(props);
    }
  }

  //Added by Arijit Chatterjee.Sets the value of timeout
  /**
   * Sets the valid of the timeout (in seconds)
   */
  public void setTimeout(final int timeInSec) {
    timeOut = timeInSec;
  }

  //Added by Arijit Chatterjee. gets the value of timeout
  /**
   * Returns the query timeout value (in seconds)
   */
  public int getTimeout() {
    return this.timeOut;
  }

  public SQLConnection(final String driverName, final String location, final String userName, final String password,
      final ILogger logger) {
    super();
    this.logger = logger;
    init(driverName, location, userName, password);
  }

  protected void init(final String driverName, final String location, final String userName, final String password) {
    // bump();
    try {
      /*
       * TODO This is where we use the java.sql package to provide a SQL connection object back to the caller
       */
      Driver driver = null;
      try {
        driver = DriverManager.getDriver(location);
      } catch (Exception e) {
        // if we don't find this connection, it isn't registered, so we'll try to find it on the classpath
      }
      if (driver == null) {
        Class driverClass = Class.forName(driverName);
        driver = (Driver) driverClass.newInstance();
        DriverManager.registerDriver(driver);
      }
      Properties info = new Properties();
      info.put("user", userName); //$NON-NLS-1$
      info.put("password", password); //$NON-NLS-1$
      nativeConnection = driver.connect(location, info);
      if (nativeConnection == null) {
        logger.error(Messages.getErrorString("ConnectFactory.ERROR_0001_INVALID_CONNECTION2", driverName, location)); //$NON-NLS-1$
      }
    } catch (Throwable t) {
      logger.error(Messages.getErrorString("ConnectFactory.ERROR_0001_INVALID_CONNECTION2", driverName, location), t); //$NON-NLS-1$
    }
  }

  public boolean initialized() {
    return nativeConnection != null;
  }

  /**
   * return datasource type SQL
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.SQL_DATASOURCE;
  }

  protected void initWithJNDI(final String jndiName) {
    // bump();
    // myCtr = connectionCtr;
    try {
      IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);
      DataSource dataSource = datasourceService.getDataSource(jndiName);      
      if (dataSource != null) {
        nativeConnection = dataSource.getConnection();
        if (nativeConnection == null) {
          logger.error(Messages.getErrorString("ConnectFactory.ERROR_0001_INVALID_CONNECTION", jndiName)); //$NON-NLS-1$
          // clear datasource cache
          datasourceService.clearDataSource(jndiName);
        }
      } else {
        logger.error(Messages.getErrorString("ConnectFactory.ERROR_0001_INVALID_CONNECTION", jndiName)); //$NON-NLS-1$
        // clear datasource cache
        datasourceService.clearDataSource(jndiName);
      }
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectFactory.ERROR_0001_INVALID_CONNECTION", jndiName), e); //$NON-NLS-1$
      // clear datasource cache
      try {
      IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);
      datasourceService.clearDataSource(jndiName);
      } catch(ObjectFactoryException objface) {
    	  logger.error(Messages.getErrorString("ConnectFactory.ERROR_0002_UNABLE_TO_FACTORY_OBJECT=Unable to factory object", jndiName), e); //$NON-NLS-1$
      }
    }
  }

  /**
   * iterate over and close all statements.  Remove each statement from the list.
   */
  private void closeStatements() {
    Iterator iter = stmts.iterator();
    while (iter.hasNext()) {
      Statement stmt = (Statement) iter.next();
      if (stmt != null) {
        try {
          stmt.close();
        } catch (Exception ignored) {
        }
      }
      iter.remove();
    }
  }

  /**
   * iterate over and close all resultsets.  Remove each result set from the list.
   */
  private void closeResultSets() {
    Iterator iter = resultSets.iterator();
    while (iter.hasNext()) {
      IPentahoResultSet rset = (IPentahoResultSet) iter.next();
      if (rset != null) {
        try {
          rset.close();
        } catch (Exception ignored) {
        }
      }
      iter.remove();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#close()
   */
  public void close() {
    closeResultSets();
    closeStatements();
    if (nativeConnection != null) {
      try {
        nativeConnection.close();
      } catch (SQLException e) {
        logger.error(null, e);
      }
    }
    nativeConnection = null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#getLastQuery()
   */
  public String getLastQuery() {
    return lastQuery;
  }

  /**
   * Executes the specified query.
   * @param query the query to execute
   * @return the resultset from the query
   * @throws SQLException indicates an error running the query
   * @throws InterruptedException indicates that the query took longer than the allowed timeout value
   * @throws PentahoSystemException 
   */
  public IPentahoResultSet executeQuery(final String query) throws SQLException, InterruptedException, PentahoSystemException {
    return executeQuery(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
  }

  /**
   * Executes the specified query with the defined parameters
   * @param query the query to be executed
   * @param scrollType
   * @param concur
   * @return the result set of data for the query
   * @throws SQLException indicates an error running the query
   * @throws InterruptedException indicates the query took longer than allowable by the query timeout
   * @throws PentahoSystemException 
   */
  public IPentahoResultSet executeQuery(final String query, final int scrollType, final int concur)
      throws SQLException, InterruptedException, PentahoSystemException {
    // Create a statement for a scrollable resultset.
    Statement stmt = nativeConnection.createStatement(scrollType, concur);
    logger.debug("Executing query with timeout value of [" + timeOut + "]"); //$NON-NLS-1$//$NON-NLS-2$

    //Added by Arijit Chatterjee. Sets the value of statement.setQueryTimeout() in seconds
    //The setQueryTimeout introduced a bug where some drivers don't support setting the timeout
    //So what we're going to do is wrap this in a try/catch and if the timeout was being set to zero
    //well won't do anything.  If it was being set to anything else we'll throw a pentaho exception
    try {
      stmt.setQueryTimeout(timeOut);
    } catch (Exception e) {
      if (timeOut != 0) {
        throw new PentahoSystemException(Messages.getErrorString("SQLConnection.ERROR_0001_TIMEOUT_NOT_SET", new Integer(timeOut).toString()), e); //$NON-NLS-1$
      }
    }
    
    stmts.add(stmt);
    if (fetchSize > 0) {
      stmt.setFetchSize(fetchSize);
    }
    if (maxRows != -1) {
      stmt.setMaxRows(maxRows);
    }
    ResultSet resultSet = stmt.executeQuery(query);
    sqlResultSet = new SQLResultSet(resultSet, this);
    // add to list of resultsets for cleanup later.
    resultSets.add(sqlResultSet);
    lastQuery = query;
    return sqlResultSet;
  }

  public IPentahoResultSet prepareAndExecuteQuery(final String query, final List parameters) throws SQLException {
    return prepareAndExecuteQuery(query, parameters, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
  }

  public IPentahoResultSet prepareAndExecuteQuery(final String query, final List parameters, final int scrollType,
      final int concur) throws SQLException {
    // Create a prepared statement
    PreparedStatement pStmt = nativeConnection.prepareStatement(query, scrollType, concur);
    // add to stmts list for closing when connection closes
    stmts.add(pStmt);
    if (fetchSize > 0) {
      pStmt.setFetchSize(fetchSize);
    }
    if (maxRows != -1) {
      pStmt.setMaxRows(maxRows);
    }

    for (int i = 0; i < parameters.size(); i++) {
      pStmt.setObject(i + 1, parameters.get(i));
    }
    ResultSet resultSet = pStmt.executeQuery();

    sqlResultSet = new SQLResultSet(resultSet, this);
    // add to list of resultsets for cleanup later.
    resultSets.add(sqlResultSet);
    lastQuery = query;
    return sqlResultSet;
  }

  public boolean preparedQueriesSupported() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isClosed()
   */
  public boolean isClosed() {
    try {
      return nativeConnection.isClosed();
    } catch (SQLException e) {
      logger.error(null, e);
    }
    return true; // assume since we couldn't get here if it
    // was open then we must be closed.
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#isReadOnly()
   * 
   * Right now this archetecture only support selects (read only)
   */
  public boolean isReadOnly() {
    return true;
  }

  public void clearWarnings() {
    try {
      nativeConnection.clearWarnings();
    } catch (SQLException e) {
      logger.error(null, e);
    }
  }

  public IPentahoResultSet getResultSet() {
    return sqlResultSet;
  }

  public boolean connect(final Properties props) {
    close();
    String jndiName = props.getProperty(IPentahoConnection.JNDI_NAME_KEY);
    if ((jndiName != null) && (jndiName.length() > 0)) {
      initWithJNDI(jndiName);
    } else {
      String driver = props.getProperty(IPentahoConnection.DRIVER_KEY);
      String provider = props.getProperty(IPentahoConnection.LOCATION_KEY);
      String userName = props.getProperty(IPentahoConnection.USERNAME_KEY);
      String password = props.getProperty(IPentahoConnection.PASSWORD_KEY);
      init(driver, provider, userName, password);
      String query = props.getProperty(IPentahoConnection.QUERY_KEY);
      if ((query != null) && (query.length() > 0)) {
        try {
          executeQuery(query);
        } catch (Exception e) {
          logger.error(null, e);
        }
      }
    }
    return ((nativeConnection != null) && !isClosed());
  }

  public int execute(final String query) throws SQLException {
    return execute(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
  }

  public int execute(final String query, final int scrollType, final int concur) throws SQLException {
    // Create a statement for a scrollable resultset.
    Statement stmt = nativeConnection.createStatement(scrollType, concur);

    //Added by Arijit Chatterjee. Sets the value of timeout for the stmt object of class Statement (in seconds)
    if (stmt != null) {
      logger.debug("Setting the query timeout to [" + timeOut + "]"); //$NON-NLS-1$ //$NON-NLS-2$
      stmt.setQueryTimeout(timeOut);
    }

    // add to stmts list for closing when connection closes
    stmts.add(stmt);
    int result = stmt.executeUpdate(query);
    lastQuery = query;
    return result;
  }

  /**
   * @return Returns the nativeConnection.
   */
  public Connection getNativeConnection() {
    return nativeConnection;
  }

  /**
   * @return Returns the fetchSize.
   */
  public int getFetchSize() {
    return fetchSize;
  }

  /**
   * @param fetchSize
   *          The fetchSize to set.
   */
  public void setFetchSize(final int fetchSize) {
    this.fetchSize = fetchSize;
  }

  /**
   * @return Returns the maxRows.
   */
  public int getMaxRows() {
    return maxRows;
  }

  /**
   * @param maxRows
   *          The maxRows to set.
   */
  public void setMaxRows(final int maxRows) {
    this.maxRows = maxRows;
  }
}
