package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource.EditType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulColumns;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulGrid;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulRows;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeChildren;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class DatasourceController extends AbstractXulEventHandler {
  private XulDialog connectionDialog;

  private XulDialog datasourceDialog;

  private XulDialog removeConfirmationDialog;

  private XulDialog waitingDialog = null;
  
  private XulLabel waitingDialogLabel = null;
  
  private XulDialog previewResultsDialog = null;

  private DatasourceService service;

  public static final int CONNECTION_DECK = 0;

  public static final int MODELLING_DECK = 1;

  private List<DatasourceDialogListener> listeners = new ArrayList<DatasourceDialogListener>();

  private DatasourceModel datasourceModel;

  private ConnectionModel connectionModel;

  BindingFactory bf;

  XulTree previewResultsTable = null;

  XulTextbox connectionname = null;

  XulTextbox driverClass = null;

  XulTextbox username = null;

  XulTextbox password = null;

  XulTextbox url = null;

  XulTextbox datasourceName = null;

  XulListbox connections = null;

  XulTextbox query = null;

  XulCheckbox mqlModelCheckBox = null;

  XulTreeCols previewResultsTreeCols = null;

  XulTextbox previewLimit = null;

  XulButton editConnectionButton = null;

  XulButton removeConnectionButton = null;

  XulButton editQueryButton = null;

  XulButton okButton = null;

  XulButton cancelButton = null;

  XulButton previewButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulColumns columns = null;

  private XulRows rows = null;

  private XulGrid grid = null;

  private XulTree modelDataTable = null;
  private XulTreeChildren  modelDataRows = null;
  
  private XulHbox buttonBox = null;  
  public DatasourceController() {

  }

  public void init() {
    modelDataTable = (XulTree) document.getElementById("modelDataTable");
    modelDataRows = (XulTreeChildren) document.getElementById("modelDataRows");
    buttonBox = (XulHbox) document.getElementById("buttonBox");
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$
    
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$

    datasourceName = (XulTextbox) document.getElementById("datasourcename"); //$NON-NLS-1$
    connections = (XulListbox) document.getElementById("connectionList"); //$NON-NLS-1$
    query = (XulTextbox) document.getElementById("query"); //$NON-NLS-1$
    connectionDialog = (XulDialog) document.getElementById("connectionDialog");//$NON-NLS-1$
    datasourceDialog = (XulDialog) document.getElementById("datasourceDialog");//$NON-NLS-1$
    previewResultsDialog = (XulDialog) document.getElementById("previewResultsDialog");//$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById("removeConfirmationDialog");//$NON-NLS-1$
    previewResultsTable = (XulTree) document.getElementById("previewResultsTable"); //$NON-NLS-1$
    previewResultsTreeCols = (XulTreeCols) document.getElementById("previewResultsTreeCols"); //$NON-NLS-1$
    previewLimit = (XulTextbox) document.getElementById("previewLimit"); //$NON-NLS-1$

    editConnectionButton = (XulButton) document.getElementById("editConnection"); //$NON-NLS-1$
    removeConnectionButton = (XulButton) document.getElementById("removeConnection"); //$NON-NLS-1$

    editQueryButton = (XulButton) document.getElementById("editQuery"); //$NON-NLS-1$

    okButton = (XulButton) document.getElementById("datasourceDialog_accept"); //$NON-NLS-1$
    cancelButton = (XulButton) document.getElementById("datasourceDialog_cancel"); //$NON-NLS-1$
    previewButton = (XulButton) document.getElementById("preview"); //$NON-NLS-1$

    mqlModelCheckBox = (XulCheckbox) document.getElementById("metadataModelCheckbox"); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(datasourceModel, "validated", previewButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "validated", mqlModelCheckBox, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "validated", okButton, "!disabled");//$NON-NLS-1$ //$NON-NLS-2$
    BindingConvertor<IConnection, Boolean> buttonConvertor = new BindingConvertor<IConnection, Boolean>() {

      @Override
      public Boolean sourceToTarget(IConnection value) {
        return !(value == null);
      }

      @Override
      public IConnection targetToSource(Boolean value) {
        return null;
      }

    };

    bf.setBindingType(Binding.Type.ONE_WAY);
    final Binding domainBinding = bf.createBinding(datasourceModel, "connections", connections, "elements"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "selectedConnection", editConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ 
    bf.createBinding(datasourceModel, "selectedConnection", removeConnectionButton, "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel,
        "selectedConnection", connections, "selectedIndex", new BindingConvertor<IConnection, Integer>() { //$NON-NLS-1$ //$NON-NLS-2$

          @Override
          public Integer sourceToTarget(IConnection connection) {
            if (connection != null) {
              return datasourceModel.getConnectionIndex(connection);
            } else {
              return -1;
            }

          }

          @Override
          public IConnection targetToSource(Integer value) {
            if (value >= 0) {
              return datasourceModel.getConnections().get(value);
            } else {
              return null;
            }

          }

        });
    bf.createBinding(datasourceModel, "dataRows", modelDataTable, "elements");
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel, "generateModelChecked", mqlModelCheckBox, "checked");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "generateModelChecked", mqlModelCheckBox, "selected");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "modelTableVisible", modelDataTable, "visible");//$NON-NLS-1$ //$NON-NLS-2$     
    bf.createBinding(datasourceModel, "previewLimit", previewLimit, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    // Not sure if editQuery button is doing much
    //bf.createBinding(editQueryButton, "!disabled", "removeConnectionButton", "!disabled", buttonConvertor); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    bf.createBinding(datasourceModel, "query", query, "value"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "datasourceName", datasourceName, "value"); //$NON-NLS-1$ //$NON-NLS-2$

    okButton.setDisabled(true);
    modelDataTable.setVisible(false);
    // Setting the Button Panel background to white
    buttonBox.setBgcolor("#FFFFFF");
    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  public void showDatasourceDialog() {
    datasourceDialog.show();
  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public void setConnectionModel(ConnectionModel model) {
    this.connectionModel = model;
  }

  public ConnectionModel getConnectionModel() {

    return this.connectionModel;
  }

  public String getName() {
    return "datasourceController";
  }

  public void closeDatasourceDialog() {
    this.datasourceDialog.hide();
    for (DatasourceDialogListener listener : listeners) {
      listener.onDialogCancel();
    }
  }


  public void generateModel() {
    if (mqlModelCheckBox.isChecked()) {
      datasourceDialog.setHeight(595);
      query.setDisabled(true);
      if (validateInputs()) {
        try {
          showWaitingDialog("Generating Metadata Model", "Please wait ....");
          service.generateModel(datasourceModel.getDatasourceName(), datasourceModel.getSelectedConnection(),
              datasourceModel.getQuery(), datasourceModel.getPreviewLimit(), new XulServiceCallback<BusinessData>() {

                public void error(String message, Throwable error) {
                  hideWaitingDialog();
                  mqlModelCheckBox.setChecked(false);
                  query.setDisabled(false);
                  datasourceDialog.setHeight(300);
                  openErrorDialog("Error occurred", "Unable to retrieve business data. " + error.getLocalizedMessage());
                }

                public void success(BusinessData businessData) {
                  try {
                    hideWaitingDialog();
                    datasourceModel.setBusinessData(businessData);
                    modelDataTable.setVisible(true);                    
                  } catch (Exception xe) {
                    xe.printStackTrace();
                  }
                }
              });
        } catch (DatasourceServiceException e) {
          hideWaitingDialog();
          mqlModelCheckBox.setChecked(false);
          query.setDisabled(false);
          datasourceDialog.setHeight(300);
          openErrorDialog("Error occurred", "Unable to retrieve business data. " + e.getLocalizedMessage());
        }
      } else {
				mqlModelCheckBox.setChecked(false);
				query.setDisabled(false);
				datasourceDialog.setHeight(300);
        openErrorDialog("Missing Input", "Some of the required inputs are missing");
      }
    } else {
      // If the user un checks the modeling check box we will clear out the business data 
      modelDataTable.setVisible(false); 
      query.setDisabled(false);
      datasourceDialog.setHeight(300);
      datasourceModel.setBusinessData(null);
    }
  }

  private boolean validateInputs() {
    return (datasourceModel.getSelectedConnection() != null
        && (datasourceModel.getQuery() != null && datasourceModel.getQuery().length() > 0) && (datasourceModel
        .getDatasourceName() != null && datasourceModel.getDatasourceName().length() > 0));
  }

  public void exitDatasourceDialog() {
    this.datasourceDialog.hide();
  }

  public void saveModel() {
    List<ModelDataRow> dataRows = datasourceModel.getDataRows();
    if (dataRows != null && dataRows.size() > 0) {
      // User has decided to choose data modeling process and have customized the mode. So we will save the customized model
      try {
      // Get the domain from the business data
      BusinessData businessData = datasourceModel.getBusinessData();
      Domain domain = businessData.getDomain();
      List<LogicalModel> logicalModels = domain.getLogicalModels();
      for (LogicalModel logicalModel : logicalModels) {
        List<Category> categories = logicalModel.getCategories();
        for (Category category : categories) {
          List<LogicalColumn> logicalColumns = category.getLogicalColumns();
          int i = 0;
          for (LogicalColumn logicalColumn : logicalColumns) {
            ModelDataRow row = dataRows.get(i++);
            logicalColumn.setDataType(row.getSelectedDataType());
            logicalColumn.setName(new LocalizedString(domain.getLocales().get(0).getCode(), row.getColumnName()));
          }
        }
      }
      saveModel(businessData, false);
      } catch (Exception xe) {
        openErrorDialog("Error occurred", "Unable to save model. " + datasourceModel.getDatasourceName()
            + xe.getLocalizedMessage());
      }
    } else {
      // User has decided to skip the data modeling process. So we will generate the default model and save it
      if (validateInputs()) {
        try {

          service.saveModel(datasourceModel.getDatasourceName(), datasourceModel.getSelectedConnection(),
              datasourceModel.getQuery(), false, datasourceModel.getPreviewLimit(), new XulServiceCallback<BusinessData>() {

                public void error(String message, Throwable error) {
                  openErrorDialog("Error occurred", "Unable to save model. " + datasourceModel.getDatasourceName()
                      + error.getLocalizedMessage());
                }

                public void success(BusinessData businessData) {
                  datasourceDialog.hide();
                  datasourceModel.setBusinessData(businessData);
                  for (DatasourceDialogListener listener : listeners) {
                    listener.onDialogFinish(datasourceModel.getDatasource());
                  }                  
                }
              });
        } catch (DatasourceServiceException e) {
          openErrorDialog("Error occurred", "Unable to save model. " + datasourceModel.getDatasourceName()
              + e.getLocalizedMessage());
        }
      } else {
        openErrorDialog("Missing Input", "Some of the required inputs are missing");
      }
    }
  }

  private void saveModel(BusinessData businessData, boolean overwrite) {
    try {
      // TODO setting value to false to always create a new one. Save as is not yet implemented
      service.saveModel(businessData, overwrite, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          openErrorDialog("Error occurred", "Unable to save model: " + datasourceModel.getDatasourceName()
              + error.getLocalizedMessage());
        }

        public void success(Boolean value) {
           datasourceDialog.hide();
          for (DatasourceDialogListener listener : listeners) {
            listener.onDialogFinish(datasourceModel.getDatasource());
          }
        }
      });
    } catch (DatasourceServiceException e) {
      openErrorDialog("Error occurred", "Unable to save model: " + datasourceModel.getDatasourceName()
          + e.getLocalizedMessage());
    }
  }

  public void editQuery() {

  }

  public void addConnection() {
    datasourceModel.setEditType(EditType.ADD);
    connectionModel.clearModel();
    connectionModel.setDisableConnectionName(false);
    showConnectionDialog();
  }

  public void editConnection() {
    datasourceModel.setEditType(EditType.EDIT);
    connectionModel.setDisableConnectionName(true);
    connectionModel.setConnection(datasourceModel.getSelectedConnection());
    showConnectionDialog();
  }

  public void removeConnection() {
    // Display the warning message. If ok then remove the connection from the list
    int index = connections.getSelectedIndex();
    removeConfirmationDialog.show();
  }

  public void selectSql() {

  }

  public void selectOlap() {

  }

  public void selectCsv() {

  }

  public void selectMql() {

  }

  public void selectXml() {

  }

  public void showConnectionDialog() {
    connectionDialog.show();
  }

  public void closeConnectionDialog() {
    connectionDialog.hide();
  }

  public void closeRemoveConfirmationDialog() {
    removeConfirmationDialog.hide();
  }

  public void displayPreview() {

    if (!validateInputs()) {
      openErrorDialog("Missing Input", "Some of the required inputs are missing"); //$NON-NLS-2$
    } else {
      try {
        showWaitingDialog("Generating Preview Data", "Please wait ....");

        service.doPreview(datasourceModel.getSelectedConnection(), datasourceModel.getQuery(), datasourceModel
            .getPreviewLimit(), new XulServiceCallback<SerializedResultSet>() {

          public void error(String message, Throwable error) {
            hideWaitingDialog();
            openErrorDialog("Preview Failed", "Unable to preview data: " + error.getLocalizedMessage()); //$NON-NLS-1$ //$NON-NLS-2$ 
          }

          public void success(SerializedResultSet rs) {
            String[][] data = rs.getData();
            String[] columns = rs.getColumns();
            int columnCount = columns.length;
            // Remove any existing children
            List<XulComponent> previewResultsList = previewResultsTable.getChildNodes();

            for (int i = 0; i < previewResultsList.size(); i++) {
              previewResultsTable.removeChild(previewResultsList.get(i));
            }
            XulTreeChildren treeChildren = previewResultsTable.getRootChildren();
            List<XulComponent> treeChildrenList = treeChildren.getChildNodes();
            for(int i=0;i<treeChildrenList.size();i++) {
              treeChildren.removeItem(i);
            }
            List<XulComponent> componentList = modelDataRows.getChildNodes();
            for(int i=0;i<componentList.size();i++) {
              modelDataRows.removeAll();
            }
            // Remove all the existing columns
            int curTreeColCount = previewResultsTable.getColumns().getColumnCount();
            List<XulComponent> cols = previewResultsTable.getColumns().getChildNodes();
            for (int i = 0; i < curTreeColCount; i++) {
              previewResultsTable.getColumns().removeChild(cols.get(i));
            }
            previewResultsTable.update();
            // Recreate the colums
            XulTreeCols treeCols = previewResultsTable.getColumns();
            if (treeCols == null) {
              try {
                treeCols = (XulTreeCols) document.createElement("treecols");
              } catch (XulException e) {

              }
            }
            // Setting column data
            for (int i = 0; i < columnCount; i++) {
              try {
                XulTreeCol treeCol = (XulTreeCol) document.createElement("treecol");
                treeCol.setLabel(columns[i]);
                treeCol.setFlex(1);
                treeCols.addColumn(treeCol);
              } catch (XulException e) {

              }
            }

            XulTreeCols treeCols1 = previewResultsTable.getColumns();
            int count = previewResultsTable.getColumns().getColumnCount();
            // Create the tree children and setting the data
            try {
              for (int i = 0; i < data.length; i++) {
                XulTreeRow row = (XulTreeRow) document.createElement("treerow");

                for (int j = 0; j < columnCount; j++) {
                  XulTreeCell cell = (XulTreeCell) document.createElement("treecell");
                  cell.setLabel(data[i][j]);
                  row.addCell(cell);
                }

                previewResultsTable.addTreeRow(row);
              }
              previewResultsTable.update();
              hideWaitingDialog();
              previewResultsDialog.show();
            } catch (XulException e) {
              // TODO: add logging
              hideWaitingDialog();
              System.out.println(e.getMessage());
              e.printStackTrace();
            }
          }
        });
      } catch (DatasourceServiceException e) {
        hideWaitingDialog();
        openErrorDialog("Preview Failed", "Unable to preview data: " + e.getLocalizedMessage());
      }
    }
  }

  public void closePreviewResultsDialog() {
    previewResultsDialog.hide();
  }

  public DatasourceService getService() {
    return service;
  }

  public void setService(DatasourceService service) {
    this.service = service;
  }

  public void addDatasourceDialogListener(DatasourceDialogListener listener) {
    if (listeners.contains(listener) == false) {
      listeners.add(listener);
    }
  }

  public void removeDatasourceDialogListener(DatasourceDialogListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
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
  
  public void showWaitingDialog(String title, String message) {
    waitingDialog.setTitle(title);
    waitingDialogLabel.setValue(message);
    waitingDialog.show();
    
  }
  
  public void hideWaitingDialog() {
    waitingDialog.hide();
  }
}
