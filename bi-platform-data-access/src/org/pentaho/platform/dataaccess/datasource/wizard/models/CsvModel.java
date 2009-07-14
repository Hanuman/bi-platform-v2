package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class CsvModel extends XulEventSourceAdapter{
  private CsvModelValidationListenerCollection csvModelValidationListeners;
  private boolean validated;
  private BusinessData businessData;
  private boolean headersPresent = false;
  private List<CsvModelDataRow> dataRows = new ArrayList<CsvModelDataRow>();
  private String selectedFile = null;
  private Enclosure enclosure;
  private Delimiter delimiter;
  private List<String> enclosureList;
  private List<String> delimiterList;
  
  public CsvModel() {
  }

  public List<String> getEnclosureList() {
    return enclosureList;
  }

  public void setEnclosureList() {
    enclosureList = new ArrayList<String>();
    Enclosure[] enclosureArray = Enclosure.values();
    for(int i=0;i<enclosureArray.length;i++) {
      enclosureList.add(enclosureArray[i].getName());
    }
    this.firePropertyChange("enclosureList", null, enclosureList); //$NON-NLS-1$
  }

  public List<String> getDelimiterList() {
    return delimiterList;
  }

  public void setDelimiterList() {
    delimiterList = new ArrayList<String>();
    Delimiter[] delimiterArray = Delimiter.values();
    for(int i=0;i<delimiterArray.length;i++) {
      delimiterList.add(delimiterArray[i].getName());
    }
    this.firePropertyChange("delimiterList", null, delimiterList); //$NON-NLS-1$
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
  
  
  public Enclosure getEnclosure() {
    return enclosure;
  }

  public void setEnclosure(Enclosure enclosure) {
    Enclosure previousValue = this.enclosure;
    this.enclosure = enclosure;
    this.firePropertyChange("enclosure", previousValue, enclosure); //$NON-NLS-1$
  }

  public Delimiter getDelimiter() {
    return delimiter;
  }

  public void setDelimiter(Delimiter delimiter) {
    Delimiter previousValue = this.delimiter;
    this.delimiter = delimiter;
    this.firePropertyChange("delimiter", previousValue, delimiter); //$NON-NLS-1$
  }

  /*
   * Clears out the model
   */
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setSelectedFile(null);
    setDelimiter(Delimiter.COMMA);
    setEnclosure(Enclosure.DOUBLEQUOTE);
    setHeadersPresent(true);
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
