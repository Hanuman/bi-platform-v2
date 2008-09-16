package org.pentaho.mantle.client.objects;

import java.io.Serializable;
import java.util.List;

public class SubscriptionBean implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 3279051936233043771L;
  private String id;
  private String name;
  private String xactionName;
  private String scheduleDate;
  private String size;
  private String type;
  private List<String[]> content;
  
  public SubscriptionBean() {
    
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getXactionName() {
    return xactionName;
  }
  public void setXactionName(String xactionName) {
    this.xactionName = xactionName;
  }
  public String getScheduleDate() {
    return scheduleDate;
  }
  public void setScheduleDate(String scheduleDate) {
    this.scheduleDate = scheduleDate;
  }
  public String getSize() {
    return size;
  }
  public void setSize(String size) {
    this.size = size;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }  
  public List<String[]> getContent() {
    return content;
  }
  public void setContent(List<String[]> content) {
    this.content = content;
  }
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
}
