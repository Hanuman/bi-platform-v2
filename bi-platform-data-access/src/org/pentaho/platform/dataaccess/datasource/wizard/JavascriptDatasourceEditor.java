package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 * Default implementation usable by standard web applications or other GWT apps through
 * pure Javascript
 *
 */
public class JavascriptDatasourceEditor implements EntryPoint{

  private GwtDatasourceEditor editor;
  private DatasourceService datasourceService;
  private ConnectionService connectionService;

  public void onModuleLoad() {
    editor = new GwtDatasourceEditor();
    datasourceService = new org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl();
    connectionService = new org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl();
    
    editor.setConnectionService(connectionService);
    editor.setDatasourceService(datasourceService);
    setupNativeHooks(this);
    
  }

  private native void setupNativeHooks(JavascriptDatasourceEditor editor)/*-{

    $wnd.openDatasourceEditor= function(callback) {
      editor.@org.pentaho.platform.dataaccess.datasource.wizard.JavascriptDatasourceEditor::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
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
    final DatasourceDialogListener listener = new DatasourceDialogListener(){
      public void onDialogFinish(final IDatasource datasource) {
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

      public void onDialogCancel() {
        editor.hide();
        notifyCallbackCancel(callback);
        editor.removeDatasourceDialogListener(this);
      }
    };
    editor.addDatasourceDialogListener(listener);
    editor.show();
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
