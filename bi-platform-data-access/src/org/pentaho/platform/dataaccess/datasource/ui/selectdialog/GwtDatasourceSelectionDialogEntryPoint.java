package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;

import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditor;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
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

  private IXulAsyncConnectionService connectionService;

  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    editor = new GwtDatasourceEditor(datasourceService, connectionService, null);
    selectDialog = new GwtDatasourceSelectionDialog(datasourceService, editor, null);
    setupNativeHooks(this);
  }

  public native void setupNativeHooks(final GwtDatasourceSelectionDialogEntryPoint d) /*-{
    $wnd.showDatasourceSelectionDialog = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
  }-*/;

  private void show(final JavaScriptObject callback) {
    final DialogListener<LogicalModelSummary> listener = new DialogListener<LogicalModelSummary>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final LogicalModelSummary logicalModelSummary) {
        notifyCallbackSuccess(callback, logicalModelSummary.getDomainId(), logicalModelSummary.getModelId());
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
