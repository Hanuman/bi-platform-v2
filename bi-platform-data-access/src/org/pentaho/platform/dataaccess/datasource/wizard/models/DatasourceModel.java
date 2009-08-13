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
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.Datasource;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceModel extends XulEventSourceAdapter implements IRelationalModelValidationListener, ICsvModelValidationListener{
  private boolean validated;
  private String datasourceName;
  private DatasourceType datasourceType = DatasourceType.NONE;
  private RelationalModel relationalModel;
  private CsvModel csvModel;
  
  public DatasourceModel() {
    relationalModel = new RelationalModel();
    csvModel = new CsvModel();
    relationalModel.addRelationalModelValidationListener(this);
    csvModel.addCsvModelValidationListener(this);
  }

  @Bindable
  public RelationalModel getRelationalModel() {
    return relationalModel;
  }

  @Bindable
  public void setRelationalModel(RelationalModel relationalModel) {
    this.relationalModel = relationalModel;
  }

  @Bindable
  public CsvModel getCsvModel() {
    return csvModel;
  }

  @Bindable
  public void setCsvModel(CsvModel csvModel) {
    this.csvModel = csvModel;
  }


  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }

  @Bindable
  public void setDatasourceName(String datasourceName) {
    String previousVal = this.datasourceName;
    this.datasourceName = datasourceName;
    
    // if we're editing a generated or already defined domain,
    // we need to keep the datasource name in sync
    if (csvModel != null && csvModel.getBusinessData() != null &&
        csvModel.getBusinessData().getDomain() != null) {
      Domain domain = csvModel.getBusinessData().getDomain(); 
      domain.setId(datasourceName);
      LogicalModel model = domain.getLogicalModels().get(0);
      String localeCode = domain.getLocales().get(0).getCode();
      model.getName().setString(localeCode, datasourceName);
    }

    if (relationalModel != null && relationalModel.getBusinessData() != null &&
        relationalModel.getBusinessData().getDomain() != null) {
      Domain domain = relationalModel.getBusinessData().getDomain(); 
      domain.setId(datasourceName);
      LogicalModel model = domain.getLogicalModels().get(0);
      String localeCode = domain.getLocales().get(0).getCode();
      model.getName().setString(localeCode, datasourceName);
    }
    
    this.firePropertyChange("datasourcename", previousVal, datasourceName); //$NON-NLS-1$
    validate();
  }

  @Bindable
  public boolean isValidated() {
    return validated;
  }

  @Bindable
  private void setValidated(boolean validated) {
    boolean prevVal = this.validated;
    this.validated = validated;
    this.firePropertyChange("validated", prevVal, validated); //$NON-NLS-1$
  }
  
  @Bindable
  public DatasourceType getDatasourceType() {
    return this.datasourceType;
  }

  @Bindable
  public void setDatasourceType(DatasourceType datasourceType) {
    DatasourceType previousVal = this.datasourceType;
    this.datasourceType = datasourceType;
    this.firePropertyChange("datasourceType", previousVal, datasourceType); //$NON-NLS-1$
    validate();
  }

  private void validate() {
    boolean value = false;
    if(DatasourceType.SQL == getDatasourceType()) {
      value = relationalModel.isValidated();
    } else if(DatasourceType.CSV == getDatasourceType()){
      value = csvModel.isValidated();
    }
    setValidated(datasourceName != null && datasourceName.length() > 0 && value);
  }

  /*
   * Clears out the model
   */
  public void clearModel() {
    // clear the models before switching the datasource type, otherwise
    // an error is presented to the user.
    relationalModel.clearModel();
    csvModel.clearModel();
    setDatasourceName("");
    setDatasourceType(DatasourceType.SQL);
  }
  
  @Bindable
  public IDatasource getDatasource() {
    IDatasource datasource = new Datasource();
    if(DatasourceType.SQL == getDatasourceType()) {
      datasource.setBusinessData(getRelationalModel().getBusinessData());
      datasource.setConnections(getRelationalModel().getConnections());
      datasource.setQuery(getRelationalModel().getQuery());
      datasource.setSelectedConnection(getRelationalModel().getSelectedConnection());
    } else {
      datasource.setBusinessData(getCsvModel().getBusinessData());
      datasource.setSelectedFile(getCsvModel().getSelectedFile());
      datasource.setHeadersPresent(getCsvModel().isHeadersPresent());
    }
    return datasource;
  }

  public void onRelationalModelInValid() {
    if(DatasourceType.SQL == getDatasourceType()) {
      setValidated(false);
    }
  }

  public void onRelationalModelValid() {
    if(DatasourceType.SQL == getDatasourceType()) {
      setValidated(datasourceName != null && datasourceName.length() > 0 && true);
    }
  }

  public void onCsvModelInValid() {
    if(DatasourceType.CSV == getDatasourceType()) {
      setValidated(false);
    }
  }

  public void onCsvModelValid() {
    if(DatasourceType.CSV == getDatasourceType()) {
      setValidated(datasourceName != null && datasourceName.length() > 0 && true);
    }
  }
  
  /**
   * This is a utility method that looks into an old domain for the same column ids, and then 
   * copies over the old metadata into the new.
   * @param oldDomain
   * @param newDomain
   */
  public void copyOverMetadata(Domain oldDomain, Domain newDomain) {
    Category category = newDomain.getLogicalModels().get(0).getCategories().get(0);
    LogicalModel oldModel = oldDomain.getLogicalModels().get(0);
    for (LogicalColumn column : category.getLogicalColumns()) {
      LogicalColumn oldColumn = oldModel.findLogicalColumn(column.getId());
      if (oldColumn != null) {
        column.setDataType(oldColumn.getDataType());
        column.setName(oldColumn.getName());
        column.setAggregationList(oldColumn.getAggregationList());
        column.setAggregationType(oldColumn.getAggregationType());
      }
    }
  }


}
