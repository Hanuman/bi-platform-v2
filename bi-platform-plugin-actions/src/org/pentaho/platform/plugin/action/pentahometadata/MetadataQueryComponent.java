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

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLResultSet;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * This is the BI Platform Pojo Component for Pentaho Metadata Queries.  It currently supports
 * executing the inline etl and sql physical models. 
 * 
 * 
 * 
 * TODO: We should eventually move the copy and pasted code that executes the SQL into a pojo SQL Component.
 * 
 * @author Will Gorman
 *
 */
public class MetadataQueryComponent {
  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  static final Log logger = LogFactory.getLog(MetadataQueryComponent.class);
  String query;
  Integer maxRows; //-1;
  Integer timeout; // -1;
  Boolean readOnly; // false;
  
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
  
  public void setMaxRows(Integer maxRows) {
    this.maxRows = maxRows;
  }
  
  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }
  
  public void setLive(boolean live) {
    this.live = live;
  }
  
  /**
   * This sets the read only property in the Pentaho SQLConnection API 
   * 
   * @param readOnly true if read only
   */
  public void setReadOnly(Boolean readOnly) {
    this.readOnly = readOnly;
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
    
    // apply templates to the query
    String templatedQuery = null;
    if (inputs != null) {
      Properties properties = new Properties();
      for (String name : inputs.keySet()) {
        properties.put(name, inputs.get(name).toString());
      }
      templatedQuery = TemplateUtil.applyTemplate(query, properties, null);
    } else {
      templatedQuery = query;
    }
    
    Query queryObject = null;
    try {
      queryObject = helper.fromXML(repo, templatedQuery);
    } catch (Exception e) {
      logger.error("error", e); //$NON-NLS-1$
      return false;
    }
    
    if (queryObject == null) {
      logger.error("error query object null"); //$NON-NLS-1$
      return false;
    }

    // Read metadata for new timeout/max_rows and set in superclass
    // Can still be overridden in the action sequence
    if (timeout == null) {
      Object timeoutProperty = queryObject.getLogicalModel().getProperty("timeout"); //$NON-NLS-1$
      if (timeoutProperty != null && timeoutProperty instanceof Number) {
        int timeoutVal = ((Number)timeoutProperty).intValue();
        this.setTimeout(timeoutVal);
      }
    }
    
    if (maxRows == null) {
      Object maxRowsProperty = queryObject.getLogicalModel().getProperty("max_rows"); //$NON-NLS-1$
      if (maxRowsProperty != null && maxRowsProperty instanceof Number) {
        int maxRowsVal = ((Number)maxRowsProperty).intValue();
        this.setMaxRows(maxRowsVal);
      }
    }
    
    IPhysicalModel physicalModel = queryObject.getLogicalModel().getPhysicalModel();
    
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
          if (physicalModel instanceof InlineEtlPhysicalModel) {
            Object paramVal = convertParameterValue(param, inputs);
            parameters.put(param.getName(), paramVal);
          } else {
            parameters.put(param.getName(), value);
          }
        } else {
          parameters.put(param.getName(), param.getDefaultValue());
        }
      }
    }
    
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
    
    String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$
    String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
    
    try {
      resultSet = executor.executeQuery(queryObject, csvFileLoc, parameters);
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
        di2.setAttributes(databaseMeta.getAttributes());
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
    SqlPhysicalModel sqlModel = (SqlPhysicalModel)queryObject.getLogicalModel().getPhysicalModel();
    DatabaseMeta databaseMeta = ThinModelConverter.convertToLegacy(sqlModel.getId(), sqlModel.getDatasource());
    // this connection needs closed
    boolean closeConnection = true;
    
    DatabaseMeta activeDatabaseMeta = getActiveDatabaseMeta(databaseMeta); 
    SQLConnection sqlConnection = getConnection(activeDatabaseMeta);
    String sql = null;
    try {
      if ((sqlConnection == null) || !sqlConnection.initialized()) {
        logger.error(Messages.getErrorString("SQLBaseComponent.ERROR_0007_NO_CONNECTION")); //$NON-NLS-1$
        // TODO: throw an exception up the stack.
        return false;
      }

      MappedQuery mappedQuery = null;
      try {
        SqlGenerator sqlGenerator = createSqlGenerator();
        mappedQuery = sqlGenerator.generateSql(queryObject, LocaleHelper.getLocale().toString(), repo, activeDatabaseMeta, parameters, true);
      } catch (Exception e) {
        // TODO: throw an exception up the stack.
        logger.error(Messages.getErrorString("MetadataQueryComponent.ERROR_0001_ERROR_EXECUTING_QUERY", e.getLocalizedMessage())); //$NON-NLS-1$
        logger.debug("error", e); //$NON-NLS-1$
        return false;
      }
      
      if (timeout != null && timeout >= 0 ) {
        sqlConnection.setQueryTimeout(timeout);
      }

      if (maxRows != null && maxRows >= 0) {
        sqlConnection.setMaxRows(maxRows);
      }
      
      if (readOnly != null && readOnly.booleanValue()) {
        sqlConnection.setReadOnly(true);
      }
      
      IPentahoResultSet localResultSet = null;
      sql = mappedQuery.getQuery();
      if (logger.isDebugEnabled()) {
        logger.debug("SQL: " + sql); //$NON-NLS-1$
      }
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
            localResultSet = sqlConnection.prepareAndExecuteQuery(sql, sqlParams, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY);
          } else {
            localResultSet = sqlConnection.executeQuery(sql, SQLConnection.RESULTSET_FORWARDONLY, SQLConnection.CONCUR_READONLY);
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
        logger.error(Messages.getErrorString("MetadataQueryComponent.ERROR_0001_ERROR_EXECUTING_QUERY", e.getLocalizedMessage(), sql)); //$NON-NLS-1$
        logger.debug("error", e); //$NON-NLS-1$
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
  
  /**
   * Convert a parameter to it's expected query input type.
   * 
   * @param param the expected query parameter
   * @param parameters the list of inputs
   * 
   * @return the converted value
   */
  private Object convertParameterValue(Parameter param, Map<String, Object> parameters) {
    Object paramObj = null;
    if (parameters != null) {
      paramObj = parameters.get(param.getName());
      if (paramObj == null) {
        return null;
      }
      // convert the input parameter to the right parameter type
      switch(param.getType()) {
        case NUMERIC:
          if (!(paramObj instanceof Number)) {
            try  {
              paramObj = Double.parseDouble(paramObj.toString());
            } catch (NumberFormatException e) {
              // ignore failed conversion
            }
          }
          break;
        case BOOLEAN:
          if (!(paramObj instanceof Boolean)) {
            paramObj = Boolean.parseBoolean(paramObj.toString());
          }
          break;
        case STRING:
          if (!(paramObj instanceof String)) {
            paramObj = paramObj.toString();
          }
          break;
      }
    }
    return paramObj;
  }
}
