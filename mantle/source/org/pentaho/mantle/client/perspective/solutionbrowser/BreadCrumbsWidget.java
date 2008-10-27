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
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.List;

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;

public class BreadCrumbsWidget extends HorizontalPanel {

  IBreadCrumbCallback breadCrumbCallback;

  public BreadCrumbsWidget(IBreadCrumbCallback breadCrumbCallback) {
    this.breadCrumbCallback = breadCrumbCallback;
  }

  public void buildBreadCrumbs(String currentSolutionPath, Document solutionDocument) {
    clear();
    FlexTable breadCrumbs = new FlexTable();
    // build top level
    breadCrumbs.setWidget(0, 0, buildBreadCrumb(Messages.getString("pentahoSolutionBrowser"), "/")); //$NON-NLS-1$
    List<String> pathParts = ClassicNavigatorView.getPathParts(currentSolutionPath);
    String myPath = ""; //$NON-NLS-1$
    int i = 1;
    for (String path : pathParts) {
      myPath += path;

      String localizedName = ClassicNavigatorView.getElementByPath(ClassicNavigatorView.getPathParts(myPath), solutionDocument.getDocumentElement()).getAttribute("localized-name"); //$NON-NLS-1$
      
      if (path.startsWith("/")) { //$NON-NLS-1$
        path = path.substring(1);
      }
      // no paths will end with a / (getPathParts trims them)
      breadCrumbs.setWidget(0, i++, buildBreadCrumb(localizedName, myPath));
    }
    add(breadCrumbs);
  }

  private Widget buildBreadCrumb(final String text, final String path) {
    FlexTable breadCrumb = new FlexTable();
    final Image breadCrumbImage = new Image();
    final Label breadCrumbLabel = new Label(text);
    breadCrumbImage.setStyleName("breadCrumbLabel"); //$NON-NLS-1$
    breadCrumbLabel.setStyleName("breadCrumbLabel"); //$NON-NLS-1$
    MantleImages.images.smallFolder().applyTo(breadCrumbImage);
    MouseListener imageHoverListener = new MouseListener() {

      public void onMouseDown(Widget sender, int x, int y) {

      }

      public void onMouseEnter(Widget sender) {
        MantleImages.images.smallFolderHover().applyTo(breadCrumbImage);
        breadCrumbLabel.setStyleName("breadCrumbLabelHover"); //$NON-NLS-1$
        breadCrumbImage.setStyleName("breadCrumbLabelHover"); //$NON-NLS-1$
      }

      public void onMouseLeave(Widget sender) {
        MantleImages.images.smallFolder().applyTo(breadCrumbImage);
        breadCrumbLabel.setStyleName("breadCrumbLabel"); //$NON-NLS-1$
        breadCrumbImage.setStyleName("breadCrumbLabel"); //$NON-NLS-1$
      }

      public void onMouseMove(Widget sender, int x, int y) {
      }

      public void onMouseUp(Widget sender, int x, int y) {
      }

    };
    ClickListener clickListener = new ClickListener() {

      public void onClick(Widget sender) {
        breadCrumbCallback.breadCrumbSelected(path);
      }

    };
    breadCrumbImage.addMouseListener(imageHoverListener);
    breadCrumbLabel.addMouseListener(imageHoverListener);
    breadCrumbImage.addClickListener(clickListener);
    breadCrumbLabel.addClickListener(clickListener);
    breadCrumb.setWidget(0, 0, breadCrumbImage);
    breadCrumb.setWidget(0, 1, breadCrumbLabel);
    return breadCrumb;
  }
}
