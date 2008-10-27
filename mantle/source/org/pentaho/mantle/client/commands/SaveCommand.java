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
import org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.perspective.solutionbrowser.TabWidget;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;

public class SaveCommand implements Command {

  SolutionBrowserPerspective navigatorPerspective;
  boolean isSaveAs = false;

  private String name;
  private String solution;
  private String path;
  private SolutionFileInfo.Type type;

  public SaveCommand(SolutionBrowserPerspective navigatorPerspective, boolean isSaveAs) {
    this.navigatorPerspective = navigatorPerspective;
    this.isSaveAs = isSaveAs;
  }

  public void execute() {

    retrieveCachedValues(navigatorPerspective.getCurrentFrame());
    
    if (isSaveAs || name == null) {
      final FileChooserDialog dialog = new FileChooserDialog(FileChooserMode.SAVE, "/", navigatorPerspective.getSolutionDocument(), false, true); //$NON-NLS-1$
      if (isSaveAs) {
        dialog.setTitle(Messages.getString("saveAs"));
        dialog.setText(Messages.getString("saveAs"));
      } else {
        dialog.setTitle(Messages.getString("save"));
      }

      if (!MantleApplication.showAdvancedFeatures) {
        dialog.setShowSearch(false);
      }
      dialog.addFileChooserListener(new FileChooserListener() {

        public void fileSelected(final String solution, final String path, final String name, String localizedFileName) {
          setSolution(solution);
          setPath(path);
          setName(name);
          setType(SolutionFileInfo.Type.XACTION); //$NON-NLS-1$

          TabWidget tab = navigatorPerspective.getCurrentTab();
          tab.setLabelText(name);
          tab.setLabelTooltip(name);

          if (false) {// if (dialog.doesSelectedFileExist()) {
            dialog.hide();
            PromptDialogBox overWriteDialog = new PromptDialogBox(Messages.getString("question"), Messages.getString("yes"), Messages.getString("no"),
                false, true);
            overWriteDialog.setContent(new Label(Messages.getString("fileExistsOverwrite"), false));
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
            persistFileInfoInFrame();
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

  private void persistFileInfoInFrame(){
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName(this.name);
    fileInfo.setPath(this.path);
    fileInfo.setSolution(this.solution);
    fileInfo.setType(this.type);
    navigatorPerspective.getCurrentFrame().setFileInfo(fileInfo);
  }

  private void clearValues() {
    name = null;
    solution = null;
    path = null;
    type = null;
  }

  private void retrieveCachedValues(ReloadableIFrameTabPanel tabPanel){
    SolutionFileInfo info = tabPanel.getFileInfo();
    if(info != null){
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
  public static native void doSaveAs(String elementId, String filename, String solution, String path, SolutionFileInfo.Type type, boolean overwrite) 
  /*-{
    var frame = $doc.getElementById(elementId);
    frame = frame.contentWindow;
    frame.focus();                                
                
    if(frame.pivot_initialized) {
      // do jpivot save
      if (filename.indexOf("analysisview.xaction") == -1) {
        frame.myFilename = filename + ".analysisview.xaction";
      }
      frame.controller.saveAs(frame.myFilename, filename, solution, path, overwrite);
    } else {
      frame.gCtrlr.repositoryBrowserController.remoteSave(frame.myFilename, solution, path, "html", overwrite);
    }
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

  public SolutionFileInfo.Type getType() {

    return type;
  }

  public void setType(SolutionFileInfo.Type type) {

    this.type = type;
  }

}
