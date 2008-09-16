/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
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
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.perspective.solutionbrowser;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

public class TabCommand implements Command {
  public static final int RELOAD = 0;
  public static final int RELOAD_ALL = 1;
  public static final int CLOSE = 2;
  public static final int CLOSE_OTHERS = 3;
  public static final int CLOSE_ALL = 4;
  public static final int NEW_WINDOW = 5;
  public static final int BOOKMARK = 6;

  int mode = RELOAD;
  PopupPanel popupMenu;
  TabWidget tab;
  
  public TabCommand(int inMode, PopupPanel popupMenu, TabWidget tab) {
    this.mode = inMode;
    this.popupMenu = popupMenu;
    this.tab = tab;
  }

  public void execute() {
    popupMenu.hide();
    if (mode == RELOAD) {
      tab.reloadTab();
    } else if (mode == RELOAD_ALL) {
      tab.reloadAllTabs();
    } else if (mode == CLOSE) {
      tab.closeTab();
    } else if (mode == CLOSE_OTHERS) {
      tab.closeOtherTabs();
    } else if (mode == CLOSE_ALL) {
      tab.closeAllTabs();
    } else if (mode == NEW_WINDOW) {
      tab.openTabInNewWindow();
    } else if (mode == BOOKMARK) {
      tab.bookmark();
    }
  }
}