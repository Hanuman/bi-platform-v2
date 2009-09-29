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
import org.pentaho.ui.xul.stereotype.Bindable;


public class RelationalModel extends XulEventSourceAdapter{
  private RelationalModelValidationListenerCollection relationalModelValidationListeners;
  private boolean validated;
  private boolean previewValidated;
  private boolean applyValidated;
  private String datasourceName;
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
  
  @Bindable
  public ConnectionEditType getEditType() {
    return editType;
  }

  @Bindable
  public void setEditType(ConnectionEditType value) {
    this.editType = value;
  }
  
  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }
  
  @Bindable
  public void setDatasourceName(String datasourceName) {
    String previousVal = this.datasourceName;
    this.datasourceName = datasourceName;
    
    // if we're editing a generated or already defined domain,
    // we need to keep the datasource name in sync
    if (getBusinessData() != null &&
        getBusinessData().getDomain() != null) {
      Domain domain = getBusinessData().getDomain(); 
      domain.setId(datasourceName);
      LogicalModel model = domain.getLogicalModels().get(0);
      String localeCode = domain.getLocales().get(0).getCode();
      model.getName().setString(localeCode, datasourceName);
    }
   
    this.firePropertyChange("datasourcename", previousVal, datasourceName); //$NON-NLS-1$
    validate();
  }
  
  @Bindable
  public IConnection getSelectedConnection() {
    return selectedConnection;
  }

  @Bindable
  public void setSelectedConnection(IConnection value) {
    IConnection previousValue = this.selectedConnection;
    this.selectedConnection = value;
    this.firePropertyChange("selectedConnection", previousValue, value);
    validate();
  }

  @Bindable
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

  @Bindable
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

  @Bindable
  public void setConnections(List<IConnection> value) {
    List<IConnection> previousValue = getPreviousValue();
    this.connections = value;
    this.firePropertyChange("connections", previousValue, value); //$NON-NLS-1$
  }

  @Bindable
  public String getQuery() {
    return query;
  }

  @Bindable
  public void setQuery(String value) {
    String previousVal = this.query;
    this.query = value;
    this.firePropertyChange("query", previousVal, value); //$NON-NLS-1$
    validate();
  }
  
  @Bindable
  public String getPreviewLimit() {
    return previewLimit;
  }

  @Bindable
  public void setPreviewLimit(String value) {
    String previousVal = this.previewLimit;
    this.previewLimit = value;
    this.firePropertyChange("previewLimit", previousVal, value); //$NON-NLS-1$
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

  @Bindable
  public boolean isValidated() {
    return validated;
  }

  @Bindable
  private void setValidated(boolean value) {
    if(value != this.validated) {
      this.validated = value;
      this.firePropertyChange("validated", !value, value);
    }
  }

  public void validate() {
    if((getQuery() != null && getQuery().length() > 0)&& (getSelectedConnection() != null)) {
      this.setPreviewValidated(true);
      if(datasourceName != null && datasourceName.length() > 0) {
        this.setApplyValidated(true);
        if(getBusinessData() != null) {
          this.setValidated(true);
          fireRelationalModelValid();
        } else {
          this.setValidated(false);
          fireRelationalModelInValid();
        }
      } else {
        this.setApplyValidated(false);
        this.setValidated(false);
        fireRelationalModelInValid();
      }
    } else {
      this.setApplyValidated(false);
      this.setPreviewValidated(false);
      this.setValidated(false);
      fireRelationalModelInValid();
    }
  }

  @Bindable
  public BusinessData getBusinessData() {
    return businessData;
  }

  @Bindable
  public void setBusinessData(BusinessData value) {
    this.businessData = value;
    if (value != null) {
      Domain domain = value.getDomain();
      List<List<String>> data = value.getData();
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
    validate();
  }

  private void addModelDataRow(LogicalColumn column, List<String> columnData, String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<ModelDataRow>();
    }
    this.dataRows.add(new ModelDataRow(column, columnData, locale));
  }

  @Bindable
  public List<ModelDataRow> getDataRows() {
    return dataRows;
  }

  @Bindable
  public void setDataRows(List<ModelDataRow> value) {
    this.dataRows = value;
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
  @Bindable
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setPreviewLimit("10");
    setQuery("");
    
    // BISERVER-3664: Temporary solution for IE ListBoxs not accepting -1 selectedIndex.
    // Explicitly selecting the first connection object makes all browsers behave the same.
    IConnection firstConnection = connections.size() > 0 ? connections.get(0) : null;
    setSelectedConnection(firstConnection);
    setDatasourceName("");
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
  public void setPreviewValidated(boolean value) {
    if (value != this.previewValidated) {
      this.previewValidated = value;
      this.firePropertyChange("previewValidated", !value, this.previewValidated);
    }
  }
  public boolean isPreviewValidated() {
    return this.previewValidated;
  }
  
  public boolean isApplyValidated() {
    return applyValidated;
  }

  public void setApplyValidated(boolean value) {
    if (value != this.applyValidated) {
      this.applyValidated = value;
      this.firePropertyChange("applyValidated", !value, this.applyValidated);
    }    
  }
}
