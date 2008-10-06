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

package org.pentaho.mantle.client.perspective.solutionbrowser;

import java.util.List;

import org.pentaho.gwt.widgets.client.toolbar.Toolbar;
import org.pentaho.mantle.client.perspective.solutionbrowser.toolbars.FilesToolbar;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.xml.client.Element;

/**
 * @author wseyler
 * 
 */
public class FilesListPanel extends FlowPanel {
  protected String FILES_LABEL_STYLE_NAME = "filesPanelMenuLabel"; //$NON-NLS-1$

  FlexTable filesList = new FlexTable();
  FilesToolbar toolbar;
  IFileItemCallback fileItemCallback;

  public FilesListPanel(final IFileItemCallback fileItemCallback) {
    super();
    this.fileItemCallback = fileItemCallback;
    // Create the toolbar
    toolbar = new FilesToolbar(fileItemCallback);
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.add(toolbar);
    toolbarWrapper.setStyleName("files-toolbar");
    add(toolbarWrapper);

    SimplePanel filesListWrapper = new SimplePanel();
    FocusPanel fp = new FocusPanel(filesList) {
      public void onBrowserEvent(Event event) {
        if ((DOM.eventGetType(event) & Event.ONKEYDOWN) == Event.ONKEYDOWN) {
          if (event.getKeyCode() == KeyboardListener.KEY_UP) {
            fileItemCallback.selectPreviousItem(fileItemCallback.getSelectedFileItem());
          } else if (event.getKeyCode() == KeyboardListener.KEY_DOWN) {
            fileItemCallback.selectNextItem(fileItemCallback.getSelectedFileItem());
          } else if (event.getKeyCode() == KeyboardListener.KEY_ENTER) {
            fileItemCallback.openFile(FileCommand.RUN);
          }
        }
        super.onBrowserEvent(event);
      }
    };
    fp.sinkEvents(Event.KEYEVENTS);

    filesListWrapper.add(fp);
    fp.getElement().getStyle().setProperty("marginTop", "29px");
    filesListWrapper.setStyleName("files-list-panel");
    add(filesListWrapper);

    this.setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  
  }

  public void populateFilesList(SolutionBrowserPerspective perspective, SolutionTree solutionTree, FileItem selectedFileItem, TreeItem item) {
    filesList.clear();
    List<Element> files = (List<Element>) item.getUserObject();
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
          final FileItem fileLabel = new FileItem(name, fileElement.getAttribute("localized-name"), solutionTree.showLocalizedFileNames, solution, path, //$NON-NLS-1$
              lastModifiedDateStr, url, perspective);
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
