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
package org.pentaho.mantle.client.perspective.solutionbrowser.classic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosureEvent;
import com.google.gwt.user.client.ui.DisclosureHandler;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

@SuppressWarnings("deprecation")
public class ClassicNavigatorView extends DeckPanel implements IBreadCrumbCallback {

  String currentSolutionPath = ""; //$NON-NLS-1$
  Document solutionDocument;
  Widget oldNavigatorTable = null;

  public ClassicNavigatorView() {
    setWidth("100%"); //$NON-NLS-1$
    setAnimationEnabled(false);
  }

  public void buildSolutionNavigator() {
    // build breadcrumbs for current selection
    // bread crumbs built as a table because of ui flexibility
    BreadCrumbsWidget breadCrumbs = new BreadCrumbsWidget(this);
    breadCrumbs.buildBreadCrumbs(currentSolutionPath, solutionDocument);
    VerticalPanel wrapper = new VerticalPanel();
    wrapper.setWidth("100%"); //$NON-NLS-1$
    wrapper.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

    FlexTable navigatorTable = new FlexTable();
    navigatorTable.setWidth("100%"); //$NON-NLS-1$
    navigatorTable.setWidget(0, 0, breadCrumbs);
    navigatorTable.getCellFormatter().setWidth(0, 0, "100%"); //$NON-NLS-1$
    navigatorTable.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
    navigatorTable.setWidget(1, 0, buildNavigator());
    navigatorTable.getCellFormatter().setWidth(1, 0, "100%"); //$NON-NLS-1$
    navigatorTable.getCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
    wrapper.add(navigatorTable);
    add(wrapper);
    for (int i = 0; i < getWidgetCount(); i++) {
      if (wrapper == getWidget(i)) {
        showWidget(i);
        break;
      }
    }
    if (oldNavigatorTable != null) {
      remove(oldNavigatorTable);
    }
    oldNavigatorTable = wrapper;
  }

