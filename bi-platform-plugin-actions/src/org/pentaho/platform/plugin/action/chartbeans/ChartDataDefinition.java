package org.pentaho.platform.plugin.action.chartbeans;

import java.io.Serializable;

public class ChartDataDefinition implements Serializable {
  
  String query;

  String rangeColumn;

  String domainColumn;

  String categoryColumn;

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * Equals returns true if the parameter Object is an instanceof ChartDataDefinition
   * with ALL properties being "*.equals()" to this instance's. 
   */
  public boolean equals(Object obj) {
    if(obj == null){
      return false;
    }
    
    if (!(obj instanceof ChartDataDefinition)) {
      return false;
    }

    ChartDataDefinition chartDataQuery = (ChartDataDefinition) obj;
    
    if(query == null){
      if(chartDataQuery.query != null){
        return false;
      }
    } else {
      if(!query.equals(chartDataQuery.query)){
        return false;
      }
    }
    
    if(rangeColumn == null){
      if(chartDataQuery.rangeColumn != null){
        return false;
      }
    } else {
      if(!rangeColumn.equals(chartDataQuery.rangeColumn)){
        return false;
      }
    }

    if(domainColumn == null){
      if(chartDataQuery.domainColumn != null){
        return false;
      }
    } else {
      if(!domainColumn.equals(chartDataQuery.domainColumn)){
        return false;
      }
    }

    if(categoryColumn == null){
      if(chartDataQuery.categoryColumn != null){
        return false;
      }
    } else {
      if(!categoryColumn.equals(chartDataQuery.categoryColumn)){
        return false;
      }
    }

    return true;
  }

  public String getRangeColumn() {
    return rangeColumn;
  }

  public void setRangeColumn(String rangeColumn) {
    this.rangeColumn = rangeColumn;
  }

  public String getDomainColumn() {
    return domainColumn;
  }

  public void setDomainColumn(String domainColumn) {
    this.domainColumn = domainColumn;
  }

  public String getCategoryColumn() {
    return categoryColumn;
  }

  public void setCategoryColumn(String categoryColumn) {
    this.categoryColumn = categoryColumn;
  }

}
