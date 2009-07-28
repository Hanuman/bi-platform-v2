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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created Sep 21, 2005 
 * @author wseyler
 */
package org.pentaho.platform.plugin.action.xml.xquery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.sf.saxon.trans.XPathException;

import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.IActionOutput;
import org.pentaho.actionsequence.dom.actions.XQueryAction;
import org.pentaho.actionsequence.dom.actions.XQueryConnectionAction;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.engine.services.runtime.MapParameterResolver;
import org.pentaho.platform.engine.services.runtime.TemplateUtil;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.engine.services.solution.StandardSettings;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.xquery.XQConnection;

/**
 * XQueryBaseComponent provides a mechanism to run xqueries within the Pentaho BI Platform.
 * 
 * 
 * TODO: In regards to IPreparedComponent, implement a method for choosing the datasource on the fly 
 */
public abstract class XQueryBaseComponent extends ComponentBase implements IPreparedComponent {

  private IPentahoResultSet rSet;

  /** reference to connection object */
  protected IPentahoConnection connection;

  /** keeps track of ownership of connection */
  protected boolean connectionOwner = true;

  private static final String FILENAME_PREFIX = "tmp"; //$NON-NLS-1$

  private static final String EXTENSION = ".xml"; //$NON-NLS-1$

  private static final String TEMP_DIRECTORY = "system/tmp/"; //$NON-NLS-1$

  private static final String XML_DOCUMENT_TAG = "XML_DOCUMENT"; //$NON-NLS-1$

  @Override
  public abstract boolean validateSystemSettings();

  @Override
  public abstract Log getLogger();
  
  private int maxRows = -1;

  /** string to hold prepared query until execution */
  String preparedQuery = null;

  /** string array to hold prepared column types until execution */
  String preparedColumnTypes[] = null;

  public IPentahoResultSet getResultSet() {
    return rSet;
  }

