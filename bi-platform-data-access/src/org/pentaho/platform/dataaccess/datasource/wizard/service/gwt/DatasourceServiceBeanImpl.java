package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceDelegate;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

public class DatasourceServiceBeanImpl implements DatasourceGwtService {

  DatasourceServiceDelegate SERVICE;

  public DatasourceServiceBeanImpl() {
    SERVICE = new DatasourceServiceDelegate();
  }

  public Boolean addDatasource(IDatasource datasource) {
    return SERVICE.addDatasource(datasource);
  }

  public Boolean deleteDatasource(IDatasource datasource) {
    return SERVICE.deleteDatasource(datasource);
  }

  public SerializedResultSet doPreview(IConnection connection, String query, String previewLimit)
      throws DatasourceServiceException {
    return SERVICE.doPreview(connection, query, previewLimit);
  }

  public IDatasource getDatasourceByName(String name) {
    return SERVICE.getDatasourceByName(name);
  }

  public List<IDatasource> getDatasources() {
    return SERVICE.getDatasources();
  }

  public Boolean updateDatasource(IDatasource datasource) {
    return SERVICE.updateDatasource(datasource);
  }

  public BusinessData generateModel(String modelName, IConnection connection, String query, String previewLimit)
      throws DatasourceServiceException {
    return SERVICE.generateModel(modelName, connection, query, previewLimit);
  }
  public BusinessData saveModel(String modelName, IConnection connection, String query, Boolean overwrite, String previewLimit)
  throws DatasourceServiceException {
    return SERVICE.saveModel(modelName, connection, query, overwrite, previewLimit);
  }

  public Boolean saveModel(BusinessData businessData, Boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveModel(businessData, overwrite);
  }
  public Boolean isAdministrator() {
    SERVICE.setSession(PentahoSessionHolder.getSession());
    return SERVICE.isAdministrator();
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

  public BusinessData generateInlineEtlModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure) throws DatasourceServiceException {
    return SERVICE.generateInlineEtlModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure);
   }

  public Boolean saveInlineEtlModel(Domain modelName, Boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveInlineEtlModel(modelName, overwrite);
  }

  public String getUploadFilePath() throws DatasourceServiceException {
    return SERVICE.getUploadFilePath();
  }

  public Boolean deleteModel(String domainId, String modelName)  throws DatasourceServiceException {
    return SERVICE.deleteModel(domainId, modelName);
  }
}