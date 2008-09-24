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

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.service.MantleServiceCache;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TabWidget extends HorizontalPanel implements MouseListener {

  public static final int TAB_TEXT_LENGTH = 12;

  PopupPanel popupMenu = new PopupPanel(true);
  TabPanel tabPanel;
  Widget tabContent;
  SolutionBrowserPerspective perspective;
  String text;
  String tooltip;
  HorizontalPanel panel = new HorizontalPanel();
  HorizontalPanel leftCap = new HorizontalPanel();
  Image closeTabImage = new Image();

  public TabWidget(String text, String tooltip, final SolutionBrowserPerspective perspective, final TabPanel tabPanel, final Widget tabContent) {
    this.tabPanel = tabPanel;
    this.tabContent = tabContent;
    this.perspective = perspective;
    this.text = text;
    this.tooltip = tooltip;
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    String trimmedText = text.substring(0, Math.min(18, text.length()));
    if (!trimmedText.equals(text)) {
      trimmedText += "..";
    }

    panel.setStyleName("tabWidget");

    leftCap.setStyleName("tabWidgetCap");
    Image leftCapImage = new Image();
    MantleImages.images.space1x20().applyTo(leftCapImage);
    //DOM.setStyleAttribute(leftCapImage.getElement(), "padding", "2px");
    leftCap.setSpacing(0);
    leftCap.add(leftCapImage);
    HTML spaceLabel = new HTML("&nbsp;");
    leftCap.add(spaceLabel);

    final Label textLabel = new Label(trimmedText, false);
    textLabel.setTitle(tooltip);
    textLabel.addMouseListener(this);

    tabPanel.addTabListener(new TabListener() {

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        ElementUtils.blur(getElement().getParentElement());
        if (tabIndex == tabPanel.getWidgetIndex(tabContent)) {
          panel.setStyleName("tabWidget-selected");
          leftCap.setStyleName("tabWidgetCap-selected");
        } else {
          panel.setStyleName("tabWidget");
          leftCap.setStyleName("tabWidgetCap");
        }
      }

    });

    MantleImages.images.closeTab().applyTo(closeTabImage);
    closeTabImage.setTitle("Close Tab");
    closeTabImage.addMouseListener(this);
    closeTabImage.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        closeTab();
      }

    });

    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    panel.add(textLabel);
    if (perspective != null) {
      panel.add(closeTabImage);
      DOM.setStyleAttribute(closeTabImage.getElement(), "margin", "5px");
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "5px 0px 5px 0px");
      DOM.setStyleAttribute(spaceLabel.getElement(), "margin", "5px 0px 5px 0px");
    } else {
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "4px 5px 5px 5px");
      DOM.setStyleAttribute(textLabel.getElement(), "paddingRight", "5px");
      DOM.setStyleAttribute(spaceLabel.getElement(), "margin", "4px 0px 5px 0px");
    }

    add(leftCap);
    add(panel);
    sinkEvents(Event.ONDBLCLICK | Event.ONMOUSEUP);
  }

  public void closeTab() {
    if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
      if (tabPanel.getTabBar().getSelectedTab() > 0) {
        tabPanel.selectTab(tabPanel.getTabBar().getSelectedTab() - 1);
      } else if (tabPanel.getTabBar().getTabCount() > 1) {
        tabPanel.selectTab(tabPanel.getTabBar().getSelectedTab() + 1);
      }
    }
    tabPanel.remove(tabPanel.getWidgetIndex(tabContent));
    if (tabPanel.getWidgetCount() == 0) {
      if (perspective != null) {
        perspective.allTabsClosed();
      }
    }

  }

  public void closeOtherTabs() {
    // remove from 0 -> me
    tabPanel.clear();
    tabPanel.add(tabContent, this);
  }

  public void closeAllTabs() {
    tabPanel.clear();
    if (tabPanel.getWidgetCount() == 0) {
      perspective.allTabsClosed();
    }
  }

  public void reloadTab() {
    if (tabContent instanceof IReloadableTabPanel) {
      ((IReloadableTabPanel) tabContent).reload();
    }
  }

  public void reloadAllTabs() {
    for (int i = 0; i < tabPanel.getTabBar().getTabCount(); i++) {
      if (tabPanel.getWidget(i) instanceof IReloadableTabPanel) {
        ((IReloadableTabPanel) tabPanel.getWidget(i)).reload();
      }
    }
  }

  public void openTabInNewWindow() {
    if (tabContent instanceof IReloadableTabPanel) {
      ((IReloadableTabPanel) tabContent).openTabInNewWindow();
    }
  }

  public void bookmark() {
    if (tabContent instanceof ReloadableIFrameTabPanel) {
      final String url = ((ReloadableIFrameTabPanel) tabContent).getUrl();
      final AsyncCallback callback = new AsyncCallback() {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(Object result) {
          if (perspective != null) {
            perspective.loadBookmarks();
          }
        }

      };

      MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
      for (Bookmark bookmark : perspective.getBookmarks()) {
        oracle.add(bookmark.getGroup());
      }

      final TextBox groupNameTextBox = new TextBox();
      SuggestBox suggestTextBox = new SuggestBox(oracle, groupNameTextBox);

      PromptDialogBox dialogBox = new PromptDialogBox("Group Name", "OK", "Cancel", false, true, suggestTextBox);
      if (perspective != null) {
        perspective.getBookmarks();
      }
      dialogBox.setCallback(new IDialogCallback() {
        public void cancelPressed() {
        }

        public void okPressed() {
          Bookmark bookmark = new Bookmark();
          bookmark.setTitle(text);
          bookmark.setUrl(url);
          bookmark.setGroup(groupNameTextBox.getText());
          MantleServiceCache.getService().addBookmark(bookmark, callback);
        }
      });
      dialogBox.center();
    }
  }

  public void onBrowserEvent(Event event) {
    if (perspective != null) {
      if ((DOM.eventGetType(event) & Event.ONDBLCLICK) == Event.ONDBLCLICK) {
        openTabInNewWindow();
      } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
        int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
        int top = Window.getScrollTop() + DOM.eventGetClientY(event);
        popupMenu.setPopupPosition(left, top);
        MenuBar menuBar = new MenuBar(true);
        menuBar.setAutoOpen(true);
        if (tabContent instanceof ReloadableIFrameTabPanel) {
          if (MantleApplication.showAdvancedFeatures) {
            menuBar.addItem(new MenuItem("Bookmark Tab", new TabCommand(TabCommand.BOOKMARK, popupMenu, this)));
            menuBar.addSeparator();
          }
        }
        if (tabContent instanceof IReloadableTabPanel) {
          menuBar.addItem(new MenuItem("Reload Tab", new TabCommand(TabCommand.RELOAD, popupMenu, this)));
        }
        menuBar.addItem(new MenuItem("Reload All Tabs", new TabCommand(TabCommand.RELOAD_ALL, popupMenu, this)));
        menuBar.addSeparator();
        if (tabContent instanceof IReloadableTabPanel) {
          menuBar.addItem(new MenuItem("Open Tab in New Window", new TabCommand(TabCommand.NEW_WINDOW, popupMenu, this)));
          menuBar.addSeparator();
        }
        menuBar.addItem(new MenuItem("Close Tab", new TabCommand(TabCommand.CLOSE, popupMenu, this)));
        menuBar.addItem(new MenuItem("Close Other Tabs", new TabCommand(TabCommand.CLOSE_OTHERS, popupMenu, this)));
        menuBar.addItem(new MenuItem("Close All Tabs", new TabCommand(TabCommand.CLOSE_ALL, popupMenu, this)));
        popupMenu.setWidget(menuBar);
        popupMenu.hide();
        popupMenu.show();
      }
    }
    super.onBrowserEvent(event);
  }

  public void onMouseDown(Widget sender, int x, int y) {
  }

  public void onMouseEnter(Widget sender) {
    if (sender == closeTabImage) {
      MantleImages.images.closeTabHover().applyTo(closeTabImage);
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        panel.setStyleName("tabWidget-selected");
        leftCap.setStyleName("tabWidgetCap-selected");
      } else {
        panel.setStyleName("tabWidget-hover");
        leftCap.setStyleName("tabWidgetCap-hover");
      }
    } else {
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        // don't do anything
      } else {
        panel.setStyleName("tabWidget-hover");
        leftCap.setStyleName("tabWidgetCap-hover");
      }
    }
  }

  public void onMouseLeave(Widget sender) {
    if (sender == closeTabImage) {
      MantleImages.images.closeTab().applyTo(closeTabImage);
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        panel.setStyleName("tabWidget-selected");
        leftCap.setStyleName("tabWidgetCap-selected");
      } else {
        panel.setStyleName("tabWidget");
        leftCap.setStyleName("tabWidgetCap");
      }
    } else {
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        // don't do anything
      } else {
        panel.setStyleName("tabWidget");
        leftCap.setStyleName("tabWidgetCap");
      }
    }
  }

  public void onMouseMove(Widget sender, int x, int y) {
  }

  public void onMouseUp(Widget sender, int x, int y) {
  }

}
