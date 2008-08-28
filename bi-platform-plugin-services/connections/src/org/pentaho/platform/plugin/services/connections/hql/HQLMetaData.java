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
 * Created Dec 27, 2006
 * @author mdamour
 */

package org.pentaho.platform.plugin.services.connections.hql;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;
import org.pentaho.commons.connection.AbstractPentahoMetaData;

/**
 * @author mdamour
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class HQLMetaData extends AbstractPentahoMetaData {
  private Object[][] columnHeaders;

  protected static final Log logger = LogFactory.getLog(HQLMetaData.class);

  private HQLResultSet resultSet;

  private String columnNames[];

  // private Type columnTypes[];

  public HQLMetaData(final List data, final HQLResultSet resultSet, final String[] columnNames,
      final Type colummTypes[]) {
    this.resultSet = resultSet;
    this.columnNames = columnNames;
    // this.columnTypes = colummTypes;
    getColumnHeaders();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   * 
   * In the case of HQL data there is only 1 row
   */
  @Override
  public Object[][] getColumnHeaders() {
    if (columnHeaders == null) {
      try {
        int rowCount = 1;
        int columnCount = resultSet.getColumnCount();
        Object[][] result = new Object[rowCount][columnCount];
        for (int column = 0; column < columnCount; column++) {
          try {
            result[0][column] = columnNames[column];
          } catch (Exception e) {
          }
        }
        this.columnHeaders = result;
      } catch (Exception e) {
        HQLMetaData.logger.error(null, e);
      }
    }
    return columnHeaders;
  }

  @Override
  public int getColumnCount() {
    try {
      return resultSet.getColumnCount();
    } catch (Exception ex) {
      HQLMetaData.logger.error(null, ex);
    }
    // TODO: Ripple the exception out of this package
    return -1;
  }

  @Override
  public Object[][] getRowHeaders() {
    return null;
  }
}
