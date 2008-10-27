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
import com.google.gwt.user.client.ui.Label;
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
    add(new Label(Messages.getString("files")));
    add(GLUE);
    Image runImage = new Image();
    MantleImages.images.run().applyTo(runImage);
    Image runDisabledImage = new Image();
    MantleImages.images.runDisabled().applyTo(runDisabledImage);
    runBtn = new ToolbarButton(runImage, runDisabledImage);
    runCmd = new FileCommand(FileCommand.COMMAND.RUN, null, callback);
    runBtn.setCommand(runCmd);
    runBtn.setToolTip(Messages.getString("open"));
    add(runBtn);

    Image editImage = new Image();
    MantleImages.images.update().applyTo(editImage);
    Image editDisabledImage = new Image();
    MantleImages.images.updateDisabled().applyTo(editDisabledImage);
    editBtn = new ToolbarButton(editImage, editDisabledImage);
    editCmd = new FileCommand(FileCommand.COMMAND.EDIT, null, callback);
    editBtn.setCommand(editCmd);
    editBtn.setToolTip(Messages.getString("edit"));
    add(editBtn);

    Image miscImage = new Image();
    MantleImages.images.misc().applyTo(miscImage);
    Image miscDisabledImage = new Image();
    MantleImages.images.miscDisabled().applyTo(miscDisabledImage);
    miscComboBtn = new ToolbarComboButton(miscImage, miscDisabledImage);
    MantleServiceCache.getService().repositorySupportsACLS(new AsyncCallback<Boolean>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true);
        dialogBox.center();
        scheduleCmd = new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getString("schedule"), scheduleCmd); //$NON-NLS-1$
        propertiesCmd = new FileCommand(FileCommand.COMMAND.PROPERTIES, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getString("properties"), propertiesCmd); //$NON-NLS-1$
        miscComboBtn.setMenu(miscMenus);
      }

      public void onSuccess(Boolean result) {
        if (result) {
          shareCmd = new FileCommand(FileCommand.COMMAND.SHARE, miscComboBtn.getPopup(), callback);
          miscMenus.addItem(Messages.getString("share"), shareCmd); //$NON-NLS-1$
        }
        scheduleCmd = new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getString("schedule"), scheduleCmd); //$NON-NLS-1$
        propertiesCmd = new FileCommand(FileCommand.COMMAND.PROPERTIES, miscComboBtn.getPopup(), callback);
        miscMenus.addItem(Messages.getString("properties"), propertiesCmd); //$NON-NLS-1$
        miscComboBtn.setMenu(miscMenus);
      }

    });
    miscComboBtn.setToolTip(Messages.getString("options"));
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
