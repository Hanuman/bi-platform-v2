/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Sep 4, 2008 
 * @author mdamour
 */

package org.pentaho.mantle.client.toolbars;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarToggleButton;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.ToggleWorkspaceCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;

/**
 * @author mdamour
 * 
 */
public class MainToolbar extends Toolbar implements SolutionBrowserListener {
  protected String MAIN_TOOBAR_STYLE_NAME = "mainToolbar"; //$NON-NLS-1$

  private String[] saveTypes = new String[] { ".analysisview.xaction", ".waqr.xaction", "waqr.html" };

  SolutionBrowserPerspective solutionBrowser;
  
  
  ToolbarButton openFileButton;
  ToolbarButton newAnalysisButton;
  ToolbarButton newAdhocButton;
  ToolbarButton printButton;
  ToolbarButton saveButton;
  ToolbarButton saveAsButton;
  ToolbarButton showBrowserToggleButton;
  Image browseShowImage = new Image();
  Image browseHideImage = new Image();
  ToolbarToggleButton workspaceToggleButton;

  public MainToolbar(final SolutionBrowserPerspective solutionBrowser) {
    super();
    this.solutionBrowser = solutionBrowser;
    this.setStylePrimaryName(MAIN_TOOBAR_STYLE_NAME);

    Image openImage = new Image();
    MantleImages.images.open_32().applyTo(openImage);
    openFileButton = new ToolbarButton(openImage);
    openFileButton.setCommand(new OpenFileCommand(solutionBrowser));
    openFileButton.setToolTip("Open...");

    
    Image newAnalysisImage = new Image();
    MantleImages.images.new_analysis_32().applyTo(newAnalysisImage);
    newAnalysisButton = new ToolbarButton(newAnalysisImage);
    newAnalysisButton.setCommand(new AnalysisViewCommand(solutionBrowser));
    newAnalysisButton.setToolTip("New Analysis View...");

    Image newAdhocImage = new Image();
    MantleImages.images.new_report_32().applyTo(newAdhocImage);
    newAdhocButton = new ToolbarButton(newAdhocImage);
    newAdhocButton.setCommand(new WAQRCommand(solutionBrowser));
    newAdhocButton.setToolTip("New Report...");

    
    
    Image printImage = new Image();
    MantleImages.images.print_32().applyTo(printImage);
    Image printDisabledImage = new Image();
    MantleImages.images.print_32_disabled().applyTo(printDisabledImage);
    printButton = new ToolbarButton(printImage, printDisabledImage);
    printButton.setCommand(new PrintCommand(solutionBrowser));
    printButton.setToolTip("Print");
    printButton.setEnabled(false);

    Image saveButtonImage = new Image();
    MantleImages.images.save_32().applyTo(saveButtonImage);
    Image saveDisabledImage = new Image();
    MantleImages.images.save_32_disabled().applyTo(saveDisabledImage);
    saveButton = new ToolbarButton(saveButtonImage, saveDisabledImage);
    saveButton.setCommand(new SaveCommand(solutionBrowser, false));
    saveButton.setToolTip("Save");
    saveButton.setEnabled(false);

    Image saveAsButtonImage = new Image();
    MantleImages.images.saveAs_32().applyTo(saveAsButtonImage);
    Image saveAsDisabledImage = new Image();
    MantleImages.images.saveAs_32_disabled().applyTo(saveAsDisabledImage);
    saveAsButton = new ToolbarButton(saveAsButtonImage, saveAsDisabledImage);
    saveAsButton.setCommand(new SaveCommand(solutionBrowser, true));
    saveAsButton.setToolTip("Save As");
    saveAsButton.setEnabled(false);

    Image toggleWorkspaceImage = new Image();
    MantleImages.images.workspace_32().applyTo(toggleWorkspaceImage);
    Image toggleWorkspaceImageDisabled = saveAsDisabledImage = new Image();
    MantleImages.images.workspace_32().applyTo(toggleWorkspaceImageDisabled);
    workspaceToggleButton = new ToolbarToggleButton(toggleWorkspaceImage, toggleWorkspaceImageDisabled, solutionBrowser.isNavigatorShowing());
    final Command workspaceCmd = new ToggleWorkspaceCommand(solutionBrowser);
    workspaceToggleButton.setCommand(new Command() {
     public void execute() {
         workspaceCmd.execute();
      }  
    });
    toggleWorkspaceButton();
    workspaceToggleButton.setToolTip(Messages.getInstance().workspace());

    MantleImages.images.browser_show_32().applyTo(browseShowImage);
    MantleImages.images.browser_hide_32().applyTo(browseHideImage);
    showBrowserToggleButton = new ToolbarButton(browseShowImage, browseShowImage);
    toggleBrowserButton();
    showBrowserToggleButton.setCommand(new Command() {
      public void execute() {
        Command cmd = new ShowBrowserCommand(solutionBrowser);
        cmd.execute();
        toggleBrowserButton();
      }
    });
    showBrowserToggleButton.setToolTip(Messages.getInstance().toggleSolutionBrowser());

    addSpacer(10);
    add(openFileButton);
    addSpacer(20);
    add(newAdhocButton);
    add(newAnalysisButton);
    addSpacer(20);
    add(saveButton);
    add(saveAsButton);
    addSpacer(20);
    add(printButton);
    addSpacer(20);
    add(workspaceToggleButton);
    add(showBrowserToggleButton);
  }

  public void toggleWorkspaceButton() {
    workspaceToggleButton.setSelected(solutionBrowser.isWorkspaceShowing(), false);
  }
  
  public void toggleBrowserButton() {
    showBrowserToggleButton.setEnabled(false);
    if (solutionBrowser.isNavigatorShowing()) {
      showBrowserToggleButton.setImage(browseHideImage);
    } else {
      showBrowserToggleButton.setImage(browseShowImage);
    }
    showBrowserToggleButton.setEnabled(true);
  }
  
  public void solutionBrowserEvent(String selectedTabURL, FileItem selectedFileItem) {
    toggleBrowserButton();
    toggleWorkspaceButton();
    printButton.setEnabled(selectedTabURL != null && !"".equals(selectedTabURL));
    boolean saveEnabled = false;
    if (selectedTabURL != null) {
      for (String saveType : saveTypes) {
        if (selectedTabURL.toLowerCase().indexOf(saveType) != -1) {
          saveEnabled = true;
        }
      }
    }
    saveButton.setEnabled(saveEnabled);
    saveAsButton.setEnabled(saveEnabled);
  }

}
