package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Creates the singleton datasource editor and sets up native JavaScript functions to show the editor.
 */
public class GwtDatasourceEditorEntryPoint implements EntryPoint {

  private GwtDatasourceEditor editor;
  private IXulAsyncDatasourceService datasourceService;
  private IXulAsyncConnectionService connectionService;

  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    editor = new GwtDatasourceEditor(datasourceService, connectionService, null);
    setupNativeHooks(this);
  }

  private native void setupNativeHooks(GwtDatasourceEditorEntryPoint editor)/*-{
    $wnd.openDatasourceEditor= function(callback) {
      editor.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
    $wnd.openEditDatasourceEditor= function(domainId, modelId, callback) {
      editor.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showEdit(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelId, callback);
    }
    $wnd.deleteModel=function(domainId, modelName, callback) {
      editor.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::deleteLogicalModel(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelName, callback);
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
  private void show(final JavaScriptObject callback) {
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }
    };
    editor.addDialogListener(listener);
    editor.showDialog();
  }
  
  /**
   * edit entry-point from Javascript, responds to provided callback with the following:
   *
   *    onOk(String JSON, String mqlString);
   *    onCancel();
   *    onError(String errorMessage);
   *
   * @param callback
   *
   */
  private void showEdit(final String domainId, final String modelId, final JavaScriptObject callback) {
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
            WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
            notifyCallbackSuccess(callback, true, transport);
      }
    };
    editor.addDialogListener(listener);
    editor.showEditDialog(domainId, modelId);
  }
  

  /**
   * Deletes the selected model
   *
   *    onOk(Boolean value);
   *    onCancel();
   *    onError(String errorMessage);
   *
   * @param callback
   *
   */
  private void deleteLogicalModel(String domainId, String modelName, final JavaScriptObject callback) {
    datasourceService.deleteLogicalModel(domainId, modelName, new XulServiceCallback<Boolean>(){
      public void success(Boolean value) {
        notifyCallbackSuccess(callback, value);
      }

      public void error(String s, Throwable throwable) {
        notifyCallbackError(callback, throwable.getMessage());
      }
    });
  }
  private native void notifyCallbackSuccess(JavaScriptObject callback, Boolean value, WAQRTransport transport)/*-{
    callback.onFinish(value, transport);
  }-*/;

  private native void notifyCallbackSuccess(JavaScriptObject callback, Boolean value)/*-{
  callback.onFinish(value);
  }-*/;
  
  private native void notifyCallbackError(JavaScriptObject callback, String error)/*-{
    callback.onError(error);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback)/*-{
    callback.onCancel();
  }-*/;
}
