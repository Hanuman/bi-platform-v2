package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ConnectionModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

public class DatasourceController extends AbstractXulDialogController<IDatasource> {
  public static final int DEFAULT_RELATIONAL_TABLE_ROW_COUNT = 8;
  public static final int DEFAULT_CSV_TABLE_ROW_COUNT = 10;

  private XulDialog datasourceDialog;

  private XulDialog waitingDialog = null;

  private XulLabel waitingDialogLabel = null;

  private DatasourceService service;

  public static final int RELATIONAL_DECK = 0;

  public static final int CSV_DECK = 1;

  private DatasourceModel datasourceModel;

  private ConnectionModel connectionModel;

  BindingFactory bf;

  XulTextbox datasourceName = null;

  XulButton okButton = null;

  XulButton cancelButton = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulHbox buttonBox = null;

  private XulDeck datasourceDeck = null;
  
  /**
   * The datasource being edited.
   */
  private IDatasource datasource;

  XulButton relationalButton = null;
  
  XulButton csvButton = null;
  
  private XulTree modelDataTable = null;
  
  private XulTree csvDataTable = null;
  
  public DatasourceController() {

  }

  public void init() {
    datasourceDeck = (XulDeck) document.getElementById("datasourceDeck"); //$NON-NLS-1$
    csvDataTable = (XulTree) document.getElementById("csvDataTable");
    modelDataTable = (XulTree) document.getElementById("modelDataTable");
    buttonBox = (XulHbox) document.getElementById("buttonBox");
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$    
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$    
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$    
    datasourceName = (XulTextbox) document.getElementById("datasourcename"); //$NON-NLS-1$
    datasourceDialog = (XulDialog) document.getElementById("datasourceDialog");//$NON-NLS-1$
    okButton = (XulButton) document.getElementById("datasourceDialog_accept"); //$NON-NLS-1$
    cancelButton = (XulButton) document.getElementById("datasourceDialog_cancel"); //$NON-NLS-1$
    relationalButton = (XulButton) document.getElementById("relationalButton"); //$NON-NLS-1$
    csvButton = (XulButton) document.getElementById("csvButton"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.ONE_WAY);
    final Binding domainBinding = bf.createBinding(datasourceModel, "datasourceName", datasourceName, "value"); //$NON-NLS-1$ //$NON-NLS-2$
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

    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    BindingConvertor<DatasourceType, Integer> deckIndexConvertor = new BindingConvertor<DatasourceType, Integer>() {

      @Override
      public Integer sourceToTarget(DatasourceType value) {
        Integer returnValue = null;
        if (DatasourceType.SQL == value) {
          if(modelDataTable.getRows() == 0) {
            buildRelationalEmptyTable(); 
          }
          returnValue = 0;
        } else if (DatasourceType.CSV == value) {
          if(csvDataTable.getRows() == 0) {
            buildCsvEmptyTable(); 
          }
          returnValue = 1;
        } else if (DatasourceType.NONE == value) {
          return 0;
        }
        return returnValue;
      }

      @Override
      public DatasourceType targetToSource(Integer value) {
        DatasourceType type = null;
        if (value == 0) {
          type = DatasourceType.SQL;
          if(modelDataTable.getRows() == 0) {
            buildRelationalEmptyTable(); 
          }
        } else if (value == 1) {
          type = DatasourceType.CSV;
          if(csvDataTable.getRows() == 0) {
            buildCsvEmptyTable(); 
          }
        }
        return type;
      }
    };
    bf.createBinding(datasourceModel, "datasourceType", datasourceDeck, "selectedIndex", deckIndexConvertor);//$NON-NLS-1$ //$NON-NLS-2$
    bf.setBindingType(Binding.Type.ONE_WAY);
    BindingConvertor<DatasourceType, Boolean> relationalToggleButtonConvertor = new BindingConvertor<DatasourceType, Boolean>() {

        @Override
        public Boolean sourceToTarget(DatasourceType value) {
          if(DatasourceType.SQL == value) {
        	  return true;
          } else {
        	  return false;
          }
        }

        @Override
        public DatasourceType targetToSource(Boolean value) {
          return null;
        }
      };

      BindingConvertor<DatasourceType, Boolean> csvToggleButtonConvertor = new BindingConvertor<DatasourceType, Boolean>() {

          @Override
          public Boolean sourceToTarget(DatasourceType value) {
            if(DatasourceType.CSV == value) {
          	  return true;
            } else {
          	  return false;
            }
          }

          @Override
          public DatasourceType targetToSource(Boolean value) {
            return null;
          }
        };
    bf.createBinding(datasourceModel, "datasourceType", relationalButton, "disabled", relationalToggleButtonConvertor);//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel, "datasourceType", csvButton, "disabled", csvToggleButtonConvertor);//$NON-NLS-1$ //$NON-NLS-2$

    okButton.setDisabled(true);
    // Setting the Button Panel background to white
    buttonBox.setBgcolor("#FFFFFF");
    datasourceModel.setDatasourceType(DatasourceType.SQL);
    try {
      // Fires the population of the model listbox. This cascades down to the categories and columns. In essence, this
      // call initializes the entire UI.
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

  public void setConnectionModel(ConnectionModel model) {
    this.connectionModel = model;
  }

  public ConnectionModel getConnectionModel() {
    return this.connectionModel;
  }

  public String getName() {
    return "datasourceController"; //$NON-NLS-1$
  }

  public void saveModel() {
    try {
      if (datasourceModel.getDatasourceType() == DatasourceType.SQL) {
        saveRelationalModel();
      } else if (datasourceModel.getDatasourceType() == DatasourceType.CSV) {
        saveCsvModel();
      }
    } catch (Exception e) {
      handleSaveError(datasourceModel, e);
    }
  }
  
  private void handleSaveError(DatasourceModel datasourceModel, Throwable xe) {
    openErrorDialog("Error occurred", "Unable to save model. " + datasourceModel.getDatasourceName()
        + xe.getLocalizedMessage());
    
  }

  private void saveCsvModel() throws DatasourceServiceException {
    List<CsvModelDataRow> dataRows = datasourceModel.getCsvModel().getDataRows();
      // Get the domain from the business data
      BusinessData businessData = datasourceModel.getCsvModel().getBusinessData();
      if (businessData != null) {
        Domain domain = businessData.getDomain();
        if (domain != null) {
          List<LogicalModel> logicalModels = domain.getLogicalModels();
          for (LogicalModel logicalModel : logicalModels) {
            List<Category> categories = logicalModel.getCategories();
            for (Category category : categories) {
              List<LogicalColumn> logicalColumns = category.getLogicalColumns();
              int i = 0;
              for (LogicalColumn logicalColumn : logicalColumns) {
                CsvModelDataRow row = dataRows.get(i++);
                logicalColumn.setDataType(row.getSelectedDataType());
                logicalColumn.setName(new LocalizedString(domain.getLocales().get(0).getCode(), row.getColumnName()));
              }
            }
          }
          saveCsvModel(domain, false);
        } else {
          throw new RuntimeException("Domain model is null. There is nothing to save");
        }
      } else {
        throw new RuntimeException("Domain model is null. There is nothing to save");

      }
  }

  private void saveRelationalModel() throws DatasourceServiceException {
    List<ModelDataRow> dataRows = datasourceModel.getRelationalModel().getDataRows();
      // Get the domain from the business data
      BusinessData businessData = datasourceModel.getRelationalModel().getBusinessData();
      if (businessData != null) {
        Domain domain = businessData.getDomain();
        if (domain != null) {
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
          saveRelationalModel(businessData, false);
        } else {
          throw new RuntimeException("Domain model is null. There is nothing to save");
        }
      } else {
        throw new RuntimeException("Domain model is null. There is nothing to save");
      }

  }

  private void saveRelationalModel(BusinessData businessData, boolean overwrite) throws DatasourceServiceException {
      // TODO setting value to false to always create a new one. Save as is not yet implemented
      service.saveModel(businessData, overwrite, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          handleSaveError(datasourceModel, error);
        }

        public void success(Boolean value) {
          datasource = datasourceModel.getRelationalModel().getDatasource();
        }
      });
  }

