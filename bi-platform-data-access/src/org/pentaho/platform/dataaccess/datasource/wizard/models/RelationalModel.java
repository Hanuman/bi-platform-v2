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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 26, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Datasource;
import org.pentaho.ui.xul.XulEventSourceAdapter;


public class RelationalModel extends XulEventSourceAdapter{
  private RelationalModelValidationListenerCollection relationalModelValidationListeners;
  private boolean validated;
  private boolean previewValidated;
  public static enum ConnectionEditType {ADD, EDIT};
  private IConnection selectedConnection;
  private List<IConnection> connections = new ArrayList<IConnection>();
  private List<ModelDataRow> dataRows = new ArrayList<ModelDataRow>();
  private String query;
  private String previewLimit;
  private ConnectionEditType editType = ConnectionEditType.ADD;
  private BusinessData businessData;

  public RelationalModel() {
    previewLimit = "10";
  }
  public ConnectionEditType getEditType() {
    return editType;
  }

  public void setEditType(ConnectionEditType editType) {
    this.editType = editType;
  }

  public IConnection getSelectedConnection() {
    return selectedConnection;
  }

  public void setSelectedConnection(IConnection selectedConnection) {
    IConnection previousValue = this.selectedConnection;
    this.selectedConnection = selectedConnection;
    this.firePropertyChange("selectedConnection", previousValue, selectedConnection);
    validate();
  }

  public List<IConnection> getConnections() {
    return connections;
  }

  public void addConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    connections.add(connection);
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  public void updateConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    IConnection conn = getConnectionByName(connection.getName());
    conn.setDriverClass(connection.getDriverClass());
    conn.setPassword(connection.getPassword());
    conn.setUrl(connection.getUrl());
    conn.setUsername(connection.getUsername());
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  private List<IConnection> getPreviousValue() {
    List<IConnection> previousValue = new ArrayList<IConnection>();
    for (IConnection conn : connections) {
      previousValue.add(conn);
    }
    return previousValue;
  }

  public void deleteConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    connections.remove(connections.indexOf(connection));
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  public void deleteConnection(String name) {
    for (IConnection connection : connections) {
      if (connection.getName().equals(name)) {
        deleteConnection(connection);
        break;
      }
    }
  }

  public void setConnections(List<IConnection> connections) {
    List<IConnection> previousValue = getPreviousValue();
    this.connections = connections;
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    String previousVal = this.query;
    this.query = query;
    this.firePropertyChange("query", previousVal, query); //$NON-NLS-1$
    validate();
  }
  public String getPreviewLimit() {
    return previewLimit;
  }

  public void setPreviewLimit(String previewLimit) {
    String previousVal = this.previewLimit;
    this.previewLimit = previewLimit;
    this.firePropertyChange("previewLimit", previousVal, previewLimit); //$NON-NLS-1$
  }

  public IConnection getConnectionByName(String name) {
    for (IConnection connection : connections) {
      if (connection.getName().equals(name)) {
        return connection;
      }
    }
    return null;
  }

  public Integer getConnectionIndex(IConnection conn) {
    IConnection connection = getConnectionByName(conn.getName());
    return connections.indexOf(connection);
  }

  public boolean isValidated() {
    return validated;
  }

  private void setValidated(boolean validated) {
    boolean prevVal = this.validated;
    this.validated = validated;
    this.firePropertyChange("validated", prevVal, validated);
  }

  public void validate() {
    if((getQuery() != null && getQuery().length() > 0)&& (getSelectedConnection() != null)) {
      this.setPreviewValidated(true);
      if(getBusinessData() != null) {
        this.setValidated(true);
        fireRelationalModelValid();
      } else {
        this.setValidated(false);
        fireRelationalModelInValid();
      }
    } else {
      this.setPreviewValidated(false);
    }
  }

  public BusinessData getBusinessData() {
    return businessData;
  }

  public void setBusinessData(BusinessData businessData) {
    this.businessData = businessData;
    setModelData(businessData);
    validate();
  }
  public IDatasource getDatasource() {
    IDatasource datasource = new Datasource();
    datasource.setBusinessData(getBusinessData());
    datasource.setConnections(getConnections());
    datasource.setQuery(getQuery());
    datasource.setSelectedConnection(getSelectedConnection());
    return datasource;
  }

  private void setModelData(BusinessData businessData) {
    if (businessData != null) {
      Domain domain = businessData.getDomain();
      List<List<String>> data = businessData.getData();
      List<LogicalModel> logicalModels = domain.getLogicalModels();
      int columnNumber = 0;
      for (LogicalModel logicalModel : logicalModels) {
        List<Category> categories = logicalModel.getCategories();
        for (Category category : categories) {
          List<LogicalColumn> logicalColumns = category.getLogicalColumns();
          for (LogicalColumn logicalColumn : logicalColumns) {
            addModelDataRow(logicalColumn, getColumnData(columnNumber++, data), domain.getLocales().get(0).getCode());
          }
        }
      }
      firePropertyChange("dataRows", null, dataRows);
    } else {
      if (this.dataRows != null) {
        this.dataRows.removeAll(dataRows);
        List<ModelDataRow> previousValue = this.dataRows;
        firePropertyChange("dataRows", previousValue, null);
      }
    }
  }
  public void addModelDataRow(LogicalColumn column, List<String> columnData, String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<ModelDataRow>();
    }
    this.dataRows.add(new ModelDataRow(column, columnData, locale));
  }

  public List<ModelDataRow> getDataRows() {
    return dataRows;
  }

  public void setDataRows(List<ModelDataRow> dataRows) {
    this.dataRows = dataRows;
    firePropertyChange("dataRows", null, dataRows);
  }

  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }

  /*
   * Clears out the model
   */
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setPreviewLimit("10");
    setQuery("");
    setSelectedConnection(null);
  }

  public void addRelationalModelValidationListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners == null) {
      relationalModelValidationListeners = new RelationalModelValidationListenerCollection();
    }
    relationalModelValidationListeners.add(listener);
  }

  public void removeRelationalListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.remove(listener);
    }
  }

  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelValid();
    }
  }
  
  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelInValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelInValid();
    }
  }
  public void setPreviewValidated(boolean previewValidated) {
    boolean prevVal = this.previewValidated;
    this.previewValidated = previewValidated;
    this.firePropertyChange("previewValidated", prevVal, previewValidated);
  }
  public boolean isPreviewValidated() {
    return previewValidated;
  }
}
