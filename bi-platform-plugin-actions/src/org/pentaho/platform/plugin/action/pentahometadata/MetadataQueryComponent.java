/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.plugin.action.pentahometadata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.IPhysicalModel;
import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.query.impl.ietl.InlineEtlQueryExecutor;
import org.pentaho.metadata.query.impl.sql.MappedQuery;
import org.pentaho.metadata.query.impl.sql.SqlGenerator;
import org.pentaho.metadata.query.model.Parameter;
import org.pentaho.metadata.query.model.Query;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.DatabaseMetaUtil;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLResultSet;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;

public class MetadataQueryComponent {
  
  static final Log logger = LogFactory.getLog(MetadataQueryComponent.class);
  String query;
  int maxRows = -1;
  int timeout = -1;
  boolean live = false;
  boolean useForwardOnlyResultSet = false;
  boolean logSql = false;
  boolean forceDbDialect = false;
  IPentahoSession session = null;
  IPentahoResultSet resultSet = null;
  
  String xmlHelperClass = "org.pentaho.metadata.query.model.util.QueryXmlHelper"; //$NON-NLS-1$
  String sqlGeneratorClass = "org.pentaho.metadata.query.impl.sql.SqlGenerator"; //$NON-NLS-1$
  
  Map<String,Object> inputs = null;
  
  /*
   * The list of inputs to this component, used when resolving parameter values.
   * 
   * @param inputs map of inputs
   */
  public void setInputs(Map<String,Object> inputs) {
    this.inputs = inputs;
  }
  
  public void setLogSql(boolean logSql) {
    this.logSql = logSql;
  }
  
  public void setQuery(String query) {
    this.query = query;
  }
  
  public void setMaxRows(int maxRows) {
    this.maxRows = maxRows;
  }
  
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  
  public void setLive(boolean live) {
    this.live = live;
  }
  
  public void setUseForwardOnlyResultSet(boolean useForwardOnlyResultSet) {
    this.useForwardOnlyResultSet = useForwardOnlyResultSet;
  }
  
  public void setQueryModelXmlHelper(String xmlHelperClass) {
    this.xmlHelperClass = xmlHelperClass; 
  }
  
  public void setQueryModelSqlGenerator(String sqlGeneratorClass) {
    this.sqlGeneratorClass = sqlGeneratorClass;
  }
  
  public void setForceDbDialect(boolean forceDbDialect) {
    this.forceDbDialect = forceDbDialect;
  }
  
  @SuppressWarnings("unchecked")
  private QueryXmlHelper createQueryXmlHelper() throws Exception {
    Class clazz = Class.forName(xmlHelperClass);
    return (QueryXmlHelper)clazz.getConstructor(new Class[]{}).newInstance(new Object[]{});
  }
  
  @SuppressWarnings("unchecked")
  private SqlGenerator createSqlGenerator() throws Exception {
    Class clazz = Class.forName(sqlGeneratorClass);
    return (SqlGenerator)clazz.getConstructor(new Class[]{}).newInstance(new Object[]{});
  }
  
  public boolean execute() {

    // get the xml parser
    QueryXmlHelper helper = null;
    try {
      helper = createQueryXmlHelper();
    } catch (Exception e) {
      logger.error("error", e); //$NON-NLS-1$
      return false;
    }
    
    // parse the metadata query
    IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class, null);
    Query queryObject = null;
    try {
      queryObject = helper.fromXML(repo, query);
    } catch (Exception e) {
      logger.error("error", e); //$NON-NLS-1$
      return false;
    }
    
    if (queryObject == null) {
      logger.error("error query object null"); //$NON-NLS-1$
      return false;
    }

    // determine parameter values
    Map<String, Object> parameters = null;
    if (queryObject.getParameters() != null) {
      for (Parameter param : queryObject.getParameters()) {
        if (parameters == null) {
          parameters = new HashMap<String, Object>();
        }
        
        Object value = null;
        if (inputs != null) {
          value = inputs.get(param.getName());
        }
        if (value != null) {
          // convert object to correct type based on input here?
          parameters.put(param.getName(), value);
        } else {
          parameters.put(param.getName(), param.getDefaultValue());
        }
      }
    }
    
