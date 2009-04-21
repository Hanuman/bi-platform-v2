package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.IConnection;

public interface ConnectionDialogListener {

  public void onDialogAccept(IConnection connection);

  public void onDialogCancel();
}
