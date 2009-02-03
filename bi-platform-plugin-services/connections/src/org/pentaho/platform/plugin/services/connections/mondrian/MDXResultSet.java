/*
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
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
   * @param result
   */
  public MDXResultSet(final Result nativeResultSet, final Connection nativeConnection) {
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

    mdxMetaData = new MDXMetaData(this.nativeResultSet);
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
