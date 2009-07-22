package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.metadata.query.model.util.CsvDataReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

public class DatasourceServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceServiceHelper.class);


  public static Connection getDataSourceConnection(String connectionName, IPentahoSession session) {
    SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, session, null);
    return sqlConnection.getNativeConnection(); 
  }
  
  public static List<List<String>> getRelationalDataSample(String connectionName, String query, int rowLimit, IPentahoSession session) throws DatasourceServiceException{
    List<List<String>> dataSample = new ArrayList<List<String>>(rowLimit);
    MarshallableResultSet resultSet  = getMarshallableResultSet(connectionName, query, rowLimit, session);
    MarshallableRow[] rows =  resultSet.getRows();
    for(int i=0;i<rows.length;i++) {
      MarshallableRow row = rows[i];
      String[] rowData = row.getCell();
      List<String> rowDataList = new ArrayList<String>(rowData.length);
      for(int j=0;j<rowData.length;j++) {
        rowDataList.add(rowData[j]);
      }
      dataSample.add(rowDataList);
    }
    return dataSample;
  }

  public static MarshallableResultSet getMarshallableResultSet(String connectionName, String query, int rowLimit, IPentahoSession session) throws DatasourceServiceException{
    MarshallableResultSet marshallableResultSet = null;
    SQLConnection sqlConnection = null; 
    try {
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, PentahoSessionHolder.getSession(), null);
      IPentahoResultSet resultSet =  sqlConnection.executeQuery(query);
      marshallableResultSet = new MarshallableResultSet();
      marshallableResultSet.setResultSet(resultSet);
    } catch (Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED",e.getLocalizedMessage()), e); //$NON-NLS-1$      
    } finally {
        if (sqlConnection != null) {
          sqlConnection.close();
        }
    }
    return marshallableResultSet;

  }
  public static List<List<String>> getCsvDataSample(String fileLocation, boolean headerPresent, String delimiter, String enclosure, int rowLimit) {
    CsvDataReader reader = new CsvDataReader(fileLocation, headerPresent, delimiter, enclosure, rowLimit);
    return reader.loadData();
  }

}
