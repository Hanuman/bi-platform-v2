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
package org.pentaho.mantle.client.solutionbrowser.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.ISolutionDocumentListener;
import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.SolutionDocumentManager;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.solutionbrowser.fileproperties.FilePropertiesDialog.Tabs;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Focusable;
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

public class SolutionTree extends Tree implements ISolutionDocumentListener, IUserSettingsListener {
  private boolean showLocalizedFileNames = true;
  private boolean showHiddenFiles = false;
  private Document solutionDocument;
  private boolean isAdministrator = false;
  private boolean createRootNode = false;
  private boolean useDescriptionsForTooltip = false;

  private FileTreeItem selectedItem = null;

  FocusPanel focusable = new FocusPanel();

  public SolutionTree() {
    super(MantleImages.images);
    setAnimationEnabled(true);
    sinkEvents(Event.ONDBLCLICK);
    // popupMenu.setAnimationEnabled(false);
    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$

    DOM.setStyleAttribute(focusable.getElement(), "fontSize", "0"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "outline", "0px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "width", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setStyleAttribute(focusable.getElement(), "height", "1px"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute(focusable.getElement(), "hideFocus", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setIntStyleAttribute(focusable.getElement(), "zIndex", -1); //$NON-NLS-1$
    DOM.appendChild(getElement(), focusable.getElement());
    DOM.sinkEvents(focusable.getElement(), Event.FOCUSEVENTS);

    // By default, expanding a node does not select it. Add that in here
    this.addOpenHandler(new OpenHandler<TreeItem>() {
      public void onOpen(OpenEvent<TreeItem> event) {
        SolutionTree.this.setSelectedItem(event.getTarget());
      }
    });

    getElement().setId("solutionTree");
    getElement().getStyle().setProperty("marginTop", "29px"); //$NON-NLS-1$ //$NON-NLS-2$

    SolutionDocumentManager.getInstance().addSolutionDocumentListener(this);
    UserSettingsManager.getInstance().addUserSettingsListener(this);
  }

  public void onFetchUserSettings(ArrayList<IUserSetting> settings) {
    if (settings == null) {
      return;
    }

    for (IUserSetting setting : settings) {
      if (IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals(setting.getSettingName())) {
        boolean showLocalizedFileNames = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setShowLocalizedFileNames(showLocalizedFileNames);
      } else if (IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals(setting.getSettingName())) {
        boolean useDescriptions = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setUseDescriptionsForTooltip(useDescriptions);
      } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getSettingName())) {
        boolean showHiddenFiles = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
        setShowHiddenFiles(showHiddenFiles);
      }
    }

