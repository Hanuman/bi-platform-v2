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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.plugin.action.pentahometadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.MQLAction;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;

public class MQLRelationalDataComponent extends ComponentBase {

  private static final long serialVersionUID = -6376955619869902045L;

  /*
  private MQLQuery mqlQuery;

  private MappedQuery mappedQuery;

  // if true, skip the attempt to load the metadata source within the 
  // getConnection() method.  This is used as part of the detection 
  // and overriding of the metadata.xmi database dialect.
  private boolean skipMetadataDatasource = false;

  public MQLQuery getMqlQuery() {
    return mqlQuery;
  }
*/
  @Override
  public Log getLogger() {
    return LogFactory.getLog(MQLRelationalDataComponent.class);
  }
  
  private boolean initialize() {
    return true;
  }

  @Override
  public boolean validateAction() {

    boolean result = true;
    if (!(getActionDefinition() instanceof MQLAction)) {
      error(Messages.getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$
      result = false;
    } else if (!initialize()) {
      result = false;
    } else {
      MQLAction mqlAction = (MQLAction) getActionDefinition();
      IActionInput query = mqlAction.getQuery();
      result = (query != ActionInputConstant.NULL_INPUT);
    }

    return result;
  }
  
  /**
   * makes the necessary calls to generate the SQL query based on the MQL XML provided.
   * 
   * @return sql
   */
  /*
  @Override
  public String getQuery() {
    MQLAction mqlAction = (MQLAction) getActionDefinition();
    
    // parameters in the query string are resolved in this call
    String mql = mqlAction.getQuery().getStringValue();
    
    
    String mqlQueryClassName = mqlAction.getMqlQueryClassName().getStringValue();
    if (mql != null) {
      if (ComponentBase.debug) {
        debug(Messages.getString("MQLRelationalDataComponent.DEBUG_DISPLAY_MQL", mql)); //$NON-NLS-1$
      }

      //GEM PMD-175 Display names no longer a legit param for this ocmponent
      // boolean displayNames = this.getInputBooleanValue("display-names", true); //$NON-NLS-1$

      MetadataPublisher.loadMetadata(getSolutionName(), getSession(), false);
      CwmSchemaFactoryInterface cwmSchemaFactory = PentahoSystem.get(CwmSchemaFactoryInterface.class,
          "ICwmSchemaFactory", getSession()); 
      try {
        if (mqlQueryClassName != null) {
          mqlQuery = MQLQueryFactory.getMQLQuery(mqlQueryClassName, mql, null, LocaleHelper.getLocale().toString(),
              cwmSchemaFactory);
        } else {
          mqlQuery = MQLQueryFactory.getMQLQuery(mql, null, LocaleHelper.getLocale().toString(), cwmSchemaFactory);
        }

        BusinessModel model = mqlQuery.getModel();

        // Read metadata for new timeout/max_rows and set in superclass
        // Can still be overridden in the action sequence
        ConceptPropertyInterface timeoutInterface = model.getConcept().getProperty("timeout"); //$NON-NLS-1$
        if (timeoutInterface != null) {
          Object tmp = timeoutInterface.getValue();
          if (tmp instanceof Number) {
            int timeout = ((Number)tmp).intValue();
            this.setQueryTimeout(timeout);
          }
        }
        ConceptPropertyInterface maxRowsInterface = model.getConcept().getProperty("max_rows"); //$NON-NLS-1$
        if (maxRowsInterface != null) {
          Object tmp = maxRowsInterface.getValue();
          if (tmp instanceof Number) {
            int maxRows = ((Number)tmp).intValue();
            this.setMaxRows(maxRows);
          }
        }
        
        // detect the actual db dialect and apply it to the MQLQuery if different from the XMI dialect
        if (!mqlAction.getForceDbDialect().getBooleanValue(false)) {
          // retrieve a temporary connection to determine if a dialect change is necessary
          // for generating the MQL Query.
          SQLConnection tempConnection = (SQLConnection) getConnection();
          try {

            // if the connection type is not of the current dialect, regenerate the query
            DatabaseInterface di = getDatabaseInterface(tempConnection);

            if ((di != null) && (mqlQuery.getDatabaseMeta().getDatabaseType() != di.getDatabaseType())) {
              // we need to reinitialize our mqlQuery object and reset the query.
              // note that using this di object wipes out connection info
              DatabaseMeta meta = (DatabaseMeta) mqlQuery.getDatabaseMeta().clone();

              DatabaseInterface di2 = (DatabaseInterface) di.clone();

              di2.setAccessType(mqlQuery.getDatabaseMeta().getAccessType());
              di2.setDatabaseName(mqlQuery.getDatabaseMeta().getDatabaseName());

              meta.setDatabaseInterface(di2);
              if (mqlQueryClassName != null) {
                mqlQuery = MQLQueryFactory.getMQLQuery(mqlQueryClassName, mql, meta, LocaleHelper.getLocale()
                    .toString(), cwmSchemaFactory);
              } else {
                mqlQuery = MQLQueryFactory
                    .getMQLQuery(mql, meta, LocaleHelper.getLocale().toString(), cwmSchemaFactory);
              }
              // don't attempt to use the metadata's connection info when retrieving a connection in the
              // future. because of the dialect change, the connection info is now invalid.
              skipMetadataDatasource = true;
            }
          } finally {
            if (tempConnection != null) {
              tempConnection.close();
            }
          }
        }

        if (mqlAction.getDisableDistinct() != ActionInputConstant.NULL_INPUT) {
          mqlQuery.setDisableDistinct(mqlAction.getDisableDistinct().getBooleanValue().booleanValue());
        }

        mappedQuery = mqlQuery.getQuery();
        String sqlQuery = mappedQuery.getQuery();
        if (ComponentBase.debug) {
          debug(Messages.getString("MQLRelationalDataComponent.DEBUG_DISPLAY_SQL", sqlQuery)); //$NON-NLS-1$
        }
        return sqlQuery;
      } catch (PentahoMetadataException e) {

        // If the metadata model does not exist, we might be looking for a thin model.
        // this code is temporary until the new thin model is fully compatible with 
        // the old metadata model
        
        try {
          String query = getThinModelQuery(mql);
          if (query == null) {
            error(Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
          }
          return query;
        } catch (Exception ex) {
          error(Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
          error(Messages.getErrorString("MQLRelationalDataComponent.ERROR_0002_THIN_EXECUTE_FAILED", getActionName()), ex); //$NON-NLS-1$
        }
        
      }
    } else {
      error(Messages.getErrorString("MQLRelationalDataComponent.ERROR_0001_QUERY_XML_EMPTY", getActionName())); //$NON-NLS-1$
    }
    return null;
  }

  public String getThinModelQuery(String mql) {
    QueryXmlHelper helper = new QueryXmlHelper();
    IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class, null);
    Query queryObject = null;
    try {
      queryObject = helper.fromXML(repo, mql);
    } catch (Exception e) {
      getLogger().error("error", e);
      return null;
    }
    
    if (queryObject == null) {
      getLogger().error("error query object null");
      return null;
    }
    // need to get the correct DatabaseMeta
    SqlPhysicalModel sqlModel = (SqlPhysicalModel)queryObject.getLogicalModel().getLogicalTables().get(0).getPhysicalTable().getPhysicalModel();
    DatabaseMeta databaseMeta = null;
    SQLConnection connection = null;
    try {
    if (sqlModel.getDatasource().getType() == DataSourceType.JNDI) {
      String jndiName = sqlModel.getDatasource().getDatabaseName();
      // this is temporary until we can get a database meta from the metadata model
      databaseMeta = new DatabaseMeta(
          jndiName, 
          "MYSQL", 
          "JNDI", "", "", "", "", "");
  
      connection = retrieveThinConnection(jndiName);
        if ((connection == null) || !connection.initialized()) {
          getLogger().error(Messages.getErrorString("SQLBaseComponent.ERROR_0007_NO_CONNECTION")); //$NON-NLS-1$
          return null;
        }
        
        DatabaseInterface databaseInterface = retrieveThinDatabaseInterface(connection);
        databaseInterface.setAccessType(databaseMeta.getAccessType());
        databaseInterface.setDatabaseName(jndiName);
        databaseInterface.setName(jndiName);
        databaseMeta.setDatabaseInterface(databaseInterface);
    } else {
      // TODO: Direct JDBC
    }
      try {    
        mqlQuery = ThinModelConverter.convertToLegacy(queryObject, databaseMeta);
      } catch (Exception e) {
        // TODO
        getLogger().error("error", e);
        return null;
      }
      
      try {
        mappedQuery = mqlQuery.getQuery();
        return mappedQuery.getQuery();
      } catch (Exception e) {
        // TODO
        getLogger().error("error", e);
        return mappedQuery.getQuery();      
      }
    } finally {
      connection.close();
    }
  }
    
  protected SQLConnection retrieveThinConnection(String jndiName) {
    // use the connection specified in the query.
    SQLConnection localConnection = null;
    
    // TODO: ILogger needed
    SimpleLogger logger = new SimpleLogger(this);
    localConnection = (SQLConnection)PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, jndiName,
                getSession(), logger);
    
    return localConnection;
  }
  */
  
  /**
   * determines the PDI database interface of a given connection object
   * 
   * @param conn
   * @return
   */
  /*
  protected DatabaseInterface retrieveThinDatabaseInterface(final SQLConnection conn) {
    String prod = null;
    try {
      prod = conn.getNativeConnection().getMetaData().getDatabaseProductName();

      if (prod == null) {
        return null;
      }

      prod = prod.toLowerCase();

      // special case to map hsql to hypersonic
      if (prod.indexOf("hsql") >= 0) { //$NON-NLS-1$
        prod = "hypersonic"; //$NON-NLS-1$
      }

      // look through all available database dialects for a match
      for (int i = 0; i < DatabaseMeta.getDatabaseInterfaces().length; i++) {
        String typeDesc = DatabaseMeta.getDatabaseInterfaces()[i].getDatabaseTypeDesc().toLowerCase();
        if (prod.indexOf(typeDesc) >= 0) {
          return DatabaseMeta.getDatabaseInterfaces()[i];
        }
      }

      getLogger().warn(Messages.getString("MQLRelationalDataComponent.WARN_0001_NO_DIALECT_DETECTED", prod)); //$NON-NLS-1$

    } catch (SQLException e) {
      getLogger().warn(Messages.getString("MQLRelationalDataComponent.WARN_0002_DIALECT_EXCEPTION", prod), e); //$NON-NLS-1$
    }
    return null;
  }
  */
  @Override
  public boolean executeAction() {
    MetadataQueryComponent component = new MetadataQueryComponent();
    // setup component
    MQLAction actionDefinition = (MQLAction) getActionDefinition();
    
    String mql = actionDefinition.getQuery().getStringValue();
    
    component.setQuery(mql);
    
    if (actionDefinition.getMaxRows() != ActionInputConstant.NULL_INPUT) {
      component.setMaxRows(actionDefinition.getMaxRows().getIntValue());
    }

    if (actionDefinition.getQueryTimeout() != ActionInputConstant.NULL_INPUT) {
      component.setTimeout(actionDefinition.getQueryTimeout().getIntValue());
    }
    
    boolean success =  component.execute(); 

    if (success) {
      IActionOutput actionOutput = actionDefinition.getOutputResultSet();
      if (actionOutput != null) {
        actionOutput.setValue(component.getResultSet());
      }
    }
    
    return success;
  }
  /*
    long start = new Date().getTime();

    boolean result = super.executeAction();

    MQLAction actionDefinition = (MQLAction) getActionDefinition();
    
    String mql = actionDefinition.getQuery().getStringValue();
    
    // if this is an inline etl model, we need to do things differently.
    QueryXmlHelper helper = new QueryXmlHelper();
    IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class, null);
    Query queryObject = null;
    try {
      queryObject = helper.fromXML(repo, mql);
    } catch (Exception e) {
      // if we fail to parse the model, it most likely lives as part of the old
      // metadata layer
    }
    
    if (queryObject != null ) {
      // need to get the correct DatabaseMeta
      if (queryObject.getLogicalModel().getLogicalTables().get(0).getPhysicalTable().getPhysicalModel() instanceof InlineEtlPhysicalModel) {
        InlineEtlPhysicalModel ietlModel = (InlineEtlPhysicalModel)queryObject.getLogicalModel().getLogicalTables().get(0).getPhysicalTable().getPhysicalModel();

        InlineEtlQueryExecutor executor = new InlineEtlQueryExecutor();
        try {
          IPentahoResultSet resultset = executor.executeQuery(queryObject);
          IActionOutput actionOutput = actionDefinition.getOutputResultSet();
          if (actionOutput != null) {
            actionOutput.setValue(resultset);
          }
          return true;
        } catch (Exception e ) {
          getLogger().error("Error", e);
          return false;
        }
      }
    }
    
    long end = new Date().getTime();
    // Fix for BISERVER-459 - MQL can be too large for the audit message column.
    // audit(MessageTypes.INSTANCE_ATTRIBUTE, "metadata query", mql, (int) (end - start)); //$NON-NLS-1$
    //
    audit(MessageTypes.INSTANCE_ATTRIBUTE, "metadata query action sequence", this.getActionName(), (int) (end - start)); //$NON-NLS-1$
    // Use debug logging instead
    trace(actionDefinition.getQuery().getStringValue());
    return result;

  }

  @Override
  protected IPentahoConnection getConnection() {
    // use the connection specified in the query.

    IPentahoConnection localConnection = null;
    if (mqlQuery != null) {
      try {
        DatabaseMeta databaseMeta = mqlQuery.getDatabaseMeta();
        if (databaseMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_JNDI) {
          String jndiName = databaseMeta.getDatabaseName();
          if (jndiName != null) {
            localConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, jndiName,
                getSession(), this);
          }
        }
        if ((localConnection == null) && !skipMetadataDatasource) {
          String driver = databaseMeta.getDriverClass();
          String userId = databaseMeta.getUsername();
          String password = databaseMeta.getPassword();
          String connectionInfo = databaseMeta.getURL();
          if ((driver == null) && (connectionInfo == null)) {
            // TODO raise an error
          }
          localConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, driver,
              connectionInfo, userId, password, getSession(), this);
        }
        // try the parent to allow the connection to be overridden
        localConnection = getConnection(localConnection);
        return localConnection;
      } catch (Exception e) {
        error(Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
      }
    }
    return null;
  }
*/
  /**
   * determines the PDI database interface of a given connection object
   * 
   * @param conn
   * @return
   */
  /*
  protected DatabaseInterface getDatabaseInterface(final SQLConnection conn) {
    String prod = null;
    try {
      prod = conn.getNativeConnection().getMetaData().getDatabaseProductName();

      if (prod == null) {
        return null;
      }

      prod = prod.toLowerCase();

      // special case to map hsql to hypersonic
      if (prod.indexOf("hsql") >= 0) { //$NON-NLS-1$
        prod = "hypersonic"; //$NON-NLS-1$
      }

      // look through all available database dialects for a match
      for (int i = 0; i < DatabaseMeta.getDatabaseInterfaces().length; i++) {
        String typeDesc = DatabaseMeta.getDatabaseInterfaces()[i].getDatabaseTypeDesc().toLowerCase();
        if (prod.indexOf(typeDesc) >= 0) {
          return DatabaseMeta.getDatabaseInterfaces()[i];
        }
      }

      warn(Messages.getString("MQLRelationalDataComponent.WARN_0001_NO_DIALECT_DETECTED", prod)); //$NON-NLS-1$

    } catch (SQLException e) {
      warn(Messages.getString("MQLRelationalDataComponent.WARN_0002_DIALECT_EXCEPTION", prod), e); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  protected IPentahoMetaData getMetadata(final IPentahoResultSet resultSet, final boolean live) {
    IPentahoMetaData metadata = mappedQuery.generateMetadata(resultSet.getMetaData());
    ((SQLResultSet) resultSet).setMetaData(metadata);
    return metadata;

  }
  */

  @Override
  public void done() {
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  protected boolean validateSystemSettings() {
    return true;
  }
}
