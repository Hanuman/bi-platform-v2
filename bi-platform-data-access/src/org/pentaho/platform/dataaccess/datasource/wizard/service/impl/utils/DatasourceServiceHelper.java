package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

public class DatasourceServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceServiceHelper.class);


  public static Connection getDataSourceConnection(String connectionName, IPentahoSession session) {
    SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, session, null);
    return sqlConnection.getNativeConnection(); 
  }
  
  public static List<List<String>> getRelationalDataSample(String connectionName, String query, int rowLimit, IPentahoSession session) {
    List<List<String>> dataSample = new ArrayList<List<String>>(rowLimit);
    Connection conn = null;
    Statement stmt = null;
    ResultSet results = null;

    try {
      conn = getDataSourceConnection(connectionName, session);
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
