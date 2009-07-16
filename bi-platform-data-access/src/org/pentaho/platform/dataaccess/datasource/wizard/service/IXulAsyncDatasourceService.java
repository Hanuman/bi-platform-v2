package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncDatasourceService {
  void getLogicalModels(XulServiceCallback<List<LogicalModelSummary>> callback);
  void deleteLogicalModel(String domainId, String modelName, XulServiceCallback<Boolean> callback);
  void doPreview(String connectionName, String query, String previewLimit, XulServiceCallback<SerializedResultSet> callback);
  void generateLogicalModel(String modelName, String connectionName, String query, String previewLimit, XulServiceCallback<BusinessData> callback);
  void generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit, XulServiceCallback<BusinessData> callback);  
  void saveLogicalModel(Domain domain, boolean overwrite,XulServiceCallback<Boolean> callback);
  void generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure, XulServiceCallback<BusinessData> callback);
  void getUploadFilePath(XulServiceCallback<String> callback);
  void hasPermission(XulServiceCallback<Boolean> callback);
  void loadBusinessData(String domainId, String modelId, XulServiceCallback<BusinessData> callback);
}

  