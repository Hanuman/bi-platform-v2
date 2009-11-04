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
 * Created June 18, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.metadata.query.model.util.CsvDataReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLMetaData;
import org.pentaho.platform.util.logging.SimpleLogger;

public class DatasourceServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceServiceHelper.class);
  
  public static Connection getDataSourceConnection(String connectionName, IPentahoSession session) {
    SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, session, new SimpleLogger(DatasourceServiceHelper.class.getName()));
    return sqlConnection.getNativeConnection(); 
  }

  public static SerializedResultSet getSerializeableResultSet(String connectionName, String query, int rowLimit, IPentahoSession session) throws DatasourceServiceException{
    SerializedResultSet serializedResultSet = null;
    SQLConnection sqlConnection = null; 
    try {
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, PentahoSessionHolder.getSession(), null);
      sqlConnection.setMaxRows(rowLimit);
      sqlConnection.setReadOnly(true);
      IPentahoResultSet resultSet =  sqlConnection.executeQuery(query);
      MarshallableResultSet marshallableResultSet = new MarshallableResultSet();
      marshallableResultSet.setResultSet(resultSet);
      IPentahoMetaData ipmd = resultSet.getMetaData();
      if (ipmd instanceof SQLMetaData) {
        // Hack warning - get JDBC column types
        // TODO: Need to generalize this amongst all IPentahoResultSets
        SQLMetaData smd = (SQLMetaData)ipmd;
        int[] columnTypes = smd.getJDBCColumnTypes();
        List<List<String>> data = new ArrayList<List<String>>();
        for (MarshallableRow row : marshallableResultSet.getRows()) {
          String[] rowData = row.getCell();
          List<String> rowDataList = new ArrayList<String>(rowData.length);
          for(int j=0;j<rowData.length;j++) {
            rowDataList.add(rowData[j]);
          }
          data.add(rowDataList);
        }
        serializedResultSet = new SerializedResultSet(columnTypes, marshallableResultSet.getColumnNames().getColumnName(), data);
      }
    } catch (Exception e) {
      logger.error(Messages.getInstance().getErrorString("DatasourceServiceHelper.ERROR_0001_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()),e); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getInstance().getErrorString("DatasourceServiceHelper.ERROR_0001_QUERY_VALIDATION_FAILED",e.getLocalizedMessage()), e); //$NON-NLS-1$      
    } finally {
        if (sqlConnection != null) {
          sqlConnection.close();
        }
    }
    return serializedResultSet;

  }
  public static List<List<String>> getCsvDataSample(String fileLocation, boolean headerPresent, String delimiter, String enclosure, int rowLimit) {
    CsvDataReader reader = new CsvDataReader(fileLocation, headerPresent, delimiter, enclosure, rowLimit);
    return reader.loadData();
  }
}
