package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetObject;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.pms.schema.v3.model.Column;
import org.pentaho.pms.schema.v3.physical.IDataSource;
import org.pentaho.pms.schema.v3.physical.SQLDataSource;
import org.pentaho.pms.service.IModelManagementService;
import org.pentaho.pms.service.IModelQueryService;
import org.pentaho.pms.service.JDBCModelManagementService;

public class DatasourceServiceDelegate {

  private List<IDatasource> datasources = new ArrayList<IDatasource>();
  private IModelManagementService modelManagementService;
  private IModelQueryService modelQueryService;
  
  public DatasourceServiceDelegate() {
    modelManagementService =  new JDBCModelManagementService();
  }
  
  public List<IDatasource> getDatasources() {
    return datasources;
  }
  public IDatasource getDatasourceByName(String name) {
    for(IDatasource datasource:datasources) {
      if(datasource.getDatasourceName().equals(name)) {
        return datasource;
      }
    }
    return null;
  }
  public Boolean addDatasource(IDatasource datasource) {
    datasources.add(datasource);
    return true;
  }
  public Boolean updateDatasource(IDatasource datasource) {
    for(IDatasource datasrc:datasources) {
      if(datasrc.getDatasourceName().equals(datasource.getDatasourceName())) {
        datasources.remove(datasrc);
        datasources.add(datasource);
      }
    }
    return true;
  }
  public Boolean deleteDatasource(IDatasource datasource) {
    datasources.remove(datasources.indexOf(datasource));
    return true;
  }
  public Boolean deleteDatasource(String name) {
    for(IDatasource datasource:datasources) {
      if(datasource.getDatasourceName().equals(name)) {
        return deleteDatasource(datasource);
      }
    }
    return false;
  }

  /**
   * Preview the data based on the connection and query provided
   * 
   * @param IConnection connection, String query, String previewLimit
   * @return ResultSetObject
   * @throws DataSourceManagementException
   */
  public ResultSetObject doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException{
    IPentahoResultSet resultSet = null;
    ResultSetObject resultSetObject = null; 
    IPentahoConnection pentahoConnection = null;
    try {
      pentahoConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(), connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      if(previewLimit != null && previewLimit.length() > 0) {
        pentahoConnection.setMaxRows(Integer.parseInt(previewLimit));  
      }
      resultSet = pentahoConnection.executeQuery(query);
      resultSetObject = makeSerializeable(resultSet);
    } catch(Exception e) {
      throw new DatasourceServiceException(e);
    } finally {
      if(resultSet != null) {
        resultSet.close();
        resultSet.closeConnection();
      }
    }
    return  resultSetObject;
  }
 
  /**
   * Converts the IPentahoResultSet to ResultSetObject which is serializeable
   * 
   * @param IPentahoResultSet resultSet
   * @return ResultSetObject
   *
   */
  private ResultSetObject  makeSerializeable(IPentahoResultSet resultSet) {
    String[] columnHeader = new String[resultSet.getColumnCount()];
    String[][] data = new String[resultSet.getRowCount()][resultSet.getColumnCount()];
    ResultSetObject object = new ResultSetObject();
    IPentahoMetaData metadata = resultSet.getMetaData();
    Object[] colHeader =  metadata.getColumnHeaders()[0];
    // Get the column Headers
    for(int i=0;i<colHeader.length;i++) {
      columnHeader[i] = (colHeader[i] != null) ? colHeader[i].toString(): ""; //$NON-NLS-1$
    }
    object.setColumns(columnHeader);
    // Get the row data
    for(int row = 0; row < resultSet.getRowCount(); row++) {
      Object[] dataRowObject = resultSet.getDataRow(row);
      String[] rowData = new String[resultSet.getColumnCount()];
      for(int i=0;i<resultSet.getColumnCount();i++) {
        rowData[i] = (dataRowObject[i] != null) ? dataRowObject[i].toString(): "";//$NON-NLS-1$
      }
      data[row] = rowData;
    }
    object.setData(data);
    return object;
  }

 
  /**
   * Preview the data based on the connection and query provided
   * 
   * @param IConnection connection, String query, String previewLimit
   * @return ResultSetObject
   * @throws DataSourceManagementException
   */
  public ResultSetObject doPreview(IConnection connection, String query) throws DatasourceServiceException{
    IPentahoResultSet resultSet = null;
    ResultSetObject resultSetObject = null; 
    IPentahoConnection pentahoConnection = null;
    try {
      pentahoConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(), connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      resultSet = pentahoConnection.executeQuery(query);
      resultSetObject = makeSerializeable(resultSet);
    } catch(Exception e) {
      throw new DatasourceServiceException(e);
    } finally {
      if(resultSet != null) {
        resultSet.close();
        resultSet.closeConnection();
      }
    }
    return  resultSetObject;
  }

