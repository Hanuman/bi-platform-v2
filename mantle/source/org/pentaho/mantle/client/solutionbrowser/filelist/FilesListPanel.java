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
 * @created Aug 20, 2008 
 * @author wseyler
 */
package org.pentaho.mantle.client.solutionbrowser.filelist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.dialogs.FileDialog;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.SolutionDocumentManager;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper.ContentTypePlugin;
import org.pentaho.mantle.client.solutionbrowser.toolbars.FilesToolbar;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;

/**
 * @author wseyler
 * 
 */
public class FilesListPanel extends FlowPanel {
  protected String FILES_LABEL_STYLE_NAME = "filesPanelMenuLabel"; //$NON-NLS-1$

  private FlexTable filesList = new FlexTable();
  private FilesToolbar toolbar;
  private FileItem selectedFileItem;

  public FilesListPanel() {
    super();
    // Create the toolbar
    toolbar = new FilesToolbar();
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.add(toolbar);
    toolbarWrapper.setStyleName("files-toolbar"); //$NON-NLS-1$
    add(toolbarWrapper);

    SimplePanel filesListWrapper = new SimplePanel();
    FocusPanel fp = new FocusPanel(filesList) {
      public void onBrowserEvent(Event event) {
        if ((DOM.eventGetType(event) & Event.ONKEYDOWN) == Event.ONKEYDOWN) {
          if (event.getKeyCode() == KeyCodes.KEY_UP) {
            selectPreviousItem(selectedFileItem);
          } else if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
            selectNextItem(selectedFileItem);
          } else if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
            SolutionBrowserPerspective.getInstance().openFile(FileCommand.COMMAND.RUN);
          }
        }
        super.onBrowserEvent(event);
      }
    };
    filesList.setWidth("100%");
    fp.sinkEvents(Event.KEYEVENTS);

    filesListWrapper.add(fp);
    fp.getElement().getStyle().setProperty("marginTop", "29px"); //$NON-NLS-1$ //$NON-NLS-2$
    filesListWrapper.setStyleName("files-list-panel"); //$NON-NLS-1$
    add(filesListWrapper);

    setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  
    setWidth("100%"); //$NON-NLS-1$

    getElement().setId("filesListPanel");

    setupNativeHooks(this);
  }

  @SuppressWarnings("unused")
  private void showOpenFileDialog(final JavaScriptObject callback, final String path, final String title, final String okText, final String fileTypes) {
    SolutionDocumentManager.getInstance().fetchSolutionDocument(new AsyncCallback<Document>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Document result) {
        FileDialog dialog = new FileDialog(result, path, title, okText, fileTypes.split(","));
        dialog.addFileChooserListener(new FileChooserListener() {

          public void fileSelected(String solution, String path, String name, String localizedFileName) {
            notifyOpenFileCallback(callback, solution, path, name, localizedFileName);
          }

          public void fileSelectionChanged(String solution, String path, String name) {
          }

        });
        dialog.show();
      }
    }, false);
  }

  private native void notifyOpenFileCallback(JavaScriptObject obj, String solution, String path, String name, String localizedFileName)
  /*-{
    obj.fileSelected(solution, path, name, localizedFileName);
  }-*/;

  private static native void setupNativeHooks(FilesListPanel filesListPanel)
  /*-{
    $wnd.openFileDialog = function(callback,title, okText, fileTypes) { 
      filesListPanel.@org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, null, title, okText, fileTypes);      
    }
    $wnd.openFileDialogWithPath = function(callback, path, title, okText, fileTypes) { 
      filesListPanel.@org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, path, title, okText, fileTypes);      
    }
  }-*/;

  @SuppressWarnings("unchecked")
  public void populateFilesList(SolutionBrowserPerspective perspective, SolutionTree solutionTree, TreeItem item) {
    filesList.clear();
    ArrayList<Element> files = (ArrayList<Element>) item.getUserObject();
    // let's sort this list based on localized name
    Collections.sort(files, new Comparator<Element>() {
      public int compare(Element o1, Element o2) {
        String name1 = o1.getAttribute("localized-name"); //$NON-NLS-1$
        String name2 = o2.getAttribute("localized-name"); //$NON-NLS-1$
        return name1.compareTo(name2);
      }
    });
    if (files != null) {
      int rowCounter = 0;
      for (int i = 0; i < files.size(); i++) {
        Element fileElement = files.get(i);
        if ("false".equals(fileElement.getAttribute("isDirectory"))) { //$NON-NLS-1$ //$NON-NLS-2$
          String name = fileElement.getAttribute("name"); //$NON-NLS-1$
          String solution = solutionTree.getSolution();
          String path = solutionTree.getPath();
          String lastModifiedDateStr = fileElement.getAttribute("lastModifiedDate"); //$NON-NLS-1$
          String url = fileElement.getAttribute("url"); //$NON-NLS-1$
          ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin(name);
          String icon = null;
          if (plugin != null) {
            icon = plugin.getFileIcon();
          }
          String localizedName = fileElement.getAttribute("localized-name");
          String description = fileElement.getAttribute("description");
          String tooltip = localizedName;
          if (solutionTree.isUseDescriptionsForTooltip() && !StringUtils.isEmpty(description)) {
            tooltip = description;
          }
          final FileItem fileLabel = new FileItem(name, localizedName, tooltip, solution, path, //$NON-NLS-1$
              lastModifiedDateStr, url, this, PluginOptionsHelper.getEnabledOptions(name), toolbar.getSupportsACLs(), icon);
          // BISERVER-2317: Request for more IDs for Mantle UI elements
          // set element id as the filename
          fileLabel.getElement().setId("file-" + name); //$NON-NLS-1$
          fileLabel.addFileSelectionChangedListener(toolbar);
          fileLabel.setWidth("100%"); //$NON-NLS-1$
          filesList.setWidget(rowCounter++, 0, fileLabel);

          if (selectedFileItem != null && selectedFileItem.getFullPath().equals(fileLabel.getFullPath())) {
            fileLabel.setStyleName("fileLabelSelected"); //$NON-NLS-1$
            selectedFileItem = fileLabel;
          }
        }
      }
    }
  }

  public void deselect() {
    for (int i = 0; i < filesList.getRowCount(); i++) {
      FileItem item = (FileItem) filesList.getWidget(i, 0);
      item.setStyleName("fileLabel"); //$NON-NLS-1$
    }
  }

  public FileItem getSelectedFileItem() {
    return selectedFileItem;
  }

  public void setSelectedFileItem(FileItem fileItem) {
    selectedFileItem = fileItem;
  }

  public void selectNextItem(FileItem currentItem) {
    if (currentItem == null) {
      return;
    }
    int myIndex = -1;
    for (int i = 0; i < getFileCount(); i++) {
      FileItem fileItem = getFileItem(i);
      if (fileItem == currentItem) {
        myIndex = i;
      }
    }
    if (myIndex >= 0 && myIndex < getFileCount() - 1) {
      currentItem.setStyleName("fileLabel"); //$NON-NLS-1$
      FileItem nextItem = getFileItem(myIndex + 1);
      nextItem.setStyleName("fileLabelSelected"); //$NON-NLS-1$
      setSelectedFileItem(nextItem);
      nextItem.fireFileSelectionEvent();
    }
  }

  public void selectPreviousItem(FileItem currentItem) {
    if (currentItem == null) {
      return;
    }
    int myIndex = -1;
    for (int i = 0; i < getFileCount(); i++) {
      FileItem fileItem = getFileItem(i);
      if (fileItem == currentItem) {
        myIndex = i;
      }
    }
    if (myIndex > 0 && myIndex < getFileCount()) {
      currentItem.setStyleName("fileLabel"); //$NON-NLS-1$
      FileItem nextItem = getFileItem(myIndex - 1);
      nextItem.setStyleName("fileLabelSelected"); //$NON-NLS-1$
      setSelectedFileItem(nextItem);
      nextItem.fireFileSelectionEvent();
    }
  }

  public FileItem getFileItem(int index) {
    return (FileItem) filesList.getWidget(index, 0);
  }

  /**
   * @return
   */
  public int getFileCount() {
    return filesList.getRowCount();
  }

  /**
   * @return
   */
  public Toolbar getToolbar() {
    return toolbar;
  }

}
