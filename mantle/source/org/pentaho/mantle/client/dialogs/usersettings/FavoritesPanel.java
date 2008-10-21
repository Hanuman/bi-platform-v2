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
 */
package org.pentaho.mantle.client.dialogs.usersettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FavoritesPanel extends UserPreferencesPanel {

  VerticalPanel content = new VerticalPanel();
  List<Bookmark> bookmarks;
  PopupPanel popupMenu = new PopupPanel(true);
  Tree bookmarkTree = new Tree(MantleImages.bookmarkImages) {

    public void onBrowserEvent(Event event) {
      super.onBrowserEvent(event);
      if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT && getSelectedItem().getUserObject() instanceof Bookmark) {
        // load menu
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        menuBar.addItem(new MenuItem(Messages.getInstance().edit(), new Command() {
          public void execute() {
          }
        }));
        menuBar.addItem(new MenuItem(Messages.getInstance().delete(), new Command() {
          public void execute() {
          }
        }));
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
    }
  };

  public FavoritesPanel() {
    init();
  }

  public void init() {
    content.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    content.setWidth("100%"); //$NON-NLS-1$
    content.add(bookmarkTree);
    bookmarkTree.setAnimationEnabled(true);
    bookmarkTree.sinkEvents(Event.ONDBLCLICK);
    DOM.setStyleAttribute(bookmarkTree.getElement(), "border", "1px solid black"); //$NON-NLS-1$ //$NON-NLS-2$
    ScrollPanel scroller = new ScrollPanel();
    scroller.add(content);
    add(scroller);
    scroller.setHeight("400px"); //$NON-NLS-1$
    scroller.setWidth("400px"); //$NON-NLS-1$
    loadBookmarks();
  }

  public void loadBookmarks() {
    AsyncCallback<List<Bookmark>> callback = new AsyncCallback<List<Bookmark>>() {

      public void onFailure(Throwable caught) {
        //Window.alert(caught.toString());
      }

      public void onSuccess(List<Bookmark> bookmarks) {
        // group these guys in a map
        FavoritesPanel.this.bookmarks = bookmarks;
        Map<String, List<Bookmark>> groupMap = new HashMap<String, List<Bookmark>>();
        for (Bookmark bookmark : bookmarks) {
          List<Bookmark> groupList = groupMap.get(bookmark.getGroup());
          if (groupList == null) {
            groupList = new ArrayList<Bookmark>();
            groupMap.put(bookmark.getGroup(), groupList);
          }
          groupList.add(bookmark);
        }
        for (String groupName : groupMap.keySet()) {
          TreeItem bookmarkGroupTreeItem = new TreeItem(groupName);
          bookmarkTree.addItem(bookmarkGroupTreeItem);
          for (Bookmark bookmark : groupMap.get(groupName)) {
            TreeItem bookmarkItem = new TreeItem(bookmark.getUrl());
            bookmarkItem.setTitle(bookmark.getTitle());
            bookmarkItem.setUserObject(bookmark);
            bookmarkGroupTreeItem.addItem(bookmarkItem);
          }
          bookmarkGroupTreeItem.setState(true);
        }
      }
    };
    MantleServiceCache.getService().getBookmarks(callback);
  }

  public boolean onApply() {
    return true;
  }

  public void onCancel() {
  }

}
