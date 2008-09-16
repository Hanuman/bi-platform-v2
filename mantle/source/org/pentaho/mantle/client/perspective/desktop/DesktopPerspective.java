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
package org.pentaho.mantle.client.perspective.desktop;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.UIObject;

public class DesktopPerspective extends DockPanel implements IPerspective {

  private boolean loaded = false;
  IPerspectiveCallback perspectiveCallback;
  
  public DesktopPerspective(final IPerspectiveCallback perspectiveCallback) {
    add(new Label("My Desktop"), DockPanel.CENTER);
    this.perspectiveCallback = perspectiveCallback;
  }

  public void installViewMenu(final IPerspectiveCallback perspectiveCallback) {
    List<UIObject> viewMenuItems = new ArrayList<UIObject>();
    viewMenuItems.add(new MenuItem(Messages.getInstance().refresh(), new RefreshPerspectiveCommand(this)));
    perspectiveCallback.installViewMenu(viewMenuItems);
  }
  
  public void loadPerspective(boolean force, boolean showStatus) {
    // do this once
    if (!loaded || force) {
      // load
      loaded = true;
    }
    installViewMenu(perspectiveCallback);
  }

  public void refreshPerspective(boolean showStatus) {
    loadPerspective(true, showStatus);
  }
  
  public void unloadPerspective() {
  }
  
}