  private Widget buildNavigator() {
    Element solutionRoot = solutionDocument.getDocumentElement();
    VerticalPanel navigatorPanel = new VerticalPanel();
    navigatorPanel.setStyleName("classicNavigatorPanel"); //$NON-NLS-1$

    if (currentSolutionPath == null || "".equals(currentSolutionPath) || "/".equals(currentSolutionPath)) { //$NON-NLS-1$ //$NON-NLS-2$
      NodeList children = solutionRoot.getChildNodes();

      HTML solutionBrowserDescription = new HTML(Messages.getString("classicSolutionBrowserDescription"), true); //$NON-NLS-1$
      solutionBrowserDescription.setStyleName("solutionBrowserDescription"); //$NON-NLS-1$
      navigatorPanel.add(solutionBrowserDescription);
      int numSolutions = 0;
      for (int i = 0; i < children.getLength(); i++) {
        boolean isDirectory = "true".equals(((Element) children.item(i)).getAttribute("isDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
        boolean visible = "true".equals(((Element) children.item(i)).getAttribute("visible")); //$NON-NLS-1$ //$NON-NLS-2$
        if (isDirectory && visible) {
          numSolutions++;
        }
      }
      Label solutionBrowseLabel = new Label(Messages.getString("browse") + " " + numSolutions + " " + Messages.getString("solutions")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      solutionBrowseLabel.setStyleName("numSolutionsLabel"); //$NON-NLS-1$
      navigatorPanel.add(solutionBrowseLabel);

      // get solution paths
      for (int i = 0; i < children.getLength(); i++) {
        Element childElement = (Element) children.item(i);
//        String fileName = childElement.getAttribute("name"); //$NON-NLS-1$
        String localizedName = childElement.getAttribute("localized-name"); //$NON-NLS-1$
        boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
        boolean visible = "true".equals(childElement.getAttribute("visible")); //$NON-NLS-1$ //$NON-NLS-2$
        if (isDirectory && visible) {
          final Label solutionDisclosureHeaderLabel = new Label(localizedName);
          final Image solutionDisclosureHeaderImage = new Image();
          final FlexTable solutionDisclosureHeader = new FlexTable();
          solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidget"); //$NON-NLS-1$
          MouseListener headerMouseListener = new MouseListener() {

            public void onMouseDown(Widget sender, int x, int y) {
            }

            public void onMouseEnter(Widget sender) {
              solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidgetHover"); //$NON-NLS-1$
            }

            public void onMouseLeave(Widget sender) {
              solutionDisclosureHeader.setStyleName("solutionDisclosureHeaderWidget"); //$NON-NLS-1$
            }

            public void onMouseMove(Widget sender, int x, int y) {
            }

            public void onMouseUp(Widget sender, int x, int y) {
            }

          };
          solutionDisclosureHeaderLabel.addMouseListener(headerMouseListener);
          solutionDisclosureHeaderImage.addMouseListener(headerMouseListener);
          MantleImages.images.plus().applyTo(solutionDisclosureHeaderImage);

          solutionDisclosureHeader.setWidget(0, 0, solutionDisclosureHeaderImage);
          solutionDisclosureHeader.getCellFormatter().setWidth(0, 0, "12px"); //$NON-NLS-1$
          solutionDisclosureHeader.setWidget(0, 1, solutionDisclosureHeaderLabel);
          solutionDisclosureHeader.getCellFormatter().setWidth(0, 1, "100%"); //$NON-NLS-1$
          solutionDisclosureHeader.setWidth("100%"); //$NON-NLS-1$
          DisclosurePanel solutionDisclosurePanel = new DisclosurePanel(solutionDisclosureHeader);
          solutionDisclosurePanel.setAnimationEnabled(false);
          solutionDisclosurePanel.setStyleName("solutionDisclosureWidget"); //$NON-NLS-1$
          solutionDisclosurePanel.addEventHandler(new DisclosureHandler() {

            public void onClose(DisclosureEvent event) {
              MantleImages.images.plus().applyTo(solutionDisclosureHeaderImage);
            }

            public void onOpen(DisclosureEvent event) {
              MantleImages.images.minus().applyTo(solutionDisclosureHeaderImage);
            }

          });
          solutionDisclosurePanel.setContent(buildFolderContent(childElement));
          solutionDisclosurePanel.setWidth("100%"); //$NON-NLS-1$
          navigatorPanel.add(solutionDisclosurePanel);
        }
      }
    } else {
      // find element representing the folder aimed at currentSolutionPath
      navigatorPanel.add(buildFolderContent(getElementByPath(getPathParts(currentSolutionPath), solutionRoot)));
      navigatorPanel.setWidth("100%"); //$NON-NLS-1$
    }
    return navigatorPanel;
  }

  public static Element getElementByPath(List<String> pathParts, Element parentElement) {
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Element childElement = (Element) children.item(i);
      String fileName = childElement.getAttribute("name"); //$NON-NLS-1$
      String pathPart = pathParts.get(0);
      if (pathPart.startsWith("/")) { //$NON-NLS-1$
        pathPart = pathPart.substring(1);
      }
      if (fileName.equals(pathPart)) {
        pathParts.remove(0);
        if (pathParts.size() > 0) {
          // keep looking
          return getElementByPath(pathParts, childElement);
        } else {
          return childElement;
        }
      }
    }
    return null;
  }

  public static List<String> getPathParts(String fullPath) {
    String currentSolutionPath = "" + fullPath; //$NON-NLS-1$
    int startIndex = currentSolutionPath.indexOf("/"); //$NON-NLS-1$
    int endIndex = currentSolutionPath.indexOf("/", startIndex + 1); //$NON-NLS-1$
    if (endIndex == -1) {
      endIndex = currentSolutionPath.length() - 1;
    }
    List<String> pathParts = new ArrayList<String>();
    while (startIndex != -1 && startIndex != endIndex) {
      String pathPart = currentSolutionPath.substring(startIndex, endIndex + 1);
      if (pathPart.endsWith("/")) { //$NON-NLS-1$
        pathPart = pathPart.substring(0, pathPart.length() - 1);
      }
      pathParts.add(pathPart);
      startIndex = endIndex;
      endIndex = currentSolutionPath.indexOf("/", startIndex + 1); //$NON-NLS-1$
      if (endIndex == -1) {
        endIndex = currentSolutionPath.length() - 1;
      }
    }
    return pathParts;
  }

  private Widget buildFolderContent(final Element parentElement) {
    // two view modes: icons / list
    // do list mode first

    // possibly use a flow panel for icons mode
    FlexTable contentList = new FlexTable();
    contentList.setStyleName("classicNavigatorTable"); //$NON-NLS-1$
    contentList.setWidth("100%"); //$NON-NLS-1$
    NodeList children = parentElement.getChildNodes();
    contentList.setWidget(0, 0, new Label("Name")); //$NON-NLS-1$
    contentList.getCellFormatter().setStyleName(0, 0, "classicNavigatorTableHeader"); //$NON-NLS-1$
    // contentList.setWidget(0, 2, new Label("Author"));

    for (int i = 0; i < children.getLength(); i++) {
      final Element childElement = (Element) children.item(i);
      final String fileName = childElement.getAttribute("name"); //$NON-NLS-1$
      String localizedName = childElement.getAttribute("localized-name"); //$NON-NLS-1$
      String description = childElement.getAttribute("description"); //$NON-NLS-1$
      final boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
      final Image icon = new Image();
      final Label entryLabel = new Label(localizedName);
      final HTML entryDecriptionLabel = new HTML(description);
      FlexTable entryPanel = new FlexTable();
      entryPanel.setWidget(0, 0, entryLabel);
      entryPanel.setWidget(1, 0, entryDecriptionLabel);
      entryPanel.setWidth("100%"); //$NON-NLS-1$

      if (isDirectory) {
        MantleImages.images.folder().applyTo(icon);
      } else {
        MantleImages.images.file().applyTo(icon);
      }
      icon.setStyleName("classicNavigatorFileLabel"); //$NON-NLS-1$
      entryLabel.setStyleName("classicNavigatorFileLabel"); //$NON-NLS-1$
      MouseListener headerMouseListener = new MouseListener() {

        public void onMouseDown(Widget sender, int x, int y) {
        }

        public void onMouseEnter(Widget sender) {
          if (isDirectory) {
            MantleImages.images.folderHover().applyTo(icon);
          } else {
            MantleImages.images.fileHover().applyTo(icon);
          }
          entryLabel.setStyleName("classicNavigatorFileLabelHover"); //$NON-NLS-1$
        }

        public void onMouseLeave(Widget sender) {
          if (isDirectory) {
            MantleImages.images.folder().applyTo(icon);
          } else {
            MantleImages.images.file().applyTo(icon);
          }
          entryLabel.setStyleName("classicNavigatorFileLabel"); //$NON-NLS-1$
        }

        public void onMouseMove(Widget sender, int x, int y) {
        }

        public void onMouseUp(Widget sender, int x, int y) {
        }

      };
      ClickListener clickListener = new ClickListener() {
        public void onClick(Widget sender) {
          if (isDirectory) {
            breadCrumbSelected(buildPathForElement(childElement));
          } else {
            // load xaction or .url
            // get the default URL for this
        	  String url = childElement.getAttribute("url"); //$NON-NLS-1$
            if (fileName.endsWith(".xaction")) { //$NON-NLS-1$
              String fullPath = buildPathForElement(childElement);
              List<String> pathParts = ClassicNavigatorView.getPathParts(fullPath);
              String solution = pathParts.get(0);
              if (solution.startsWith("/")) { //$NON-NLS-1$
                solution = solution.substring(1);
              }
              String path = ""; //$NON-NLS-1$
              for (int i = 1; i < pathParts.size() - 1; i++) {
                path += pathParts.get(i);
              }
              if (path.startsWith("/")) { //$NON-NLS-1$
                path = path.substring(1);
              }
              url = "ViewAction?solution=" + solution + "&path=" + path + "&action=" + fileName; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            Window.open(url, "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }

      };
      icon.addMouseListener(headerMouseListener);
      entryLabel.addMouseListener(headerMouseListener);
      icon.addClickListener(clickListener);
      entryLabel.addClickListener(clickListener);

      FlexTable rowPanel = new FlexTable();
      rowPanel.setWidget(0, 0, icon);
      rowPanel.setWidget(0, 1, entryPanel);

      contentList.setWidget(i + 1, 0, rowPanel);
      contentList.getCellFormatter().setStyleName(i + 1, 0, "classicNavigatorTableCell"); //$NON-NLS-1$
      contentList.getCellFormatter().setWidth(i + 1, 0, "100%"); //$NON-NLS-1$
    }
    return contentList;
  }

  private String buildPathForElement(Element element) {
    List<String> parents = new ArrayList<String>();
    while (element != null) {
      String name = element.getAttribute("name"); //$NON-NLS-1$
      if (name != null && !"".equalsIgnoreCase(name)) { //$NON-NLS-1$
        parents.add(name);
      }
      Object tmpParent = element.getParentNode();
      if (tmpParent instanceof Element) {
        element = (Element) tmpParent;
      } else {
        element = null;
      }
    }
    Collections.reverse(parents);
    String pathForElement = ""; //$NON-NLS-1$
    for (String pathPart : parents) {
      pathForElement += "/" + pathPart; //$NON-NLS-1$
    }
    return pathForElement;
  }

  public void breadCrumbSelected(String path) {
    currentSolutionPath = path;
    buildSolutionNavigator();
  }

  public void setSolutionDocument(Document solutionDocument) {
    this.solutionDocument = solutionDocument;
  }

}
