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

  public boolean equals(Object obj) {
    boolean result = false;
    if (obj instanceof ChartDataDefinition) {
      ChartDataDefinition chartDataQuery = (ChartDataDefinition) obj;
      result = (query != null)
          && query.equals(chartDataQuery.query)
          && (rangeColumn != null)
          && rangeColumn.equals(chartDataQuery.rangeColumn)
          && (domainColumn == null ? chartDataQuery.domainColumn == null : domainColumn
              .equals(chartDataQuery.domainColumn))
          && (categoryColumn == null ? chartDataQuery.categoryColumn == null : categoryColumn
              .equals(chartDataQuery.categoryColumn));

    }
    return result;
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
