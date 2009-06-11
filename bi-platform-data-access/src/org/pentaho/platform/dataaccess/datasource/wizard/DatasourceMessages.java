package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

public interface DatasourceMessages {
  public String getString(String key);
  public String getString(String key, String... parameters);
  public ResourceBundle getMessageBundle();
  public void setMessageBundle(ResourceBundle messageBundle);
}
