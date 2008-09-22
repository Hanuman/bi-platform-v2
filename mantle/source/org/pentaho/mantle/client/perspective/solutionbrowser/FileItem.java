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

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.perspective.solutionbrowser.events.FileSelectionListenerCollection;
import org.pentaho.mantle.client.perspective.solutionbrowser.events.IFileSelectionChangedListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.events.SourcesFileSelectionChanged;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;

public class FileItem extends FlexTable implements SourcesFileSelectionChanged {

  // by creating a single popupMenu, we're reducing total # of widgets used
  // and we can be sure to hide any existing ones by calling hide
  static PopupPanel popupMenu = new PopupPanel(true);
  Label fileLabel = new Label();
  IFileItemCallback fileItemCallback;
  String name;
  String solution;
  String path;
  String lastModifiedDateStr;
  String url;
  String localizedName;

  FileSelectionListenerCollection fileSelectionListenerCollection = new FileSelectionListenerCollection();

  public FileItem(String name, String localizedName, boolean useLocalizedName, String solution, String path, String lastModifiedDateStr, String url,
      IFileItemCallback fileItemCallback) {
    sinkEvents(Event.ONDBLCLICK | Event.ONMOUSEUP);
    fileLabel.setWordWrap(false);
    fileLabel.setText(localizedName);
    fileLabel.setTitle(localizedName);
    fileLabel.setStyleName("fileLabel");
    Image fileIcon = new Image() {
      public void onBrowserEvent(Event event) {
        if ((DOM.eventGetType(event) & Event.BUTTON_LEFT) == Event.BUTTON_LEFT
            && (DOM.eventGetButton(event) == Event.BUTTON_LEFT || DOM.eventGetButton(event) == Event.BUTTON_RIGHT)) {
          final int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
          final int top = Window.getScrollTop() + DOM.eventGetClientY(event);
          handleRightClick(left, top);
        }
        super.onBrowserEvent(event);
      }
    };
    fileIcon.setStyleName("fileLabel");
    fileIcon.sinkEvents(Event.MOUSEEVENTS);
    if (name.endsWith(".waqr.xaction")) {
      MantleImages.images.file_report().applyTo(fileIcon);
    } else if (name.endsWith(".analysisview.xaction")) {
      MantleImages.images.file_analysis().applyTo(fileIcon);
    } else if (name.endsWith(".xaction")) {
      MantleImages.images.file_action().applyTo(fileIcon);
    } else if (name.endsWith(".url")) {
      MantleImages.images.file_url().applyTo(fileIcon);
    } else {
      MantleImages.images.fileIcon().applyTo(fileIcon);
    }
    fileIcon.setWidth("16px");
    fileLabel.setWidth("100%");
    setWidget(0, 0, fileIcon);
    getCellFormatter().setWidth(0, 0, "16px");
    setWidget(0, 1, fileLabel);
    getCellFormatter().setWidth(0, 1, "100%");
    this.fileItemCallback = fileItemCallback;
    this.name = name;
    this.localizedName = localizedName;
    this.solution = solution;
    this.path = path;
    this.lastModifiedDateStr = lastModifiedDateStr;
    this.url = url;
  }

