package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Datasource;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetConverter;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.webservices.PentahoSessionHolder;
import org.pentaho.pms.schema.v3.physical.IDataSource;
import org.pentaho.pms.schema.v3.physical.SQLDataSource;
import org.pentaho.pms.service.CsvModelManagementService;
import org.pentaho.pms.service.IModelManagementService;
import org.pentaho.pms.service.IModelQueryService;
import org.pentaho.pms.service.JDBCModelManagementService;
import org.pentaho.pms.service.ModelManagementServiceException;

/*
 * TODO mlowery This class professes to be a datasource service yet it takes as inputs both IDatasource instances and 
 * lower-level BusinessData instances. (BusinessData instances are stored in IDatasources.) They are not currently being
 * kept in sync. I propose that the service only deals with IDatasources from a caller perspective.
 */
public class DatasourceServiceDelegate {

  private IDataAccessPermissionHandler dataAccessPermHandler;
  private IModelManagementService modelManagementService;
  private IModelQueryService modelQueryService;
  private IMetadataDomainRepository metadataDomainRepository;
  private IPentahoSession session;
  private static final Log logger = LogFactory.getLog(DatasourceServiceDelegate.class);

  public DatasourceServiceDelegate() {
    modelManagementService =  new JDBCModelManagementService();
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, null);
  }

  public IPentahoSession getSession() {
    return session;
  }

  public void setSession(IPentahoSession session) {
    this.session = session;
  }

  protected boolean hasDataAccessPermission() {
    if (dataAccessPermHandler == null) {
      String dataAccessClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessPermHandler = (IDataAccessPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
      }
      
    }
    return dataAccessPermHandler != null && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }
  
  
  public List<IDatasource> getDatasources() {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    List<IDatasource> datasources = new ArrayList<IDatasource>();
    Set<String> domainIds = metadataDomainRepository.getDomainIds();
    for (String domainId : domainIds) {
      Domain domain = metadataDomainRepository.getDomain(domainId);
      BusinessData bs = new BusinessData();
      bs.setDomain(domain);
      Datasource ds = new Datasource();
      ds.setBusinessData(bs);
      ds.setDatasourceName(domain.getId());
      datasources.add(ds);
    }
    return datasources;
  }
  
  public IDatasource getDatasourceByName(String name) {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    for(IDatasource datasource:getDatasources()) {
      if(datasource.getDatasourceName().equals(name)) {
        return datasource;
      }
    }
    return null;
  }
  
  public Boolean addDatasource(IDatasource datasource) {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    getDatasources().add(datasource);
    return true;
  }
  
  public Boolean updateDatasource(IDatasource datasource) {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    List<IDatasource> datasources = getDatasources();
    for(IDatasource datasrc:datasources) {
      if(datasrc.getDatasourceName().equals(datasource.getDatasourceName())) {
        datasources.remove(datasrc);
        datasources.add(datasource);
      }
    }
    return true;
  }
  public Boolean deleteDatasource(IDatasource datasource) {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    List<IDatasource> datasources = getDatasources();
    datasources.remove(datasources.indexOf(datasource));
    return true;
  }
  public Boolean deleteDatasource(String name) {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        return null;
    }
    List<IDatasource> datasources = getDatasources();
    for(IDatasource datasource:datasources) {
      if(datasource.getDatasourceName().equals(name)) {
        return deleteDatasource(datasource);
      }
    }
    return false;
  }

  
  public SerializedResultSet doPreview(IConnection connection, String query, String previewLimit) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    SerializedResultSet serializedResultSet = null;
    int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit): -1;
    try {
      conn = getDataSourceConnection(connection);

      if (!StringUtils.isEmpty(query)) {
        stmt = conn.createStatement();
        if(limit >=0) {
          stmt.setMaxRows(limit);
        }        
        ResultSetConverter rsc = new ResultSetConverter(stmt.executeQuery(query));
        serializedResultSet =  new SerializedResultSet(rsc.getColumnTypeNames(), rsc.getMetaData(), rsc.getResultSet());
  
      } else {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0008_QUERY_NOT_VALID"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0008_QUERY_NOT_VALID")); //$NON-NLS-1$
      }
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED"), e); //$NON-NLS-1$
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED", e.getLocalizedMessage()), e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED"),e);
      }
    }
    return serializedResultSet;

  }
  
  public SerializedResultSet doPreview(IConnection connection, String query) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Connection conn = null;
    Statement stmt = null;
    ResultSet rs = null;
    SerializedResultSet serializedResultSet = null;
    try {
      conn = getDataSourceConnection(connection);

      if (!StringUtils.isEmpty(query)) {
        stmt = conn.createStatement();
        ResultSetConverter rsc = new ResultSetConverter(stmt.executeQuery(query));
        serializedResultSet =  new SerializedResultSet(rsc.getColumnTypeNames(), rsc.getMetaData(), rsc.getResultSet());
      } else {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0008_QUERY_NOT_VALID"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0008_QUERY_NOT_VALID")); //$NON-NLS-1$
      }
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED",e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0009_QUERY_VALIDATION_FAILED"), e); //$NON-NLS-1$
    } finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
          logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED",e.getLocalizedMessage()),e);
          throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0010_PREVIEW_FAILED"),e);
      }
    }
    return serializedResultSet;

  }
  public SerializedResultSet doPreview(IDatasource datasource) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    String limit = datasource.getPreviewLimit();
    if(limit != null && limit.length() > 0) {
      return doPreview(datasource.getSelectedConnection(), datasource.getQuery(), limit);
    } else {
      return doPreview(datasource.getSelectedConnection(), datasource.getQuery());  
    }
    
  }

  public boolean testDataSourceConnection(IConnection connection) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Connection conn = null;
    try {
      conn = getDataSourceConnection(connection);
    } catch (DatasourceServiceException dme) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connection.getName()),dme);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName()),dme); //$NON-NLS-1$
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connection.getName()),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName()),e); //$NON-NLS-1$
      }
    }
    return true;
  }

  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DatasourceServiceException
   */
  private static Connection getDataSourceConnection(IConnection connection) throws DatasourceServiceException {
    Connection conn = null;

    String driverClass = connection.getDriverClass();
    if (StringUtils.isEmpty(driverClass)) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0014_CONNECTION_ATTEMPT_FAILED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0014_CONNECTION_ATTEMPT_FAILED")); //$NON-NLS-1$
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH"),e); //$NON-NLS-1$
    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0011_DRIVER_NOT_FOUND_IN_CLASSPATH",driverClass)); //$NON-NLS-1$
    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0012_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0013_UNABLE_TO_CONNECT"), e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0013_UNABLE_TO_CONNECT"), e); //$NON-NLS-1$
    }
  }

  /**
   * Construct the IDataSource from IConnection and a SQL query
   * This is a temporary fix. We need to figure out a better way of doing. Will be gone once we implement the thin version of common database dialog
   * @param IConnection connection, String query
   * @return IDataSource
   * @throws DatasourceServiceException
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
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNKNOWN_ERROR"),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0014_UNKNOWN_ERROR"), e); //$NON-NLS-1$
    }
  }

  /**
   * This method gets the business data which are the business columns, columns types and sample preview data
   * 
   * @param modelName, connection, query, previewLimit
   * @return BusinessData
   * @throws DatasourceServiceException
   */
  
  public BusinessData generateModel(String modelName, IConnection connection, String query, String previewLimit) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
        throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    try {
      IDataSource dataSource = constructIDataSource(connection, query);
      SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(),
          connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      
      Domain domain = getModelManagementService().generateModel(modelName, connection.getName(), sqlConnection.getNativeConnection(), query);
      List<List<String>> data = getModelManagementService().getDataSample(dataSource, Integer.parseInt(previewLimit));
      
      return new BusinessData(domain, data);
    } catch(ModelManagementServiceException mmse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",mmse.getLocalizedMessage()),mmse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), mmse); //$NON-NLS-1$
    }
  }

  /**
   * This method generates the business mode from the query and save it
   * 
   * @param modelName, connection, query
   * @return BusinessData
   * @throws DatasourceServiceException
   */  
  public BusinessData saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit)  throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Domain domain = null;
    try {
      IDataSource dataSource = constructIDataSource(connection, query);
      SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(),
          connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      domain = getModelManagementService().generateModel(modelName, connection.getName(),
          sqlConnection.getNativeConnection(), query);
      List<List<String>> data = getModelManagementService().getDataSample(dataSource, Integer.parseInt(previewLimit));
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return new BusinessData(domain, data);
    } catch(ModelManagementServiceException mmse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",mmse.getLocalizedMessage()),mmse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), mmse); //$NON-NLS-1$
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domain.getName().toString()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domain.getName().toString()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domain.getName().toString()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domain.getName().toString()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
  }
  /**
   * This method save the model
   * 
   * @param businessData, overwrite
   * @return Boolean
   * @throws DataSourceManagementException
   */  
  public Boolean saveModel(BusinessData businessData, Boolean overwrite)throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }
    Boolean returnValue = false;
    LocalizedString domainName = businessData.getDomain().getName();    
    try {
    getMetadataDomainRepository().storeDomain(businessData.getDomain(), overwrite);
    returnValue = true;
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainName.toString()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainName.toString()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domainName.toString()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domainName.toString()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
    return returnValue;
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
  
  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public BusinessData generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    try  {
    CsvModelManagementService service = new CsvModelManagementService();
    Domain domain  = service.generateModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure);
    List<List<String>> data = service.getDataSample(relativeFilePath, headersPresent, delimeter, enclosure, 5);
    return  new BusinessData(domain, data);
    } catch(Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",e.getLocalizedMessage()),e);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), e); //$NON-NLS-1$
    }
  }

  public Boolean saveInlineEtlModel(Domain modelName, boolean overwrite) throws DatasourceServiceException  {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    LocalizedString domainName = modelName.getName();    
    try {
      getMetadataDomainRepository().storeDomain(modelName, overwrite);
      return true;
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainName.toString()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainName.toString()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domainName.toString()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domainName.toString()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
  }
  public boolean isAdministrator() {
    if(getSession() != null) {
      return SecurityHelper.isPentahoAdministrator(getSession());
    } else {
      return false;
    }
  }
}
