package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource.EditType;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class ConnectionController extends AbstractXulEventHandler {
  private XulDialog dialog;

  private ConnectionService service;

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private ConnectionModel connectionModel;

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog errorDialog;
  private XulDialog successDialog;
  private XulLabel errorLabel = null;
  private XulLabel successLabel = null;
  
  BindingFactory bf;

  XulTextbox name = null;

  XulTextbox driverClass = null;

  XulTextbox username = null;

  XulTextbox password = null;

  XulTextbox url = null;

  XulButton okBtn = null;

  XulButton testBtn = null;

  XulListbox driverClassList = null;
  public ConnectionController() {

  }

  public void init() {
    saveConnectionConfirmationDialog = (XulDialog) document.getElementById("saveConnectionConfirmationDialog"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    
    name = (XulTextbox) document.getElementById("connectionname"); //$NON-NLS-1$
		driverClass = (XulTextbox) document.getElementById("driverClass"); //$NON-NLS-1$
    
    username = (XulTextbox) document.getElementById("username"); //$NON-NLS-1$
    password = (XulTextbox) document.getElementById("password"); //$NON-NLS-1$
    url = (XulTextbox) document.getElementById("url"); //$NON-NLS-1$
    dialog = (XulDialog) document.getElementById("connectionDialog"); //$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById("removeConfirmationDialog"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    final Binding domainBinding = bf.createBinding(connectionModel, "name", name, "value"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(connectionModel, "driverClass", driverClass, "value"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(connectionModel, "username", username, "value"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(connectionModel, "password", password, "value"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(connectionModel, "url", url, "value"); //$NON-NLS-1$  //$NON-NLS-2$
    okBtn = (XulButton) document.getElementById("connectionDialog_accept"); //$NON-NLS-1$
    testBtn = (XulButton) document.getElementById("testButton"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(connectionModel, "validated", okBtn, "!disabled"); //$NON-NLS-1$  //$NON-NLS-2$
    bf.createBinding(connectionModel, "validated", testBtn, "!disabled"); //$NON-NLS-1$  //$NON-NLS-2$
    okBtn.setDisabled(true);
    testBtn.setDisabled(true);
    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public void showDialog() {
    dialog.show();
  }

  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }
  public void closeErrorDialog() {
    if(!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }
  
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }
  public void closeSuccessDialog() {
    if(!successDialog.isHidden()) {
      successDialog.hide();
    }
  }
  
  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public void setConnectionModel(ConnectionModel model) {
    this.connectionModel = model;
  }

  public ConnectionModel getConnectionModel() {
    return this.connectionModel;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "connectionController";
  }

  public void closeDialog() {
    dialog.hide();
    for (ConnectionDialogListener listener : listeners) {
      listener.onDialogCancel();
    }
  }
  public void  closeSaveConnectionConfirmationDialog(){
    saveConnectionConfirmationDialog.hide(); 
 }
  public void addConnection() {
    try {
      service.testConnection(connectionModel.getConnection(), new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          System.out.println(message);
          error.printStackTrace();
          saveConnectionConfirmationDialog.show(); 
        }

        public void success(Boolean value) {
          if (value) {
            saveConnection();
          } else {
            saveConnectionConfirmationDialog.show();
          }
        }
      }  
      );
    } catch (Exception e) {
      saveConnectionConfirmationDialog.show();
    }
  }

  public void testConnection() {
    try {
      service.testConnection(connectionModel.getConnection(), new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          System.out.println(message);
          error.printStackTrace();
          openErrorDialog("Connection Test Not Successful","Unable to test the connection" + error.getLocalizedMessage());
        }

        public void success(Boolean value) {
          try {

            if (value) {
              openSuccesDialog("Connection Test Successful","Successfully tested the connection");
            } else {
              openErrorDialog("Connection Test Not Successful","Unable to test the connection");            }

          } catch (Exception e) {
            openErrorDialog("Connection Test Not Successful","Unable to test the connection" );              
          }
        }
      });
    } catch (Exception e) {
        openErrorDialog("Connection Test Not Successful","Unable to test the connection");
    }
  }

  public void deleteConnection() {
    removeConfirmationDialog.hide();
    service.deleteConnection(datasourceModel.getSelectedConnection().getName(), new XulServiceCallback<Boolean>() {

      public void error(String message, Throwable error) {
        System.out.println(message);
        error.printStackTrace();
      }

      public void success(Boolean value) {
        try {
          if (value) {
            openSuccesDialog("Connection Deleted","Successfully deleted the connection");
            System.out.println("Deleting from the model " + connectionModel.getName());
            System.out.println("Count before deleting " + datasourceModel.getConnections().size());
            datasourceModel.deleteConnection(connectionModel.getConnection().getName());
            System.out.println("Count after deleting " + datasourceModel.getConnections().size());          
            List<IConnection> connections = datasourceModel.getConnections();
            if (connections != null && connections.size() > 0) {
              System.out.println("Setting selected connection to  " + connections.get(connections.size() - 1).getName());
              datasourceModel.setSelectedConnection(connections.get(connections.size() - 1));
            } else {
              System.out.println("Setting selected connection to  null");
              datasourceModel.setSelectedConnection(null);
            }

          } else {
            openErrorDialog("Connection Not Deleted","Unable to deleted the connection");
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void saveConnection() {
    if(!saveConnectionConfirmationDialog.isHidden()) {
      saveConnectionConfirmationDialog.hide();
    }
    if (EditType.ADD.equals(datasourceModel.getEditType())) {
      service.addConnection(connectionModel.getConnection(), new XulServiceCallback<Boolean>() {

        public void error(String message, Throwable error) {
          System.out.println(message);
          error.printStackTrace();
          openErrorDialog("Connection Not saved","Unable to save the connection "+error.getLocalizedMessage());
        }

        public void success(Boolean value) {
          try {
            dialog.hide();
            if (value) {
              openSuccesDialog("Connection Saved","Successfully saved the connection");
              datasourceModel.addConnection(connectionModel.getConnection());
              datasourceModel.setSelectedConnection(connectionModel.getConnection());
            } else {
              openErrorDialog("Connection Not saved","Unable to save the connection");
            }

          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      });
    } else {
      service.updateConnection(connectionModel.getConnection(), new XulServiceCallback<Boolean>() {

        public void error(String message, Throwable error) {
          System.out.println(message);
          error.printStackTrace();
          openErrorDialog("Connection Not Updated","Unable to update the connection "+error.getLocalizedMessage());          
        }

        public void success(Boolean value) {
          try {
            dialog.hide();
            if (value) {
              openSuccesDialog("Connection Updated","Successfully updated the connection");
              datasourceModel.updateConnection(connectionModel.getConnection());
              datasourceModel.setSelectedConnection(connectionModel.getConnection());
            } else {
               openErrorDialog("Connection Not updated","Unable to updated the connection");
            }

          } catch (Exception e) {
          }
        }
      });
    }
  }

  public ConnectionService getService() {
    return service;
  }

  public void setService(ConnectionService service) {
    this.service = service;
  }

  public void addConnectionDialogListener(ConnectionDialogListener listener) {
    if (listeners.contains(listener) == false) {
      listeners.add(listener);
    }
  }

  public void removeConnectionDialogListener(ConnectionDialogListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }
}
