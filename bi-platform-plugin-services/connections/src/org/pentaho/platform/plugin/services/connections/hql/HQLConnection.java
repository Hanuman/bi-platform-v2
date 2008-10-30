/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Dec 27, 2006
 * @author mdamour
 */

package org.pentaho.platform.plugin.services.connections.hql;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.Type;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.engine.core.system.IPentahoLoggingConnection;

/**
 * @author mdamour
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class HQLConnection implements IPentahoLoggingConnection {
  protected String lastQuery = null;

  protected ILogger logger = null;

  IPentahoResultSet resultSet = null;

  File hibernateConfigFile = null;

  Configuration hibernateConfig = null;

  public HQLConnection() {
    super();
  }

  public void setConfigFile(final File hbmCfg) {
    hibernateConfigFile = hbmCfg;
    hibernateConfig = new Configuration();
    hibernateConfig.configure(hibernateConfigFile);
  }

  public void setClassNames(final String[] classNames) {
    for (int i = 0; (classNames != null) && (i < classNames.length); i++) {
      try {
        hibernateConfig.addClass(Class.forName(classNames[i]));
      } catch (ClassNotFoundException e) {
        logger.error(null, e);
      }
    }
  }

  public void setLogger(final ILogger logger) {
    this.logger = logger;
  }

  public void setProperties(Properties props) {
  }

  public boolean initialized() {
    // TODO create a good test
    return true;
  }

  /**
   * return datasource type HQL
   * @return datasource type
   */
  public String getDatasourceType() {
    return IPentahoConnection.HQL_DATASOURCE;
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
  public IPentahoResultSet executeQuery(final String query) {
    lastQuery = query;
    Session sess = null;
    IPentahoResultSet localResultSet = null;
    try {
      SessionFactory sf = hibernateConfig.buildSessionFactory();
      // open session
      sess = sf.openSession();
      Query q = sess.createQuery(query);
      List list = q.list();
      localResultSet = generateResultSet(list, q.getReturnAliases(), q.getReturnTypes());
    } finally {
      try {
        if (sess != null) {
          sess.close();
        }
     } catch (Exception e) {
        // Doesn't seem like we would get any exception from sess.close()
        logger.error("Exception closing connection", e);
      }
    }

    return localResultSet;
  }

  public IPentahoResultSet generateResultSet(final List list, final String[] columnHeaders, final Type[] columnTypes) {
    HQLResultSet localResultSet = new HQLResultSet(list, columnHeaders, columnTypes);
    return localResultSet;
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
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoConnection#setMaxRows(int)
   */
  public void setMaxRows(final int maxRows) {
    // TODO Auto-generated method stub
    // throw new UnsupportedOperationException();
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
