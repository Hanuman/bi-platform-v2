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

import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
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

  public static final String ANALYSIS_VIEW_SUFFIX = ".analysisview.xaction"; //$NON-NLS-1$
  public static final String WAQR_VIEW_SUFFIX = ".waqr.xaction"; //$NON-NLS-1$
  
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
    setStyleName("fileLabel"); //$NON-NLS-1$
    ElementUtils.preventTextSelection(fileLabel.getElement());

    Image fileIcon = new Image();
    if (name.endsWith(WAQR_VIEW_SUFFIX)) { //$NON-NLS-1$
      MantleImages.images.file_report().applyTo(fileIcon);
    } else if (name.endsWith(ANALYSIS_VIEW_SUFFIX)) { 
      MantleImages.images.file_analysis().applyTo(fileIcon);
    } else if (name.endsWith(".xaction")) { //$NON-NLS-1$
      MantleImages.images.file_action().applyTo(fileIcon);
    } else if (name.endsWith(".url")) { //$NON-NLS-1$
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

  public void handleRightClick(final int left, final int top) {
    select();
    fileSelectionListenerCollection.fireFileSelectionChanged(fileItemCallback);

    popupMenu.setPopupPosition(left, top);
    MenuBar menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);

    if (name.endsWith(".xaction")) { //$NON-NLS-1$
      menuBar.addItem(new MenuItem(Messages.getString("open"), new FileCommand(FileCommand.COMMAND.RUN, popupMenu, fileItemCallback))); //$NON-NLS-1$
      menuBar.addItem(new MenuItem(Messages.getString("openInNewWindow"), new FileCommand(FileCommand.COMMAND.NEWWINDOW, popupMenu, fileItemCallback))); //$NON-NLS-1$
      if (!name.endsWith(ANALYSIS_VIEW_SUFFIX)) { // Don't want to run JPivot in the background
        menuBar.addItem(new MenuItem(Messages.getString("runInBackground"), new FileCommand(FileCommand.COMMAND.BACKGROUND, popupMenu, fileItemCallback))); //$NON-NLS-1$
      }
      /*
       * Need to get the file name that was clicked on to see if it is a WAQR report. 
       * Since as of this coding date GWT did not have a disable functionality for Menu item we are achieving so by applying a style and
       * nullifying the command attached to MenutItem click.
       */
      if (name.endsWith(WAQR_VIEW_SUFFIX)) {
        menuBar.addItem(new MenuItem(Messages.getString("edit"), new FileCommand(FileCommand.COMMAND.EDIT, popupMenu, fileItemCallback))); //$NON-NLS-1$
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          menuBar.addItem(new MenuItem(Messages.getString("editAction"), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback))); //$NON-NLS-1$
        }
        
      } else if (name.endsWith(ANALYSIS_VIEW_SUFFIX)) {
        menuBar.addItem(new MenuItem(Messages.getString("edit"), new FileCommand(FileCommand.COMMAND.EDIT, popupMenu, fileItemCallback))); //$NON-NLS-1$
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          menuBar.addItem(new MenuItem(Messages.getString("editAction"), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback))); //$NON-NLS-1$
        }
      } else {
        final FileCommand nullFileCommand = null;
        final MenuItem editMenuItem = new MenuItem(Messages.getString("edit"), nullFileCommand); //$NON-NLS-1$
        editMenuItem.setStyleName("disabledMenuItem"); //$NON-NLS-1$
        menuBar.addItem(editMenuItem);
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          if (name.endsWith(".xaction")) {  //$NON-NLS-1$
            menuBar.addItem(new MenuItem(Messages.getString("editAction"), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback))); //$NON-NLS-1$
          }
        }
      }
      menuBar.addItem(new MenuItem(Messages.getString("delete"), new FileCommand(FileCommand.COMMAND.DELETE, popupMenu, fileItemCallback)));
      if (!name.endsWith(ANALYSIS_VIEW_SUFFIX)) { // Don't want to run JPivot views to be scheduled
        menuBar.addSeparator();
        menuBar.addItem(Messages.getString("scheduleEllipsis"), new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, popupMenu, fileItemCallback)); //$NON-NLS-1$
      }
    }

    menuBar.addSeparator();
    menuBar.addItem(new MenuItem(Messages.getString("propertiesEllipsis"), new FileCommand(FileCommand.COMMAND.PROPERTIES, popupMenu, fileItemCallback))); //$NON-NLS-1$
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