  private void saveCsvModel(Domain domain, boolean overwrite) throws DatasourceServiceException {
      // TODO setting value to false to always create a new one. Save as is not yet implemented
      service.saveInlineEtlModel(domain, overwrite, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          handleSaveError(datasourceModel, error);
        }

        public void success(Boolean value) {
          datasource = datasourceModel.getDatasource();
        }
      });
  }

  public void selectSql() {
    datasourceDeck.setSelectedIndex(RELATIONAL_DECK);
  }

  public void selectOlap() {

  }

  public void selectCsv() {
    datasourceDeck.setSelectedIndex(CSV_DECK);
  }

  public void selectMql() {

  }

  public void selectXml() {

  }

  public DatasourceService getService() {
    return service;
  }

  public void setService(DatasourceService service) {
    this.service = service;
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

  @Override
  protected XulDialog getDialog() {
    return datasourceDialog;  
  }

  @Override
  protected IDatasource getDialogResult() {
    return datasource;  
  }

  @Override
  public void onDialogAccept() {
    saveModel();
    super.onDialogAccept();
  }
  
    private void buildCsvEmptyTable() {
    // Create the tree children and setting the data
    try {
      int count = csvDataTable.getColumns().getColumnCount();
      for (int i = 0; i < DEFAULT_CSV_TABLE_ROW_COUNT; i++) {
        XulTreeRow row = (XulTreeRow) document.createElement("treerow");

        for (int j = 0; j < count; j++) {
          XulTreeCell cell = (XulTreeCell) document.createElement("treecell");
          cell.setLabel(" ");
          row.addCell(cell);
        }

        csvDataTable.addTreeRow(row);
      }
      csvDataTable.update();
    } catch(XulException e) {
      e.printStackTrace();
    }
  }
  
  private void buildRelationalEmptyTable() {
    // Create the tree children and setting the data
    try {
      int count = modelDataTable.getColumns().getColumnCount();
      for (int i = 0; i < DEFAULT_RELATIONAL_TABLE_ROW_COUNT; i++) {
        XulTreeRow row = (XulTreeRow) document.createElement("treerow");

        for (int j = 0; j < count; j++) {
          XulTreeCell cell = (XulTreeCell) document.createElement("treecell");
          cell.setLabel(" ");
          row.addCell(cell);
        }

        modelDataTable.addTreeRow(row);
      }
      modelDataTable.update();
    } catch(XulException e) {
      e.printStackTrace();
    }
  }
  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th));
    errorLabel.setValue(ExceptionParser.getErrorMessage(th));
    errorDialog.show();
  }
}
