package org.pentaho.mantle.client.toolbars;

import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.ToggleWorkspaceCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
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
  private boolean saveAsEnabled = false;
  private boolean printEnabled = false;
  private boolean newAnalysisEnabled = false;
  
  //TODO: Remove once bindings in place
  private MainToolbarController controller;
  
  
  public MainToolbarModel(final SolutionBrowserPerspective solutionBrowser){
    this.solutionBrowser = solutionBrowser;
    this.solutionBrowser.addSolutionBrowserListener(this);
  }
  
  public void setSaveEnabled(Boolean enabled){
    saveEnabled = enabled;
    
    //TODO: Replace following when bindings in place
    controller.setSaveEnabled(enabled);
  }

  public void setSaveAsEnabled(Boolean enabled){
    saveAsEnabled = enabled;
    
    //TODO: Replace following when bindings in place
    controller.setSaveAsEnabled(enabled);
  }

  public void setPrintEnabled(Boolean enabled){
    printEnabled = enabled;
    
    //TODO: Replace following when bindings in place
    controller.setPrintEnabled(enabled);
  }
  
  public void setNewAnalysisEnabled(Boolean enabled){
    newAnalysisEnabled = enabled;
    
    //TODO: Replace following when bindings in place
    controller.setNewAnalysisEnabled(enabled);
  }
  
  public void executeOpenFileCommand() {
    OpenFileCommand openFileCommand = new OpenFileCommand(solutionBrowser);
    openFileCommand.execute();
  }
  
  public void executeAnalysisViewCommand() {
    AnalysisViewCommand analysisViewCommand = new AnalysisViewCommand(solutionBrowser);
    analysisViewCommand.execute();
  }
  
  public void executePrintCommand() {
    PrintCommand printCommand = new PrintCommand(solutionBrowser);
    printCommand.execute();
  }
  
  public void executeSaveCommand() {
    SaveCommand saveCommand = new SaveCommand(solutionBrowser, false);
    saveCommand.execute();
  }  
  
  public void executeSaveAsCommand() {
    SaveCommand saveCommand = new SaveCommand(solutionBrowser, true);
    saveCommand.execute();
  }  
  
  public void executeWAQRCommand() {
    WAQRCommand wAQRCommand = new WAQRCommand(solutionBrowser);
    wAQRCommand.execute();
  }
  
  public void executeWorkspaceCommand() {
    ToggleWorkspaceCommand toggleWorkspaceCommand = new ToggleWorkspaceCommand(solutionBrowser);
    toggleWorkspaceCommand.execute();  
  }
  
  public void executeShowBrowserCommand() {
    ShowBrowserCommand showBrowserCommand = new ShowBrowserCommand(solutionBrowser);
    showBrowserCommand.execute();
  }
  /**
   * Process incoming events from the SolutionBrowser here
   */
  public void solutionBrowserEvent(IReloadableTabPanel panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;

    controller.setWorkspaceSelected(isWorkspaceShowing());
    controller.setShowBrowserSelected(isSolutionBrowserShowing());
    
    if(panel != null){
      selectedTabURL = panel.getUrl();
      saveEnabled = panel.isSaveEnabled();
    }
    
    setPrintEnabled(selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$
    setSaveEnabled(saveEnabled);
    setSaveAsEnabled(saveEnabled);

  }
  
  public void setController(MainToolbarController controller){
    this.controller = controller;
    
  }
  
  public boolean isSolutionBrowserShowing() {
    return solutionBrowser.isNavigatorShowing();
  }
  
  public boolean isWorkspaceShowing() {
    return solutionBrowser.isWorkspaceShowing();
  }
}

  