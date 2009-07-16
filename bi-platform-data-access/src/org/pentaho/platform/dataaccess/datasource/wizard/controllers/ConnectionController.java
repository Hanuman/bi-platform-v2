package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.RelationalModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.RelationalModel.ConnectionEditType;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseConnectionService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class ConnectionController extends AbstractXulEventHandler implements DatabaseDialogListener {

  private DatasourceMessages datasourceMessages;

  private IXulAsyncConnectionService service;

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  IConnection currentConnection;

  public ConnectionController() {

  }

  public void init() {

    XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
      public void error(String message, Throwable error) {
      }

      public void success(List<IDatabaseType> retVal) {
        databaseTypeHelper = new DatabaseTypeHelper(retVal);
        databaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
            "dataaccess-databasedialog.xul", ConnectionController.this); //$NON-NLS-1$
      }
    };
    connService.getDatabaseTypes(callback);

    saveConnectionConfirmationDialog = (XulDialog) document.getElementById("saveConnectionConfirmationDialog"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById("removeConfirmationDialog"); //$NON-NLS-1$
  }

  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "connectionController";
  }

  public void closeDialog() {
    for (ConnectionDialogListener listener : listeners) {
      listener.onDialogCancel();
    }
  }

  public void closeSaveConnectionConfirmationDialog() {
    saveConnectionConfirmationDialog.hide();
  }

  public void addConnection() {
    try {
      service.testConnection(currentConnection, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
           saveConnectionConfirmationDialog.show();
        }
        public void success(Boolean value) {
          if (value) {
            saveConnection();
          } else {
            saveConnectionConfirmationDialog.show();
          }
        }
      });
    } catch (Exception e) {
      saveConnectionConfirmationDialog.show();
    }
  }

  public void testConnection() {
    try {
      service.testConnection(currentConnection, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }

        public void success(Boolean value) {
          try {
            if (value) {
              openSuccesDialog(datasourceMessages.getString("SUCCESS"), datasourceMessages
                  .getString("ConnectionController.CONNECTION_TEST_SUCCESS"));
            } else {
              openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages
                  .getString("ConnectionController.ERROR_0003_CONNECTION_TEST_FAILED"));
            }

          } catch (Exception e) {
            displayErrorMessage(e);
          }
        }
      });
    } catch (Exception e) {
      displayErrorMessage(e);
    }
  }

  public void deleteConnection() {
    removeConfirmationDialog.hide();
    service.deleteConnection(datasourceModel.getRelationalModel().getSelectedConnection().getName(),
        new XulServiceCallback<Boolean>() {

          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                openSuccesDialog(datasourceMessages.getString("SUCCESS"), datasourceMessages
                    .getString("ConnectionController.CONNECTION_DELETED"));
                datasourceModel.getRelationalModel().deleteConnection(
                    datasourceModel.getRelationalModel().getSelectedConnection().getName());
                List<IConnection> connections = datasourceModel.getRelationalModel().getConnections();
                if (connections != null && connections.size() > 0) {
                  datasourceModel.getRelationalModel().setSelectedConnection(connections.get(connections.size() - 1));
                } else {
                  datasourceModel.getRelationalModel().setSelectedConnection(null);
                }

              } else {
                openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages
                    .getString("ConnectionController.ERROR_0002_UNABLE_TO_DELETE_CONNECTION"));
              }

            } catch (Exception e) {
              displayErrorMessage(e);
            }
          }
        });
  }

  public void saveConnection() {
    if (!saveConnectionConfirmationDialog.isHidden()) {
      saveConnectionConfirmationDialog.hide();
    }

    if (RelationalModel.ConnectionEditType.ADD.equals(datasourceModel.getRelationalModel().getEditType())) {
        service.addConnection(currentConnection, new XulServiceCallback<Boolean>() {
          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                datasourceModel.getRelationalModel().addConnection(currentConnection);
                datasourceModel.getRelationalModel().setSelectedConnection(currentConnection);
              } else {
                openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages
                    .getString("ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION"));
              }

            } catch (Exception e) {
              displayErrorMessage(e);
            }
          }
        });
    } else {
      service.updateConnection(currentConnection, new XulServiceCallback<Boolean>() {

        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }

        public void success(Boolean value) {
          try {
            if (value) {
              openSuccesDialog(datasourceMessages.getString("SUCCESS"), datasourceMessages
                  .getString("ConnectionController.CONNECTION_UPDATED"));
              datasourceModel.getRelationalModel().updateConnection(currentConnection);
              datasourceModel.getRelationalModel().setSelectedConnection(currentConnection);
            } else {
              openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages
                  .getString("ConnectionController.ERROR_0004_UNABLE_TO_UPDATE_CONNECTION"));
            }

          } catch (Exception e) {
          }
        }
      });
    }
  }

  public IXulAsyncConnectionService getService() {
    return service;
  }

  public void setService(IXulAsyncConnectionService service) {
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

  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th));
    errorLabel.setValue(ExceptionParser.getErrorMessage(th));
    errorDialog.show();
  }

  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  public void onDialogAccept(IDatabaseConnection arg0) {
    service.convertToConnection(arg0, new XulServiceCallback<IConnection>() {
      public void error(String message, Throwable error) {
        displayErrorMessage(error);
      }
      public void success(IConnection retVal) {
        currentConnection = retVal;
        addConnection();
      }
    });
  }

  public void onDialogCancel() {
    // do nothing
  }

  public void onDialogReady() {
    // TODO: enable the database edit and add buttons
  }

  public void showAddConnectionDialog() {
    datasourceModel.getRelationalModel().setEditType(ConnectionEditType.ADD);
    databaseDialog.setDatabaseConnection(null);
    databaseDialog.show();
  }

  public void showEditConnectionDialog() {
    datasourceModel.getRelationalModel().setEditType(ConnectionEditType.EDIT);
    IConnection connection = datasourceModel.getRelationalModel().getSelectedConnection();
    service.convertFromConnection(connection, new XulServiceCallback<IDatabaseConnection>() {
      public void error(String message, Throwable error) {
        displayErrorMessage(error);
      }

      public void success(IDatabaseConnection conn) {
        databaseDialog.setDatabaseConnection(conn);
        databaseDialog.show();
      }
    });
  }

  public void showRemoveConnectionDialog() {
    // Display the warning message. 
    // If ok then remove the connection from the list
    removeConfirmationDialog.show();
  }
  
  public void closeRemoveConfirmationDialog() {
    removeConfirmationDialog.hide();
  }

}
