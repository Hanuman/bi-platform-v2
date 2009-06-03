package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.gwt.widgets.client.utils.MessageBundle;

public interface DatasourceMessages {
  public String getString(String key);
  public String getString(String key, String... parameters);
  public MessageBundle getMessageBundle();
  public void setMessageBundle(MessageBundle messageBundle);
}
