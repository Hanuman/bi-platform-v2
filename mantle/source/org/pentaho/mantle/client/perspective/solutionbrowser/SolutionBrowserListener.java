package org.pentaho.mantle.client.perspective.solutionbrowser;

public interface SolutionBrowserListener {
  
  // would like to let the listeners know (where possible):
  // -current tab (url)
  // -selected file item
  public void solutionBrowserEvent(String selectedTabURL, FileItem selectedFileItem);
}
