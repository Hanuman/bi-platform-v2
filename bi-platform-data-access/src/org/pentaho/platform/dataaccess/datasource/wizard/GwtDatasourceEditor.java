package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.List;

import org.pentaho.gwt.widgets.client.utils.MessageBundle;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.CsvDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.DatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.RelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.EventHandlerWrapper;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController;

import com.google.gwt.core.client.GWT;

/**
 * GWT implementation of a datasource editor. Constructor takes all external dependencies. Dialog is shown/hidden via
 * the <code>DialogController</code>.
 */
public class GwtDatasourceEditor implements IXulLoaderCallback, DialogController<IDatasource> {

  DatasourceController datasourceController = new DatasourceController();
  private CsvDatasourceController csvDatasourceController = new CsvDatasourceController();
  private RelationalDatasourceController relationalDatasourceController = new RelationalDatasourceController();
  private ConnectionController connectionController = new ConnectionController();
  private ConnectionService connectionService;
  private DatasourceService datasourceService;
  private DatasourceModel datasourceModel = new DatasourceModel();
  private ConnectionModel connectionModel = new ConnectionModel();
  private DatasourceMessages datasourceMessages = new GwtDatasourceMessages();
  private GwtXulDomContainer container;
  private AsyncConstructorListener constructorListener;
  private boolean initialized;
  
  public GwtDatasourceEditor(final DatasourceService datasourceService, final ConnectionService connectionService, final AsyncConstructorListener constructorListener) {
    this.constructorListener = constructorListener;
    setDatasourceService(datasourceService);
    setConnectionService(connectionService);
    AsyncXulLoader.loadXulFromUrl("connectionFrame.xul", "connectionFrame", this); //$NON-NLS-1$//$NON-NLS-2$
  }
  
  private void reloadConnections() {
    if(connectionService != null) {
      try {
      connectionService.getConnections(new XulServiceCallback<List<IConnection>>(){

        public void error(String message, Throwable error) {
          showErrorDialog("Error Occurred","Unable to show the dialog." +error.getLocalizedMessage());
        }

        public void success(List<IConnection> connections) {
          datasourceModel.getRelationalModel().setConnections(connections);
        }
        
      });
      } catch(ConnectionServiceException cse) {
        showErrorDialog("Error Occurred","Unable to get connections" + cse.getLocalizedMessage());
      }
    } else {
      showErrorDialog("Error Occurred","Connection Service is null");
    }

  }
  
  private void showErrorDialog(String title, String message) {
    XulDialog errorDialog = (XulDialog) container.getDocumentRoot().getElementById("errorDialog");
    XulLabel errorLabel = (XulLabel) container.getDocumentRoot().getElementById("errorLabel");        
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
  }

 
  public void addConnectionDialogListener(ConnectionDialogListener listener){
    checkInitialized();
    connectionController.addConnectionDialogListener(listener);
  }
  
  public void removeConnectionDialogListener(ConnectionDialogListener listener){
    checkInitialized();
    connectionController.removeConnectionDialogListener(listener);
  }
  
  
  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayLoaded() {
  }

  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void overlayRemoved() {
  }
  
  /**
   * Specified by <code>IXulLoaderCallback</code>.
   */
  public void xulLoaded(final GwtXulRunner runner) {
    try {
      
      container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);
      AsyncXulLoader.loadOverlayFromUrl("connectionFrame-gwt-overlay.xul", "connectionFrame", container, this); //$NON-NLS-1$//$NON-NLS-2$
      datasourceMessages.setMessageBundle((MessageBundle) container.getResourceBundles().get(0));
      GwtBindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());
      
      EventHandlerWrapper wrapper = GWT.create(DatasourceController.class);
      datasourceController.setBindingFactory(bf);
      datasourceController.setDatasourceMessages(datasourceMessages);
      wrapper.setHandler(datasourceController);      
      container.addEventHandler(wrapper);

      wrapper = GWT.create(CsvDatasourceController.class);
      csvDatasourceController.setBindingFactory(bf);
      csvDatasourceController.setDatasourceMessages(datasourceMessages);
      wrapper.setHandler(csvDatasourceController);      
      container.addEventHandler(wrapper);

      wrapper = GWT.create(RelationalDatasourceController.class);
      relationalDatasourceController.setBindingFactory(bf);
      relationalDatasourceController.setDatasourceMessages(datasourceMessages);
      wrapper.setHandler(relationalDatasourceController);      
      container.addEventHandler(wrapper);

      
      wrapper = GWT.create(ConnectionController.class);
      connectionController.setBindingFactory(bf);
      connectionController.setDatasourceMessages(datasourceMessages);      
      wrapper.setHandler(connectionController);      
      container.addEventHandler(wrapper);

      datasourceController.setConnectionModel(connectionModel);
      datasourceController.setDatasourceModel(datasourceModel);
      csvDatasourceController.setDatasourceModel(datasourceModel);
      relationalDatasourceController.setConnectionModel(connectionModel);
      relationalDatasourceController.setDatasourceModel(datasourceModel);
      connectionController.setConnectionModel(connectionModel);
      connectionController.setDatasourceModel(datasourceModel);
      runner.initialize();
      runner.start();
      initialized = true;
      if (constructorListener != null) {
        constructorListener.asyncConstructorDone();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException("You must wait until the constructor listener is notified."); //$NON-NLS-1$
    }
  }

  private void setConnectionService(ConnectionService service){
    this.connectionService = service;
    connectionController.setService(service);
    reloadConnections();
  }

  private void setDatasourceService(DatasourceService service){
    this.datasourceService = service;
    datasourceController.setService(service);
    csvDatasourceController.setService(service);
    relationalDatasourceController.setService(service);
  }

  public DatasourceModel getDatasourceModel() {
    checkInitialized();
    return datasourceModel;
  }
  
  public ConnectionModel getConnectionModel() {
    checkInitialized();
    return connectionModel;
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasource> listener) {
    checkInitialized();
    datasourceController.addDialogListener(listener);  
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void hideDialog() {
    checkInitialized();
    datasourceController.hideDialog();  
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<IDatasource> listener) {
    checkInitialized();
    datasourceController.removeDialogListener(listener);
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {

    if(datasourceModel.getRelationalModel().getConnections() == null || datasourceModel.getRelationalModel().getConnections().size() <= 0) {
      checkInitialized();
      reloadConnections();
    }
    datasourceController.initialize();
    datasourceController.showDialog();  
  }
}
