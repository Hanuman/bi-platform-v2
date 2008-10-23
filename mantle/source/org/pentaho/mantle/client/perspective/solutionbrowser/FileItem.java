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
    if (name.endsWith(".waqr.xaction")) { //$NON-NLS-1$
      MantleImages.images.file_report().applyTo(fileIcon);
    } else if (name.endsWith(".analysisview.xaction")) { //$NON-NLS-1$
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

      menuBar.addItem(new MenuItem(Messages.getInstance().open(), new FileCommand(FileCommand.COMMAND.RUN, popupMenu, fileItemCallback)));
      menuBar.addItem(new MenuItem(Messages.getInstance().openInNewWindow(), new FileCommand(FileCommand.COMMAND.NEWWINDOW, popupMenu, fileItemCallback)));
      menuBar.addItem(new MenuItem(Messages.getInstance().runInBackground(), new FileCommand(FileCommand.COMMAND.BACKGROUND, popupMenu, fileItemCallback)));
      /*
       * Need to get the file name that was clicked on to see if it is a WAQR report. 
       * Since as of this coding date GWT did not have a disable functionality for Menu item we are achieving so by applying a style and
       * nullifying the command attached to MenutItem click.
       */
      if (name.contains("waqr.xaction")) { //$NON-NLS-1$
        menuBar.addItem(new MenuItem(Messages.getInstance().edit(), new FileCommand(FileCommand.COMMAND.EDIT, popupMenu, fileItemCallback)));
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          menuBar.addItem(new MenuItem(Messages.getInstance().editAction(), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback)));
        }
        
      } else if (name.contains("analysisview.xaction")) { //$NON-NLS-1$
        menuBar.addItem(new MenuItem(Messages.getInstance().edit(), new FileCommand(FileCommand.COMMAND.EDIT, popupMenu, fileItemCallback)));
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          menuBar.addItem(new MenuItem(Messages.getInstance().editAction(), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback)));
        }
      } else {
        final FileCommand nullFileCommand = null;
        final MenuItem editMenuItem = new MenuItem(Messages.getInstance().edit(), nullFileCommand);
        editMenuItem.setStyleName("disabledMenuItem"); //$NON-NLS-1$
        menuBar.addItem(editMenuItem);
        
        // WG: Experimental Action Sequence Editor
        if (MantleApplication.showAdvancedFeatures) {
          if (name.endsWith(".xaction")) {  //$NON-NLS-1$
            menuBar.addItem(new MenuItem(Messages.getInstance().editAction(), new FileCommand(FileCommand.COMMAND.EDIT_ACTION, popupMenu, fileItemCallback)));
          }
        }
      }
      menuBar.addSeparator();
      menuBar.addItem(Messages.getInstance().scheduleEllipsis(), new FileCommand(FileCommand.COMMAND.SCHEDULE_NEW, popupMenu, fileItemCallback));
    }

    menuBar.addSeparator();
    menuBar.addItem(new MenuItem(Messages.getInstance().propertiesEllipsis(), new FileCommand(FileCommand.COMMAND.PROPERTIES, popupMenu, fileItemCallback)));
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
