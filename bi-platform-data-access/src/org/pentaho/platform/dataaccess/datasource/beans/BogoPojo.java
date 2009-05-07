package org.pentaho.platform.dataaccess.datasource.beans;

import java.io.Serializable;

import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetColumnType;
import org.pentaho.metadata.model.concept.types.TargetTableType;
/*
 * This class is a workaround for GWT. GWT is not able to compile these classes are they have been used in a map
 * http://code.google.com/p/google-web-toolkit/issues/detail?id=3521
 */
public class BogoPojo implements Serializable{

  TargetTableType targetTableType;
  LocalizedString localizedString;
  DataType dataType;
  AggregationType aggType;
  TargetColumnType targetColumnType;
  LocaleType localeType;
  
  public TargetTableType getTargetTableType() {
    return targetTableType;
  }
  public void setTargetTableType(TargetTableType targetTableType) {
    this.targetTableType = targetTableType;
  }
  public LocalizedString getLocalizedString() {
    return localizedString;
  }
  public void setLocalizedString(LocalizedString localizedString) {
    this.localizedString = localizedString;
  }
  public DataType getDataType() {
    return dataType;
  }
  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }
  public AggregationType getAggType() {
    return aggType;
  }
  public void setAggType(AggregationType aggType) {
    this.aggType = aggType;
  }
  public TargetColumnType getTargetColumnType() {
    return targetColumnType;
  }
  public void setTargetColumnType(TargetColumnType targetColumnType) {
    this.targetColumnType = targetColumnType;
  }
  public void setLocaleType(LocaleType localeType) {
    this.localeType = localeType;
  }
  public LocaleType getLocaleType() {
    return localeType;
  }
}
