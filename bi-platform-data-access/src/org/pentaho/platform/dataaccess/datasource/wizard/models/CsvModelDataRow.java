package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.ui.xul.XulEventSourceAdapter;


public class CsvModelDataRow extends XulEventSourceAdapter{

  
  private String columnName, sampleData;
  private List<String> sampleDataList;
  private List<DataType> dataTypes = new ArrayList<DataType>();
  private DataType selectedDataType;
  private String locale;
  private List<AggregationType> aggregationList = new ArrayList<AggregationType>() {

    @Override
    public String toString() {
      StringBuffer buffer = new StringBuffer();
      for(int i=0;i<this.size();i++) {
        buffer.append(this.get(i));
        if(i<this.size()-1) {
          buffer.append(',');  
        }
      }
      return buffer.toString();
    }
    
  };
  //private List<DataFormatType> dataFormatTypes = new ArrayList<DataFormatType>();
  //private DataFormatType selectedDataFormatType;
  
  // Commenting out data format for now
 public CsvModelDataRow(LogicalColumn col, List<String> columnData,String locale) {
    setSelectedDataType(col.getDataType());
    //setSelectedDataFormatType(DataFormatType.CURRENCY);
    setAggregationList(col.getAggregationList());
    setColumnName(col.getName().getString(locale));
    if(columnData.size() > 0) {
      setSampleData(columnData.get(0));
      setSampleDataList(columnData);
    }
  }

  public String getColumnName() {
    return columnName;
  }


  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getSampleData() {
    return sampleData;
  }


  public void setSampleData(String sampleData) {
    this.sampleData = sampleData;
  }

  public List<DataType> getDataTypes() {
    return dataTypes;
  }


  public void setDataTypes(List<DataType> dataTypes) {
    this.dataTypes = dataTypes;
  }

  public List<String> getSampleDataList() {
    return sampleDataList;
  }


  public void setSampleDataList(List<String> sampleDataList) {
    this.sampleDataList = sampleDataList;
  }

  public DataType getSelectedDataType() {
    return selectedDataType;
  }


  public void setSelectedDataType(DataType selectedDataType) {
    this.selectedDataType = selectedDataType;
  }


  public void setSelectedDataType(Object o){
    setSelectedDataType((DataType)  o);
  }
  
  public Vector getBindingDataTypes(){
    Vector v = new Vector();
    //for(DataType t : this.dataTypes){
    for(DataType t : DataType.values()){
      v.add(t);
    }
    return v;
  }
  /**
   * @param aggregationList the aggregationList to set
   */
  public void setAggregationList(List<AggregationType> aggregationList) {
    this.aggregationList.clear();
    if(aggregationList != null && aggregationList.size() > 0) {
      this.aggregationList.addAll(aggregationList);
    } else {
      this.aggregationList.add(AggregationType.NONE);
    }
  }

  /**
   * @return the aggregationList
   */
  public List<AggregationType> getAggregationList() {
    return aggregationList;
  }

  
  //public List<DataFormatType> getDataFormatTypes() {
  //  return dataFormatTypes;
  //}


  //public void setDataFormatTypes(List<DataFormatType> dataFormatTypes) {
  //  this.dataFormatTypes = dataFormatTypes;
  //}


  //public DataFormatType getSelectedDataFormatType() {
   // return selectedDataFormatType;
  //}


  //public void setSelectedDataFormatType(DataFormatType selectedDataFormatType) {
  //  this.selectedDataFormatType = selectedDataFormatType;
 // }
  
  //public void setSelectedDataFormatType(Object o){
  //  setSelectedDataFormatType((DataFormatType)  o);
  //}
  
  //public Vector getBindingDataFormatTypes(){
  //  Vector v = new Vector();
    //for(DataFormatType t : this.dataFormatTypes){
   // for(DataFormatType t : DataFormatType.values()){
   //   v.add(t);
   // }
   // return v;
 // }

}
