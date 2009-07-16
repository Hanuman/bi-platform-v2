package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;
import java.lang.Boolean;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDatasourceServiceImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DatasourceDebugGwtServlet extends RemoteServiceServlet implements IGwtDatasourceService {

  /**
   * 
   */
  private static final long serialVersionUID = -8247397306730500944L;
  InMemoryDatasourceServiceImpl SERVICE;

  public DatasourceDebugGwtServlet() {
    SERVICE = new InMemoryDatasourceServiceImpl();
  }

  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException{
    return SERVICE.doPreview(connectionName, query, previewLimit);
  }

  public BusinessData generateLogicalModel(String modelName, String connectionName, String query, String previewLimit) throws DatasourceServiceException {
    return SERVICE.generateLogicalModel(modelName, connectionName, query, previewLimit);
   }
  public BusinessData generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit) throws DatasourceServiceException {
    return SERVICE.generateAndSaveLogicalModel(modelName, connectionName, query, overwrite, previewLimit);
  }
  public boolean saveLogicalModel(Domain modelName, boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveLogicalModel(modelName, overwrite);
  }
  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

  public BusinessData generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent,
      String delimeter, String enclosure) throws DatasourceServiceException {
    return SERVICE.generateInlineEtlLogicalModel(modelName, relativeFilePath, headersPresent, delimeter, enclosure);
  }
  
  public boolean hasPermission() {
    return SERVICE.hasPermission();
  }

  public String getUploadFilePath() throws DatasourceServiceException {
    return SERVICE.getUploadFilePath();
  }

  public boolean deleteLogicalModel(String domainId, String modelName)  throws DatasourceServiceException {
    return SERVICE.deleteLogicalModel(domainId, modelName);
  }

  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException {
    return SERVICE.getLogicalModels();
  }

  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException {
    return SERVICE.loadBusinessData(domainId, modelId);
  }

}