package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.ui.xul.util.DialogController;

public interface HasDialogController<T> {
  DialogController<T> getDialogController();
}
