package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.DialogController.DialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Creates the singleton datasource editor and sets up native JavaScript functions to show the editor.
 */
public class GwtDatasourceEditorEntryPoint implements EntryPoint {

  private GwtDatasourceEditor editor;
  private DatasourceService datasourceService;
  private ConnectionService connectionService;

  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    editor = new GwtDatasourceEditor(datasourceService, connectionService);
    setupNativeHooks(this);
  }

  private native void setupNativeHooks(GwtDatasourceEditorEntryPoint editor)/*-{
    $wnd.openDatasourceEditor= function(callback) {
      editor.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
  }-*/;

  /**
   * Entry-point from Javascript, responds to provided callback with the following:
   *
   *    onOk(String JSON, String mqlString);
   *    onCancel();
   *    onError(String errorMessage);
   *
   * @param callback
   *
   */
  private void show(final JavaScriptObject callback){
    final DialogListener<IDatasource> listener = new DialogListener<IDatasource>(){
      public void onDialogCancel() {
        editor.getDialogController().hideDialog();
        notifyCallbackCancel(callback);
        editor.getDialogController().removeDialogListener(this);
      }

      public void onDialogAccept(final IDatasource datasource) {
        datasourceService.addDatasource(datasource, new XulServiceCallback<Boolean>(){
          public void success(Boolean value) {
            WAQRTransport transport = WAQRTransport.createFromMetadata(datasource.getBusinessData().getDomain());
            notifyCallbackSuccess(callback, value, transport);
          }

          public void error(String s, Throwable throwable) {
            notifyCallbackError(callback, throwable.getMessage());
          }
        });

      }
    };
    editor.getDialogController().addDialogListener(listener);
    editor.getDialogController().showDialog();
  }

  private native void notifyCallbackSuccess(JavaScriptObject callback, Boolean value, WAQRTransport transport)/*-{
    callback.onFinish(value, transport);
  }-*/;

  private native void notifyCallbackError(JavaScriptObject callback, String error)/*-{
    callback.onError(error);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback)/*-{
    callback.onCancel();
  }-*/;
}
