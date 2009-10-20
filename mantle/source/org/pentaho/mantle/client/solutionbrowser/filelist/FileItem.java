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
package org.pentaho.mantle.client.solutionbrowser.filelist;

import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.FileTypeEnabledOptions;
import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import org.pentaho.mantle.client.solutionbrowser.events.FileSelectionListenerCollection;
import org.pentaho.mantle.client.solutionbrowser.events.IFileSelectionChangedListener;
import org.pentaho.mantle.client.solutionbrowser.events.SourcesFileSelectionChanged;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

import com.google.gwt.user.client.Command;
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

  public static final String ANALYSIS_VIEW_SUFFIX = ".analysisview.xaction"; //$NON-NLS-1$
  public static final String WAQR_VIEW_SUFFIX = ".waqr.xaction"; //$NON-NLS-1$
  public static final String XACTION_SUFFIX = ".xaction"; //$NON-NLS-1$
  public static final String URL_SUFFIX = ".url"; //$NON-NLS-1$

  private static String SEPARATOR = "separator"; //$NON-NLS-1$
  
  private static final String menuItems[] = {
      "open", //$NON-NLS-1$
      "openInNewWindow", //$NON-NLS-1$
      "runInBackground", //$NON-NLS-1$
      "edit", //$NON-NLS-1$
   // edit action is a advanced feature, hidden normally
      "editAction", //$NON-NLS-1$
      "delete", //$NON-NLS-1$
      SEPARATOR,
      "share", //$NON-NLS-1$
      "scheduleEllipsis", //$NON-NLS-1$
      SEPARATOR,
      "propertiesEllipsis" //$NON-NLS-1$ 
    };
  
  FileCommand.COMMAND menuCommands[] = {
      COMMAND.RUN,
      COMMAND.NEWWINDOW,
      COMMAND.BACKGROUND,
      COMMAND.EDIT,
      COMMAND.EDIT_ACTION,
      COMMAND.DELETE,
      null,
      COMMAND.SHARE,
      COMMAND.SCHEDULE_NEW,
      null,
      COMMAND.PROPERTIES
  };
  
  // by creating a single popupMenu, we're reducing total # of widgets used
  // and we can be sure to hide any existing ones by calling hide
  static PopupPanel popupMenu = new MantlePopupPanel(true);
  
  private Label fileLabel = new Label();
  private IFileItemCallback fileItemCallback;
  private String name;
  private String solution;
  private String path;
  private String lastModifiedDateStr;
  private String url;
  private String localizedName;
  private FileTypeEnabledOptions options;
  private boolean supportsACLs;

  FileSelectionListenerCollection fileSelectionListenerCollection = new FileSelectionListenerCollection();

  public FileItem(String name, String localizedName, String tooltip,
      String solution, String path, String lastModifiedDateStr, String url, 
      IFileItemCallback fileItemCallback, FileTypeEnabledOptions options,
      boolean supportsACLs, String fileIconStr) {
    sinkEvents(Event.ONDBLCLICK | Event.ONMOUSEUP);
    
    DOM.setElementAttribute(getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$
    DOM.setElementAttribute(popupMenu.getElement(), "oncontextmenu", "return false;"); //$NON-NLS-1$ //$NON-NLS-2$

    fileLabel.setWordWrap(false);
    fileLabel.setText(localizedName);
    fileLabel.setTitle(tooltip);
    setStyleName("fileLabel"); //$NON-NLS-1$
    ElementUtils.preventTextSelection(fileLabel.getElement());

    Image fileIcon = new Image();
    if (fileIconStr != null) {
      fileIcon.setUrl(fileIconStr);
    } else if (name.endsWith(WAQR_VIEW_SUFFIX)) {
      MantleImages.images.file_report().applyTo(fileIcon);
    } else if (name.endsWith(ANALYSIS_VIEW_SUFFIX)) { 
      MantleImages.images.file_analysis().applyTo(fileIcon);
    } else if (name.endsWith(XACTION_SUFFIX)) {
      MantleImages.images.file_action().applyTo(fileIcon);
    } else if (name.endsWith(URL_SUFFIX)) {
      MantleImages.images.file_url().applyTo(fileIcon);
    } else {
      MantleImages.images.fileIcon().applyTo(fileIcon);
    }
    fileIcon.setWidth("16px"); //$NON-NLS-1$
    fileLabel.setWidth("100%"); //$NON-NLS-1$
    setWidget(0, 0, fileIcon);
    getCellFormatter().setWidth(0, 0, "16px"); //$NON-NLS-1$
    setWidget(0, 1, fileLabel);
    getCellFormatter().setWidth(0, 1, "100%"); //$NON-NLS-1$
    this.fileItemCallback = fileItemCallback;
    this.name = name;
    this.localizedName = localizedName;
    this.solution = solution;
    this.path = path;
    this.lastModifiedDateStr = lastModifiedDateStr;
    this.url = url;
    this.options = options;
    this.supportsACLs = supportsACLs;
  }

  private void select(){
    if(fileItemCallback.getSelectedFileItem() != null){
      fileItemCallback.getSelectedFileItem().setStyleName("fileLabel"); //$NON-NLS-1$
    }
    
    fileItemCallback.setSelectedFileItem(this);
    fileItemCallback.getSelectedFileItem().setStyleName("fileLabelSelected"); //$NON-NLS-1$
    
  }
  
  public void onBrowserEvent(Event event) {
    if ((DOM.eventGetType(event) & Event.ONDBLCLICK) == Event.ONDBLCLICK) {
      select();
      fileItemCallback.openFile(FileCommand.COMMAND.RUN);
    } else if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
      select();
      fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);
    } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
      final int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
      final int top = Window.getScrollTop() + DOM.eventGetClientY(event);
      handleRightClick(left, top);
      fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);
    }
    super.onBrowserEvent(event);
  }

  public boolean isCommandEnabled(COMMAND command) {
    return options != null && options.isCommandEnabled(command);
  }
  
  public void handleRightClick(final int left, final int top) {
    select();
    fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);

    popupMenu.setPopupPosition(left, top);
    final MenuBar menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);
    
    for (int i = 0; i < menuItems.length; i++) {
      if (!MantleApplication.showAdvancedFeatures && 
          menuCommands[i] == COMMAND.EDIT_ACTION) {
        continue;
      }
      
      if (menuItems[i].equals(SEPARATOR)) {
        menuBar.addSeparator();
      } else if (options != null && options.isCommandEnabled(menuCommands[i])) {
        menuBar.addItem(new MenuItem(Messages.getString(menuItems[i]), new FileCommand(menuCommands[i], popupMenu, fileItemCallback)));        
      } else {
        MenuItem item = new MenuItem(Messages.getString(menuItems[i]), (Command)null);
        item.setStyleName("disabledMenuItem"); //$NON-NLS-1$
        menuBar.addItem(item);
      }
    }
    
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
   * @see org.pentaho.mantle.client.solutionbrowser.events.SourcesFileSelectionChanged#addFileSelectionChangedListener(org.pentaho.mantle.client.solutionbrowser.events.IFileSelectionChangedListener)
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
   * @see org.pentaho.mantle.client.solutionbrowser.events.SourcesFileSelectionChanged#removeFileSelectionChangedListener(org.pentaho.mantle.client.solutionbrowser.events.IFileSelectionChangedListener)
   */
  public void removeFileSelectionChangedListener(IFileSelectionChangedListener listener) {
    if (fileSelectionListenerCollection != null) {
      fileSelectionListenerCollection.remove(listener);
    }
  }

}
