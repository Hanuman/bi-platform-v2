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

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarGroup;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;

/**
 * @author wseyler
 * 
 */
public class BrowserToolbar extends Toolbar {
  protected String FILES_TOOLBAR_STYLE_NAME = "filesPanelToolbar"; //$NON-NLS-1$

  ToolbarButton refreshBtn;

  SolutionBrowserPerspective solutionBrowserPerspective;

  MenuBar miscMenus = new MenuBar(true);

  public BrowserToolbar(SolutionBrowserPerspective solutionBrowserPerspective) {
    super();
    this.solutionBrowserPerspective = solutionBrowserPerspective;

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
    add(new Label(Messages.getInstance().browse()));
    add(GLUE);
    Image refreshImage = new Image();
    MantleImages.images.refresh().applyTo(refreshImage);
    Image refreshDisabledImage = new Image();
    MantleImages.images.runDisabled().applyTo(refreshDisabledImage);
    refreshBtn = new ToolbarButton(refreshImage, refreshDisabledImage);
    refreshBtn.setCommand(new RefreshRepositoryCommand(solutionBrowserPerspective));
    refreshBtn.setToolTip(Messages.getInstance().refresh());
    add(refreshBtn);
  }

}
