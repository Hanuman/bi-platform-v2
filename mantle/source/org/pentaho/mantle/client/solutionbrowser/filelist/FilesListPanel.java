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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.toolbars.FilesToolbar;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.xml.client.Element;

/**
 * @author wseyler
 * 
 */
public class FilesListPanel extends FlowPanel {
  protected String FILES_LABEL_STYLE_NAME = "filesPanelMenuLabel"; //$NON-NLS-1$

  private FlexTable filesList = new FlexTable();
  private FilesToolbar toolbar;

  public FilesListPanel(final IFileItemCallback fileItemCallback) {
    super();
    // Create the toolbar
    toolbar = new FilesToolbar(fileItemCallback);
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.add(toolbar);
    toolbarWrapper.setStyleName("files-toolbar"); //$NON-NLS-1$
    add(toolbarWrapper);

    SimplePanel filesListWrapper = new SimplePanel();
    FocusPanel fp = new FocusPanel(filesList) {
      public void onBrowserEvent(Event event) {
        if ((DOM.eventGetType(event) & Event.ONKEYDOWN) == Event.ONKEYDOWN) {
          if (event.getKeyCode() == KeyCodes.KEY_UP) {
            fileItemCallback.selectPreviousItem(fileItemCallback.getSelectedFileItem());
          } else if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
            fileItemCallback.selectNextItem(fileItemCallback.getSelectedFileItem());
          } else if (event.getKeyCode() == KeyCodes.KEY_ENTER) {
            fileItemCallback.openFile(FileCommand.COMMAND.RUN);
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

    this.setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  

    getElement().setId("filesListPanel");
  }

  @SuppressWarnings("unchecked")
  public void populateFilesList(SolutionBrowserPerspective perspective, SolutionTree solutionTree, FileItem selectedFileItem, TreeItem item) {
    filesList.clear();
    List<Element> files = (List<Element>) item.getUserObject();
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
          SolutionBrowserPerspective.ContentTypePlugin plugin = perspective.getContentTypePlugin(name);
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
              lastModifiedDateStr, url, perspective, perspective.getEnabledOptions(name), toolbar.getSupportsACLs(), icon);
          // BISERVER-2317: Request for more IDs for Mantle UI elements
          // set element id as the filename
          fileLabel.getElement().setId("file-" + name); //$NON-NLS-1$
          fileLabel.addFileSelectionChangedListener(toolbar);
          fileLabel.setWidth("100%"); //$NON-NLS-1$
          filesList.setWidget(rowCounter++, 0, fileLabel);

          if (selectedFileItem != null && selectedFileItem.getFullPath().equals(fileLabel.getFullPath())) {
            fileLabel.setStyleName("fileLabelSelected"); //$NON-NLS-1$
            selectedFileItem = fileLabel;
            perspective.setSelectedFileItem(selectedFileItem);
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
