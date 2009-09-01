/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.CsvDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.DatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.IDatasourceTypeController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.RelationalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

/**
 * GWT implementation of a datasource editor. Constructor takes all external dependencies. Dialog is shown/hidden via
 * the <code>DialogController</code>.
 */
public class GwtDatasourceEditor implements IXulLoaderCallback, IDatasourceEditor {

  DatasourceController datasourceController = new DatasourceController();
  private CsvDatasourceController csvDatasourceController = new CsvDatasourceController();
  private RelationalDatasourceController relationalDatasourceController = new RelationalDatasourceController();
  private ConnectionController connectionController = new ConnectionController();
  private IXulAsyncConnectionService connectionService;
  private IXulAsyncDatasourceService datasourceService;
  private DatasourceModel datasourceModel = new DatasourceModel();
  private DatasourceMessages datasourceMessages = new GwtDatasourceMessages();
  private WaitingDialog waitingDialog;
  private GwtXulDomContainer container;
  private AsyncConstructorListener constructorListener;
  private boolean initialized;
  
  public GwtDatasourceEditor(final IXulAsyncDatasourceService datasourceService, final IXulAsyncConnectionService connectionService, final AsyncConstructorListener constructorListener) {
    this(datasourceService, connectionService, constructorListener, true);
  }
  
  public GwtDatasourceEditor(final IXulAsyncDatasourceService datasourceService, final IXulAsyncConnectionService connectionService, final AsyncConstructorListener constructorListener, boolean checkHasAccess) {
    this.constructorListener = constructorListener;
    if (checkHasAccess) {
      datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          showErrorDialog(
              datasourceMessages.getString("DatasourceEditor.ERROR"), //$NON-NLS-1$
              datasourceMessages.getString("DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG",error.getLocalizedMessage())); //$NON-NLS-1$
        }
        public void success(Boolean retVal) {
          if (retVal) {
            init(datasourceService, connectionService);
          } else {
            if (constructorListener != null) {
              constructorListener.asyncConstructorDone();
            }
          }
        }
      });
    } else {
      init(datasourceService, connectionService);
    }
  }
  
  private void init(final IXulAsyncDatasourceService datasourceService, final IXulAsyncConnectionService connectionService) {
    setDatasourceService(datasourceService);
    setConnectionService(connectionService);
    AsyncXulLoader.loadXulFromUrl("datasourceEditorDialog.xul", "datasourceEditorDialog", GwtDatasourceEditor.this); //$NON-NLS-1$//$NON-NLS-2$
  }
  
  private void reloadConnections() {
    if(connectionService != null) {
      connectionService.getConnections(new XulServiceCallback<List<IConnection>>(){

        public void error(String message, Throwable error) {
          showErrorDialog(datasourceMessages.getString("DatasourceEditor.ERROR"),datasourceMessages.getString("DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG",error.getLocalizedMessage()));
        }

        public void success(List<IConnection> connections) {
          datasourceModel.getRelationalModel().setConnections(connections);
        }
        
      });
    } else {
      showErrorDialog(datasourceMessages.getString("DatasourceEditor.ERROR"),"DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL");
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
      AsyncXulLoader.loadOverlayFromUrl("datasourceEditorDialog-gwt-overlay.xul", "datasourceEditorDialog", container, this); //$NON-NLS-1$//$NON-NLS-2$
      datasourceMessages.setMessageBundle((ResourceBundle) container.getResourceBundles().get(0));
      GwtBindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());
      
      datasourceController.setBindingFactory(bf);
      datasourceController.setDatasourceMessages(datasourceMessages);
      waitingDialog = new GwtWaitingDialog(datasourceMessages.getString("waitingDialog.previewLoading"),datasourceMessages.getString("waitingDialog.generatingPreview"));
      container.addEventHandler(datasourceController);

      csvDatasourceController.setBindingFactory(bf);
      csvDatasourceController.setDatasourceMessages(datasourceMessages);
      csvDatasourceController.setWaitingDialog(waitingDialog);
      container.addEventHandler(csvDatasourceController);

      relationalDatasourceController.setBindingFactory(bf);
      relationalDatasourceController.setDatasourceMessages(datasourceMessages);
      relationalDatasourceController.setWaitingDialog(waitingDialog);
      container.addEventHandler(relationalDatasourceController);

      
      connectionController.setDatasourceMessages(datasourceMessages);      
      container.addEventHandler(connectionController);
      
      datasourceModel.getCsvModel().setMessages(datasourceMessages);
      datasourceController.setDatasourceModel(datasourceModel);
      csvDatasourceController.setDatasourceModel(datasourceModel);
      relationalDatasourceController.setDatasourceModel(datasourceModel);
      connectionController.setDatasourceModel(datasourceModel);
      
      List<IDatasourceTypeController> datasourceTypeControllers = new ArrayList<IDatasourceTypeController>();
      datasourceTypeControllers.add(relationalDatasourceController);
      datasourceTypeControllers.add(csvDatasourceController);
      
      datasourceController.setDatasourceTypeControllers(datasourceTypeControllers);
      
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
      throw new IllegalStateException(datasourceMessages.getString("DatasourceEditor.ERROR_0003_CONSTRUCTOR_NOT_INITIALIZED_ERROR")); //$NON-NLS-1$
    }
  }

  private void setConnectionService(IXulAsyncConnectionService service){
    this.connectionService = service;
    connectionController.setService(service);
//    relationalDatasourceController.setConnectionService(service);
    reloadConnections();
  }

  private void setDatasourceService(IXulAsyncDatasourceService service){
    this.datasourceService = service;
    datasourceController.setService(service);
    csvDatasourceController.setService(service);
    relationalDatasourceController.setService(service);
  }

  public DatasourceModel getDatasourceModel() {
    checkInitialized();
    return datasourceModel;
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
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
  public void removeDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
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
  
  public void showEditDialog(final String domainId, final String modelId) {

    // initialize connections
    if(datasourceModel.getRelationalModel().getConnections() == null || datasourceModel.getRelationalModel().getConnections().size() <= 0) {
      checkInitialized();
      reloadConnections();
    }
    
    datasourceController.showEditDialog(domainId, modelId);
  }

}
