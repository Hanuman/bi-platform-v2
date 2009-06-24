package org.pentaho.platform.dataaccess.datasource.wizard;

public interface WaitingDialog {
  public String getMessage();
  public String getTitle();
  public void setTitle(String title);
  public void setMessage(String message);
  public void show();
  public void hide();
}