    IPhysicalModel physicalModel = queryObject.getLogicalModel().getLogicalTables().get(0).getPhysicalTable().getPhysicalModel();
    if (physicalModel instanceof SqlPhysicalModel) {
      return executeSqlPhysicalModel(queryObject, repo, parameters);
    } else if (physicalModel instanceof InlineEtlPhysicalModel) {
      return executeInlineEtlPhysicalModel(queryObject, repo, parameters);
    } else {
      logger.error("Physical model not supported " + physicalModel); //$NON-NLS-1$
      return false;
    }
  }
  
  protected boolean executeInlineEtlPhysicalModel(Query queryObject, IMetadataDomainRepository repo, Map<String, Object> parameters) {
    InlineEtlQueryExecutor executor = new InlineEtlQueryExecutor();
    try {
      resultSet = executor.executeQuery(queryObject, parameters);
      return true;
    } catch (Exception e ) {
      logger.error("error", e); //$NON-NLS-1$
      return false;
    }
  }
  
  protected SQLConnection getConnection(DatabaseMeta databaseMeta) {
    // use the connection specified in the query
    SQLConnection localConnection = null;
    try {
      if (databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI) {
        String jndiName = databaseMeta.getDatabaseName();
        if (jndiName != null) {
          SimpleLogger simpleLogger = new SimpleLogger(this);
          localConnection = (SQLConnection)PentahoConnectionFactory.getConnection(
              IPentahoConnection.SQL_DATASOURCE, jndiName, session, simpleLogger);
        }
      }
      if (localConnection == null) {
        String driver = databaseMeta.getDriverClass();
        String userId = databaseMeta.getUsername();
        String password = databaseMeta.getPassword();
        String connectionInfo = databaseMeta.getURL();
        if ((driver == null) && (connectionInfo == null)) {
          // TODO raise an error
        }
        SimpleLogger simpleLogger = new SimpleLogger(this);
        localConnection = (SQLConnection)PentahoConnectionFactory.getConnection(
            IPentahoConnection.SQL_DATASOURCE, driver,
            connectionInfo, userId, password, session, simpleLogger);
      }

      // This no longer is functional, it used to work with the old MQLRelationalDataComponent
      // try the parent to allow the connection to be overridden
      // localConnection = getConnection(localConnection);
      return localConnection;
    } catch (Exception e) {
      logger.error(Messages.getErrorString("MetadataQueryComponent.ERROR_0006_EXECUTE_FAILED"), e); //$NON-NLS-1$
    }
    return null;
  }

  
  protected DatabaseInterface getDatabaseInterface(final SQLConnection conn) {
    String prod = null;
    try {
      prod = conn.getNativeConnection().getMetaData().getDatabaseProductName();
      DatabaseInterface di = DatabaseMetaUtil.getDatabaseInterface(prod);
      if (prod != null && di == null) {
        logger.warn(Messages.getString("MQLRelationalDataComponent.WARN_0001_NO_DIALECT_DETECTED", prod)); //$NON-NLS-1$
      }
      return di;
    } catch (SQLException e) {
      logger.warn(Messages.getString("MQLRelationalDataComponent.WARN_0002_DIALECT_EXCEPTION", prod), e); //$NON-NLS-1$
    }
    return null;
  }

  
  protected DatabaseMeta getActiveDatabaseMeta(DatabaseMeta databaseMeta) {
    if (forceDbDialect) {
      return databaseMeta;
    }

    // retrieve a temporary connection to determine if a dialect change is necessary
    // for generating the MQL Query.
    SQLConnection tempConnection = getConnection(databaseMeta);
    try {
  
      // if the connection type is not of the current dialect, regenerate the query
      DatabaseInterface di = getDatabaseInterface(tempConnection);

      if ((di != null) && (databaseMeta.getDatabaseType() != di.getDatabaseType())) {
        // we need to reinitialize our mqlQuery object and reset the query.
        // note that using this di object wipes out connection info
        DatabaseMeta meta = (DatabaseMeta)databaseMeta.clone();
        DatabaseInterface di2 = (DatabaseInterface) di.clone();
        di2.setAccessType(databaseMeta.getAccessType());
        di2.setDatabaseName(databaseMeta.getDatabaseName());
        meta.setDatabaseInterface(di2);
        return meta;
      } else {
        return databaseMeta;
      }
    } finally {
      if (tempConnection != null) {
        tempConnection.close();
      }
    }
    
  }
  
  protected boolean executeSqlPhysicalModel(Query queryObject, IMetadataDomainRepository repo, Map<String, Object> parameters) {
    // need to get the correct DatabaseMeta
    SqlPhysicalModel sqlModel = (SqlPhysicalModel)queryObject.getLogicalModel().getLogicalTables().get(0).getPhysicalTable().getPhysicalModel();
    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(sqlModel.getId(), sqlModel.getDatasource());
    // this connection needs closed
    boolean closeConnection = true;
    
    DatabaseMeta activeDatabaseMeta = getActiveDatabaseMeta(databaseMeta); 
    SQLConnection sqlConnection = getConnection(activeDatabaseMeta);
    try {
      if ((sqlConnection == null) || !sqlConnection.initialized()) {
        logger.error(Messages.getErrorString("SQLBaseComponent.ERROR_0007_NO_CONNECTION")); //$NON-NLS-1$
        return false;
      }

      MappedQuery mappedQuery = null;
      try {
        SqlGenerator sqlGenerator = createSqlGenerator();
        mappedQuery = sqlGenerator.generateSql(queryObject, LocaleHelper.getLocale().toString(), repo, activeDatabaseMeta, parameters, true);
      } catch (Exception e) {
        // TODO
        logger.error("error", e); //$NON-NLS-1$ 
        return false;      
      }
      
      if (timeout >= 0 ) {
        sqlConnection.setQueryTimeout(timeout);
      }
      if (maxRows >= 0) {
        sqlConnection.setMaxRows(maxRows);
      }
      
      IPentahoResultSet localResultSet = null;
      String sql = mappedQuery.getQuery();
      logger.debug("SQL: " + sql); //$NON-NLS-1$
      if (logSql) {
        logger.info("SQL: " + sql); //$NON-NLS-1$
      }

      // populate prepared sql params
      List<Object> sqlParams = null;
      if (mappedQuery.getParamList() != null) {
        sqlParams = new ArrayList<Object>();
        for (String param : mappedQuery.getParamList()) {
          sqlParams.add(parameters.get(param));
        }
      }
      
      try {
        if (!useForwardOnlyResultSet) {
          if (sqlParams != null) {
            localResultSet = sqlConnection.prepareAndExecuteQuery(sql, sqlParams);
          } else {
            localResultSet = sqlConnection.executeQuery(sql);
          }
        } else {
          if (sqlParams != null) {
            localResultSet = sqlConnection.prepareAndExecuteQuery(query, sqlParams, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY);
          } else {
            localResultSet = sqlConnection.executeQuery(query, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY);
          }
        }
        IPentahoMetaData metadata = mappedQuery.generateMetadata(localResultSet.getMetaData());
        if (live) {
          ((SQLResultSet) localResultSet).setMetaData(metadata);
          // live, don't close the connection
          closeConnection = false;
        } else {
          // read the results and cache them
          try {
            MemoryResultSet cachedResultSet = new MemoryResultSet(metadata);
            Object[] rowObjects = localResultSet.next();
            while (rowObjects != null) {
              cachedResultSet.addRow(rowObjects);
              rowObjects = localResultSet.next();
            }
            localResultSet = cachedResultSet;
          } finally {
            sqlConnection.close();
            sqlConnection = null;
          }
        }
        
      } catch (Exception e) {
        // TODO
        logger.error("error", e); //$NON-NLS-1$
        return false;
      }
        
      if (localResultSet != null) {
        resultSet = localResultSet;
        return true;
      } else {
        logger.error(Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED")); //$NON-NLS-1$
        return false;
      }
      
    } finally {
      if (closeConnection && sqlConnection != null) {
        sqlConnection.close();
      }
    }
  }
  
  public boolean validate() {
    if (query == null) {
      logger.error("no query specified"); //$NON-NLS-1$
      return false;
    }
    
    return true;
  }
  
  public IPentahoResultSet getResultSet() {
    return resultSet;
  }
}
