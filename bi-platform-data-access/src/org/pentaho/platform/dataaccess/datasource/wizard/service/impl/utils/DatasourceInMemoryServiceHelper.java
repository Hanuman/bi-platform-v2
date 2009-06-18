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
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;

public class DatasourceInMemoryServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceInMemoryServiceHelper.class);
  
  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DatasourceServiceException
   */
  public static Connection getDataSourceConnection(IConnection connection) throws DatasourceServiceException {
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

  
  public static List<List<String>> getRelationalDataSample(IConnection connection, String query, int rowLimit) {
    List<List<String>> dataSample = new ArrayList<List<String>>(rowLimit);
    Connection conn = null;
    Statement stmt = null;
    ResultSet results = null;

    try {
      conn = getDataSourceConnection(connection);
      stmt = conn.createStatement();
      results = stmt.executeQuery(query);
      
      int colCount = results.getMetaData().getColumnCount();
      //loop through rows
      int rowIdx = 0;
      while (results.next()) {
        if(rowIdx >= rowLimit) {
          break;
        }
        dataSample.add(new ArrayList<String>(colCount));
        //loop through columns
        for (int colIdx = 1; colIdx <= colCount; colIdx++) {
          dataSample.get(rowIdx).add(results.getString(colIdx));
        }
        rowIdx++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (results != null)
          results.close();
        if (stmt != null)
          stmt.close();
        if (conn != null)
          conn.close();
      } catch (SQLException e) {
      }
    }
    return dataSample;
  }

  public static List<List<String>> getCsvDataSample(String fileLocation, boolean headerPresent, String enclosure, String delimiter, int rowLimit) {
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
