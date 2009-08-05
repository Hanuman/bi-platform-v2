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
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.io.Serializable;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;

public class Datasource implements IDatasource, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String selectedFile;

  private boolean headersPresent;

  private String datasourceName;

  private String query;

  private List<IConnection> connections;

  private IConnection selectedConnection;

  private DatasourceType type;

  private String previewLimit;

  private BusinessData businessData;

  public Datasource() {

  }

  public Datasource(IDatasource datasource) {
    setDatasourceName(datasource.getDatasourceName());
    setQuery(datasource.getQuery());
    setConnections(datasource.getConnections());
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getQuery() {
    return query;
  }

  public void setPreviewLimit(String previewLimit) {
    this.previewLimit = previewLimit;
  }

  public String getPreviewLimit() {
    return previewLimit;
  }

  public IConnection getConnection(String name) {
    for (IConnection connection : connections) {
      if (connection.getName().equals(name)) {
        return connection;
      }
    }
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((datasourceName == null) ? 0 : datasourceName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Datasource other = (Datasource) obj;
    if (datasourceName == null) {
      if (other.datasourceName != null)
        return false;
    } else if (!datasourceName.equals(other.datasourceName))
      return false;
    return true;
  }

  public List<IConnection> getConnections() {
    return connections;
  }

  public void setConnection(IConnection connection) {
    connections.add(connection);
  }

  public void setConnections(List<IConnection> connections) {
    connections.clear();
    connections.addAll(connections);
  }

  public DatasourceType getDatasourceType() {
    return type;
  }

  public IConnection getSelectedConnection() {
    return selectedConnection;
  }

  public void setSelectedConnection(IConnection connection) {
    selectedConnection = connection;
  }

  public BusinessData getBusinessData() {
    return businessData;
  }

  public void setBusinessData(BusinessData businessData) {
    this.businessData = businessData;
  }

  public void setDatasourceType(DatasourceType type) {
    this.type = type;
  }

  public void setHeadersPresent(boolean headersPresent) {
    this.headersPresent = headersPresent;
  }

  public void setSelectedFile(String selectedFile) {
    this.selectedFile = selectedFile;
  }

  public String getSelectedFile() {
    return selectedFile;
  }

  public boolean isHeadersPresent() {
    return headersPresent;
  }

  public DatasourceType getType() {
    return type;
  }

  public void setType(DatasourceType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return datasourceName;
  }

}
