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
 * Created Sep 14, 2005 
 * @author wseyler
 */
package org.pentaho.platform.plugin.services.connections.mondrian;

import java.util.List;

import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Result;

import org.pentaho.commons.connection.AbstractPentahoMetaData;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MDXMetaData extends AbstractPentahoMetaData {
  private static final int AXIS_COLUMN = 0;

  private Object[][] columnHeaders;

  private Object[][] rowHeaders;

  private String[] columnNames;

  private static final int AXIS_ROW = 1;

  Result nativeResultSet = null;

  /**
   * @param connection
   */
  public MDXMetaData(final Result nativeResultSet) {
    super();
    this.nativeResultSet = nativeResultSet;
    columnHeaders = createColumnHeaders();
    rowHeaders = createRowHeaders();
    columnNames = createColumnNames();
  }

  private Object[][] createColumnHeaders() {
    int rowCount = 0;
    int colCount = 0;

    List positions = nativeResultSet.getAxes()[MDXMetaData.AXIS_COLUMN].getPositions();
    if ((positions != null) && (positions.size() > 0)) {
      rowCount = ((List) positions.get(0)).size() + 1;
      colCount = positions.size();
    }
    Object[][] result = new Object[rowCount][colCount];
    for (int c = 0; c < colCount; c++) {
      List members = (List) positions.get(c);
      Member member = null;
      for (int r = 0; r < rowCount - 1; r++) {
        member = (Member) members.get(r);
        result[r][c] = member.getCaption();
      }
      result[rowCount - 1][c] = member.getHierarchy().getCaption();
    }
    return result;
  }

  private Object[][] createRowHeaders() {
    int rowCount = 0;
    int colCount = 0;

    List positions = nativeResultSet.getAxes()[MDXMetaData.AXIS_ROW].getPositions();
    if ((positions != null) && (positions.size() > 0)) {
      rowCount = positions.size();
      colCount = ((List) positions.get(0)).size() + 1;
    }
    Object[][] result = new Object[rowCount][colCount];
    for (int r = 0; r < rowCount; r++) {
      List members = (List) positions.get(r);
      Member member = null;
      for (int c = 0; c < colCount - 1; c++) {
        member = (Member) members.get(c);
        result[r][c] = member.getCaption();
      }
      result[r][colCount - 1] = member.getHierarchy().getCaption();
    }
    return result;
  }

  /**
   * Flattens the row headers into column names (where the useful columns have
   * useful names and the unuseful columns have unusful names).
   * @return the row headers in a String array
   */
  private String[] createColumnNames() {
    String[] colNames = null;

    if (nativeResultSet != null) {
      colNames = new String[getColumnCount()];

      // Flatten out the column headers into one column-name
      for (int i = 0; i < colNames.length; ++i) {
        List positions = nativeResultSet.getAxes()[MDXMetaData.AXIS_ROW].getPositions();
        if (i < ((List) positions.get(0)).size()) {
          Member member = (Member) ((List) positions.get(0)).get(i);
          Hierarchy hierarchy = member.getHierarchy();
          colNames[i] = hierarchy.getCaption();
        } else {
          colNames[i] = ((Member) ((List) positions.get(0)).get(((List) positions.get(0)).size() - 1)).getHierarchy()
              .getName()
              + "{" + i + "}"; //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }

    return colNames;
  }

  public String getColumnName(final int columnNumber) {
    return ((columnNames != null) && (columnNumber >= 0) && (columnNumber < columnNames.length) ? columnNames[columnNumber]
        : ""); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return nativeResultSet.getAxes()[MDXMetaData.AXIS_COLUMN].getPositions().size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   */
  @Override
  public Object[][] getColumnHeaders() {
    return columnHeaders;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getRowHeaders()
   */
  @Override
  public Object[][] getRowHeaders() {
    return rowHeaders;
  }

}
