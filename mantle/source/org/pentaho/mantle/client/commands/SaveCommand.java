package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;

public class SaveCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;
  boolean isSaveAs = false;

  private String name;
  private String solution;
  private String path;
  private String type;

  public SaveCommand(SolutionBrowserPerspective navigatorPerspective, boolean isSaveAs) {
    this.navigatorPerspective = navigatorPerspective;
    this.isSaveAs = isSaveAs;
  }

  public void execute() {
    
    retrieveCachedValues(navigatorPerspective.getCurrentFrameElementId());
    
    if (isSaveAs || name == null) {
      final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.SAVE, "/", navigatorPerspective.getSolutionDocument(), false, true); //$NON-NLS-1$
      if (isSaveAs) {
        dialog.setTitle(Messages.getInstance().saveAs());
        dialog.setText(Messages.getInstance().saveAs());
      } else {
        dialog.setTitle(Messages.getInstance().save());
      }
      
      if (!MantleApplication.showAdvancedFeatures) {
        dialog.setShowSearch(false);
      }
      dialog.addFileChooserListener(new FileChooserListener() {

        public void fileSelected(final String solution, final String path, final String name, String localizedFileName) {
          setSolution(solution);
          setPath(path);
          setName(name);
          setType("html"); //$NON-NLS-1$

          if(false){//if (dialog.doesSelectedFileExist()) {
            dialog.hide();
            PromptDialogBox overWriteDialog = new PromptDialogBox(Messages.getInstance().question(), Messages.getInstance().yes(), Messages.getInstance().no(), false, true);
            overWriteDialog.setContent(new Label(Messages.getInstance().fileExistsOverwrite(), false));
            overWriteDialog.setCallback(new IDialogCallback() {
              public void okPressed() {
                doSaveAs(navigatorPerspective.getCurrentFrameElementId(), name, solution, path, type, true);
              }

              public void cancelPressed() {
                dialog.show();
              }
            });
            overWriteDialog.center();
          } else {
            doSaveAs(navigatorPerspective.getCurrentFrameElementId(), name, solution, path, type, true);
            clearValues();
          }
        }

        public void fileSelectionChanged(String solution, String path, String name) {
        }

      });
      dialog.center();
    } else {
      doSaveAs(navigatorPerspective.getCurrentFrameElementId(), name, solution, path, type, true);
      clearValues();
    }
  }
  
  private void clearValues(){
    name = null;
    solution = null;
    path = null;
    type = null;
  }
  
  private native void retrieveCachedValues(String id)/*-{
    var frame = $doc.getElementById(id);
    frame = frame.contentWindow;
    
    
    
    if(frame.mySolution != undefined){
      this.@org.pentaho.mantle.client.commands.SaveCommand::setSolution(Ljava/lang/String;)(frame.mySolution);
      this.@org.pentaho.mantle.client.commands.SaveCommand::setPath(Ljava/lang/String;)(frame.myPath);
      this.@org.pentaho.mantle.client.commands.SaveCommand::setName(Ljava/lang/String;)(frame.myFilename);
      this.@org.pentaho.mantle.client.commands.SaveCommand::setType(Ljava/lang/String;)(frame.myType);
    }
     
  }-*/;


  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  public static native void doSaveAs(String elementId, String filename, String solution, String path, String type, boolean overwrite) /*-{
       var frame = $doc.getElementById(elementId);
       frame = frame.contentWindow;
       frame.focus();                                
       
       //cache values for subsequent calls                   
       frame.mySolution = solution;
       frame.myPath = path;
       frame.myFilename = filename;
       frame.myType = type;
       frame.myOverwrite = overwrite;
       
       frame.gCtrlr.repositoryBrowserController.remoteSave(frame.myFilename, frame.mySolution, frame.myPath, frame.myType, frame.myOverwrite);
     }-*/;

  public String getName() {
  
    return name;
  }

  public void setName(String name) {
  
    this.name = name;
  }

  public String getSolution() {
  
    return solution;
  }

  public void setSolution(String solution) {
  
    this.solution = solution;
  }

  public String getPath() {
  
    return path;
  }

  public void setPath(String path) {
  
    this.path = path;
  }

  public String getType() {
  
    return type;
  }

  public void setType(String type) {
  
    this.type = type;
  }

}
