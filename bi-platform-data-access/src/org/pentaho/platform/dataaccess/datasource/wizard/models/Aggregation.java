package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.XulEventSourceAdapter;

public class Aggregation  extends XulEventSourceAdapter{
  
  public Aggregation(List<AggregationType> aggregationList, AggregationType defaultAggregationType) {
    super();
    setAggregationList(aggregationList);
    setDefaultAggregationType(defaultAggregationType);
  }

  public Aggregation() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static final int MAX_COL_SIZE = 15;
  private AggregationType defaultAggregationType = AggregationType.NONE;
  private List<AggregationType> aggregationList = new ArrayList<AggregationType>() {
    @Override
    public String toString() {
      StringBuffer buffer = new StringBuffer();
      for(int i=0;i<this.size();i++) {
        buffer.append(this.get(i));
        if(i<this.size()-1 && (buffer.length()
            + this.get(i+1).name().length() < MAX_COL_SIZE)) {
          buffer.append(',');  
        } else {
          break;
        }
      }
      return buffer.toString();
    }

    
  };
  

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
    firePropertyChange("aggregationList", null, aggregationList);
  }

  /**
   * @return the aggregationList
   */
  public List<AggregationType> getAggregationList() {
    return aggregationList;
  }

  public void setDefaultAggregationType(AggregationType defaultAggregationType) {
    this.defaultAggregationType = defaultAggregationType;
  }

  public AggregationType getDefaultAggregationType() {
    return defaultAggregationType;
  }
  
  public String toString() {
    return aggregationList.toString();
  }
}
