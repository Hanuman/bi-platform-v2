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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;

public class SolutionTree extends Tree implements IFileItemCallback {
  PopupPanel popupMenu = new PopupPanel(true);
  boolean showLocalizedFileNames = true;
  boolean showHiddenFiles = false;
  Document solutionDocument;
  boolean isAdministrator = false;
  boolean createRootNode = false;

  public SolutionTree() {
    super(MantleImages.images);
    setAnimationEnabled(true);
    sinkEvents(Event.ONDBLCLICK);
    // popupMenu.setAnimationEnabled(false);
    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;");
    DOM.setElementAttribute(popupMenu.getElement(), "oncontextmenu", "return false;");
    addItem(new TreeItem("Loading..."));
  }

  public void onBrowserEvent(Event event) {
    super.onBrowserEvent(event);
    try {
      if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
        // load menu
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        menuBar.addItem(new MenuItem("Delete", new FileCommand(FileCommand.DELETE, popupMenu, this)));
        menuBar.addSeparator();
        menuBar.addItem(new MenuItem("Properties", new FileCommand(FileCommand.PROPERTIES, popupMenu, this)));
        popupMenu.setWidget(menuBar);
        popupMenu.hide();
        Timer t = new Timer() {
          public void run() {
            popupMenu.show();
          }
        };
        t.schedule(250);
      } else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
        getSelectedItem().setState(!getSelectedItem().getState(), true);
      }
    } catch (Throwable t) {
      // death to this browser event
    }
  }

  public void buildSolutionTree(Document solutionDocument) {
    if (solutionDocument == null) {
      return;
    }
    this.solutionDocument = solutionDocument;
    // remember selectedItem, so we can reselect it after the tree is loaded
    FileTreeItem selectedItem = (FileTreeItem) getSelectedItem();
    clear();
    // get document root item
    Element solutionRoot = solutionDocument.getDocumentElement();
    if (createRootNode) {
      FileTreeItem rootItem = new FileTreeItem();
      rootItem.setText(solutionRoot.getAttribute("path"));
      rootItem.setTitle(solutionRoot.getAttribute("path"));
      // added so we can traverse the true names
      rootItem.setFileName("/");
      addItem(rootItem);
      buildSolutionTree(rootItem, solutionRoot);
    } else {
      buildSolutionTree(null, solutionRoot);
      // sort the root elements
      List<TreeItem> roots = new ArrayList<TreeItem>();
      for (int i = 0; i < getItemCount(); i++) {
        roots.add(getItem(i));
      }
      Collections.sort(roots, new Comparator<TreeItem>() {
        public int compare(TreeItem o1, TreeItem o2) {
          return o1.getText().compareTo(o2.getText());
        }
      });
      clear();
      for (TreeItem myRootItem : roots) {
        addItem(myRootItem);
      }
    }
    if (selectedItem != null) {
      List<FileTreeItem> parents = new ArrayList<FileTreeItem>();
      while (selectedItem != null) {
        parents.add(selectedItem);
        selectedItem = (FileTreeItem) selectedItem.getParentItem();
      }
      Collections.reverse(parents);
      selectFromList(parents);
    } else {
      for (int i = 0; i < getItemCount(); i++) {
        ((FileTreeItem) getItem(i)).setState(true);
      }
    }
  }

  public List<FileTreeItem> getAllNodes() {
    List<FileTreeItem> nodeList = new ArrayList<FileTreeItem>();
    nodeList.add((FileTreeItem) getItem(0));
    getAllNodes((FileTreeItem) getItem(0), nodeList);
    return nodeList;
  }

  private void getAllNodes(FileTreeItem parent, List<FileTreeItem> nodeList) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      FileTreeItem child = (FileTreeItem) parent.getChild(i);
      nodeList.add(child);
      getAllNodes(child, nodeList);
    }
  }

  public FileTreeItem getTreeItem(List<String> pathSegments) {
    // find the tree node whose location matches the pathSegment paths
    FileTreeItem selectedItem = (FileTreeItem) getItem(0);
    for (String segment : pathSegments) {
      for (int i = 0; i < selectedItem.getChildCount(); i++) {
        FileTreeItem item = (FileTreeItem) selectedItem.getChild(i);
        if (segment.equals(item.getFileName())) {
          selectedItem = item;
          break;
        }
      }
    }

    return selectedItem;
  }

  private void selectFromList(List<FileTreeItem> parents) {
    FileTreeItem pathDown = null;
    for (int i = 0; i < parents.size(); i++) {
      FileTreeItem parent = parents.get(i);
      if (pathDown == null) {
        for (int j = 0; j < getItemCount(); j++) {
          FileTreeItem possibleItem = (FileTreeItem) getItem(j);
          if (parent.getFileName().equals(possibleItem.getFileName())) {
            pathDown = possibleItem;
            pathDown.setState(true, true);
            break;
          }
        }
      } else {
        for (int j = 0; j < pathDown.getChildCount(); j++) {
          FileTreeItem possibleItem = (FileTreeItem) pathDown.getChild(j);
          if (parent.getFileName().equals(possibleItem.getFileName())) {
            pathDown = possibleItem;
            pathDown.setState(true, true);
            break;
          }
        }
      }
    }
    if (pathDown != null) {
      setSelectedItem(pathDown);
      pathDown.setState(true, true);
    }
  }

  @SuppressWarnings("unchecked")
  private void buildSolutionTree(FileTreeItem parentTreeItem, Element parentElement) {
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Element childElement = (Element) children.item(i);
      boolean isVisible = "true".equals(childElement.getAttribute("visible"));
      boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory"));
      if (isVisible || showHiddenFiles) {
        String fileName = childElement.getAttribute("name");
        String localizedName = childElement.getAttribute("localized-name");
        FileTreeItem childTreeItem = new FileTreeItem();
        childTreeItem.setURL(childElement.getAttribute("url"));
        if (showLocalizedFileNames) {
          childTreeItem.setText(localizedName);
          childTreeItem.setTitle(fileName);
        } else {
          childTreeItem.setText(fileName);
          childTreeItem.setTitle(localizedName);
        }
        childTreeItem.setFileName(fileName);
        if (parentTreeItem == null && isDirectory) {
          addItem(childTreeItem);
        } else {
          // find the spot in the parentTreeItem to insert the node (based on showLocalizedFileNames)
          if (parentTreeItem.getChildCount() == 0) {
            parentTreeItem.addItem(childTreeItem);
          } else {
            // this does sorting
            boolean inserted = false;
            for (int j = 0; j < parentTreeItem.getChildCount(); j++) {
              FileTreeItem kid = (FileTreeItem) parentTreeItem.getChild(j);
              if (showLocalizedFileNames) {
                if (childTreeItem.getText().compareTo(kid.getText()) <= 0) {
                  // leave all items ahead of the insert point
                  // remove all items between the insert point and the end
                  // add the new item
                  // add back all removed items
                  List<FileTreeItem> removedItems = new ArrayList<FileTreeItem>();
                  for (int x = j; x < parentTreeItem.getChildCount(); x++) {
                    FileTreeItem removedItem = (FileTreeItem) parentTreeItem.getChild(x);
                    removedItems.add(removedItem);
                  }
                  for (FileTreeItem removedItem : removedItems) {
                    parentTreeItem.removeItem(removedItem);
                  }
                  parentTreeItem.addItem(childTreeItem);
                  inserted = true;
                  for (FileTreeItem removedItem : removedItems) {
                    parentTreeItem.addItem(removedItem);
                  }
                  break;
                }
              } else {
                parentTreeItem.addItem(childTreeItem);
                inserted = true;
              }
            }
            if (!inserted) {
              parentTreeItem.addItem(childTreeItem);
            }
          }
        }
        FileTreeItem tmpParent = childTreeItem;
        String pathToChild = tmpParent.getFileName();
        while (tmpParent.getParentItem() != null) {
          tmpParent = (FileTreeItem) tmpParent.getParentItem();
          pathToChild = tmpParent.getFileName() + "/" + pathToChild;
        }

        if (parentTreeItem != null) {
          List<Element> files = (List<Element>) parentTreeItem.getUserObject();
          if (files == null) {
            files = new ArrayList<Element>();
            parentTreeItem.setUserObject(files);
          }
          files.add(childElement);
        }

        if (isDirectory) {
          buildSolutionTree(childTreeItem, childElement);
        } else {
          if (parentTreeItem != null) {
            parentTreeItem.removeItem(childTreeItem);
          }
        }
      }
    }
  }

  public void setShowLocalizedFileNames(boolean showLocalizedFileNames) {
    this.showLocalizedFileNames = showLocalizedFileNames;
    // use existing tree and switch text/title
    for (int i = 0; i < getItemCount(); i++) {
      toggleLocalizedFileNames((FileTreeItem) getItem(i));
    }
  }

  private void toggleLocalizedFileNames(FileTreeItem parentTreeItem) {
    String title = parentTreeItem.getTitle();
    String text = parentTreeItem.getText();
    parentTreeItem.setTitle(text);
    parentTreeItem.setText(title);
    for (int i = 0; i < parentTreeItem.getChildCount(); i++) {
      toggleLocalizedFileNames((FileTreeItem) parentTreeItem.getChild(i));
    }
  }

  public String getSolution() {
    // http://localhost:8080/pentaho/ViewAction?solution=samples&path=reporting&action=JFree_XQuery_report.xaction
    // the solution part of the url
    for (int i = 0; i < getItemCount(); i++) {
      if (getSelectedItem() == getItem(i)) {
        return ((FileTreeItem) getItem(i)).getFileName();
      }
    }
    FileTreeItem tmpParent = (FileTreeItem) getSelectedItem();
    List<FileTreeItem> parents = new ArrayList<FileTreeItem>();
    while (tmpParent != null) {
      parents.add(tmpParent);
      tmpParent = (FileTreeItem) tmpParent.getParentItem();
    }
    // if each solution is a root, then 1st item is solution
    // else solution is 2nd item
    return parents.get(parents.size() - (isCreateRootNode() ? 2 : 1)).getFileName();
  }

  public String getPath() {
    // http://localhost:8080/pentaho/ViewAction?solution=samples&path=reporting&action=JFree_XQuery_report.xaction
    // the path part of the url
    for (int i = 0; i < getItemCount(); i++) {
      if (getSelectedItem() == getItem(i)) {
        return ((FileTreeItem) getItem(i)).getFileName();
      }
    }

    FileTreeItem tmpParent = (FileTreeItem) getSelectedItem();
    List<FileTreeItem> parents = new ArrayList<FileTreeItem>();
    while (tmpParent != null) {
      parents.add(tmpParent);
      tmpParent = (FileTreeItem) tmpParent.getParentItem();
    }
    // if each solution is a root, then 1st item is solution
    // else solution is 2nd item
    // so we start from either of these positions
    String path = "";
    for (int i = parents.size() - (isCreateRootNode() ? 3 : 2); i >= 0; i--) {
      FileTreeItem parent = parents.get(i);
      path += "/" + parent.getFileName();
    }
    return path;
  }

  public FileItem getSelectedFileItem() {
    // TODO Auto-generated method stub
    return null;
  }

  public void loadPropertiesDialog() {
    // brings up permission dialog
    FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    String path = getPath().substring(0, getPath().lastIndexOf("/"));
    FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), showLocalizedFileNames, getSolution(), path, null, null,
        null);
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, isAdministrator, new TabPanel(), null, Tabs.GENERAL);
    dialog.center();
  }

  public void openFile(int mode) {
    // TODO Auto-generated method stub
    // noop
  }

  public void editFile() {
    // TODO Auto-generated method stub
    // noop
  }

  public void createSchedule(String cronExpression) {
    // TODO Auto-generated method stub
    // noop
  }

  public void createSchedule() {
    // TODO Auto-generated method stub
    // noop
  }

  public void setSelectedFileItem(FileItem fileItem) {
    // TODO Auto-generated method stub
    // noop
  }

  public boolean isShowHiddenFiles() {
    return showHiddenFiles;
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    this.showHiddenFiles = showHiddenFiles;
    buildSolutionTree(solutionDocument);
  }

  public boolean isShowLocalizedFileNames() {
    return showLocalizedFileNames;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.IFileItemCallback#shareFile()
   */
  public void shareFile() {
    // TODO Auto-generated method stub
    // noop
  }

  public boolean isCreateRootNode() {
    return createRootNode;
  }

}
