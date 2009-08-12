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
package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasFocus;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class SolutionTree extends Tree implements IFileItemCallback {
  PopupPanel popupMenu = new MantlePopupPanel(true);
  boolean showLocalizedFileNames = true;
  boolean showHiddenFiles = false;
  Document solutionDocument;
  boolean isAdministrator = false;
  boolean createRootNode = false;
  boolean useDescriptionsForTooltip = false;
  
  SolutionBrowserPerspective solutionBrowserPerspective;
  FocusPanel focusable = new FocusPanel();

  public SolutionTree(SolutionBrowserPerspective solutionBrowserPerspective) {
    super(MantleImages.images);
    this.solutionBrowserPerspective = solutionBrowserPerspective;
    setAnimationEnabled(true);
    sinkEvents(Event.ONDBLCLICK);
    // popupMenu.setAnimationEnabled(false);
    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute(popupMenu.getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$
    addItem(new TreeItem(Messages.getString("loadingEllipsis"))); //$NON-NLS-1$

    DOM.setStyleAttribute(focusable.getElement(), "fontSize", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "outline", "0px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "width", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "height", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute(focusable.getElement(), "hideFocus", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setIntStyleAttribute(focusable.getElement(), "zIndex", -1); //$NON-NLS-1$
    DOM.appendChild(getElement(), focusable.getElement());
    DOM.sinkEvents(focusable.getElement(), Event.FOCUSEVENTS);
  }

  public void onBrowserEvent(Event event) {
    int eventType = DOM.eventGetType(event);
    switch (eventType) {
    case Event.ONMOUSEDOWN:
    case Event.ONMOUSEUP:
    case Event.ONCLICK: {
      try {
        int[] scrollOffsets = ElementUtils.calculateScrollOffsets(getElement());
        int[] offsets = ElementUtils.calculateOffsets(getElement());
        DOM.setStyleAttribute(focusable.getElement(), "top", (event.getClientY() + scrollOffsets[1] - offsets[1]) + "px"); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (Exception ex) {
        // wtf!
      }
      break;
    }
    }

    try {
      if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
        // load menu (Note: disabled as Delete and Properties have no meaning for Folders now
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        menuBar.addItem(new MenuItem(Messages.getString("createNewFolderEllipsis"), new FileCommand(FileCommand.COMMAND.CREATE_FOLDER, popupMenu, this)));
        menuBar.addItem(new MenuItem(Messages.getString("delete"), new FileCommand(FileCommand.COMMAND.DELETE, popupMenu, this))); //$NON-NLS-1$
        menuBar.addSeparator();
        menuBar.addItem(new MenuItem(Messages.getString("properties"), new FileCommand(FileCommand.COMMAND.PROPERTIES, popupMenu, this))); //$NON-NLS-1$
        popupMenu.setWidget(menuBar);
        popupMenu.hide();
        Timer t = new Timer() {
          public void run() {
            popupMenu.show();
          }
        };
        t.schedule(250);
        super.onBrowserEvent(event);
        //event.cancelBubble(true);
      } else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
        getSelectedItem().setState(!getSelectedItem().getState(), true);
      } else {
        super.onBrowserEvent(event);
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
      rootItem.setText(solutionRoot.getAttribute("path")); //$NON-NLS-1$
      rootItem.setTitle(solutionRoot.getAttribute("path")); //$NON-NLS-1$
      rootItem.getElement().setId(solutionRoot.getAttribute("path"));
      killAllTextSelection(rootItem.getElement());

      // added so we can traverse the true names
      rootItem.setFileName("/"); //$NON-NLS-1$
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
    for (int i = 0; i < this.getItemCount(); i++) {
      nodeList.add((FileTreeItem) this.getItem(i));
      getAllNodes((FileTreeItem) this.getItem(i), nodeList);
    }
    return nodeList;
  }

  private void getAllNodes(FileTreeItem parent, List<FileTreeItem> nodeList) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      FileTreeItem child = (FileTreeItem) parent.getChild(i);
      nodeList.add(child);
      getAllNodes(child, nodeList);
    }
  }

  /**
   * Checks if the given file name exists in the directory specified by pathSegments
   * 
   * @param pathSegments
   *          List consisting of hierarchial names of directory {a/b/c/example.txt => [a,b,c]}
   * @param pFileName
   *          File name to be looked for in the given directory {a/b/c/example.txt => example.txt}
   * @return True if file exists, false otherwise
   */
  public boolean doesFileExist(final List<String> pathSegments, final String pFileName) {
    // The IF part is to check if we are looking only at the top most level
    // If so then we need to iterate through itemCount
    if (pathSegments.size() == 0) {
      final int itemCount = getItemCount();
      for (int x = 0; x < itemCount; x++) {
        final FileTreeItem selectedItem = (FileTreeItem) getItem(x);
        if (selectedItem.fileName.equalsIgnoreCase(pFileName)) {
          return true;
        }
      }
    } else {
      // If we are here then we are looking for a file inside a sub directory in the solution tree
      // getTreeItem method returns us the directory node we are looking for based on the pathSegments variable
      final FileTreeItem directoryItem = getTreeItem(pathSegments);

      if (directoryItem != null) {
        // Iterate through the directory and check if the name we are searching for exists in
        // the file list of current dir
        final List<Element> filesInCurrDirectory = (List<Element>) directoryItem.getUserObject();
        if (filesInCurrDirectory != null) {
          final int fileListSize = filesInCurrDirectory.size();
          for (int i = 0; i < fileListSize; i++) {
            final Element fileElement = filesInCurrDirectory.get(i);
            final String currentFileName = fileElement.getAttribute("name"); //$NON-NLS-1$
            if ((currentFileName != null) && (currentFileName.equalsIgnoreCase(pFileName))) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public FileTreeItem getTreeItem(List<String> pathSegments) {
    if (pathSegments.size() == 0) {
      return null;
    }
    int segmentIndex = 0;
    for (int x = 0; x < getItemCount(); x++) {
      FileTreeItem item = (FileTreeItem) getItem(x);
      if (item.getFileName().equals(pathSegments.get(segmentIndex))) {
        // we found the correct 'root', now search children
        segmentIndex++;
        for (int i=0;i<item.getChildCount() && segmentIndex < pathSegments.size();i++) {
          FileTreeItem childItem = (FileTreeItem)item.getChild(i);
          if (childItem.getFileName().equals(pathSegments.get(segmentIndex))) {
            item = childItem;
            i=0;
            segmentIndex++;
          }
        }
        // if segmentIndex has reached the end, we have found our match
        if (segmentIndex == pathSegments.size()) {
          return item;
        }
      }
    }
    return null;
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

  @SuppressWarnings("unchecked")//$NON-NLS-1$
  private void buildSolutionTree(FileTreeItem parentTreeItem, Element parentElement) {
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Element childElement = (Element) children.item(i);
      if (childElement == this.focusable) {
        continue;
      }
      boolean isVisible = "true".equals(childElement.getAttribute("visible")); //$NON-NLS-1$ //$NON-NLS-2$
      boolean isDirectory = "true".equals(childElement.getAttribute("isDirectory")); //$NON-NLS-1$ //$NON-NLS-2$
      if (isVisible || showHiddenFiles) {
        String fileName = childElement.getAttribute("name"); //$NON-NLS-1$
        String localizedName = childElement.getAttribute("localized-name"); //$NON-NLS-1$
        String description = childElement.getAttribute("description"); //$NON-NLS-1$
        FileTreeItem childTreeItem = new FileTreeItem();

        String id = null;
        Element parent = childElement;
        while (parent != null) {
          if (StringUtils.isEmpty(parent.getAttribute("name"))) {
            try {
              parent = (Element)parent.getParentNode();
            } catch (Throwable t) {
              parent = null;
            }
            continue;
          }
          if (id != null) {
            id = parent.getAttribute("name") + "/" + id;
          } else {
            id = parent.getAttribute("name");
          }
          if (parent.getParentNode() == null) {
            break;
          }
          try {
            parent = (Element)parent.getParentNode();
          } catch (Throwable t) {
            parent = null;
          }
        }
        childTreeItem.getElement().setAttribute("id", id);
        
        killAllTextSelection(childTreeItem.getElement());
        childTreeItem.setURL(childElement.getAttribute("url")); //$NON-NLS-1$
        if (showLocalizedFileNames) {
          childTreeItem.setText(localizedName);
          if (isUseDescriptionsForTooltip() && !StringUtils.isEmpty(description)) {
            childTreeItem.setTitle(description);
          } else {
            childTreeItem.setTitle(fileName);
          }
        } else {
          childTreeItem.setText(fileName);
          if (isUseDescriptionsForTooltip() && !StringUtils.isEmpty(description)) {
            childTreeItem.setTitle(description);
          } else {
            childTreeItem.setTitle(localizedName);
          }
        }
        childTreeItem.setFileName(fileName);
        if (parentTreeItem == null && isDirectory) {
          addItem(childTreeItem);
        } else {

          try {
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
          } catch (Exception e) { /* Error with FF */
          }
        }
        FileTreeItem tmpParent = childTreeItem;
        String pathToChild = tmpParent.getFileName();
        while (tmpParent.getParentItem() != null) {
          tmpParent = (FileTreeItem) tmpParent.getParentItem();
          pathToChild = tmpParent.getFileName() + "/" + pathToChild; //$NON-NLS-1$
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

    // if we've selected a root level node, we're at the root of a solution, so return "/"
    for (int i = 0; i < getItemCount(); i++) {
      if (getSelectedItem() == getItem(i)) {
        // return ((FileTreeItem) getItem(i)).getFileName();
        return "/"; //$NON-NLS-1$
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
    String path = ""; //$NON-NLS-1$
    for (int i = parents.size() - (isCreateRootNode() ? 3 : 2); i >= 0; i--) {
      FileTreeItem parent = parents.get(i);
      path += "/" + parent.getFileName(); //$NON-NLS-1$
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
    String path = getPath().substring(0, getPath().lastIndexOf("/")); //$NON-NLS-1$
    FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), selectedTreeItem.getText(), getSolution(), path, null, null,
        null, null, false, null);
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, null, isAdministrator, new TabPanel(), null, Tabs.GENERAL);
    dialog.center();
  }

  public void openFile(FileCommand.COMMAND mode) {
    // TODO Auto-generated method stub
    // noop
  }

  public void editFile() {
    // TODO Auto-generated method stub
    // noop
  }

  public void editActionFile() {
    // TODO Auto-generated method stub
    // noop
  }

  public void selectNextItem(FileItem currentItem) {
    // noop
  }

  public void selectPreviousItem(FileItem currentItem) {
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

  public void createNewFolder() {
    NewFolderCommand cmd = new NewFolderCommand(this);
    cmd.execute();
  }

  public void deleteFile() {
    // delete folder
    FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    String path = getPath().substring(0, getPath().lastIndexOf("/")); //$NON-NLS-1$
    final FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), selectedTreeItem.getText(), getSolution(), path, null,
        null, null, null, false, null);
    String repoPath = selectedItem.getPath();
    // if a solution folder is selected then the solution-name/path are the same, we can't allow that
    // but we need them to be in the tree like this for building the tree paths correctly (other code)
    if (repoPath.equals(selectedItem.getSolution())) {
      repoPath = ""; //$NON-NLS-1$
    }
    String url = ""; //$NON-NLS-1$
    if (GWT.isScript()) {
      String windowpath = Window.Location.getPath();
      if (!windowpath.endsWith("/")) { //$NON-NLS-1$
        windowpath = windowpath.substring(0, windowpath.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      url = windowpath + "SolutionRepositoryService?component=delete&solution=" + selectedItem.getSolution() + "&path=" + repoPath + "&name=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + selectedItem.getName();
    } else if (!GWT.isScript()) {
      url = "http://localhost:8080/pentaho/SolutionRepositoryService?component=delete&solution=" + selectedItem.getSolution() + "&path=" //$NON-NLS-1$ //$NON-NLS-2$
          + repoPath + "&name=" + selectedItem.getName(); //$NON-NLS-1$
    }
    final String myurl = url;
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("deleteQuestion", selectedItem.getLocalizedName()))); //$NON-NLS-1$
    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(
        Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, myurl);
        try {
          builder.sendRequest(null, new RequestCallback() {

            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete", selectedItem.getName()), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
            }

            public void onResponseReceived(Request request, Response response) {
              Document resultDoc = (Document) XMLParser.parse((String) (String) response.getText());
              boolean result = "true".equals(resultDoc.getDocumentElement().getFirstChild().getNodeValue()); //$NON-NLS-1$
              if (result) {
                RefreshRepositoryCommand cmd = new RefreshRepositoryCommand(solutionBrowserPerspective);
                cmd.execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                    Messages.getString("couldNotDelete", selectedItem.getName()), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
        }
      }
    };
    deleteConfirmDialog.setCallback(callback);
    deleteConfirmDialog.center();
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

  
  public boolean isUseDescriptionsForTooltip()
  {
    return useDescriptionsForTooltip;
  }

  public void setUseDescriptionsForTooltip(boolean useDescriptionsForTooltip)
  {
    this.useDescriptionsForTooltip = useDescriptionsForTooltip;
    buildSolutionTree(solutionDocument);
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

  HasFocus getFocusableWidget() {
    return this.focusable;
  }

  private void killAllTextSelection(com.google.gwt.dom.client.Element item) {
    ElementUtils.preventTextSelection(item);
    com.google.gwt.dom.client.NodeList<Node> children = item.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      killAllTextSelection((com.google.gwt.dom.client.Element) children.getItem(i));
    }

  }

}