  @Override
  protected boolean validateAction() {
    boolean result = false;
    IActionDefinition actionDefinition = getActionDefinition();

    if (actionDefinition instanceof XQueryAction) {
      XQueryAction xQueryAction = (XQueryAction) actionDefinition;
      if ((xQueryAction.getSourceXml() == ActionInputConstant.NULL_INPUT) && (xQueryAction.getXmlDocument() == null)) {
        error(Messages.getString("XQueryBaseComponent.ERROR_0008_SOURCE_NOT_DEFINED", getActionName())); //$NON-NLS-1$
      } else if (xQueryAction.getQuery() == ActionInputConstant.NULL_INPUT) {
        error(Messages.getErrorString("XQueryBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName())); //$NON-NLS-1$
      } else if ((xQueryAction.getOutputPreparedStatement() == null) && (xQueryAction.getOutputResultSet() == null)) {
        error(Messages.getErrorString("XQueryBaseComponent.ERROR_0003_OUTPUT_NOT_SPECIFIED", getActionName())); //$NON-NLS-1$
      } else {
        result = true;
      }
    } else if (actionDefinition instanceof XQueryConnectionAction) {
      XQueryConnectionAction xQueryConnectionAction = (XQueryConnectionAction) actionDefinition;
      if (xQueryConnectionAction.getOutputConnection() == null) {
        error(Messages.getErrorString("XQueryBaseComponent.ERROR_0003_OUTPUT_NOT_SPECIFIED", getActionName())); //$NON-NLS-1$
      } else {
        result = true;
      }
    } else {
      error(Messages.getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", actionDefinition.getElement().asXML())); //$NON-NLS-1$
    }
    return result;
  }

  @Override
  public void done() {
    // TODO Auto-generated method stub
  }

  @Override
  protected boolean executeAction() {
    boolean result = false;
    IActionDefinition actionDefinition = getActionDefinition();
    int maxRows = -1;
    int queryTimeout = -1;
    if (actionDefinition instanceof XQueryAction) {
      XQueryAction xQueryAction = (XQueryAction) actionDefinition;

      // Not implemented yet
      // IActionInput queryTimeoutInput = xQueryAction.getQueryTimeout();

      IActionInput maxRowsInput = xQueryAction.getMaxRows();
      if (maxRowsInput != ActionInputConstant.NULL_INPUT) {
        this.setMaxRows(maxRowsInput.getIntValue());
      }
      
      IPreparedComponent sharedConnection = (IPreparedComponent) xQueryAction.getSharedConnection().getValue();
      if (sharedConnection != null) {
        connectionOwner = false;
        connection = sharedConnection.shareConnection();
      } else {
        connection = getConnection();
      }
      if (connection == null) {
        error(Messages.getErrorString("IPreparedComponent.ERROR_0002_CONNECTION_NOT_AVAILABLE", getActionName())); //$NON-NLS-1$
      } else if (connection.getDatasourceType() != IPentahoConnection.XML_DATASOURCE) {
        error(Messages.getErrorString("IPreparedComponent.ERROR_0001_INVALID_CONNECTION_TYPE", getActionName())); //$NON-NLS-1$
      } else {
        result = runQuery(connection, xQueryAction.getQuery().getStringValue());
      }
    } else if (actionDefinition instanceof XQueryConnectionAction) {
      XQueryConnectionAction xQueryConnectionAction = (XQueryConnectionAction) getActionDefinition();
      connection = getConnection();
      if (connection == null) {
        error(Messages.getErrorString("IPreparedComponent.ERROR_0002_CONNECTION_NOT_AVAILABLE", getActionName())); //$NON-NLS-1$
      } else if (connection.getDatasourceType() != IPentahoConnection.XML_DATASOURCE) {
        error(Messages.getErrorString("IPreparedComponent.ERROR_0001_INVALID_CONNECTION_TYPE", getActionName())); //$NON-NLS-1$
      } else {
        xQueryConnectionAction.getOutputConnection().setValue(this);
        result = true;
      }
    }
    return result;
  }

  protected boolean runQuery(final IPentahoConnection localConnection, String rawQuery) {
    XQueryAction xQueryAction = (XQueryAction) getActionDefinition();
    try {
      if (localConnection == null) {
        return false;
      }
      if (ComponentBase.debug) {
        debug(Messages.getString("XQueryBaseComponent.DEBUG_RUNNING_QUERY", rawQuery)); //$NON-NLS-1$
      }
      String documentPath = null;
      int resourceType = -1;
      String srcXml = xQueryAction.getSourceXml().getStringValue();
      org.pentaho.actionsequence.dom.IActionResource xmlResource = xQueryAction.getXmlDocument();
      if (srcXml != null) {
        documentPath = createTempXMLFile(srcXml);
        resourceType = IActionSequenceResource.FILE_RESOURCE;
      } else if (xmlResource != null) {
        // we have a local document to use as the data source
        IActionSequenceResource resource = getResource(xmlResource.getName());
        resourceType = resource.getSourceType();
        if (resourceType == IActionSequenceResource.SOLUTION_FILE_RESOURCE) {
          documentPath = PentahoSystem.getApplicationContext().getSolutionPath(resource.getAddress());
        } else if (resourceType == IActionSequenceResource.XML) {
          documentPath = createTempXMLFile(resource.getAddress());
        } else {
          documentPath = resource.getAddress();
        }
      }

      File documentFile = null;
      if (resourceType != IActionSequenceResource.URL_RESOURCE) {
        // check that the document exists
        documentFile = new File(documentPath);
        if (!documentFile.exists()) {
          error(Messages.getString("XQueryBaseComponent.ERROR_0007_FILE_NOT_FOUND", documentPath)); //$NON-NLS-1$
          return false;
        }
        // convert any '\' to '/'
        documentPath = documentFile.getCanonicalPath();
        documentPath = documentPath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      }

      // Retrieve the column types
      String columnTypes[] = null;
      if (retrieveColumnTypes()) {
        try {
          SAXReader reader = new SAXReader();
          Document document;
          if (resourceType == IActionSequenceResource.URL_RESOURCE) {
            document = reader.read(new URL(documentPath));
          } else {
            document = reader.read(documentFile);
          }
          Node commentNode = document.selectSingleNode("/result-set/comment()"); //$NON-NLS-1$
          if (commentNode != null) {
            String commentString = commentNode.getText();
            StringTokenizer st = new StringTokenizer(commentString, ","); //$NON-NLS-1$
            List columnTypesList = new LinkedList();
            while (st.hasMoreTokens()) {
              String token = st.nextToken().trim();
              columnTypesList.add(token);
            }
            columnTypes = (String[]) columnTypesList.toArray(new String[0]);
          }
        } catch (Exception e) {
          getLogger().warn(Messages.getString("XQueryBaseComponent.ERROR_0009_ERROR_BUILDING_COLUMN_TYPES"), e); //$NON-NLS-1$
        }
      }

      if (rawQuery != null) {
        if (rawQuery.indexOf("{" + XQueryBaseComponent.XML_DOCUMENT_TAG + "}") >= 0) { //$NON-NLS-1$//$NON-NLS-2$
          rawQuery = TemplateUtil.applyTemplate(rawQuery, XQueryBaseComponent.XML_DOCUMENT_TAG, documentPath);
        } else {
          rawQuery = "doc(\"" + documentPath + "\")" + rawQuery; //$NON-NLS-1$ //$NON-NLS-2$
        }
      }

      if (xQueryAction.getOutputPreparedStatement() != null) {
        return prepareFinalQuery(rawQuery, columnTypes);
      } else {
        return runFinalQuery(localConnection, rawQuery, columnTypes);
      }
    } catch (Exception e) {
      getLogger().error(Messages.getString("XQueryBaseComponent.ERROR_0010_ERROR_RUNNING_QUERY"), e); //$NON-NLS-1$
      return false;
    }
  }

  protected boolean prepareFinalQuery(final String rawQuery, final String[] columnTypes) {
    if (rawQuery != null) {
      preparedQuery = applyInputsToFormat(rawQuery);
    }
    preparedColumnTypes = columnTypes;
    ((XQueryAction) getActionDefinition()).getOutputPreparedStatement().setValue(this);
    return true;
  }

  protected boolean runFinalQuery(final IPentahoConnection localConnection, final String rawQuery,
      final String[] columnTypes) {
    XQueryAction xQueryAction = (XQueryAction) getActionDefinition();
    boolean success = false;
    String finalQuery = applyInputsToFormat(rawQuery);
    // execute the query, read the results and cache them
    try {
      IPentahoResultSet resultSet = ((XQConnection) localConnection).executeQuery(finalQuery, columnTypes);
      if (resultSet != null) {
        if (!xQueryAction.getLive().getBooleanValue(true)) {
          resultSet = resultSet.memoryCopy();
        }
        try {
          IActionOutput resultSetOutput = xQueryAction.getOutputResultSet();
          if (resultSetOutput != null) {
            resultSetOutput.setValue(resultSet);
          }
          success = true;
        } finally {
          resultSet.close();
        }
      }
    } catch (XPathException e) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
    }
    return success;
  }

  protected String createTempXMLFile(final String xmlString) {
    // Save it to a temporary file
    File file;
    String documentPath = null;
    try {
      file = PentahoSystem.getApplicationContext().createTrackedTempFile(getSession(), XQueryBaseComponent.FILENAME_PREFIX, XQueryBaseComponent.EXTENSION); 
      
      documentPath = file.getCanonicalPath();

      BufferedWriter out = new BufferedWriter(new FileWriter(file));
      out.write(xmlString);
      out.close();
    } catch (IOException e) {
      getLogger().error(Messages.getString("XQueryBaseComponent.ERROR_0011_ERROR_CREATING_TEMP_FILE"), e); //$NON-NLS-1$
    }

    documentPath = documentPath.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
    return documentPath;
  }

  protected IPentahoConnection getConnection() {
    IPentahoConnection conn = null;
    try {
      conn = PentahoConnectionFactory.getConnection(IPentahoConnection.XML_DATASOURCE, getSession(), this);
      if (conn == null) {
        error(Messages.getErrorString("XQueryBaseComponent.ERROR_0005_INVALID_CONNECTION")); //$NON-NLS-1$
        return null;
      }
      if (this.getMaxRows() >=0) {
        conn.setMaxRows(this.getMaxRows());
      }
      return conn;
    } catch (Exception e) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
    }
    return null;
  }

  @Override
  public boolean init() {
    return true;
  }

  /** 
   * implements IPreparedComponents shareConnection, allowing
   * other xquery components to access the connection
   * 
   * @return shared connection
   */
  public IPentahoConnection shareConnection() {
    return connection;
  }

  /**
   * implements the IPreparedComponent executePrepared, which
   * allows other components to execute the prepared statement.
   *
   * @param preparedParams lookup for prepared parameters
   * @return pentaho result set
   */
  public IPentahoResultSet executePrepared(final Map preparedParams) {

    if (connection == null) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0012_NO_CONNECTION", getActionName())); //$NON-NLS-1$
      return null;
    }
    if (!connection.initialized()) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0012_NO_CONNECTION", getActionName())); //$NON-NLS-1$
      return null;
    }

