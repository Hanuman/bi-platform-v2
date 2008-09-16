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
package org.pentaho.mantle.client.perspective.halogen;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;
import org.pentaho.mantle.client.service.Utility;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.UIObject;

public class HalogenPerspective extends DockPanel implements IPerspective {

  private boolean loaded = false;
  IPerspectiveCallback perspectiveCallback;
  
  public HalogenPerspective(final IPerspectiveCallback perspectiveCallback) {
    this.perspectiveCallback = perspectiveCallback;
    // setup halogen
//    Olap4JServiceAsync olap4JService = (Olap4JServiceAsync) GWT.create(Olap4JService.class);
//    org.pentaho.halogen.client.Messages messages = (org.pentaho.halogen.client.Messages) GWT
//        .create(org.pentaho.halogen.client.Messages.class);
//    ServiceDefTarget endpoint = (ServiceDefTarget) olap4JService;
//    String moduleRelativeURL = "/pentaho/olap4j"; //$NON-NLS-1$
//    endpoint.setServiceEntryPoint(moduleRelativeURL);
//    String guid = Long.toString(System.currentTimeMillis());
//    HalogenTabPanel tabPanel = new HalogenTabPanel(olap4JService, guid, messages);
//    add(tabPanel, DockPanel.CENTER);
  }

  public void installViewMenu(final IPerspectiveCallback perspectiveCallback) {
    List<UIObject> viewMenuItems = new ArrayList<UIObject>();
    viewMenuItems.add(new MenuItem(Messages.getInstance().refresh(), new RefreshPerspectiveCommand(this)));
    perspectiveCallback.installViewMenu(viewMenuItems);
  }
  
  public void loadPerspective(boolean force, boolean showStatus) {
    Utility.enableMouseSelection();
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
    Utility.disableMouseSelection();
  }
}
