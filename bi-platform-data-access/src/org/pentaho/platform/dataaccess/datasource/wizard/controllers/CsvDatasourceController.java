package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.WaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.models.Aggregation;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorCallback;
import org.pentaho.ui.xul.util.TreeCellRenderer;

public class CsvDatasourceController extends AbstractXulEventHandler {
  public static final int MAX_SAMPLE_DATA_ROWS = 5;
  public static final int MAX_COL_SIZE = 12;
  public static final String EMPTY_STRING = "";
  public static final String COMMA = ",";
  private DatasourceMessages datasourceMessages;
  private WaitingDialog waitingDialogBox;
  private DatasourceService service;
  private XulDialog regenerateModelConfirmationDialog = null;
  private XulDialog waitingDialog = null;
  private XulLabel waitingDialogLabel = null;
  private DatasourceModel datasourceModel;
  private BindingFactory bf;
  private XulTextbox datasourceName = null;
  private XulDialog errorDialog;
  private XulDialog successDialog;
  private XulLabel errorLabel = null;
  private XulLabel successLabel = null;
  private XulTree csvDataTable = null;
  private XulTextbox selectedFile = null;
  private XulCheckbox headersPresent = null;
  private XulTreeCol columnNameTreeCol = null;
  private XulTreeCol columnTypeTreeCol = null;
  //private XulTreeCol columnFormatTreeCol = null;
  private XulDialog aggregationEditorDialog = null;
  private XulDialog sampleDataDialog = null;
  private XulTree sampleDataTree = null;
  private CustomAggregateCellEditor aggregationCellEditor = null;
  private CustomSampleDataCellEditor sampleDataCellEditor = null;
  private CustomAggregationCellRenderer aggregationCellRenderer = null;
  private XulDialog applyCsvConfirmationDialog = null;
  private XulVbox csvAggregationEditorVbox = null;
  private CustomSampleDataCellRenderer sampleDataCellRenderer = null;
  private XulMenuList delimiterList = null;
  private XulMenuList enclosureList = null;
  private XulFileUpload fileUpload = null;
  private XulButton applyCsvButton = null;

  public CsvDatasourceController() {

  }

  public void init() {
    fileUpload = (XulFileUpload) document.getElementById("fileUpload"); //$NON-NLS-1$
    applyCsvButton = (XulButton) document.getElementById("applyCsvButton"); //$NON-NLS-1$
    csvAggregationEditorVbox = (XulVbox) document.getElementById("csvAggregationEditorVbox"); //$NON-NLS-1$
    applyCsvConfirmationDialog = (XulDialog) document.getElementById("applyCsvConfirmationDialog"); //$NON-NLS-1$
    csvDataTable = (XulTree) document.getElementById("csvDataTable");
    sampleDataTree = (XulTree) document.getElementById("csvSampleDataTable");
    aggregationEditorDialog = (XulDialog) document.getElementById("csvAggregationEditorDialog");
    aggregationCellEditor = new CustomAggregateCellEditor(aggregationEditorDialog, datasourceMessages, document, bf);
    csvDataTable.registerCellEditor("aggregation-cell-editor", aggregationCellEditor);
    aggregationCellRenderer = new CustomAggregationCellRenderer();
    csvDataTable.registerCellRenderer("aggregation-cell-editor", aggregationCellRenderer);
    sampleDataDialog = (XulDialog) document.getElementById("csvSampleDataDialog");
    sampleDataCellEditor = new CustomSampleDataCellEditor(sampleDataDialog);
    csvDataTable.registerCellEditor("sample-data-cell-editor", sampleDataCellEditor);
    sampleDataCellRenderer = new CustomSampleDataCellRenderer();
    csvDataTable.registerCellRenderer("sample-data-cell-editor", sampleDataCellRenderer);
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
    delimiterList = (XulMenuList) document.getElementById("delimiterList"); //$NON-NLS-1$
    enclosureList = (XulMenuList) document.getElementById("enclosureList"); //$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("csvDatasourceName"); //$NON-NLS-1$
    
    //columnFormatTreeCol = (XulTreeCol) document.getElementById("csvColumnFormatTreeCol"); //$NON-NLS-1$    
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    final Binding domainBinding = bf.createBinding(datasourceModel.getCsvModel(), "headersPresent", headersPresent, "checked"); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getCsvModel(), "dataRows", csvDataTable, "elements");
    bf.createBinding(datasourceModel.getCsvModel(), "delimiterList", delimiterList, "elements");
    bf.createBinding(datasourceModel.getCsvModel(), "enclosureList", enclosureList, "elements");
    bf.createBinding(datasourceModel.getCsvModel(), "selectedFile", fileUpload, "selectedFile");
    BindingConvertor<String, Boolean> buttonConverter = new BindingConvertor<String, Boolean>() {

      @Override
      public Boolean sourceToTarget(String value) {
        return (value != null && value.length() > 0);
      }

      @Override
      public String targetToSource(Boolean value) {
        return null;
      }
    };
    
