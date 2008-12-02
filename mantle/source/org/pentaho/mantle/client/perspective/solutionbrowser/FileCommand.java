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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.Date;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FileCommand implements Command {

  public static enum COMMAND {
    RUN, EDIT, DELETE, PROPERTIES, BACKGROUND, NEWWINDOW,
    SCHEDULE_CUSTOM, SCHEDULE_NEW, SUBSCRIBE, SHARE, EDIT_ACTION, CREATE_FOLDER
  };

  COMMAND mode = COMMAND.RUN;
  PopupPanel popupMenu;
  IFileItemCallback fileItemCallback;

  public FileCommand(COMMAND inMode, PopupPanel popupMenu, IFileItemCallback fileItemCallback) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.fileItemCallback = fileItemCallback;
  }

  public void execute() {
    if (popupMenu != null) {
      popupMenu.hide();
    }
    if (mode == COMMAND.RUN || mode == COMMAND.BACKGROUND || mode == COMMAND.NEWWINDOW) {
      fileItemCallback.openFile(mode);
    } else if (mode == COMMAND.PROPERTIES) {
      fileItemCallback.loadPropertiesDialog();
    } else if (mode == COMMAND.EDIT) {
      fileItemCallback.editFile();
    } else if (mode == COMMAND.DELETE) {
      fileItemCallback.deleteFile();
    } else if (mode == COMMAND.CREATE_FOLDER) {
      fileItemCallback.createNewFolder();
    } else if (mode == COMMAND.EDIT_ACTION) {
      fileItemCallback.editActionFile();
    } else if (mode == COMMAND.SCHEDULE_NEW) {
      fileItemCallback.createSchedule();
    } else if (mode == COMMAND.SCHEDULE_CUSTOM) {
      Date now = new Date();
      int second = now.getSeconds();
      int minute = now.getMinutes();
      int hour = now.getHours();
      final TextBox cronTextBox = new TextBox();
      cronTextBox.setText(second + " " + minute + " " + hour + " * * ?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
        }

        public void okPressed() {
          TextBox cronTextBox = new TextBox();
          fileItemCallback.createSchedule(cronTextBox.getText());
        }

      };
      PromptDialogBox inputDialog = new PromptDialogBox(Messages.getString("customCRONSchedule"), Messages.getString("schedule"), Messages.getString("cancel"), false, true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      inputDialog.setContent(cronTextBox);
      inputDialog.setCallback(callback);
      inputDialog.center();
    } else if (mode == COMMAND.SHARE) {
      fileItemCallback.shareFile();
    }
  }

  public void setFileItemCallback(IFileItemCallback fileItemCallback) {
    this.fileItemCallback = fileItemCallback;
  }

}