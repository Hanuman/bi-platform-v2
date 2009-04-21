package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.IDatasource;


public interface DatasourceDialogListener {

  public void onDialogFinish(IDatasource datasource);

  public void onDialogCancel();
}
