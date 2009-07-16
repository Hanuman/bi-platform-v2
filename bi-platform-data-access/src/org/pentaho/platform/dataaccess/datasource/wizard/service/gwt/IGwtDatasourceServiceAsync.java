package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDatasourceServiceAsync {
  void getLogicalModels(AsyncCallback<List<LogicalModelSummary>> callback);
  void deleteLogicalModel(String domainId, String modelName, AsyncCallback<Boolean> callback);
  void doPreview(String connectionName, String query, String previewLimit, AsyncCallback<SerializedResultSet> callback);
  void generateLogicalModel(String modelName, String connectionName, String query, String previewLimit, AsyncCallback<BusinessData> callback);
  void generateAndSaveLogicalModel(String modelName, String connectionName, String query, boolean overwrite, String previewLimit, AsyncCallback<BusinessData> callback);
  void saveLogicalModel(Domain domain, boolean overwrite,AsyncCallback<Boolean> callback);
  void generateInlineEtlLogicalModel(String modelName, String relativeFilePath, boolean headersPresent, String delimeter, String enclosure, AsyncCallback<BusinessData> callback);
  void getUploadFilePath(AsyncCallback<String> callback);
  void hasPermission(AsyncCallback<Boolean> callback);
  void gwtWorkaround (BogoPojo pojo, AsyncCallback<BogoPojo> callback);
  void loadBusinessData(String domainId, String modelId, AsyncCallback<BusinessData> callback);
}

  