package org.pentaho.mantle.client.objects;

import java.io.Serializable;

public class Bookmark implements Serializable  {

  String title;
  String url;
  String group;
  
  public Bookmark() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }
  
}