    bf.createBinding(datasourceModel, "datasourceName", applyCsvButton, "!disabled", buttonConverter);
    BindingConvertor<Integer, Enclosure> indexToEnclosureConverter = new BindingConvertor<Integer, Enclosure>() {

      @Override
      public Enclosure sourceToTarget(Integer value) {
        if(value == 0) {
          return Enclosure.NONE;
        } else if(value == 1) {
          return Enclosure.SINGLEQUOTE;
        } else if(value == 2) {
          return Enclosure.DOUBLEQUOTE;
        }
        return Enclosure.NONE;
      }

      @Override
      public Integer targetToSource(Enclosure value) {
        if(value == Enclosure.NONE) {
          return 0;
        } else if(value == Enclosure.SINGLEQUOTE) {
          return 1;
        } else if(value == Enclosure.DOUBLEQUOTE) {
          return 2;
        }
        return 0;
      }
    };

    BindingConvertor<Integer, Delimiter> indexToDelimiterConverter = new BindingConvertor<Integer, Delimiter>() {

      @Override
      public Delimiter sourceToTarget(Integer value) {
        if(value == 0) {
          return Delimiter.NONE;
        } else if(value == 1) {
          return Delimiter.COMMA;
        } else if(value == 2) {
          return Delimiter.TAB;
        } else if(value == 3) {
          return Delimiter.SEMICOLON;
        } else if(value == 4) {
          return Delimiter.SPACE;
        }
        return Delimiter.NONE;
      }

      @Override
      public Integer targetToSource(Delimiter value) {
        if(value == Delimiter.NONE) {
          return 0;
        } else if(value == Delimiter.COMMA) {
          return 1;
        } else if(value == Delimiter.TAB) {
          return 2;
        } else if(value == Delimiter.SEMICOLON) {
          return 3;
        } else if(value == Delimiter.SPACE) {
          return 4;
        }
        return 0;
      }
    };    
    bf.createBinding(enclosureList, "selectedIndex", datasourceModel.getCsvModel(), "enclosure", indexToEnclosureConverter);
    bf.createBinding(delimiterList, "selectedIndex", datasourceModel.getCsvModel(), "delimiter", indexToDelimiterConverter);
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(csvDataTable, "selectedIndex", this, "selectedCsvDataRow");
    try {
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    datasourceModel.getCsvModel().setHeadersPresent(true);
    datasourceModel.getCsvModel().setEnclosureList();
    datasourceModel.getCsvModel().setDelimiterList();
    datasourceModel.getCsvModel().setDelimiter(Delimiter.COMMA);
    datasourceModel.getCsvModel().setEnclosure(Enclosure.DOUBLEQUOTE);
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

  public void submitCsv() {
    fileUpload.submit();
  }
  
  public void applyCsv() {
    if(datasourceModel.getCsvModel().getBusinessData() != null) {
      applyCsvConfirmationDialog.show();
    } else {
      generateModel();
    }    
  }
  
  public void closeApplyCsvConfirmationDialog() {
    applyCsvConfirmationDialog.hide();
  }

  
  public void generateModel() {
    if (validateIputForCsv()) {
      if(applyCsvConfirmationDialog.isVisible()) {
        applyCsvConfirmationDialog.hide();
      }
      try {
        showWaitingDialog(datasourceMessages.getString("DatasourceController.GENERATE_MODEL"), datasourceMessages.getString("DatasourceController.WAIT"));
        service.generateInlineEtlModel(datasourceModel.getDatasourceName(), datasourceModel.getCsvModel()
            .getSelectedFile(), datasourceModel.getCsvModel().isHeadersPresent(), datasourceModel.getCsvModel().getEnclosure().getValue(), datasourceModel.getCsvModel().getDelimiter().getValue(),
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
      openErrorDialog(datasourceMessages.getString("DatasourceController.ERROR_0001_MISSING_INPUTS"), getMissingInputs());
    }
  }

  private boolean validateIputForCsv() {
    return (datasourceModel.getCsvModel().getSelectedFile() != null && (datasourceModel.getDatasourceName() != null && datasourceModel
        .getDatasourceName().length() > 0) && (datasourceModel.getCsvModel().getDelimiter()
            != Delimiter.NONE) && (datasourceModel.getCsvModel().getEnclosure() != Enclosure.NONE));
  }

  private String getMissingInputs() {
    StringBuffer buffer = new StringBuffer();
    if(datasourceModel.getCsvModel().getSelectedFile() == null
        && datasourceModel.getCsvModel().getSelectedFile().length() <= 0) {
      buffer.append(datasourceMessages.getString("datasourceDialog.File"));
      buffer.append(" \n");
    }
    if(datasourceModel.getDatasourceName() == null || datasourceModel.getDatasourceName().length() <=0) {
      buffer.append(datasourceMessages.getString("datasourceDialog.Name"));
      buffer.append(" \n");
    }
    if(datasourceModel.getCsvModel().getDelimiter() == null  || datasourceModel.getCsvModel().getDelimiter() == Delimiter.NONE) {
      buffer.append(datasourceMessages.getString("datasourceDialog.Delimiter"));
      buffer.append(" \n");
    }
    if(datasourceModel.getCsvModel().getEnclosure() == null  || datasourceModel.getCsvModel().getEnclosure() == Enclosure.NONE) {
      buffer.append(datasourceMessages.getString("datasourceDialog.Enclosure"));
      buffer.append(" \n");
    }
    return buffer.toString();
  }
  public void uploadSuccess(String results){
    datasourceModel.getCsvModel().setSelectedFile(results);
    applyCsv();
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

 /* public void showWaitingDialog(String title, String message) {
    getWaitingDialog().setTitle(title);
    getWaitingDialog().setMessage(message);
    getWaitingDialog().show();
  }

  public void hideWaitingDialog() {
    getWaitingDialog().hide();
  }
*/
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
   * @return the waitingDialog
   */
  public WaitingDialog getWaitingDialog() {
    return this.waitingDialogBox;
  }

  /**
   * @param waitingDialog the waitingDialog to set
   */
  public void setWaitingDialog(WaitingDialog waitingDialog) {
    this.waitingDialogBox = waitingDialog;
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

  private class CustomSampleDataCellEditor implements TreeCellEditor {
    XulDialog dialog = null;
    TreeCellEditorCallback callback = null;

    public CustomSampleDataCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
      dialog.setBgcolor("#FFFFFF");
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

    public void show(int row, int col, Object boundObj, String columnBinding,TreeCellEditorCallback callback) {
      this.callback = callback;
      CsvModelDataRow csvModelDataRow = (CsvModelDataRow)boundObj;
      XulTreeCol  column = sampleDataTree.getColumns().getColumn(0);
      column.setLabel(csvModelDataRow.getColumnName());
      List<String> values = csvModelDataRow.getSampleDataList();
      List<String> sampleDataList = new ArrayList<String>();
      for(int i=1;i<MAX_SAMPLE_DATA_ROWS && i<csvModelDataRow.getSampleDataList().size();i++) {
        sampleDataList.add(values.get(i));
      }
      sampleDataTree.setElements(sampleDataList);
      sampleDataTree.update();
      dialog.setTitle(datasourceMessages.getString("DatasourceController.SAMPLE_DATA"));      
      dialog.show();
    }
  }
  
  private class CustomAggregationCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      StringBuffer buffer = new StringBuffer();
      if(value instanceof Aggregation) {
        Aggregation aggregation = (Aggregation) value;
        List<AggregationType> aggregationList = aggregation.getAggregationList();
        for(int i=0;i<aggregationList.size();i++) {
        if(buffer.length() + datasourceMessages.getString(aggregationList.get(i).getDescription()).length() < MAX_COL_SIZE) {
          buffer.append(datasourceMessages.getString(aggregationList.get(i).getDescription()));
          if((i<aggregationList.size()-1 && (buffer.length() + datasourceMessages.getString(aggregationList.get(i+1).getDescription()).length() + COMMA.length() < MAX_COL_SIZE))) {
            buffer.append(COMMA);  
          }
        } else {
            break;
          }
        }
      }
      return buffer.toString();
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }
  }
  
  private class CustomSampleDataCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      if(value instanceof String) {
        return getSampleData((String) value);
      } else if (value instanceof Vector){
        Vector<String> vectorValue = (Vector<String>) value;
        StringBuffer sampleDataBuffer = new StringBuffer();
        for(int i=0;i<vectorValue.size();i++) {
          sampleDataBuffer.append(vectorValue.get(i));
        }
        return getSampleData(sampleDataBuffer.toString());
      }
      return EMPTY_STRING;
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }
    
    private String getSampleData(String sampleData) {
      if(sampleData != null && sampleData.length() > 0) {
        if(sampleData.length() <= MAX_COL_SIZE) {
          return sampleData;
        } else {
          return sampleData.substring(0, MAX_COL_SIZE); 
        }
      }
      return EMPTY_STRING;
    }
  }
}