    SolutionBrowserPerspective.getInstance().updateViewMenu();
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
      } catch (Exception ignored) {
        // ignore any exceptions fired by this. Most likely a result of the element
        // not being on the DOM
      }
      break;
    }
    }

    try {
      if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
        // load menu (Note: disabled as Delete and Properties have no meaning for Folders now
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        final PopupPanel popupMenu = MantlePopupPanel.getInstance(true);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        menuBar.addItem(new MenuItem(Messages.getString("createNewFolderEllipsis"), new FileCommand(FileCommand.COMMAND.CREATE_FOLDER, popupMenu)));
        menuBar.addItem(new MenuItem(Messages.getString("delete"), new FileCommand(FileCommand.COMMAND.DELETE, popupMenu))); //$NON-NLS-1$
        menuBar.addSeparator();
        menuBar.addItem(new MenuItem(Messages.getString("properties"), new FileCommand(FileCommand.COMMAND.PROPERTIES, popupMenu))); //$NON-NLS-1$
        popupMenu.setWidget(menuBar);
        popupMenu.hide();
        popupMenu.show();
      } else if (DOM.eventGetType(event) == Event.ONDBLCLICK) {
        getSelectedItem().setState(!getSelectedItem().getState(), true);
      } else {
        super.onBrowserEvent(event);
      }
    } catch (Throwable t) {
      // death to this browser event
    }
    TreeItem selItem = getSelectedItem();
    if (selItem != null) {
      DOM.scrollIntoView(selItem.getElement());
    }
  }

  public void beforeFetchSolutionDocument() {
    if (getSelectedItem() != null) {
      selectedItem = (FileTreeItem) getSelectedItem();
    }
    clear();
    addItem(new TreeItem(Messages.getString("loadingEllipsis"))); //$NON-NLS-1$
  }

  public void onFetchSolutionDocument(Document solutionDocument) {
    if (solutionDocument == null) {
      return;
    }
    this.solutionDocument = solutionDocument;
    // remember selectedItem, so we can reselect it after the tree is loaded
    clear();
    // get document root item
    Element solutionRoot = solutionDocument.getDocumentElement();
    if (createRootNode) {
      FileTreeItem rootItem = new FileTreeItem();
      rootItem.setText(solutionRoot.getAttribute("path")); //$NON-NLS-1$
      rootItem.setTitle(solutionRoot.getAttribute("path")); //$NON-NLS-1$
      rootItem.getElement().setId(solutionRoot.getAttribute("path"));
      ElementUtils.killAllTextSelection(rootItem.getElement());

      // added so we can traverse the true names
      rootItem.setFileName("/"); //$NON-NLS-1$
      addItem(rootItem);
      buildSolutionTree(rootItem, solutionRoot);
    } else {
      buildSolutionTree(null, solutionRoot);
      // sort the root elements
      ArrayList<TreeItem> roots = new ArrayList<TreeItem>();
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
      ArrayList<FileTreeItem> parents = new ArrayList<FileTreeItem>();
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

  public ArrayList<FileTreeItem> getAllNodes() {
    ArrayList<FileTreeItem> nodeList = new ArrayList<FileTreeItem>();
    for (int i = 0; i < this.getItemCount(); i++) {
      nodeList.add((FileTreeItem) this.getItem(i));
      getAllNodes((FileTreeItem) this.getItem(i), nodeList);
    }
    return nodeList;
  }

  private void getAllNodes(FileTreeItem parent, ArrayList<FileTreeItem> nodeList) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      FileTreeItem child = (FileTreeItem) parent.getChild(i);
      nodeList.add(child);
      getAllNodes(child, nodeList);
    }
  }

  public ArrayList<String> getPathSegments(String path) {
    ArrayList<String> pathSegments = new ArrayList<String>();
    if (path != null) {
      if (path.startsWith("/")) { //$NON-NLS-1$
        path = path.substring(1);
      }
      StringTokenizer st = new StringTokenizer(path, '/');
      for (int i = 0; i < st.countTokens(); i++) {
        pathSegments.add(st.tokenAt(i));
      }
    }
    return pathSegments;
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
  public boolean doesFileExist(final ArrayList<String> pathSegments, final String pFileName) {
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
        final ArrayList<Element> filesInCurrDirectory = (ArrayList<Element>) directoryItem.getUserObject();
        if (filesInCurrDirectory != null) {
          for (Element fileElement : filesInCurrDirectory) {
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

  public FileTreeItem getTreeItem(final ArrayList<String> pathSegments) {
    if (pathSegments.size() > 0) {
      // the first path segment is going to be a 'root' in the tree
      String rootSegment = pathSegments.get(0);
      for (int i = 0; i < getItemCount(); i++) {
        FileTreeItem root = (FileTreeItem) getItem(i);
        if (root.getFileName().equalsIgnoreCase(rootSegment)) {
          ArrayList<String> tmpPathSegs = (ArrayList<String>) pathSegments.clone();
          tmpPathSegs.remove(0);
          return getTreeItem(root, tmpPathSegs);
        }
      }
    }
    return null;
  }

  private FileTreeItem getTreeItem(final FileTreeItem root, final ArrayList<String> pathSegments) {
    int depth = 0;
    FileTreeItem currentItem = root;
    while (depth < pathSegments.size()) {
      String pathSegment = pathSegments.get(depth);
      for (int i = 0; i < currentItem.getChildCount(); i++) {
        FileTreeItem childItem = (FileTreeItem) currentItem.getChild(i);
        if (childItem.getFileName().equalsIgnoreCase(pathSegment)) {
          currentItem = childItem;
        }
      }
      depth++;
    }
    // let's check if the currentItem matches our segments (it might point to the last item before
    // we eventually failed to find the complete match)
    FileTreeItem tmpItem = currentItem;
    depth = pathSegments.size()-1;
    while (tmpItem != null && depth >= 0) {
      if (tmpItem.getFileName().equalsIgnoreCase(pathSegments.get(depth))) {
        tmpItem = (FileTreeItem) tmpItem.getParentItem();
        depth--;
      } else {
        // every item must match
        return null;
      }
    }

    return currentItem;
  }

  private void selectFromList(ArrayList<FileTreeItem> parents) {
    FileTreeItem pathDown = null;
    for (int i = 0; i < parents.size(); i++) {
      FileTreeItem parent = parents.get(i);
      if (pathDown == null) {
        for (int j = 0; j < getItemCount(); j++) {
          FileTreeItem possibleItem = (FileTreeItem) getItem(j);
          if (parent.getFileName().equals(possibleItem.getFileName())) {
            pathDown = possibleItem;
            pathDown.setState(true, true);
            pathDown.setSelected(true);
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
              parent = (Element) parent.getParentNode();
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
            parent = (Element) parent.getParentNode();
          } catch (Throwable t) {
            parent = null;
          }
        }
        childTreeItem.getElement().setAttribute("id", id);

        ElementUtils.killAllTextSelection(childTreeItem.getElement());
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
                    ArrayList<FileTreeItem> removedItems = new ArrayList<FileTreeItem>();
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
          ArrayList<Element> files = (ArrayList<Element>) parentTreeItem.getUserObject();
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
    ArrayList<FileTreeItem> parents = new ArrayList<FileTreeItem>();
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
    ArrayList<FileTreeItem> parents = new ArrayList<FileTreeItem>();
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

  public void loadPropertiesDialog() {
    // brings up permission dialog
    FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    String path = getPath().substring(0, getPath().lastIndexOf("/")); //$NON-NLS-1$
    FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), selectedTreeItem.getText(), getSolution(), path, null,
        null, null, null, false, null);
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, null, isAdministrator, new TabPanel(), null, Tabs.GENERAL);
    dialog.center();
  }

  public void createNewFolder() {
    NewFolderCommand cmd = new NewFolderCommand();
    cmd.execute();
  }

  public void deleteFile() {
    // delete folder
    FileTreeItem selectedTreeItem = (FileTreeItem) getSelectedItem();
    String path = getPath().substring(0, getPath().lastIndexOf("/")); //$NON-NLS-1$
    final FileItem selectedItem = new FileItem(selectedTreeItem.getFileName(), selectedTreeItem.getText(), selectedTreeItem.getText(), getSolution(), path,
        null, null, null, null, false, null);
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
                RefreshRepositoryCommand cmd = new RefreshRepositoryCommand();
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
    onFetchSolutionDocument(solutionDocument);
  }

  public boolean isShowLocalizedFileNames() {
    return showLocalizedFileNames;
  }

  public boolean isUseDescriptionsForTooltip() {
    return useDescriptionsForTooltip;
  }

  public void setUseDescriptionsForTooltip(boolean useDescriptionsForTooltip) {
    this.useDescriptionsForTooltip = useDescriptionsForTooltip;
    onFetchSolutionDocument(solutionDocument);
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
  }

  public boolean isCreateRootNode() {
    return createRootNode;
  }

  Focusable getFocusable() {
    return this.focusable;
  }

}
