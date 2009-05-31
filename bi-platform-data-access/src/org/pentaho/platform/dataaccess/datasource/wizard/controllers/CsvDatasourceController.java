package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class CsvDatasourceController extends AbstractXulEventHandler {
  private DatasourceService service;
  private XulDialog regenerateModelConfirmationDialog = null;
  private XulDialog waitingDialog = null;

  private XulLabel waitingDialogLabel = null;

  private DatasourceModel datasourceModel;

  BindingFactory bf;
  
  XulTextbox datasourceName = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulTree csvDataTable = null;

  XulTextbox selectedFile = null;

  XulCheckbox headersPresent = null;
  private XulTreeCol columnNameTreeCol = null;
  private XulTreeCol columnTypeTreeCol = null;
  //private XulTreeCol columnFormatTreeCol = null;


  public CsvDatasourceController() {

  }

  public void init() {
    csvDataTable = (XulTree) document.getElementById("csvDataTable");
    regenerateModelConfirmationDialog = (XulDialog) document.getElementById("regenerateModelConfirmationDialog"); //$NON-NLS-1$
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$    
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$    
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    headersPresent = (XulCheckbox) document.getElementById("headersPresent"); //$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("datasourcename"); //$NON-NLS-1$
    selectedFile = (XulTextbox) document.getElementById("selectedFile"); //$NON-NLS-1$
    columnNameTreeCol = (XulTreeCol) document.getElementById("csvColumnNameTreeCol"); //$NON-NLS-1$
    columnTypeTreeCol = (XulTreeCol) document.getElementById("csvColumnTypeTreeCol"); //$NON-NLS-1$
    //columnFormatTreeCol = (XulTreeCol) document.getElementById("csvColumnFormatTreeCol"); //$NON-NLS-1$    
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    final Binding domainBinding = bf.createBinding(datasourceModel.getCsvModel(), "headersPresent", headersPresent, "checked"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getCsvModel(), "dataRows", csvDataTable, "elements");
    try {
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
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

  public String getName() {
    return "csvDatasourceController";
  }

  public void setService(DatasourceService service) {
    this.service = service;
  }

  private void generateModel() {
    if (validateIputForCsv()) {
      try {
        showWaitingDialog("Generating Metadata Model", "Please wait ....");
        service.generateInlineEtlModel(datasourceModel.getDatasourceName(), datasourceModel.getCsvModel()
            // TODO Binding for the check box is not working. Need to investigate
            .getSelectedFile(), /*datasourceModel.getCsvModel().isHeadersPresent()*/ headersPresent.isChecked(), "\"", ",",
            new XulServiceCallback<BusinessData>() {

              public void error(String message, Throwable error) {
                hideWaitingDialog();
                openErrorDialog("Error occurred", "Unable to generate the model. " + error.getLocalizedMessage());
              }

              public void success(BusinessData businessData) {
                try {
                  hideWaitingDialog();
                  // Clear out the model for data
                  datasourceModel.getCsvModel().setBusinessData(null);
                  // Setting the editable property to true so that the table can be populated with correct cell types
                  columnNameTreeCol.setEditable(true);
                  columnTypeTreeCol.setEditable(true);
                  //columnFormatTreeCol.setEditable(true); 
                  csvDataTable.update();
                  datasourceModel.getCsvModel().setBusinessData(businessData);
                } catch (Exception xe) {
                  xe.printStackTrace();
                }
              }
            });
      } catch (DatasourceServiceException e) {
        hideWaitingDialog();
        openErrorDialog("Error occurred", "Unable to retrieve business data. " + e.getLocalizedMessage());
      }
    } else {
      openErrorDialog("Missing Input", "Some of the required inputs are missing");
    }
  }

  private boolean validateIputForCsv() {
    return (datasourceModel.getCsvModel().getSelectedFile() != null && (datasourceModel.getDatasourceName() != null && datasourceModel
        .getDatasourceName().length() > 0));
  }

  public void uploadSuccess(String results){
    datasourceModel.getCsvModel().setSelectedFile(results);
    generateModel();    
  }
  
  public void uploadFailure(Throwable t){ 
    openErrorDialog("Upload Failed", t.getLocalizedMessage());
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

  public void closeRegenerateModelConfirmationDialog() {
    regenerateModelConfirmationDialog.hide();
  }
}
