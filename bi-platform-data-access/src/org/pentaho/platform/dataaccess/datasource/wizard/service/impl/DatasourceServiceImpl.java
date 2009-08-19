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
 * Created June 4, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.InlineEtlModelGenerator;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.pentahometadata.MetadataQueryComponent;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;

public class DatasourceServiceImpl implements IDatasourceService {

  private static final Log logger = LogFactory.getLog(DatasourceServiceImpl.class);

  private IDataAccessPermissionHandler dataAccessPermHandler;

  private IDataAccessViewPermissionHandler dataAccessViewPermHandler;

  private IMetadataDomainRepository metadataDomainRepository;

  private static final String BEFORE_QUERY = " SELECT * FROM ("; //$NON-NLS-1$

  private static final String AFTER_QUERY = ") tbl"; //$NON-NLS-1$

  public DatasourceServiceImpl() {
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, null);
    String dataAccessClassName = null;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessClassName = resLoader
          .getPluginSetting(
              getClass(),
              "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler"); //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessClassName);
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessPermHandler = (IDataAccessPermissionHandler) defaultConstructor.newInstance(new Object[] {});
    } catch (Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"), e); //$NON-NLS-1$
      // TODO: Unhardcode once this is an actual plugin
      dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    }
    String dataAccessViewClassName = null;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessViewClassName = resLoader
          .getPluginSetting(
              getClass(),
              "settings/data-access-view-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler"); //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessViewClassName);
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessViewPermHandler = (IDataAccessViewPermissionHandler) defaultConstructor.newInstance(new Object[] {});
    } catch (Exception e) {
      logger.error(
          Messages.getErrorString("DatasourceServiceImpl.ERROR_0030_DATAACCESS_VIEW_PERMISSIONS_INIT_ERROR"), e); //$NON-NLS-1$
      // TODO: Unhardcode once this is an actual plugin
      dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
    }

  }

  protected boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null
        && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }

  protected boolean hasDataAccessViewPermission() {
    return dataAccessViewPermHandler != null
        && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    if (dataAccessViewPermHandler == null) {
      return -1;
    }
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
  }

  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      return false;
    }
    try {
      metadataDomainRepository.removeModel(domainId, modelName);
    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage()), dse);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
    return true;
  }

  private IPentahoResultSet executeQuery(String connectionName, String query, String previewLimit) throws QueryValidationException {
    SQLConnection sqlConnection = null;
    try {
      int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit) : -1;
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE,
          connectionName, PentahoSessionHolder.getSession(), new SimpleLogger(DatasourceServiceHelper.class.getName()));
      sqlConnection.setMaxRows(limit);
      sqlConnection.setReadOnly(true);
      return sqlConnection.executeQuery(BEFORE_QUERY + query + AFTER_QUERY);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new QueryValidationException(e.getLocalizedMessage(), e);
    } finally {
      if (sqlConnection != null) {
        sqlConnection.close();
      }
    }
  }

  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit)
      throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    SerializedResultSet returnResultSet;
    try {
      executeQuery(connectionName, query, previewLimit);
      returnResultSet = DatasourceServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$      
    }
    return returnResultSet;

  }

  public boolean testDataSourceConnection(String connectionName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    Connection conn = null;
    try {
      conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      if (conn == null) {
        logger.error(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName));//$NON-NLS-1$
        throw new DatasourceServiceException(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName)); //$NON-NLS-1$
      }
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage()), e);//$NON-NLS-1$
        throw new DatasourceServiceException(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage()), e); //$NON-NLS-1$
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

  public BusinessData generateLogicalModel(String modelName, String connectionName, String query, String previewLimit)
      throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    try {
      // Testing whether the query is correct or not
      executeQuery(connectionName, query, previewLimit);
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, resultSet.getColumnTypes(), resultSet.getColumns(), query,
          securityEnabled, getPermittedRoleList(), getPermittedUserList(), getDefaultAcls(), (PentahoSessionHolder
              .getSession() != null) ? PentahoSessionHolder.getSession().getName() : null);
      Domain domain = sqlModelGenerator.generate();
      return new BusinessData(domain, resultSet.getData());
    } catch (SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage()), smge);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage()), smge); //$NON-NLS-1$
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
   }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public BusinessData generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimiter, String enclosure) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }

    try {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);

      String relativePath = PentahoSystem.getSystemSetting(
          "file-upload-defaults/relative-path", String.valueOf(MetadataQueryComponent.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$
      String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);

      InlineEtlModelGenerator inlineEtlModelGenerator = new InlineEtlModelGenerator(modelName, csvFileLoc,
          relativeFilePath, headersPresent, delimiter, enclosure, securityEnabled, getPermittedRoleList(),
          getPermittedUserList(), getDefaultAcls(), (PentahoSessionHolder.getSession() != null) ? PentahoSessionHolder
              .getSession().getName() : null);

      Domain domain = inlineEtlModelGenerator.generate();
      List<List<String>> data = DatasourceServiceHelper.getCsvDataSample(csvFileLoc + relativeFilePath, headersPresent,
          delimiter, enclosure, 5);
      return new BusinessData(domain, data);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
  }

  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }

    String domainName = domain.getId();
    try {
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return true;
    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage()), dse);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch (DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage()), dae);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage()), dae); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
  }

  public boolean hasPermission() {
    if (PentahoSessionHolder.getSession() != null) {
      return (SecurityHelper.isPentahoAdministrator(PentahoSessionHolder.getSession()) || hasDataAccessPermission());
    } else {
      return false;
    }
  }

  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException {
    if (!hasDataAccessViewPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    List<LogicalModelSummary> logicalModelSummaries = new ArrayList<LogicalModelSummary>();
    for (String domainId : getMetadataDomainRepository().getDomainIds()) {
      Domain domain = getMetadataDomainRepository().getDomain(domainId);

      String locale = LocaleHelper.getLocale().toString();
      String locales[] = new String[domain.getLocales().size()];
      for (int i = 0; i < domain.getLocales().size(); i++) {
        locales[i] = domain.getLocales().get(i).getCode();
      }
      locale = LocaleHelper.getClosestLocale(locale, locales);

      for (LogicalModel model : domain.getLogicalModels()) {
        logicalModelSummaries.add(new LogicalModelSummary(domainId, model.getId(), model.getName(locale)));
      }
    }
    return logicalModelSummaries;
  }

  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException {
    Domain domain = getMetadataDomainRepository().getDomain(domainId);
    List<List<String>> data = null;
    if (domain.getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel) {
      InlineEtlPhysicalModel model = (InlineEtlPhysicalModel) domain.getPhysicalModels().get(0);

      String relativePath = PentahoSystem.getSystemSetting(
          "file-upload-defaults/relative-path", String.valueOf(MetadataQueryComponent.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$
      String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);

      data = DatasourceServiceHelper.getCsvDataSample(csvFileLoc + model.getFileLocation(), model.getHeaderPresent(),
          model.getDelimiter(), model.getEnclosure(), 5);
    } else {
      SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
      String query = model.getPhysicalTables().get(0).getTargetTable();
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet(model.getDatasource().getDatabaseName(), query, 5,
          PentahoSessionHolder.getSession());
      data = resultSet.getData();
    }
    return new BusinessData(domain, data);
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

}