    if (preparedQuery == null) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0001_QUERY_NOT_SPECIFIED", getActionName())); //$NON-NLS-1$
      return null;
    }

    String finalQuery = TemplateUtil.applyTemplate(preparedQuery, getRuntimeContext(), new MapParameterResolver(
        preparedParams, IPreparedComponent.PREPARE_LATER_PREFIX, getRuntimeContext()));

    // execute the query, read the results and cache them
    try {
      IPentahoResultSet resultSet = ((XQConnection) connection).executeQuery(finalQuery, preparedColumnTypes);
      if (resultSet != null) {
        boolean live = getInputBooleanValue(StandardSettings.LIVE, true);
        if (!live) {
          resultSet = resultSet.memoryCopy();
        }
        try {
          return resultSet;
        } finally {
          resultSet.close();
        }
      }
    } catch (XPathException e) {
      error(Messages.getErrorString("XQueryBaseComponent.ERROR_0006_EXECUTE_FAILED", getActionName()), e); //$NON-NLS-1$
    }
    return null;
  }

  /**
   * Determines if the action should attempt to retrieve the columns types
   */
  protected boolean retrieveColumnTypes() {
    return true;
  }

  public int getMaxRows() {
    return this.maxRows;
  }
  
  public void setMaxRows(final int value) {
    if (rSet == null) {
      this.maxRows = value;
    } else {
      throw new UnsupportedOperationException(Messages.getErrorString("XQueryBaseComponent.ERROR_0013_INVALID_ORDER_OF_OPERATION")); //$NON-NLS-1$
    }
  }
  
  /**
   * disposes of the connection
   * this is called by the runtime context
   * if the object is used as an iprepared component
   */
  public void dispose() {
    if (connectionOwner) {
      if (connection != null) {
        connection.close();
      }
    }
  }
}
