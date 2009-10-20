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
package org.pentaho.mantle.client.solutionbrowser;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

public class TabCommand implements Command {

  public static enum TABCOMMAND { BACK, RELOAD, RELOAD_ALL, CLOSE, CLOSE_ALL, CLOSE_OTHERS, NEW_WINDOW, BOOKMARK, CREATE_DEEP_LINK };
  
  TABCOMMAND mode = TABCOMMAND.RELOAD;
  PopupPanel popupMenu;
  TabWidget tab;
  
  public TabCommand(TABCOMMAND inMode, PopupPanel popupMenu, TabWidget tab) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.tab = tab;
  }

  public void execute() {
    popupMenu.hide();
    if (mode == TABCOMMAND.RELOAD) {
      tab.reloadTab();
    } else if (mode == TABCOMMAND.RELOAD_ALL) {
      tab.reloadAllTabs();
    } else if (mode == TABCOMMAND.CLOSE) {
      tab.closeTab();
    } else if (mode == TABCOMMAND.CLOSE_OTHERS) {
      tab.closeOtherTabs();
    } else if (mode == TABCOMMAND.CLOSE_ALL) {
      tab.closeAllTabs();
    } else if (mode == TABCOMMAND.NEW_WINDOW) {
      tab.openTabInNewWindow();
    } else if (mode == TABCOMMAND.BOOKMARK) {
      tab.bookmark();
    } else if (mode == TABCOMMAND.CREATE_DEEP_LINK) {
      tab.createDeepLink();
    } else if (mode == TABCOMMAND.BACK) {
      tab.back();
    }
  }
}