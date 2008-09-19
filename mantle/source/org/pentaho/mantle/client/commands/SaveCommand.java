package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;

public class SaveCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;
  boolean isSaveAs = false;

  private static String name;
  private static String solution;
  private static String path;
  private static String type;

  public SaveCommand(SolutionBrowserPerspective navigatorPerspective, boolean isSaveAs) {
    this.navigatorPerspective = navigatorPerspective;
    this.isSaveAs = isSaveAs;
  }

  public void execute() {
    if (isSaveAs || name == null) {
      final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.SAVE, "/", navigatorPerspective.getSolutionDocument(), false, true);
      if (!MantleApplication.showAdvancedFeatures) {
        dialog.setShowSearch(false);
      }
      dialog.addFileChooserListener(new FileChooserListener() {

        public void fileSelected(final String solution, final String path, final String name, String localizedFileName) {
          SaveCommand.solution = solution;
          SaveCommand.path = path;
          SaveCommand.name = name;
          SaveCommand.type = "html";

          if(false){//if (dialog.doesSelectedFileExist()) {
            dialog.hide();
            PromptDialogBox overWriteDialog = new PromptDialogBox("Question", "Yes", "No", false, true);
            overWriteDialog.setContent(new Label("File exists, overwrite?", false));
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
          }
        }

        public void fileSelectionChanged(String solution, String path, String name) {
        }

      });
      dialog.center();
    } else {
      doSaveAs(navigatorPerspective.getCurrentFrameElementId(), name, solution, path, type, true);
    }
  }

  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  public static native void doSaveAs(String elementId, String filename, String solution, String path, String type, boolean overwrite) /*-{
       var frame = $doc.getElementById(elementId);
       frame = frame.contentWindow;
       frame.focus();                                      
       var mySolution = solution;
       var myPath = path;
       var myFilename = filename;
       var myType = type;
       var myOverwrite = overwrite;
       frame.gCtrlr.repositoryBrowserController.remoteSave(myFilename, mySolution, myPath, myType, myOverwrite);
     }-*/;

}
