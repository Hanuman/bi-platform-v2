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

  private boolean useExtendedColumnNames = false;

  /**
   * @param connection
   */
  public MDXMetaData(final Result nativeResultSet, boolean useExtendedColumnNames) {
    super();
    this.useExtendedColumnNames=useExtendedColumnNames;
    this.nativeResultSet = nativeResultSet;
    columnHeaders = createColumnHeaders();
    rowHeaders = createRowHeaders();
    columnNames = createColumnNames();
  }

  /**
   * @param connection
   */
  public MDXMetaData(final Result nativeResultSet) {
    this(nativeResultSet, false);
  }

  private Object[][] createColumnHeaders() {
    int rowCount = 0;
    int colCount = 0;

    List positions = nativeResultSet.getAxes()[AXIS_COLUMN].getPositions();
    if (positions != null && positions.size() > 0) {
      rowCount = ((List) positions.get(0)).size();
      colCount = positions.size();
    }
    Object[][] result = new Object[rowCount][colCount];
    for (int c = 0; c < colCount; c++) {
      List members = (List) positions.get(c);
      Member member = null;
      for (int r = 0; r < rowCount; r++) {
        member = (Member) members.get(r);
        result[r][c] = member.getCaption();
      }
    }
    return result;
  }

  private Object[][] createRowHeaders() {
    int rowCount = 0;
    int colCount = 0;

    List positions = nativeResultSet.getAxes()[AXIS_ROW].getPositions();
    if (positions != null && positions.size() > 0) {
      rowCount = positions.size();
      colCount = ((List) positions.get(0)).size();
    }
    Object[][] result = new Object[rowCount][colCount];
    for (int r = 0; r < rowCount; r++) {
      List members = (List) positions.get(r);
      Member member = null;
      for (int c = 0; c < colCount; c++) {
        member = (Member) members.get(c);
        result[r][c] = member.getCaption();
      }
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
      
      // HACK for BISERVER-2640; need backward compatibility to old format of column
      // names, yet with the old format cross joins will have problems (BISERVER-1266).
      
      if (useExtendedColumnNames){
    	
      colNames = new String[this.rowHeaders[0].length];

      // Flatten out the column headers into one column-name
      for (int i = 0; i < colNames.length; ++i) 
      {
        Member member = (Member) ((List) nativeResultSet.getAxes()[AXIS_ROW].getPositions().get(0)).get(i);
        colNames[i] = "["+member.getDimension().getName()+"].["+member.getHierarchy().getName()+"].["+member.getLevel().getName()+"]";
      }
      }else{
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
