package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ConnectionDebugGwtServlet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

public class DatasourceInMemoryServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceInMemoryServiceHelper.class);
  
  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DatasourceServiceException
   */
  public static Connection getDataSourceConnection(String connectionName) throws DatasourceServiceException {
    IConnection connection = null;
    try {
      connection = ConnectionDebugGwtServlet.SERVICE.getConnectionByName(connectionName);
    } catch (ConnectionServiceException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    Connection conn = null;

    String driverClass = connection.getDriverClass();
    if (StringUtils.isEmpty(driverClass)) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0014_CONNECTION_ATTEMPT_FAILED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0014_CONNECTION_ATTEMPT_FAILED")); //$NON-NLS-1$
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
        logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH"),e); //$NON-NLS-1$
    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH",driverClass)); //$NON-NLS-1$
    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
        logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
        logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0013_UNABLE_TO_CONNECT"), e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceInMemoryDelegate.ERROR_0013_UNABLE_TO_CONNECT"), e); //$NON-NLS-1$
    }
  }

  public static SQLConnection getConnection(String connectionName) throws DatasourceServiceException {
    IConnection connection = null;
    try {
      connection = ConnectionDebugGwtServlet.SERVICE.getConnectionByName(connectionName);
      return new SQLConnection(connection.getDriverClass(), connection.getUrl(), connection.getUsername(), connection.getPassword(), null);
    } catch (ConnectionServiceException e1) {
      return null;
    }
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
      sqlConnection = getConnection(connectionName);
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
    String line = null;
    int row = 0;
    List<List<String>> dataSample = new ArrayList<List<String>>(rowLimit);
    File file = new File(fileLocation);
    BufferedReader bufRdr = null;
    try {
      bufRdr = new BufferedReader(new FileReader(file));
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    //read each line of text file
    try {
      while((line = bufRdr.readLine()) != null && row < rowLimit)
      {
        StringTokenizer st = new StringTokenizer(line,delimiter);
        List<String> rowData = new ArrayList<String>();
        while (st.hasMoreTokens())
        {
          //get next token and store it in the list
          rowData.add(st.nextToken());
        }
        if(headerPresent && row != 0 || !headerPresent) {
          dataSample.add(rowData);  
        }
        row++;
      }
      //close the file
      bufRdr.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return dataSample;
  }
}
