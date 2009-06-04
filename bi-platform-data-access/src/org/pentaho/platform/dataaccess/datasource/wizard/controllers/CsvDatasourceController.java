package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorListener;

public class CsvDatasourceController extends AbstractXulEventHandler {
  private DatasourceMessages datasourceMessages;
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

  XulDialog aggregationEditorDialog = null;
  XulDialog sampleDataDialog = null;
  CustomAggregateCellEditor aggregationCellEditor = null;
  CustomSampleDataCellEditor sampleDataCellEditor = null;
  public CsvDatasourceController() {

  }

  public void init() {
    csvDataTable = (XulTree) document.getElementById("csvDataTable");
    aggregationEditorDialog = (XulDialog) document.getElementById("csvAggregationEditorDialog");
    aggregationCellEditor = new CustomAggregateCellEditor(aggregationEditorDialog);
    csvDataTable.registerCellEditor("aggregation-cell-editor", aggregationCellEditor);
    sampleDataDialog = (XulDialog) document.getElementById("csvSampleDataDialog");
    sampleDataCellEditor = new CustomSampleDataCellEditor(sampleDataDialog);
    csvDataTable.registerCellEditor("sample-data-cell-editor", aggregationCellEditor);
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
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(csvDataTable, "selectedIndex", this, "selectedCsvDataRow");
    try {
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
  
  public void setSelectedCsvDataRow(int row){
    
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
        showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_MODEL"), datasourceMessages.getString("DatasourceController.WAIT"));
        service.generateInlineEtlModel(datasourceModel.getDatasourceName(), datasourceModel.getCsvModel()
            // TODO Binding for the check box is not working. Need to investigate
            .getSelectedFile(), /*datasourceModel.getCsvModel().isHeadersPresent()*/ headersPresent.isChecked(), "\"", ",",
            new XulServiceCallback<BusinessData>() {

              public void error(String message, Throwable error) {
                hideWaitingDialog();
                displayErrorMessage(error);
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
        displayErrorMessage(e);
      }
    } else {
      openErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString("DatasourceController.ERROR_0001_MISSING_INPUTS"));
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
  
  public void closeAggregationEditorDialog() {
   aggregationCellEditor.hide(); 
  }
  
  public void saveAggregationValues() {
    aggregationCellEditor.notifyListeners();
  }

  public void closeSampleDataDialog() {
    sampleDataCellEditor.hide(); 
  }

  private class CustomAggregateCellEditor implements TreeCellEditor {
    XulDialog dialog = null;
    TreeCellEditorListener listener = null;

    public CustomAggregateCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
    }

    public void addTreeCellEditorListener(TreeCellEditorListener listener) {
      this.listener = listener;
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {
      // Create the list of check box in XulDialog
      ArrayList<AggregationType> aggregationList = (ArrayList<AggregationType>) val;
      AggregationType[] aggregationTypeArray = AggregationType.values();
      for(int i=0;i<aggregationTypeArray.length;i++) {
        XulCheckbox aggregationCheckBox;
        try {
          aggregationCheckBox = (XulCheckbox) document.createElement("checkbox");
          aggregationCheckBox.setLabel(aggregationTypeArray[i].name());
          if(aggregationList.contains(aggregationTypeArray[i])) {
            aggregationCheckBox.setChecked(true);
          } else {
            aggregationCheckBox.setChecked(false);
          }
          dialog.addChild(aggregationCheckBox);
        } catch (XulException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    public void show(int row, int col, Object boundObj, String columnBinding) {
      dialog.show();
    }
    
    public void notifyListeners() {
      hide();  
      // Construct a new array list of aggregation based on what user selected
      // pass it to listener
      ArrayList<AggregationType> aggregationTypeList = new ArrayList<AggregationType>(); 
      for(XulComponent component: dialog.getChildNodes()) {
        XulCheckbox checkbox = (XulCheckbox) component;
        aggregationTypeList.add(AggregationType.valueOf(checkbox.getLabel()));
      }
      this.listener.onCellEditorClosed(aggregationTypeList);
    }
  }
  
  
  private class CustomSampleDataCellEditor implements TreeCellEditor {
    XulDialog dialog = null;
    TreeCellEditorListener listener = null;

    public CustomSampleDataCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
    }

    public void addTreeCellEditorListener(TreeCellEditorListener listener) {
      this.listener = listener;
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {

    }

    public void show(int row, int col, Object boundObj, String columnBinding) {
      XulTree sampleDataTree = null;
      XulTreeCols treeCols = null;
      XulTreeCol treeCol = null;
      try {
        sampleDataTree = (XulTree) document.createElement("tree");
        treeCols = (XulTreeCols) document.createElement("treecols");
        treeCol = (XulTreeCol) document.createElement("treecol");
        treeCol.setLabel("Sample Data");
        treeCol.setFlex(1);
        treeCols.addColumn(treeCol);
        sampleDataTree.addChild(treeCols);
        CsvModelDataRow csvModelDataRow = (CsvModelDataRow)boundObj;
        for(String sampleData : csvModelDataRow.getSampleDataList()) {
          XulTreeRow treeRow = (XulTreeRow) document.createElement("treerow");
          XulTreeCell treeCell = (XulTreeCell) document.createElement("treecell");
          treeCell.setLabel(sampleData);
          treeRow.addCell(treeCell);
          sampleDataTree.addTreeRow(treeRow);
        }
        sampleDataTree.update();
        dialog.addChild(sampleDataTree);
      } catch (XulException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      dialog.show();
    }
  }
}
