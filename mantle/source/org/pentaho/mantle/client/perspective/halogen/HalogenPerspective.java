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
package org.pentaho.mantle.client.perspective.halogen;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;

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
    viewMenuItems.add(new MenuItem(Messages.getString("refresh"), new RefreshPerspectiveCommand(this)));
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
