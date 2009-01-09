package org.pentaho.mantle.client.toolbars;

import org.pentaho.mantle.client.XulMain;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IReloadableTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.ui.xul.XulEventSourceAdapter;

/**
 * State model for the main toolbar. Replace controller code calls with Bindings when available.
 * 
 * @author NBaker
 */
public class MainToolbarModel extends XulEventSourceAdapter implements SolutionBrowserListener{


  private SolutionBrowserPerspective solutionBrowser;
  private XulMain main; 
  private boolean saveEnabled = false;
  private boolean saveAsEnabled = false;
  private boolean printEnabled = false;
  private boolean newAnalysisEnabled = false;
  private boolean contentEditEnabled = false;
  private boolean contentEditSelected = false;
  private boolean showBrowserSelected = false;
  private boolean workspaceSelected = false;
  
  
  public MainToolbarModel(final SolutionBrowserPerspective solutionBrowser, XulMain main){
    this.solutionBrowser = solutionBrowser;
    this.solutionBrowser.addSolutionBrowserListener(this);
    this.main = main;
  }
  
  public void setSaveEnabled(Boolean enabled){
    boolean prevVal = this.saveEnabled;
    saveEnabled = enabled;
    this.firePropertyChange("saveEnabled", prevVal, saveEnabled);
  }
  
  public boolean isSaveEnabled(){
    return this.saveEnabled;
  }

  public void setSaveAsEnabled(Boolean enabled){
    boolean prevVal = this.saveAsEnabled;
    saveAsEnabled = enabled;

    this.firePropertyChange("saveAsEnabled", prevVal, saveEnabled);
  }

  public void setPrintEnabled(Boolean enabled){
    boolean prevVal = this.printEnabled;
    printEnabled = enabled;

    this.firePropertyChange("printEnabled", prevVal, saveEnabled);
  }
  
  public void setNewAnalysisEnabled(Boolean enabled){
    boolean prevVal = this.newAnalysisEnabled;
    newAnalysisEnabled = enabled;

    this.firePropertyChange("printEnabled", prevVal, newAnalysisEnabled);
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

  /**
   * Process incoming events from the SolutionBrowser here
   */
  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, IReloadableTabPanel panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    boolean editIsEnabled = false;
    boolean editSelected = false;
    
    
    if(panel != null){
      selectedTabURL = panel.getUrl();
      saveEnabled = panel.isSaveEnabled();
      editIsEnabled = panel.isEditEnabled();
      editSelected = panel.isEditSelected();
    }
    
    setPrintEnabled(selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$
    setSaveEnabled(saveEnabled);
    setSaveAsEnabled(saveEnabled);
    setContentEditEnabled(editIsEnabled);
    setContentEditSelected(editSelected);

    setWorkspaceSelected(solutionBrowser.isWorkspaceShowing());
    setShowBrowserSelected(solutionBrowser.isNavigatorShowing());
    
    if(SolutionBrowserListener.EventType.OPEN.equals(type) || SolutionBrowserListener.EventType.SELECT.equals(type)) {
      if(panel != null) {
        main.applyOverlays(panel.getOverlayIds());  
      }
    } else if(SolutionBrowserListener.EventType.CLOSE.equals(type) || SolutionBrowserListener.EventType.DESELECT.equals(type)){
      if(panel != null) {
        main.removeOverlays(panel.getOverlayIds());  
      }
    }
  }
  
  public boolean isShowBrowserSelected() {
    return showBrowserSelected;
  }
  
  public boolean isWorkspaceSelected() {
    return workspaceSelected;
  }
  
  
  public void setShowBrowserSelected(boolean showBrowserSelected) {
    boolean prevVal = this.showBrowserSelected;
    
  
    this.showBrowserSelected = showBrowserSelected;
    this.firePropertyChange("showBrowserSelected", prevVal, showBrowserSelected);
  }

  public void setWorkspaceSelected(boolean workspaceSelected) {
    boolean prevVal = this.workspaceSelected;
  
    this.workspaceSelected = workspaceSelected;
    this.firePropertyChange("workspaceSelected", prevVal, workspaceSelected);
  }

  public void setContentEditEnabled(boolean enable){
    boolean prevVal = this.contentEditEnabled;
    contentEditEnabled = enable;
    this.firePropertyChange("contentEditEnabled", prevVal, contentEditEnabled);
  }
  public void setContentEditSelected(boolean selected){
    boolean prevVal = this.contentEditSelected;
    contentEditSelected = selected;
    this.firePropertyChange("contentEditSelected", prevVal, contentEditSelected);
  }
  
  public boolean isContentEditSelected(){
    return this.contentEditSelected;
  }
  
  public void setContentEditToggled(){
    setContentEditSelected(!this.contentEditSelected);
  }

  public boolean isContentEditEnabled() {
    return contentEditEnabled;
  }
  
}

  