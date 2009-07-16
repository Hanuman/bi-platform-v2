package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;
import org.pentaho.ui.xul.util.DialogController;

public interface IDatasourceEditor extends DialogController<Domain> {
  public void showEditDialog(final String domainId, final String modelId);
}
