package org.pentaho.mantle.client.perspective.solutionbrowser;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.TreeItem;

public class FileTreeItem extends TreeItem {
  public String fileName;
  public String url;
  private HasFocus focusableWidget;
  
  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getURL() {
    return url;
  }

  public void setURL(String url) {
    this.url = url;
  }

  @Override
  protected HasFocus getFocusableWidget() {
    return ((SolutionTree) this.getTree()).getFocusableWidget();
  }
  
}