  public void onBrowserEvent(Event event) {
    if ((DOM.eventGetType(event) & Event.ONDBLCLICK) == Event.ONDBLCLICK) {
      if (fileItemCallback.getSelectedFileItem() != null) {
        fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabel");
      }
      fileItemCallback.setSelectedFileItem(this);
      fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabelSelected");
      fileItemCallback.openFile(FileCommand.RUN);
    } else if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
      if (fileItemCallback.getSelectedFileItem() != null) {
        fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabel");
      }
      fileItemCallback.setSelectedFileItem(this);
      fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabelSelected");
    } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
      final int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
      final int top = Window.getScrollTop() + DOM.eventGetClientY(event);
      handleRightClick(left, top);
    }
    fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);

    super.onBrowserEvent(event);
  }

  public void handleRightClick(final int left, final int top) {
    if (fileItemCallback.getSelectedFileItem() != null) {
      fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabel");
    }
    fileItemCallback.setSelectedFileItem(this);
    fileItemCallback.getSelectedFileItem().fileLabel.setStyleName("fileLabelSelected");
    fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);

    popupMenu.setPopupPosition(left, top);
    MenuBar menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);

    if (name.endsWith(".xaction")) {
      MenuBar runMenu = new MenuBar(true);
      runMenu.addItem(new MenuItem("New Tab", new FileCommand(FileCommand.RUN, popupMenu, fileItemCallback)));
      runMenu.addItem(new MenuItem("New Window", new FileCommand(FileCommand.NEWWINDOW, popupMenu, fileItemCallback)));
      runMenu.addItem(new MenuItem("Background", new FileCommand(FileCommand.BACKGROUND, popupMenu, fileItemCallback)));
      // runMenu.addSeparator();
      // runMenu.addItem(new MenuItem("Subscribe", new FileCommand(FileCommand.SUBSCRIBE, popupMenu, fileItemCallback)));

      // MenuBar scheduleMenu = new MenuBar(true);
      // scheduleMenu.addItem(new MenuItem("Daily", new FileCommand(FileCommand.SCHEDULE_DAILY, popupMenu, fileItemCallback)));
      // scheduleMenu.addItem(new MenuItem("Weekdays (M-F)", new FileCommand(FileCommand.SCHEDULE_WEEKDAYS, popupMenu, fileItemCallback)));
      // scheduleMenu.addItem(new MenuItem("Every Mon/Wed/Fri", new FileCommand(FileCommand.SCHEDULE_MWF, popupMenu, fileItemCallback)));
      // scheduleMenu.addItem(new MenuItem("Every Tu/Th", new FileCommand(FileCommand.SCHEDULE_TUTH, popupMenu, fileItemCallback)));
      //
      // MenuBar weeklyMenu = new MenuBar(true);
      // weeklyMenu.addItem(new MenuItem("Every Sunday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_SUN, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Monday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_MON, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Tuesday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_TUE, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Wednesday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_WED, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Thursday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_THU, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Friday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_FRI, popupMenu, fileItemCallback)));
      // weeklyMenu.addItem(new MenuItem("Every Saturday", new FileCommand(FileCommand.SCHEDULE_WEEKLY_SAT, popupMenu, fileItemCallback)));
      // scheduleMenu.addItem("Weekly ", weeklyMenu);
      //
      // MenuBar monthlyMenu = new MenuBar(true);
      // monthlyMenu.addItem(new MenuItem("1st of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_1ST, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("15th of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_15TH, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("First Sunday of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_FIRST_SUN, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("First Monday of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_FIRST_MON, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("Last Sunday of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_LAST_SUN, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("Last Friday of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_LAST_FRI, popupMenu, fileItemCallback)));
      // monthlyMenu.addItem(new MenuItem("Last Day of the Month", new FileCommand(FileCommand.SCHEDULE_MONTHLY_LAST_DAY, popupMenu, fileItemCallback)));
      // scheduleMenu.addItem("Monthly ", monthlyMenu);
      // scheduleMenu.addItem("Annually", new FileCommand(FileCommand.SCHEDULE_ANUALLY, popupMenu, fileItemCallback));
      // scheduleMenu.addSeparator();
      // scheduleMenu.addItem("Custom...", new FileCommand(FileCommand.SCHEDULE_CUSTOM, popupMenu, fileItemCallback));
      // scheduleMenu.addItem("New...", new FileCommand(FileCommand.SCHEDULE_NEW, popupMenu, fileItemCallback));

      menuBar.addItem("Run ", runMenu);
      menuBar.addSeparator();
      menuBar.addItem("Schedule... ", new FileCommand(FileCommand.SCHEDULE_NEW, popupMenu, fileItemCallback));
      menuBar.addSeparator();
    }

    /*
     * Need to get the file name that was clicked on to see if it is a WAQR report. We want to disable the menu items "Delete" and "Edit" if the user has NOT
     * clicked on WAQR report. Since as of this coding date GWT did not have a disable functionality for Menu item we are achieving so by applying a style and
     * nullifying the command attached to MenutItem click.
     */
    if (name.contains("waqr")) {
      menuBar.addItem(new MenuItem("Edit", new FileCommand(FileCommand.EDIT, popupMenu, fileItemCallback)));
      menuBar.addSeparator();

      menuBar.addItem(new MenuItem("Delete", new FileCommand(FileCommand.DELETE, popupMenu, fileItemCallback)));
      menuBar.addSeparator();
    } else {
      final FileCommand nullFileCommand = null;
      final MenuItem editMenuItem = new MenuItem("Edit", nullFileCommand);
      editMenuItem.setStyleName("disabledMenuItem");
      menuBar.addItem(editMenuItem);
      menuBar.addSeparator();

      final MenuItem deleteMenuItem = new MenuItem("Delete", nullFileCommand);
      deleteMenuItem.setStyleName("disabledMenuItem");
      menuBar.addItem(deleteMenuItem);
      menuBar.addSeparator();
    }

    menuBar.addItem(new MenuItem("Properties", new FileCommand(FileCommand.PROPERTIES, popupMenu, fileItemCallback)));
    popupMenu.setWidget(menuBar);

    Timer t = new Timer() {
      public void run() {
        popupMenu.hide();
        popupMenu.show();
        if ((top + popupMenu.getOffsetHeight()) > Window.getClientHeight()) {
          popupMenu.setPopupPosition(left, top - popupMenu.getOffsetHeight());
          popupMenu.hide();
          popupMenu.show();
          DOM.scrollIntoView(popupMenu.getElement());
        }
      }
    };
    t.schedule(250);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSolution() {
    return solution;
  }

  public void setSolution(String solution) {
    this.solution = solution;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getLastModifiedDateStr() {
    return lastModifiedDateStr;
  }

  public void setLastModifiedDateStr(String lastModifiedDateStr) {
    this.lastModifiedDateStr = lastModifiedDateStr;
  }

  public String getURL() {
    return url;
  }

  public void setURL(String url) {
    this.url = url;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public void setLocalizedName(String localizedName) {
    this.localizedName = localizedName;
  }

  public String getFullPath() {
    return solution + path + name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.events.SourcesFileSelectionChanged#addFileSelectionChangedListener(org.pentaho.mantle.client.perspective.solutionbrowser.events.IFileSelectionChangedListener)
   */
  public void addFileSelectionChangedListener(IFileSelectionChangedListener listener) {
    if (fileSelectionListenerCollection == null) {
      fileSelectionListenerCollection = new FileSelectionListenerCollection();
    }
    fileSelectionListenerCollection.add(listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.mantle.client.perspective.solutionbrowser.events.SourcesFileSelectionChanged#removeFileSelectionChangedListener(org.pentaho.mantle.client.perspective.solutionbrowser.events.IFileSelectionChangedListener)
   */
  public void removeFileSelectionChangedListener(IFileSelectionChangedListener listener) {
    if (fileSelectionListenerCollection != null) {
      fileSelectionListenerCollection.remove(listener);
    }
  }

}
