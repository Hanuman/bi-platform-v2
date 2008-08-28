/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.plugin.action.pentahometadata;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.actions.MQLAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.sql.SQLLookupRule;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLResultSet;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.pms.factory.CwmSchemaFactoryInterface;
import org.pentaho.pms.mql.MQLQuery;
import org.pentaho.pms.mql.MQLQueryFactory;
import org.pentaho.pms.mql.MappedQuery;

public class MQLRelationalDataComponent extends SQLLookupRule {

  private static final long serialVersionUID = -6376955619869902045L;

  private MQLQuery mqlQuery;

  private MappedQuery mappedQuery;

  // if true, skip the attempt to load the metadata source within the 
  // getConnection() method.  This is used as part of the detection 
  // and overriding of the metadata.xmi database dialect.
  private boolean skipMetadataDatasource = false;

  public MQLQuery getMqlQuery() {
    return mqlQuery;
  }

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
  @Override
  public String getQuery() {
    MQLAction mqlAction = (MQLAction) getActionDefinition();
    String mql = mqlAction.getQuery().getStringValue();
    String mqlQueryClassName = mqlAction.getMqlQueryClassName().getStringValue();
    if (mql != null) {
      if (ComponentBase.debug) {
        debug(Messages.getString("MQLRelationalDataComponent.DEBUG_DISPLAY_MQL", mql)); //$NON-NLS-1$
      }

      //GEM PMD-175 Display names no longer a legit param for this ocmponent
      // boolean displayNames = this.getInputBooleanValue("display-names", true); //$NON-NLS-1$

      MetadataPublisher.loadMetadata(getSolutionName(), getSession(), false);
      CwmSchemaFactoryInterface cwmSchemaFactory = (CwmSchemaFactoryInterface) PentahoSystem.getObject(getSession(),
          "ICwmSchemaFactory"); //$NON-NLS-1$
      try {
        if (mqlQueryClassName != null) {
          mqlQuery = MQLQueryFactory.getMQLQuery(mqlQueryClassName, mql, null, LocaleHelper.getLocale().toString(),
              cwmSchemaFactory);
        } else {
          mqlQuery = MQLQueryFactory.getMQLQuery(mql, null, LocaleHelper.getLocale().toString(), cwmSchemaFactory);
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
            if( tempConnection != null ) {
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
        error(Messages.getErrorString("SQLBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
      }
    } else {
      error(Messages.getErrorString("MQLRelationalDataComponent.ERROR_0001_QUERY_XML_EMPTY", getActionName())); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public boolean executeAction() {

    long start = new Date().getTime();

    boolean result = super.executeAction();

    MQLAction actionDefinition = (MQLAction) getActionDefinition();

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

  /**
   * determines the PDI database interface of a given connection object
   * 
   * @param conn
   * @return
   */
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
}
