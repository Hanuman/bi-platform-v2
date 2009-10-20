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
package org.pentaho.mantle.client.solutionbrowser;

import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TabWidget extends HorizontalPanel implements MouseListener {

  private static final int TAB_TEXT_LENGTH = 12;

  private PopupPanel popupMenu = new MantlePopupPanel(true);

  private TabPanel tabPanel;
  private Widget tabContent;
  private SolutionBrowserPerspective perspective;
  private Label textLabel = new Label();
  private HorizontalPanel panel = new HorizontalPanel();
  private HorizontalPanel leftCap = new HorizontalPanel();
  private Image closeTabImage = new Image();
  private String fullText;

  public TabWidget(String text, String tooltip, final SolutionBrowserPerspective perspective, final TabPanel tabPanel, final Widget tabContent) {

    // BISERVER-2317 Request for more IDs for Mantle UI elements
    // the id for each tab shall be the text which it displays
    getElement().setId("tab-" + text); //$NON-NLS-1$

    this.tabPanel = tabPanel;
    this.tabContent = tabContent;
    this.perspective = perspective;
    this.fullText = text;
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

    panel.setStyleName("tabWidget"); //$NON-NLS-1$
    leftCap.setStyleName("tabWidgetCap"); //$NON-NLS-1$
    Image leftCapImage = new Image();
    MantleImages.images.space1x20().applyTo(leftCapImage);
    leftCap.setSpacing(0);
    leftCapImage.setWidth("5px"); //$NON-NLS-1$
    leftCap.add(leftCapImage);

    setLabelText(text);
    setLabelTooltip(tooltip);
    textLabel.setWordWrap(false);
    textLabel.addMouseListener(this);

    tabPanel.addTabListener(new TabListener() {

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        ElementUtils.blur(getElement().getParentElement());
        if (tabIndex == tabPanel.getWidgetIndex(tabContent)) {
          panel.setStyleName("tabWidget-selected"); //$NON-NLS-1$
          leftCap.setStyleName("tabWidgetCap-selected"); //$NON-NLS-1$
        } else {
          panel.setStyleName("tabWidget"); //$NON-NLS-1$
          leftCap.setStyleName("tabWidgetCap"); //$NON-NLS-1$
        }
      }

    });

    MantleImages.images.closeTab().applyTo(closeTabImage);
    closeTabImage.setTitle(Messages.getString("closeTab")); //$NON-NLS-1$
    closeTabImage.addMouseListener(this);
    closeTabImage.addClickListener(new ClickListener() {

      public void onClick(Widget sender) {
        closeTab();
      }

    });
    closeTabImage.getElement().setId("killTab"); //$NON-NLS-1$

    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    panel.add(textLabel);
    if (perspective != null) {
      panel.add(closeTabImage);
      DOM.setStyleAttribute(closeTabImage.getElement(), "margin", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "5px 0px 5px 0px"); //$NON-NLS-1$ //$NON-NLS-2$
    } else {
      DOM.setStyleAttribute(textLabel.getElement(), "margin", "4px 5px 5px 5px"); //$NON-NLS-1$ //$NON-NLS-2$
      DOM.setStyleAttribute(textLabel.getElement(), "paddingRight", "5px"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    add(leftCap);
    add(panel);
    sinkEvents(Event.ONDBLCLICK | Event.ONMOUSEUP);
  }

  public String getText() {
    return fullText;
  }

  public void setLabelText(String text) {
    String trimmedText = text.substring(0, Math.min(18, text.length()));
    if (!trimmedText.equals(text)) {
      trimmedText += ".."; //$NON-NLS-1$
    }
    textLabel.setText(trimmedText);
  }

  public void setLabelTooltip(String tooltip) {
    textLabel.setTitle(tooltip);
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
    while (tabContent != tabPanel.getWidget(0)) {
      tabPanel.remove(tabPanel.getWidget(0));
    }
    // remove from END -> me
    while (tabContent != tabPanel.getWidget(tabPanel.getTabBar().getTabCount() - 1)) {
      tabPanel.remove(tabPanel.getWidget(tabPanel.getTabBar().getTabCount() - 1));
    }
    tabPanel.selectTab(0);
  }

  public void back() {
    ((ReloadableIFrameTabPanel) tabContent).back();
  }

  public void closeAllTabs() {
    tabPanel.clear();
    if (tabPanel.getWidgetCount() == 0) {
      perspective.allTabsClosed();
    }
  }

  public void reloadTab() {
    if (tabContent instanceof ReloadableIFrameTabPanel) {
      ((ReloadableIFrameTabPanel) tabContent).reload();
    }
  }

  public void reloadAllTabs() {
    for (int i = 0; i < tabPanel.getTabBar().getTabCount(); i++) {
      if (tabPanel.getWidget(i) instanceof ReloadableIFrameTabPanel) {
        ((ReloadableIFrameTabPanel) tabPanel.getWidget(i)).reload();
      }
    }
  }

  public void openTabInNewWindow() {
    if (tabContent instanceof ReloadableIFrameTabPanel) {
      ((ReloadableIFrameTabPanel) tabContent).openTabInNewWindow();
    }
  }

  public void onBrowserEvent(Event event) {
    // the id's which are set on these menu items must be set AFTER the items are added to their menu
    // when an element is added to a menu an auto-generated id will be assigned, so we must override this
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
          MenuItem backMenuItem = new MenuItem(Messages.getString("back"), new TabCommand(TabCommand.TABCOMMAND.BACK, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(backMenuItem);
          backMenuItem.getElement().setId("back"); //$NON-NLS-1$
          menuBar.addSeparator();
          MenuItem reloadTabMenuItem = new MenuItem(Messages.getString("reloadTab"), new TabCommand(TabCommand.TABCOMMAND.RELOAD, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(reloadTabMenuItem);
          reloadTabMenuItem.getElement().setId("reloadTab"); //$NON-NLS-1$
        }
        if (tabPanel.getTabBar().getTabCount() > 1) {
          MenuItem reloadAllTabsMenuItem = new MenuItem(Messages.getString("reloadAllTabs"), new TabCommand(TabCommand.TABCOMMAND.RELOAD_ALL, popupMenu, //$NON-NLS-1$
              this));
          menuBar.addItem(reloadAllTabsMenuItem);
          reloadAllTabsMenuItem.getElement().setId("reloadAllTabs"); //$NON-NLS-1$
        } else {
          MenuItem reloadAllTabsMenuItem = new MenuItem(Messages.getString("reloadAllTabs"), (Command) null); //$NON-NLS-1$
          menuBar.addItem(reloadAllTabsMenuItem);
          reloadAllTabsMenuItem.getElement().setId("reloadAllTabs"); //$NON-NLS-1$
          reloadAllTabsMenuItem.setStyleName("disabledMenuItem"); //$NON-NLS-1$
        }
        menuBar.addSeparator();
        if (tabContent instanceof ReloadableIFrameTabPanel) {
          MenuItem openTabInNewWindowMenuItem = new MenuItem(
              Messages.getString("openTabInNewWindow"), new TabCommand(TabCommand.TABCOMMAND.NEW_WINDOW, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(openTabInNewWindowMenuItem);
          openTabInNewWindowMenuItem.getElement().setId("openTabInNewWindow"); //$NON-NLS-1$
          MenuItem createDeepLinkMenuItem = new MenuItem(
              Messages.getString("createDeepLink"), new TabCommand(TabCommand.TABCOMMAND.CREATE_DEEP_LINK, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(createDeepLinkMenuItem);
          createDeepLinkMenuItem.getElement().setId("deepLink"); //$NON-NLS-1$
          menuBar.addSeparator();
        }
        menuBar.addItem(new MenuItem(Messages.getString("closeTab"), new TabCommand(TabCommand.TABCOMMAND.CLOSE, popupMenu, this))); //$NON-NLS-1$
        if (tabPanel.getTabBar().getTabCount() > 1) {
          MenuItem closeOtherTabsMenuItem = new MenuItem(
              Messages.getString("closeOtherTabs"), new TabCommand(TabCommand.TABCOMMAND.CLOSE_OTHERS, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(closeOtherTabsMenuItem);
          closeOtherTabsMenuItem.getElement().setId("closeOtherTabs"); //$NON-NLS-1$
          MenuItem closeAllTabsMenuItem = new MenuItem(Messages.getString("closeAllTabs"), new TabCommand(TabCommand.TABCOMMAND.CLOSE_ALL, popupMenu, this)); //$NON-NLS-1$
          menuBar.addItem(closeAllTabsMenuItem);
          closeAllTabsMenuItem.getElement().setId("closeAllTabs"); //$NON-NLS-1$
        } else {
          MenuItem closeOtherTabsMenuItem = new MenuItem(Messages.getString("closeOtherTabs"), (Command) null); //$NON-NLS-1$
          closeOtherTabsMenuItem.setStyleName("disabledMenuItem"); //$NON-NLS-1$
          MenuItem closeAllTabsMenuItem = new MenuItem(Messages.getString("closeAllTabs"), (Command) null); //$NON-NLS-1$
          closeAllTabsMenuItem.setStyleName("disabledMenuItem"); //$NON-NLS-1$
          menuBar.addItem(closeOtherTabsMenuItem);
          menuBar.addItem(closeAllTabsMenuItem);
          closeOtherTabsMenuItem.getElement().setId("closeOtherTabs"); //$NON-NLS-1$
          closeAllTabsMenuItem.getElement().setId("closeAllTabs"); //$NON-NLS-1$
        }
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
        panel.setStyleName("tabWidget-selected"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap-selected"); //$NON-NLS-1$
      } else {
        panel.setStyleName("tabWidget-hover"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap-hover"); //$NON-NLS-1$
      }
    } else {
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        // don't do anything
      } else {
        panel.setStyleName("tabWidget-hover"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap-hover"); //$NON-NLS-1$
      }
    }
  }

  public void onMouseLeave(Widget sender) {
    if (sender == closeTabImage) {
      MantleImages.images.closeTab().applyTo(closeTabImage);
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        panel.setStyleName("tabWidget-selected"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap-selected"); //$NON-NLS-1$
      } else {
        panel.setStyleName("tabWidget"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap"); //$NON-NLS-1$
      }
    } else {
      if (tabPanel.getTabBar().getSelectedTab() == tabPanel.getWidgetIndex(tabContent)) {
        // don't do anything
      } else {
        panel.setStyleName("tabWidget"); //$NON-NLS-1$
        leftCap.setStyleName("tabWidgetCap"); //$NON-NLS-1$
      }
    }
  }

  public void onMouseMove(Widget sender, int x, int y) {
  }

  public void onMouseUp(Widget sender, int x, int y) {
  }

  public void createDeepLink() {
    if (tabContent instanceof ReloadableIFrameTabPanel) {
      PromptDialogBox dialogBox = new PromptDialogBox(Messages.getString("deepLink"), Messages.getString("ok"), Messages.getString("cancel"), false, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          true);
      String url = Window.Location.getProtocol() + "//" + Window.Location.getHostName() + ":" + Window.Location.getPort() + Window.Location.getPath() //$NON-NLS-1$ //$NON-NLS-2$
          + "?name=" + textLabel.getText() + "&startup-url="; //$NON-NLS-1$ //$NON-NLS-2$
      String startup = ((ReloadableIFrameTabPanel) tabContent).getUrl();
      TextBox urlbox = new TextBox();
      urlbox.setText(url + URL.encodeComponent(startup));
      urlbox.setVisibleLength(80);
      dialogBox.setContent(urlbox);
      dialogBox.center();
    }
  }

}
