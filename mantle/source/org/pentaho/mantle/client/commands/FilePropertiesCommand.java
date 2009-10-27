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

import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TabPanel;

public class FilePropertiesCommand implements Command {

  Tabs defaultTab = Tabs.GENERAL;

  public FilePropertiesCommand() {
  }

  public FilePropertiesCommand(Tabs defaultTab) {
    this.defaultTab = defaultTab;
  }

  public void execute() {
    FileItem item = SolutionBrowserPerspective.getInstance().getFilesListPanel().getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(item, PluginOptionsHelper.getEnabledOptions(item.getName()), SolutionBrowserPerspective
        .getInstance().isAdministrator(), new TabPanel(), null, defaultTab);
    dialog.showTab(defaultTab);
    dialog.center();
  }

  public Tabs getDefaultTab() {
    return defaultTab;
  }

  public void setDefaultTab(Tabs defaultTab) {
    this.defaultTab = defaultTab;
  }
}
