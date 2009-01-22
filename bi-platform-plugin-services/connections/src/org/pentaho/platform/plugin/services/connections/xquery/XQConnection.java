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
 * Created Sep 15, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.services.connections.xquery;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Properties;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;
import org.pentaho.platform.plugin.services.messages.Messages;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class XQConnection implements IPentahoLoggingConnection {
  protected Configuration config = null;

  protected StaticQueryContext sqc = null;

  protected String lastQuery = null;

  protected ILogger logger = null;

  IPentahoResultSet resultSet = null;
  
  int maxRows = -1;

  public XQConnection() {
    super();
    config = new Configuration();
    sqc = new StaticQueryContext(config);
  }

  public void setLogger(final ILogger logger) {
    this.logger = logger;
  }

  public void setProperties(Properties props) {
    connect(props);
  }

  public boolean initialized() {
    // TODO create a good test
    return true;
  }

  public IPentahoResultSet prepareAndExecuteQuery(final String query, final List parameters) throws Exception {
    throw new UnsupportedOperationException();
  }

  public boolean preparedQueriesSupported() {
    return false;
  }

  /**
   * return datasource type MDX
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.XML_DATASOURCE;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#close()
   */
  public void close() {
    // TODO Auto-generated method stub

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
  public IPentahoResultSet executeQuery(final String query) throws XPathException {
    return executeQuery(query, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#executeQuery(java.lang.String)
   */
  public IPentahoResultSet executeQuery(final String query, final String columnTypes[]) throws XPathException {
    XQueryExpression exp = sqc.compileQuery(query);
    DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
    try {
      resultSet = new XQResultSet(this, exp, dynamicContext, columnTypes);
    } catch (XPathException e) {
      if (e.getException() instanceof FileNotFoundException) {
        logger.error(Messages.getString("XQConnection.ERROR_0001_UNABLE_TO_READ", query)); //$NON-NLS-1$
      } else {
        logger.error(Messages.getString("XQConnection.ERROR_0002_XQUERY_EXCEPTION", query), e); //$NON-NLS-1$
      }
    } catch (Throwable t) {
      logger.error(Messages.getErrorString("XQConnection.ERROR_0002_XQUERY_EXCEPTION", query), t); //$NON-NLS-1$
    }
    lastQuery = query;
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
    if (props != null) {
      String query = props.getProperty(IPentahoConnection.QUERY_KEY);
      if ((query != null) && (query.length() > 0)) {
        try {
          executeQuery(query);
        } catch (XPathException e) {
          logger.error(e.getLocalizedMessage());
          return false;
        }
      }
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows(final int maxRows) {
    this.maxRows = maxRows;
  }

  public int getMaxRows() {
    return this.maxRows;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setFetchSize(int)
   */
  public void setFetchSize(final int fetchSize) {
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException();
  }

}
