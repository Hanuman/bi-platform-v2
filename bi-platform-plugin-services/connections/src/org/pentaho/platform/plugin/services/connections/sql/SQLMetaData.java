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
 * Created Sep 7, 2005 
 * @author wseyler
 */

package org.pentaho.platform.plugin.services.connections.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.AbstractPentahoMetaData;

/**
 * @author wseyler
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SQLMetaData extends AbstractPentahoMetaData {

  protected static final Log logger = LogFactory.getLog(SQLMetaData.class);

  ResultSetMetaData nativeMetaData = null;

  private Object[][] columnHeaders;

  public SQLMetaData(final ResultSetMetaData nativeMetaData) {
    this.nativeMetaData = nativeMetaData;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   * 
   * In the case of SQL data there is only 1 row
   */
  @Override
  public Object[][] getColumnHeaders() {
    if (columnHeaders == null) {
      try {
        int rowCount = 1;
        int columnCount = nativeMetaData.getColumnCount();
        Object[][] result = new Object[rowCount][columnCount];
        for (int column = 0; column < columnCount; column++) {
          result[0][column] = nativeMetaData.getColumnLabel(column + 1);
        }
        this.columnHeaders = result;
      } catch (SQLException e) {
        SQLMetaData.logger.error(null, e);
      }
    }
    return columnHeaders;
  }

  @Override
  public int getColumnCount() {
    try {
      return nativeMetaData.getColumnCount();
    } catch (SQLException ex) {
      SQLMetaData.logger.error(null, ex);
    }
    // TODO: Ripple the exception out of this package
    return -1;
  }

  @Override
  public Object[][] getRowHeaders() {
    return null;
  }
}
