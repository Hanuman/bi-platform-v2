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
package org.pentaho.platform.dataaccess.datasource.utils;

public class SerializedResultSet implements java.io.Serializable{
  private static final long serialVersionUID = 8275330793662889379L;
  private String[] columns;// contains column names
  private String[] columnTypes;// contains column types
  private String[][] data;// 2 dimensional array

  public SerializedResultSet(String[] columnTypes, String[] columns, String[][] data) {
    super();
    this.columnTypes = columnTypes;
    this.columns = columns;
    this.data = data;
  }
  
  public SerializedResultSet()
  {
    
  }
  public String[] getColumns() {
    return columns;
  }
  public void setColumns(String[] columns) {
    this.columns = columns;
  }
  public String[] getColumnTypes() {
    return columnTypes;
  }
  public void setColumnTypes(String[] columnTypes) {
    this.columnTypes = columnTypes;
  }
  public String[][] getData() {
    return data;
  }
  public void setData(String[][] data) {
    this.data = data;
  }
}