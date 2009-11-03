/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.mantle.client.commands;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.filechooser.FileChooser.FileChooserMode;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.SolutionDocumentManager;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.TabWidget;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.xml.client.Document;

public class SaveCommand extends AbstractCommand {

  boolean isSaveAs = false;

  private String name;
  private String solution;
  private String path;
  private SolutionFileInfo.Type type;
  private String tabName;

  public SaveCommand() {
  }

  public SaveCommand(boolean isSaveAs) {
    this.isSaveAs = isSaveAs;
  }

  protected void performOperation() {
    performOperation(true);
  }

  protected void performOperation(boolean feedback) {
    final SolutionBrowserPerspective navigatorPerspective = SolutionBrowserPerspective.getInstance();

    retrieveCachedValues(navigatorPerspective.getContentTabPanel().getCurrentFrame());

    SolutionDocumentManager.getInstance().fetchSolutionDocument(new AsyncCallback<Document>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Document result) {
        if (isSaveAs || name == null) {
          final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.SAVE, "/", result, false, true); //$NON-NLS-1$
          if (isSaveAs) {
            dialog.setTitle(Messages.getString("saveAs")); //$NON-NLS-1$
            dialog.setText(Messages.getString("saveAs")); //$NON-NLS-1$
          } else {
            dialog.setTitle(Messages.getString("save")); //$NON-NLS-1$
          }

          if (!MantleApplication.showAdvancedFeatures) {
            dialog.setShowSearch(false);
          }
          dialog.addFileChooserListener(new FileChooserListener() {

            public void fileSelected(final String solution, final String path, final String name, String localizedFileName) {
              SaveCommand.this.solution = solution;
              SaveCommand.this.path = path;
              SaveCommand.this.name = name;
              SaveCommand.this.type = SolutionFileInfo.Type.XACTION; //$NON-NLS-1$

              tabName = name;
              if (tabName.indexOf("analysisview.xaction") != -1) {
                // trim off the analysisview.xaction from the localized-name
                tabName = tabName.substring(0, tabName.indexOf("analysisview.xaction") - 1);
              } else if (tabName.indexOf("waqr.xaction") != -1) {
                tabName = tabName.substring(0, tabName.indexOf("waqr.xaction") - 1);
              }

              if (false) {// if (dialog.doesSelectedFileExist()) {
                dialog.hide();
                PromptDialogBox overWriteDialog = new PromptDialogBox(Messages.getString("question"), Messages.getString("yes"), Messages.getString("no"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    false, true);
                overWriteDialog.setContent(new Label(Messages.getString("fileExistsOverwrite"), false)); //$NON-NLS-1$
                overWriteDialog.setCallback(new IDialogCallback() {
                  public void okPressed() {
                    doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, solution, path, type, true);
                    Window.setTitle(Messages.getString("productName") + " - " + name); //$NON-NLS-1$ //$NON-NLS-2$
                  }

                  public void cancelPressed() {
                    dialog.show();
                  }
                });
                overWriteDialog.center();
              } else {
                doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, solution, path, type, true);
                Window.setTitle(Messages.getString("productName") + " - " + name); //$NON-NLS-1$ //$NON-NLS-2$
                persistFileInfoInFrame();
                clearValues();
              }
            }

            public void fileSelectionChanged(String solution, String path, String name) {
            }

          });
          dialog.center();
        } else {
          doSaveAs(navigatorPerspective.getContentTabPanel().getCurrentFrameElementId(), name, solution, path, type, true);
          clearValues();
        }
      }
    }, false);
  }

  private void persistFileInfoInFrame() {
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName(this.name);
    fileInfo.setPath(this.path);
    fileInfo.setSolution(this.solution);
    fileInfo.setType(this.type);
    SolutionBrowserPerspective.getInstance().getContentTabPanel().getCurrentFrame().setFileInfo(fileInfo);
  }

  private void clearValues() {
    name = null;
    solution = null;
    path = null;
    type = null;
  }

  private void retrieveCachedValues(IFrameTabPanel tabPanel) {
    clearValues();
    SolutionFileInfo info = tabPanel.getFileInfo();
    if (info != null) {
      this.name = info.getName();
      this.path = info.getPath();
      this.solution = info.getSolution();
      this.type = info.getType();
    }
  }

  /**
   * This method will call saveReportSpecAs(string filename, string solution, string path, bool overwrite)
   * 
   * @param elementId
   */
  private native void doSaveAs(String elementId, String filename, String solution, String path, SolutionFileInfo.Type type, boolean overwrite)
  /*-{
    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();                                
                
    if(frame.pivot_initialized) {
      // do jpivot save
      var actualFileName = filename;
      if (filename.indexOf("analysisview.xaction") == -1) {
        actualFileName = filename + ".analysisview.xaction";
      } else {
        // trim off the analysisview.xaction from the localized-name
        filename = filename.substring(0, filename.indexOf("analysisview.xaction")-1);
      }
      frame.controller.saveAs(actualFileName, filename, solution, path, overwrite);
    } else {
      // trim off the waqr.xaction from the localized-name (waqr's save will put it back)
      if (filename.indexOf("waqr.xaction") != -1) {
        filename = filename.substring(0, filename.indexOf("waqr.xaction")-1);
      }
      try{

        // tell WAQR to save it's state based on the current page
        var saveFuncName = "savePg"+frame.gCtrlr.wiz.currPgNum;
        var func = frame.gCtrlr[saveFuncName];
        if(func != undefined && typeof func == "function"){
          frame.gCtrlr[saveFuncName]();
        } 

        // Find save type
        var saveType = "html"; 
        try{
          saveType = frame.gCtrlr.wiz.previewTypeSelect.value;
        } catch(e){
          //consume and let default go
        }

        // Perform the save
        frame.gCtrlr.repositoryBrowserController.remoteSave(filename, solution, path, saveType, overwrite);
        this.@org.pentaho.mantle.client.commands.SaveCommand::doTabRename()();
        
      } catch(e){
        //TODO: externalize message once a solution to do so is found.
        $wnd.mantle_showMessage("Error","Error encountered while saving: "+e);
      }
    }
  }-*/;

  // used via JSNI
  @SuppressWarnings("unused")
  private void doTabRename() {
    if (tabName != null) { // Save-As does not modify the name of the tab.
      TabWidget tab = SolutionBrowserPerspective.getInstance().getContentTabPanel().getCurrentTab();
      tab.setLabelText(tabName);
      tab.setLabelTooltip(tabName);
    }
  }

}
