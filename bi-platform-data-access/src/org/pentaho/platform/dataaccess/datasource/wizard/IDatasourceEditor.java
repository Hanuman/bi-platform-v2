package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.ui.xul.util.DialogController;

public interface IDatasourceEditor extends DialogController<IDatasource> {
  public void showEditDialog(final String domainId, final String modelId);
}
