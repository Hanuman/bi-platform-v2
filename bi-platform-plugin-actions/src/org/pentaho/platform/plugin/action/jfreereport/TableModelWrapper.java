/*
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
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
 * @created Mar 2, 2006 
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.jfreereport;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jfree.report.util.CloseableTableModel;

/**
 * @deprecated This class is no longer used.
 */
@Deprecated
public class TableModelWrapper implements TableModel, CloseableTableModel {

  private TableModel tableModel;

  public TableModelWrapper(final TableModel tableModel) {
    this.tableModel = tableModel;
  }

  public int getColumnCount() {
    return tableModel.getColumnCount();
  }

  public int getRowCount() {
    return tableModel.getRowCount();
  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return tableModel.isCellEditable(rowIndex, columnIndex);
  }

  public Class getColumnClass(final int columnIndex) {
    return tableModel.getColumnClass(columnIndex);
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    return tableModel.getValueAt(rowIndex, columnIndex);
  }

  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    tableModel.setValueAt(aValue, rowIndex, columnIndex);
  }

  public String getColumnName(final int columnIndex) {
    return tableModel.getColumnName(columnIndex);
  }

  public void addTableModelListener(final TableModelListener l) {
    tableModel.addTableModelListener(l);
  }

  public void removeTableModelListener(final TableModelListener l) {
    tableModel.removeTableModelListener(l);
  }

  public void close() {
    tableModel = null;
  }

}
