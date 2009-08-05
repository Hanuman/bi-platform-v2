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
package org.pentaho.platform.dataaccess.datasource;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;

public interface IDatasource { 
  public String getDatasourceName();
  public void setDatasourceName(String name);
  public DatasourceType getDatasourceType();
  public void setDatasourceType(DatasourceType type);
  public List<IConnection> getConnections();
  public void setConnections(List<IConnection> connections);
  public IConnection getSelectedConnection();
  public void setSelectedConnection(IConnection connection);
  public String getQuery();
  public void setQuery(String query);
  public String getPreviewLimit();
  public void setPreviewLimit(String limit);
  public BusinessData getBusinessData();
  public void setBusinessData(BusinessData object);
  public void setSelectedFile(String selectedFile);
  public void setHeadersPresent(boolean headersPresent);
}
