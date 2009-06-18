package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.InlineEtlModelGenerator;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Datasource;
import org.pentaho.platform.dataaccess.datasource.utils.ResultSetConverter;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

/*
 * TODO mlowery This class professes to be a datasource service yet it takes as inputs both IDatasource instances and 
 * lower-level BusinessData instances. (BusinessData instances are stored in IDatasources.) They are not currently being
 * kept in sync. I propose that the service only deals with IDatasources from a caller perspective.
 */
public class DatasourceServiceDelegate {

  private IDataAccessPermissionHandler dataAccessPermHandler;
  private IDataAccessViewPermissionHandler dataAccessViewPermHandler;
  private IMetadataDomainRepository metadataDomainRepository;
  private IPentahoSession session;
  public static final String RELATIVE_UPLOAD_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar ;  
  private static final Log logger = LogFactory.getLog(DatasourceServiceDelegate.class);

  public DatasourceServiceDelegate() {
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
        dataAccessClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
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
  
  protected List<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }
  protected List<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    if (dataAccessViewPermHandler == null) {
      String dataAccessViewClassName = null;
      try {
        IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
        dataAccessViewClassName = resLoader.getPluginSetting(getClass(), "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler" );  //$NON-NLS-1$ //$NON-NLS-2$
        Class<?> clazz = Class.forName(dataAccessViewClassName);
        Constructor<?> defaultConstructor = clazz.getConstructor(new Class[]{});
        dataAccessViewPermHandler = (IDataAccessViewPermissionHandler)defaultConstructor.newInstance(new Object[]{});
      } catch (Exception e) {
        logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"),e);        
          // TODO: Unhardcode once this is an actual plugin
          dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
      }
      
    }
    if(dataAccessViewPermHandler == null) {
      return -1;
    }
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
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
    metadataDomainRepository.removeDomain(datasource.getDatasourceName());
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
  
  public Boolean deleteModel(String domainId, String modelName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      return null;
    }
    try {
      metadataDomainRepository.removeModel(domainId, modelName);
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domainId),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId), dse); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
    return true;
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
      conn = DatasourceServiceHelper.getDataSourceConnection(connection);

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
      conn = DatasourceServiceHelper.getDataSourceConnection(connection);

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
      conn = DatasourceServiceHelper.getDataSourceConnection(connection);
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
      SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(),
          connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
        || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connection.getName(), sqlConnection.getNativeConnection(),
          query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
            ,getDefaultAcls(),(getSession() != null) ? getSession().getName(): null); 
      Domain domain = sqlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getRelationalDataSample(connection, query, Integer.parseInt(previewLimit));
      return new BusinessData(domain, data);
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), smge); //$NON-NLS-1$
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
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
      || (getPermittedUserList() != null && getPermittedUserList().size() > 0); 

      SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection.getDriverClass(),
          connection.getUrl(), connection.getUsername(), connection.getPassword(), null, null);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connection.getName(), sqlConnection.getNativeConnection(),
          query,securityEnabled, getPermittedRoleList(),getPermittedUserList()
            ,getDefaultAcls(),(getSession() != null) ? getSession().getName(): null); 
      domain = sqlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getRelationalDataSample(connection, query, Integer.parseInt(previewLimit));
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return new BusinessData(domain, data);
    } catch(DomainStorageException dse) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0017_UNABLE_TO_STORE_DOMAIN",domain.getName().toString()),dse);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domain.getName().toString()), dse); //$NON-NLS-1$      
    } catch(DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST",domain.getName().toString()),dae);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0018_DOMAIN_ALREADY_EXIST", domain.getName().toString()), dae); //$NON-NLS-1$      
    } catch(DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"),dne);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$
    } catch(SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0016_UNABLE_TO_GENERATE_MODEL",smge.getLocalizedMessage()),smge);
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), smge); //$NON-NLS-1$
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
  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public BusinessData generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent, String delimiter, String enclosure) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0001_PERMISSION_DENIED"));
    }

    try  {
    Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
    || (getPermittedUserList() != null && getPermittedUserList().size() > 0); 
    InlineEtlModelGenerator inlineEtlModelGenerator = new InlineEtlModelGenerator(modelName,
        relativeFilePath, headersPresent, delimiter,enclosure,securityEnabled,
          getPermittedRoleList(),getPermittedUserList(),
            getDefaultAcls(), (getSession() != null) ? getSession().getName(): null);
    Domain domain  = inlineEtlModelGenerator.generate();
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample(relativeFilePath, headersPresent,
        delimiter, enclosure, 5);
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
  public String getUploadFilePath() throws DatasourceServiceException {
    String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$
    return PentahoSystem.getApplicationContext().getSolutionPath(relativePath);    
  }
  public boolean isAdministrator() {
    if(getSession() != null) {
      return SecurityHelper.isPentahoAdministrator(getSession());
    } else {
      return false;
    }
  }
}
