package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditor;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

public class GwtDatasourceSelectionDialogEntryPoint implements EntryPoint {

  private GwtDatasourceSelectionDialog selectDialog;

  private GwtDatasourceEditor editor;

  private DatasourceService datasourceService;

  private ConnectionService connectionService;

  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    editor = new GwtDatasourceEditor(datasourceService, connectionService);
    selectDialog = new GwtDatasourceSelectionDialog(datasourceService, editor);
    setupNativeHooks(this);
  }

  public native void setupNativeHooks(final GwtDatasourceSelectionDialogEntryPoint d) /*-{
    $wnd.showDatasourceSelectionDialog = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
  }-*/;

  private void show(final JavaScriptObject callback) {
    final DialogListener<IDatasource> listener = new DialogListener<IDatasource>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final IDatasource datasource) {
        notifyCallbackSuccess(callback, datasource.getBusinessData().getDomain().getId(), datasource.getBusinessData().getDomain().getLogicalModels().get(0).getId());
      }
    };
    selectDialog.addDialogListener(listener);
    selectDialog.showDialog();
  }
  
  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId, String modelId) /*-{
    callback.onFinish(domainId, modelId);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback) /*-{
    callback.onCancel();
  }-*/;
}
