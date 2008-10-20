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
 * @created Aug 20, 2008 
 * @author wseyler
 */

package org.pentaho.mantle.client.perspective.solutionbrowser.toolbars;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarComboButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarGroup;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand.COMMAND;
import org.pentaho.mantle.client.perspective.solutionbrowser.events.IFileSelectionChangedListener;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuBar;

/**
 * @author wseyler
 * 
 */
public class FilesToolbar extends Toolbar implements IFileSelectionChangedListener {
  protected String FILES_TOOLBAR_STYLE_NAME = "filesPanelToolbar"; //$NON-NLS-1$
  protected String FILE_GROUP_STYLE_NAME = "filesToolbarGroup"; //$NON-NLS-1$

  ToolbarComboButton miscComboBtn;
  ToolbarButton runBtn, editBtn;
  FileCommand runCmd, editCmd, shareCmd, scheduleCmd, deleteCmd, propertiesCmd;

  IFileItemCallback callback;

  MenuBar miscMenus = new MenuBar(true);

  public FilesToolbar(IFileItemCallback fileItemCallback) {
    super();
    this.callback = fileItemCallback;

    // Formatting stuff
    setHorizontalAlignment(ALIGN_RIGHT);
    addStyleName(FILES_TOOLBAR_STYLE_NAME);
    setSize("100%", "29px"); //$NON-NLS-1$//$NON-NLS-2$

    createMenus();
  }

  /**
   * 
   */
  private void createMenus() {
    addSpacer(5);
    add(new FilesToolbarGroup(Messages.getInstance().files()));
    add(GLUE);
    Image runImage = new Image();
    MantleImages.images.run().applyTo(runImage);
    Image runDisabledImage = new Image();
    MantleImages.images.runDisabled().applyTo(runDisabledImage);
    runBtn = new ToolbarButton(runImage, runDisabledImage);
    runCmd = new FileCommand(FileCommand.COMMAND.RUN, null, callback);
    runBtn.setCommand(runCmd);
    runBtn.setToolTip(Messages.getInstance().open());
    add(runBtn);

    Image editImage = new Image();
    MantleImages.images.update().applyTo(editImage);
    Image editDisabledImage = new Image();
    MantleImages.images.updateDisabled().applyTo(editDisabledImage);
    editBtn = new ToolbarButton(editImage, editDisabledImage);
    editCmd = new FileCommand(FileCommand.COMMAND.EDIT, null, callback);
    editBtn.setCommand(editCmd);
    editBtn.setToolTip(Messages.getInstance().edit());
    add(editBtn);

    Image miscImage = new Image();
    MantleImages.images.misc().applyTo(miscImage);
    Image miscDisabledImage = new Image();
    MantleImages.images.miscDisabled().applyTo(miscDisabledImage);
    miscComboBtn = new ToolbarComboButton(miscImage, miscDisabledImage);
    MantleServiceCache.getService().repositorySupportsACLS(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), caught.toString(), false, false, true);
        dialogBox.center();
        scheduleCmd = new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getInstance().schedule(), scheduleCmd); //$NON-NLS-1$
        propertiesCmd = new FileCommand(FileCommand.COMMAND.PROPERTIES, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getInstance().properties(), propertiesCmd); //$NON-NLS-1$
        miscComboBtn.setMenu(miscMenus);
      }

      public void onSuccess(Boolean result) {
        if (result) {
          shareCmd = new FileCommand(FileCommand.COMMAND.SHARE, miscComboBtn.getPopup(), callback);
          miscMenus.addItem(Messages.getInstance().share(), shareCmd); //$NON-NLS-1$
        }
        scheduleCmd = new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getInstance().schedule(), scheduleCmd); //$NON-NLS-1$
        propertiesCmd = new FileCommand(FileCommand.COMMAND.PROPERTIES, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getInstance().properties(), propertiesCmd); //$NON-NLS-1$
        miscComboBtn.setMenu(miscMenus);
      }

    });
    miscComboBtn.setToolTip(Messages.getInstance().options());
    add(miscComboBtn);

    setEnabled(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.events.IFileSelectionChangedListener#fileSelectionChanged(org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback)
   */
  public void fileSelectionChanged(IFileItemCallback callback) {
    updateMenus(callback.getSelectedFileItem());
  }

  /**
   * @param selectedFileItem
   */
  private void updateMenus(FileItem selectedFileItem) {
    setEnabled(selectedFileItem != null);
    // only allow edit on waqr
    editBtn.setEnabled(selectedFileItem != null && selectedFileItem.getName().endsWith(".waqr.xaction")); //$NON-NLS-1$
  }
  
  /**
   * @author wseyler
   *
   */
public class FilesToolbarGroup extends ToolbarGroup {
    public FilesToolbarGroup(String groupName) {
      super(groupName);
    }

    /**
     * Changes the enabled status of the group. If enabled is false, the buttons will be disabled.
     * If enabled is true, it will consult the buttons for their current enabled state.
     * 
     * @param enabled boolena flag
     */
    public void setEnabled(boolean enabled){
      super.setEnabled(true);
    }
    

    public void setTempDisabled(boolean disable) {
      super.setTempDisabled(false);
    }
  }
}
