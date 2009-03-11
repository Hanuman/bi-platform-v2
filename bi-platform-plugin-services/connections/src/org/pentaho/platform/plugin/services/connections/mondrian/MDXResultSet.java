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
 * Created Sep 12, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.services.connections.mondrian;

import mondrian.olap.Axis;
import mondrian.olap.Connection;
import mondrian.olap.Result;

import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MDXResultSet implements IPentahoResultSet, IPeekable {
  private static final int columnAxis = 0;

  private static final int rowAxis = 1;

  int columnCount = 0;

  int rowCount = 0;

  Result nativeResultSet;

  Connection nativeConnection;

  int rowIndex = 0;

  MDXMetaData mdxMetaData = null;

  protected Object peekRow[];

  /**
   * @param useExtendedColumnNames if true, columnNames will follow the format: 
   * "[dimension_name].[hierarchy_name].[level_name]"
   * otherwise the format for column names will be: 
   * "hierarchy_name{column_number}"
   * 
   * Implemented as a flag to allow reports prior to platform version 2.1
   * (Liberty) to continue to execute as expected with the short column names, 
   * but if the developer sets the extendedColumnNames flag to true, can overcome the
   * bug in BISERVER-1266. 
   */
  public MDXResultSet(final Result nativeResultSet, final Connection nativeConnection, boolean useExtendedColumnNames) {
    super();

    this.nativeResultSet = nativeResultSet;
    this.nativeConnection = nativeConnection;

    Axis[] axis = nativeResultSet.getAxes();
    if ((axis == null) || (axis.length == 0)) {
      columnCount = 0;
      rowCount = 0;
    } else if (axis.length == 1) {
      columnCount = axis[MDXResultSet.columnAxis].getPositions().size();
      rowCount = 0;
    } else {
      columnCount = axis[MDXResultSet.columnAxis].getPositions().size();
      rowCount = axis[MDXResultSet.rowAxis].getPositions().size();
    }

    mdxMetaData = new MDXMetaData(this.nativeResultSet, useExtendedColumnNames);
  }
  
  /**
   * @param result
   */
  public MDXResultSet(final Result nativeResultSet, final Connection nativeConnection) {
    this(nativeResultSet, nativeConnection, false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getMetaData()
   */
  public IPentahoMetaData getMetaData() {
    return mdxMetaData;
  }

  public Object[] peek() {

    if( peekRow == null ) {
      peekRow = next();
    }
    return peekRow;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#next()
   */
  public Object[] next() {

    if (peekRow != null) {
      Object row[] = peekRow;
      peekRow = null;
      return row;
    }
    Object currentRow[] = null;

    if (rowIndex < rowCount) {
      currentRow = new Object[columnCount];
      for (int i = 0; i < columnCount; i++) {
        currentRow[i] = getValueAt(rowIndex, i);
      }
      rowIndex++;
    }
    return currentRow;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#close()
   */
  public void close() {
    nativeResultSet.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#closeConnection()
   */
  public void closeConnection() {
    nativeResultSet.close();
    nativeConnection.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.runtime.IDisposable#dispose()
   */
  public void dispose() {
    closeConnection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#isScrollable()
   */
  public boolean isScrollable() {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getValueAt(int, int)
   */
  public Object getValueAt(final int row, final int column) {
    int[] key = new int[2];
    key[0] = column;
    key[1] = row;
    return nativeResultSet.getCell(key).getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getRowCount()
   */
  public int getRowCount() {
    return mdxMetaData.getRowHeaders().length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getColumnCount()
   */
  public int getColumnCount() {
    return mdxMetaData.getColumnCount();
  }

  public IPentahoResultSet memoryCopy() {
    try {
      IPentahoMetaData metadata = getMetaData();
      Object columnHeaders[][] = metadata.getColumnHeaders();
      Object rowHeaders[][] = metadata.getRowHeaders();

      MemoryMetaData cachedMetaData = new MemoryMetaData(columnHeaders, rowHeaders);
      MemoryResultSet cachedResultSet = new MemoryResultSet(cachedMetaData);

      Object[] rowObjects = next();
      while (rowObjects != null) {
        cachedResultSet.addRow(rowObjects);
        rowObjects = next();
      }
      return cachedResultSet;
    } finally {
      close();
    }
  }

  public void beforeFirst() {
    rowIndex = 0;
  }

  public Object[] getDataColumn(final int column) {
    int oldIndex = rowIndex; // save our current iteration location

    beforeFirst();
    Object[] result = new Object[getRowCount()];
    int index = 0;
    Object[] rowData = next();
    while (rowData != null) {
      result[index] = rowData[column];
      index++;
      rowData = next();
    }

    rowIndex = oldIndex; // restore the old iteration location

    return result;
  }

  public Object[] getDataRow(final int row) {
    int oldIndex = rowIndex; // save our current iteration location

    rowIndex = row;
    Object[] rowData = next();
    rowIndex = oldIndex;

    return rowData;
  }

}
