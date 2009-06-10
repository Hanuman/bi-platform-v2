package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.ui.xul.XulEventSourceAdapter;


public class CsvModelDataRow extends XulEventSourceAdapter{
  public static final int MAX_COL_SIZE = 15;
  private String columnName, sampleData;
  private List<String> sampleDataList;
  private List<DataType> dataTypes = new ArrayList<DataType>();
  private DataType selectedDataType;
  private String locale;
  private Aggregation aggregation;
  //private List<DataFormatType> dataFormatTypes = new ArrayList<DataFormatType>();
  //private DataFormatType selectedDataFormatType;
  
  // Commenting out data format for now
 public CsvModelDataRow(LogicalColumn col, List<String> columnData,String locale) {
    setSelectedDataType(col.getDataType());
    //setSelectedDataFormatType(DataFormatType.CURRENCY);
    setAggregation(new Aggregation(col.getAggregationList(), AggregationType.NONE));
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

  public void setAggregation(Aggregation aggregation) {
    this.aggregation = aggregation;
  }

  public Aggregation getAggregation() {
    return aggregation;
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
