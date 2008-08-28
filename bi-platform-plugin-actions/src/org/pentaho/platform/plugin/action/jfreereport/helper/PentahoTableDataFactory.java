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
 */
package org.pentaho.platform.plugin.action.jfreereport.helper;

import java.util.HashMap;
import java.util.Iterator;

import javax.swing.table.TableModel;

import org.jfree.report.DataFactory;
import org.jfree.report.DataRow;
import org.jfree.report.ReportDataFactoryException;
import org.jfree.report.util.CloseableTableModel;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.data.IPreparedComponent;
import org.pentaho.platform.plugin.action.messages.Messages;

/**
 * The PentahoTableDataFactory class implements JFreeReport's data factory
 * and manages the TableModels provided to JFreeReport.  The primary
 * difference between this class and JFreeReport's standard TableDataFactory
 * is the "getTableIterator" method, which allows the Platform to clean up and
 * table model resources after their use.  Also, we support Pentaho's
 * IPreparedComponent interface which allows a prepared component to generate 
 * a result set when requested.
 * 
 * @author Will Gorman
 */
public class PentahoTableDataFactory implements DataFactory, Cloneable {

  /** map of tables to keep track of */
  private HashMap tables;

  private HashMap components;

  /**
   * default constructor
   *
   */
  public PentahoTableDataFactory() {
    this.tables = new HashMap();
    this.components = new HashMap();
  }

  /**
   * constructor with one time call to addTable for convenience.
   * 
   * @param name table name
   * @param tableModel instance of table model
   */
  public PentahoTableDataFactory(final String name, final TableModel tableModel) {
    this();
    addTable(name, tableModel);
  }

  /**
   * add a table to the map
   * 
   * @param name table name
   * @param tableModel instance of table model
   */
  public void addTable(final String name, final TableModel tableModel) {
    tables.put(name, tableModel);
  }

  /**
   * add a prepared component to the map
   * 
   * @param name prepared component name
   * @param component instance of prepared component
   */
  public void addPreparedComponent(final String name, final IPreparedComponent component) {
    components.put(name, component);
  }

  /**
   * remove a table from the map
   * 
   * @param name table name
   */
  public void removeTable(final String name) {
    tables.remove(name);
  }

  /**
   * Queries a datasource. The string 'query' defines the name of the query. The
   * Parameterset given here may contain more data than actually needed.
   * <p/>
   * The dataset may change between two calls, do not assume anything!
   *
   * @param query the name of the table.
   * @param parameters are ignored for this factory.
   * @return the report data or null.
   */
  public TableModel queryData(final String query, final DataRow parameters) {
    TableModel model = (TableModel) tables.get(query);
    if (model == null) {
      final IPreparedComponent component = (IPreparedComponent) components.get(query);
      if (component != null) {
        final HashMap map = new HashMap();
        if (parameters != null) {
          for (int i = 0; i < parameters.getColumnCount(); i++) {
            map.put(parameters.getColumnName(i), parameters.get(i));
          }
        }
        final IPentahoResultSet rs = component.executePrepared(map);
        model = new PentahoTableModel(rs);
      }
    }
    return model;
  }

  public void open() {

  }

  public void close() {
    final Iterator iter = tables.values().iterator();
    while (iter.hasNext()) {
      final TableModel model = (TableModel) iter.next();
      if (model instanceof CloseableTableModel) {
        final CloseableTableModel closeableTableModel = (CloseableTableModel) model;
        closeableTableModel.close();
      }
    }
    tables.clear();
  }

  /**
   * Derives a freshly initialized report data factory, which is independend of
   * the original data factory. Opening or Closing one data factory must not
   * affect the other factories.
   *
   * @return
   */
  public DataFactory derive() throws ReportDataFactoryException {
    try {
      return (DataFactory) clone();
    } catch (CloneNotSupportedException e) {
      throw new ReportDataFactoryException(Messages
          .getErrorString("PentahoTableDataFactory.ERROR_0001_CLONE_SHOULD_NOT_FAIL")); //$NON-NLS-1$
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    final PentahoTableDataFactory dataFactory = (PentahoTableDataFactory) super.clone();
    dataFactory.tables = (HashMap) tables.clone();
    return dataFactory;
  }

}
