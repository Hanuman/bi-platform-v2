package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class CsvModel extends XulEventSourceAdapter{
  private CsvModelValidationListenerCollection csvModelValidationListeners;
  private boolean validated;
  private BusinessData businessData;
  private boolean headersPresent = false;
  private List<CsvModelDataRow> dataRows = new ArrayList<CsvModelDataRow>();
  private String selectedFile = null;

  public CsvModel() {
  }

  public BusinessData getBusinessData() {
    return businessData;
  }

  public void setBusinessData(BusinessData businessData) {
    this.businessData = businessData;
    setModelData(businessData);  
  }

  public boolean isHeadersPresent() {
    return headersPresent;
  }

  public void setHeadersPresent(boolean headersPresent) {
    boolean previousVal = this.headersPresent;
    this.headersPresent = headersPresent;
    this.firePropertyChange("headersPresent", previousVal, headersPresent); //$NON-NLS-1$
    validate();
  }


  public String getSelectedFile() {
    return selectedFile;
  }

  public void setSelectedFile(String selectedFile) {
    String previousVal = this.selectedFile;
    this.selectedFile = selectedFile;
    this.firePropertyChange("selectedFile", previousVal, selectedFile); //$NON-NLS-1$
    validate();
  }

  public boolean isValidated() {
    return validated;
  }


  private void setValidated(boolean validated) {
    boolean prevVal = validated;
    this.validated = validated;
    this.firePropertyChange("validated", prevVal, validated);
  }

  public void validate() {
    if (getSelectedFile() != null && getSelectedFile().length() > 0) {
      fireCsvModelValid();
      this.setValidated(true);
    } else {
      fireCsvModelInValid();
      this.setValidated(false);
    }
  }

  public void setModelData(BusinessData businessData) {
    if (businessData != null) {
      Domain domain = businessData.getDomain();
      List<List<String>> data = businessData.getData();
      List<LogicalModel> logicalModels = domain.getLogicalModels();
      int columnNumber = 0;
      for (LogicalModel logicalModel : logicalModels) {
        List<Category> categories = logicalModel.getCategories();
        for (Category category : categories) {
          List<LogicalColumn> logicalColumns = category.getLogicalColumns();
          for (LogicalColumn logicalColumn : logicalColumns) {
            addCsvModelDataRow(logicalColumn, getColumnData(columnNumber++, data), domain.getLocales().get(0).getCode());
          }
        }
      }
      firePropertyChange("dataRows", null, dataRows);
    } else {
      if (this.dataRows != null) {
        this.dataRows.removeAll(dataRows);
        List<CsvModelDataRow> previousValue = this.dataRows;
        firePropertyChange("dataRows", previousValue, null);
      }
    }
  }

  public void addCsvModelDataRow(LogicalColumn column, List<String> columnData,String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<CsvModelDataRow>();
    }
    this.dataRows.add(new CsvModelDataRow(column, columnData, locale));
  }


  public List<CsvModelDataRow> getDataRows() {
    return dataRows;
  }

  public void setDataRows(List<CsvModelDataRow> dataRows) {
    this.dataRows = dataRows;
  }

  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }
  /*
   * Clears out the model
   */
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setSelectedFile(null);
  }

  public void addCsvModelValidationListener(ICsvModelValidationListener listener) {
    if (csvModelValidationListeners == null) {
      csvModelValidationListeners = new CsvModelValidationListenerCollection();
    }
    csvModelValidationListeners.add(listener);
  }

  public void removeCsvModelValidationListener(IRelationalModelValidationListener listener) {
    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.remove(listener);
    }
  }

  /**
   * Fire all current {@link ICsvModelValidationListener}.
   */
  void fireCsvModelValid() {

    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.fireCsvModelValid();
    }
  }
  /**
   * Fire all current {@link ICsvModelValidationListener}.
   */
  void fireCsvModelInValid() {

    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.fireCsvModelInValid();
    }
  }
}
