package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.gwt.widgets.client.utils.MessageBundle;

public class GwtDatasourceMessages implements DatasourceMessages{
  MessageBundle messageBundle;
  
  public GwtDatasourceMessages() {
    
  }
  public GwtDatasourceMessages(MessageBundle messageBundle){
    this.messageBundle = messageBundle;
  }

  public String getString(String key) {
    if (this.messageBundle == null) {
      return key;
    }
    return this.messageBundle.getString(key);
  }

  public String getString(String key, String... parameters) {
    if (this.messageBundle == null) {
      return key;
    }
    return this.messageBundle.getString(key, parameters);
  }

  public MessageBundle getMessageBundle() {
    return this.messageBundle;
  }
  public void setMessageBundle(MessageBundle messageBundle) {
    this.messageBundle = messageBundle;
  }
}