  public ResultSetObject doPreview(IDatasource datasource) throws DatasourceServiceException {
    String limit = datasource.getPreviewLimit();
    if(limit != null && limit.length() > 0) {
      return doPreview(datasource.getSelectedConnection(), datasource.getQuery(), limit);
    } else {
      return doPreview(datasource.getSelectedConnection(), datasource.getQuery());  
    }
    
  }

  /**
   * Construct the IDataSource from IConnection and a SQL query
   * This is a temporary fix. We need to figure out a better way of doing. Will be gone once we implement the thin version of common database dialog
   * @param IConnection connection, String query
   * @return IDataSource
   * @throws DataSourceManagementException
   */
  private IDataSource constructIDataSource(IConnection connection, String query) throws DatasourceServiceException{
    final String SLASH = "/"; //$NON-NLS-1$
    final String DOUBLE_SLASH = "//";//$NON-NLS-1$
    final String COLON = ":";//$NON-NLS-1$
    String databaseType = null;
    String databaseName = null;
    String hostname = null;
    String port = null;
    String url = connection.getUrl();
    try {
    int lastIndexOfSlash = url.lastIndexOf(SLASH); 
    if((lastIndexOfSlash >= 0) &&( lastIndexOfSlash +SLASH.length() <=url.length())) {
      databaseName = url.substring(lastIndexOfSlash+SLASH.length() ,url.length());
    }
    int lastIndexOfDoubleSlash =  url.lastIndexOf(DOUBLE_SLASH);
    int indexOfColonFromDoubleSlash = url.indexOf(COLON,lastIndexOfDoubleSlash);
    if(lastIndexOfDoubleSlash >=  0 && lastIndexOfDoubleSlash+DOUBLE_SLASH.length() <= url.length()) {
      hostname = url.substring(lastIndexOfDoubleSlash+DOUBLE_SLASH.length(), indexOfColonFromDoubleSlash);
    }
    if(indexOfColonFromDoubleSlash >=0 && indexOfColonFromDoubleSlash + SLASH.length() <= url.length() &&  lastIndexOfSlash >=0 && lastIndexOfSlash <= url.length()) {
      port = url.substring(indexOfColonFromDoubleSlash + SLASH.length(), lastIndexOfSlash);
    }
    if(connection.getDriverClass().equals("org.hsqldb.jdbcDriver")) {//$NON-NLS-1$
      databaseType = "Hypersonic";//$NON-NLS-1$
    } else if(connection.getDriverClass().equals("com.mysql.jdbc.Driver") || connection.getDriverClass().equals("org.git.mm.mysql.Driver")){ //$NON-NLS-1$ //$NON-NLS-2$ 
      databaseType="MySql"; //$NON-NLS-1$
    }
    DatabaseMeta dbMeta = new DatabaseMeta(databaseName, databaseType, "JDBC", hostname, databaseName, port, connection.getUsername(), connection.getPassword()); //$NON-NLS-1$
    return new SQLDataSource(dbMeta, query);
    } catch(Exception e) {
      throw new DatasourceServiceException(e);
    }
  }
  
  /**
   * This method gets the business data which are the business columns, columns types and sample preview data
   * 
   * @param IDatasource datasource
   * @return BusinessData
   * @throws DataSourceManagementException
   */
  
  public BusinessData getBusinessData(IDatasource datasource) throws DatasourceServiceException {
    return getBusinessData(datasource.getSelectedConnection(), datasource.getQuery(), datasource.getPreviewLimit());  }

  /**
   * This method gets the business data which are the business columns, columns types and sample preview data
   * 
   * @param IConnection connection, String query, String previewLimit
   * @return BusinessData
   * @throws DataSourceManagementException
   */
  
  public BusinessData getBusinessData(IConnection connection, String query, String previewLimit) throws DatasourceServiceException {
      IDataSource dataSource = constructIDataSource(connection, query);
      List<Column> columns = getModelManagementService().getColumns(dataSource);
      List<List<String>> data = getModelManagementService().getDataSample(dataSource, Integer.parseInt(previewLimit));
      return new BusinessData(columns, data);
  }

  /**
   * This method create a catagory with the connection information and the business data as input
   * 
   * @param String categoryName, IConnection connection, String query, BusinessData businessData
   * @return Boolean
   * @throws DataSourceManagementException
   */  
  public Boolean createCategory(String categoryName, IConnection connection, String query, BusinessData businessData) throws DatasourceServiceException{
    IDataSource dataSource = constructIDataSource(connection, query);
    getModelManagementService().createCategory(dataSource, categoryName, businessData.getColumns());
    return true;
  }
  
  public void setModelManagementService(IModelManagementService modelManagementService) {
    this.modelManagementService = modelManagementService;
  }

  public IModelManagementService getModelManagementService() {
    return modelManagementService;
  }

  public void setModelQueryService(IModelQueryService modelQueryService) {
    this.modelQueryService = modelQueryService;
  }

  public IModelQueryService getModelQueryService() {
    return modelQueryService;
  }

  
}
