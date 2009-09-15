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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

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
    } else if (mode == COMMAND.SHARE) {
      fileItemCallback.shareFile();
    }
  }

  public void setFileItemCallback(IFileItemCallback fileItemCallback) {
    this.fileItemCallback = fileItemCallback;
  }

}