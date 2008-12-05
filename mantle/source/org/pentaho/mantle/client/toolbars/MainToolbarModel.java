package org.pentaho.mantle.client.toolbars;

import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IReloadableTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

/**
 * State model for the main toolbar. Replace controller code calls with Bindings when available.
 * 
 * @author NBaker
 */
public class MainToolbarModel implements SolutionBrowserListener{


  private SolutionBrowserPerspective solutionBrowser;
  
  private boolean saveEnabled = false;
  
  //TODO: Remove once bindings in place
  private MainToolbarController controller;
  
  
  public MainToolbarModel(final SolutionBrowserPerspective solutionBrowser){
    this.solutionBrowser = solutionBrowser;
  }
  
  public void setSaveEnabled(Boolean enabled){
    saveEnabled = enabled;
    
    //TODO: Replace following when bindings in place
    controller.setSaveEnabled(enabled);
  }
 
  /**
   * Process incoming events from the SolutionBrowser here
   */
  public void solutionBrowserEvent(IReloadableTabPanel panel, FileItem selectedFileItem) {
    //copy logic from MainToolbar.java
  }
  
  public void setController(MainToolbarController controller){
    this.controller = controller;
    
  }
  
}

  