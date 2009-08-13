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
 * Created May 26, 2009
 * @author rmansoor
 */
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
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

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
  private DatasourceMessages messages;


  public CsvModel() {
  }

  @Bindable
  public List<String> getEnclosureList() {
    return enclosureList;
  }

  @Bindable
  public void setEnclosureList() {
    enclosureList = new ArrayList<String>();
    Enclosure[] enclosureArray = Enclosure.values();
    for(int i=0;i<enclosureArray.length;i++) {
      enclosureList.add(getMessages().getString(enclosureArray[i].getName()));
    }
    this.firePropertyChange("enclosureList", null, enclosureList); //$NON-NLS-1$
  }

  @Bindable
  public List<String> getDelimiterList() {
    return delimiterList;
  }

  @Bindable
  public void setDelimiterList() {
    delimiterList = new ArrayList<String>();
    Delimiter[] delimiterArray = Delimiter.values();
    for(int i=0;i<delimiterArray.length;i++) {
      delimiterList.add(getMessages().getString(delimiterArray[i].getName()));
    }
    this.firePropertyChange("delimiterList", null, delimiterList); //$NON-NLS-1$
  }

  @Bindable
  public BusinessData getBusinessData() {
    return businessData;
  }

	@Bindable
  public void setBusinessData(BusinessData value) {
    this.businessData = value;
    if (value != null) {
      Domain domain = value.getDomain();
      List<List<String>> data = value.getData();
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
      firePropertyChange("dataRows", null, dataRows);//$NON-NLS-1$
    } else {
      if (this.dataRows != null) {
        this.dataRows.removeAll(dataRows);
        List<CsvModelDataRow> previousValue = this.dataRows;
        firePropertyChange("dataRows", previousValue, null);//$NON-NLS-1$
      }
    }
  }

  @Bindable
  public boolean isHeadersPresent() {
    return headersPresent;
  }

  @Bindable
  public void setHeadersPresent(boolean value) {
    if(value != this.headersPresent) {
      this.headersPresent = value;
      this.firePropertyChange("headersPresent", !value, this.headersPresent); //$NON-NLS-1$
      validate();      
    }
  }


  @Bindable
  public String getSelectedFile() {
    return selectedFile;
  }

  @Bindable
  public void setSelectedFile(String value) {
    String previousVal = this.selectedFile;
    this.selectedFile = value;
    this.firePropertyChange("selectedFile", previousVal, value); //$NON-NLS-1$
    validate();
  }

  @Bindable
  public boolean isValidated() {
    return validated;
  }


  @Bindable
  private void setValidated(boolean value) {
    if(value != this.validated) {
      this.validated = value;
      this.firePropertyChange("validated", !value, this.validated);//$NON-NLS-1$
    }
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

  @Bindable
  private void addCsvModelDataRow(LogicalColumn column, List<String> columnData,String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<CsvModelDataRow>();
    }
    this.dataRows.add(new CsvModelDataRow(column, columnData, locale));
  }


  @Bindable
  public List<CsvModelDataRow> getDataRows() {
    return dataRows;
  }

  @Bindable
  public void setDataRows(List<CsvModelDataRow> dataRows) {
    this.dataRows = dataRows;
  }

  @Bindable
  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }
  
  
  @Bindable
  public Enclosure getEnclosure() {
    return enclosure;
  }

  @Bindable
  public void setEnclosure(Enclosure value) {
    Enclosure previousValue = this.enclosure;
    this.enclosure = value;
    this.firePropertyChange("enclosure", previousValue, value); //$NON-NLS-1$
  }

  @Bindable
  public Delimiter getDelimiter() {
    return delimiter;
  }

  @Bindable
  public void setDelimiter(Delimiter value) {
    Delimiter previousValue = this.delimiter;
    this.delimiter = value;
    this.firePropertyChange("delimiter", previousValue, value); //$NON-NLS-1$
  }

  
  public DatasourceMessages getMessages() {
    return messages;
  }

  public void setMessages(DatasourceMessages value) {
    this.messages = value;
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
