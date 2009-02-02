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
 * This TableModel is used to wrap a PentahoResultSet object into a TableModel that
 * can be used as input to a JFreeReport. It could also be used as input to a 
 * swing JTable.
 *
 * Created Sep 8, 2005 
 * @author mbatchel
 */
package org.pentaho.platform.plugin.action.jfreereport.helper;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXMetaData;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXResultSet;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;

// import org.jfree.report.modules.misc.tablemodel.TypeMapper;

public class PentahoTableModel extends AbstractTableModel implements CloseableTableModel {
  private static final long serialVersionUID = 696878055074045444L;

  private IPentahoResultSet resultSet;

  private transient Object[][] rowHeaders;

  public PentahoTableModel(final IPentahoResultSet rs) {
    super();
    resultSet = rs;
  }

  /**
   * returns the logger object
   * 
   * @return log
   */
  public Log getLogger() {
    return LogFactory.getLog(getClass());
  }

  public int getColumnCount() {
    if (resultSet == null) {
      return 0;
    }

    if (rowHeaders == null) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if ((rowHeaders != null) && (rowHeaders.length > 0)) {
      return rowHeaders[0].length + resultSet.getColumnCount();
    } else {
      return resultSet.getColumnCount();
    }
  }

  public Object getValueAt(final int rowIndex, int columnIndex) {
    if (resultSet == null) {
      return null;
    }

    if (rowHeaders == null) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if (rowHeaders != null) {
      if (columnIndex < rowHeaders[0].length) {
        return rowHeaders[rowIndex][columnIndex];
      } else {
        columnIndex -= rowHeaders[0].length;
      }
    }

    // catch any exceptions so we don't blow up the entire jfreereport
    Object val = null;
    try {
      val = resultSet.getValueAt(rowIndex, columnIndex);
    } catch (IndexOutOfBoundsException e1) {
      //
      // MB - This isn't an error condition. Indeed, it will happen when there are zero rows of
      // data. So, log an info message and be done with it. We also don't want an if-check to be
      // done on every cell for this boundary case.
      //
      getLogger().info(Messages.getErrorString("PentahoTableModel.ERROR_0001_GET_VALUE_AT")); //$NON-NLS-1$
    } catch (Throwable t) {
      IVersionHelper versionHelper = PentahoSystem.get(IVersionHelper.class, null);
      getLogger().error("Error Start: Pentaho " + versionHelper.getVersionInformation(this.getClass())); //$NON-NLS-1$
      getLogger().error(Messages.getErrorString("PentahoTableModel.ERROR_0001_GET_VALUE_AT"), t); //$NON-NLS-1$
      getLogger().error("Error end:"); //$NON-NLS-1$
    }

    return val;
  }

  public int getRowCount() {
    if (resultSet != null) {
      return resultSet.getRowCount();
    }
    return 0;
  }

  @Override
  public String getColumnName(int columnNumber) {
    if (resultSet == null) {
      return null;
    }

    // Flatten out the column headers into one column-name
    Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
    if (rowHeaders == null) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if (rowHeaders != null) {
      if (columnNumber < rowHeaders[0].length) {
        if (resultSet instanceof MDXResultSet) {
          return ((MDXMetaData) resultSet.getMetaData()).getColumnName(columnNumber);
        }
      } else {
        columnNumber -= rowHeaders[0].length;
      }
    }
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < columnHeaders.length; i++) {
      if (i > 0) {
        buf.append("/"); //$NON-NLS-1$
      }
      buf.append(columnHeaders[i][columnNumber].toString());
    }
    return buf.toString();
  }

  public void close() {
    // Close the old result set if needed.
    if (resultSet != null) {
      resultSet.closeConnection();
      resultSet.close();
    }
    resultSet = null;
    // JFreeReport wont listen, but it is always good style to comply to
    // the contract ..
    fireTableStructureChanged();
  }
}
