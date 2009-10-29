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
package org.pentaho.mantle.client.solutionbrowser.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleTabPanel extends TabPanel {

  public static final int CURRENT_SELECTED_TAB = -1;

  private static final String FRAME_ID_PRE = "frame_"; //$NON-NLS-1$
  private static int frameIdCount = 0;

  private HashMap<Widget, TabWidget> contentTabMap = new HashMap<Widget, TabWidget>();

  private int previousIndex;

  public MantleTabPanel() {
    setupNativeHooks(this);
    // add window close listener
    Window.addWindowClosingHandler(new ClosingHandler() {

      public void onWindowClosing(ClosingEvent event) {
        // close only if we have stuff open
        if (getTabBar().getTabCount() > 0) {
          event.setMessage(Messages.getString("windowCloseWarning")); //$NON-NLS-1$
        }
      }
    });

    addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        previousIndex = getTabBar().getSelectedTab();
      }
    });

    addSelectionHandler(new SelectionHandler<Integer>() {

      public void onSelection(SelectionEvent<Integer> event) {
        int tabIndex = event.getSelectedItem();
        SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.DESELECT, previousIndex);
        SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, tabIndex);
        if (previousIndex != tabIndex) {
          Widget tabPanel = getWidget(tabIndex);
          Window.setTitle(Messages.getString("productName") + " - " + getCurrentTab().getText()); //$NON-NLS-1$ //$NON-NLS-2$

          if (tabPanel instanceof IFrameTabPanel) {
            NamedFrame frame = ((IFrameTabPanel) tabPanel).getFrame();
            frame.setVisible(true);
            refreshIfPDF();
          }
        }
        for (int i = 0; i < tabIndex; i++) {
          hideFrame(i);
        }
        for (int i = tabIndex + 1; i < getTabBar().getTabCount(); i++) {
          hideFrame(i);
        }
      }
    });
    setHeight("100%"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$
  }

  public void add(Widget w, TabWidget tabWidget) {
    super.add(w, tabWidget);
    contentTabMap.put(w, tabWidget);
  }

  public void showNewURLTab(String tabName, String tabTooltip, final String url) {
    final int elementId = getWidgetCount();
    String frameName = getUniqueFrameName();

    // check for other tabs with this name
    if (existingTabMatchesName(tabName)) {
      int counter = 2;
      while (true) {
        // Loop until a unique tab name is not found
        // i.e. get the last counter number and then add 1 to it for the new tab name
        if (existingTabMatchesName(tabName + " (" + counter + ")")) { // unique //$NON-NLS-1$ //$NON-NLS-2$
          counter++;
          continue;
        } else {
          tabName = tabName + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          tabTooltip = tabTooltip + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          break;
        }
      }
    }

    IFrameTabPanel panel = new IFrameTabPanel(frameName, url);
    add(panel, new TabWidget(tabName, tabTooltip, SolutionBrowserPerspective.getInstance(), this, panel));
    selectTab(elementId);

    final ArrayList<com.google.gwt.dom.client.Element> parentList = new ArrayList<com.google.gwt.dom.client.Element>();
    com.google.gwt.dom.client.Element parent = panel.getFrame().getElement();
    while (parent != getElement()) {
      parentList.add(parent);
      parent = parent.getParentElement();
    }
    Collections.reverse(parentList);
    for (int i = 1; i < parentList.size(); i++) {
      parentList.get(i).getStyle().setProperty("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    SolutionBrowserPerspective.getInstance().showContent();
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, getTabBar().getSelectedTab());

    setFileInfoInFrame(SolutionBrowserPerspective.getInstance().getFilesListPanel().getSelectedFileItem());
  }

  private native void setupNativeHooks(MantleTabPanel tabPanel)
  /*-{  
    $wnd.enableContentEdit = function(enable) { 
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::enableContentEdit(Z)(enable);      
    }
    $wnd.setContentEditSelected = function(enable) { 
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::setContentEditSelected(Z)(enable);      
    }
    $wnd.registerContentOverlay = function(id) { 
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::registerContentOverlay(Ljava/lang/String;)(id);      
    }
    $wnd.registerContentCallback = function(callback) { 
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::setCurrentTabJSCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);      
    } 
    $wnd.enableAdhocSave = function(enable) {
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::setCurrentTabSaveEnabled(Z)(enable);
    }
    $wnd.closeTab = function(url) {
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::closeTab(Ljava/lang/String;)(url);
    }    
    $wnd.mantle_openTab = function(name, title, url) {
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, title, url);
    }    
    $wnd.openURL = function(name, tooltip, url){
      tabPanel.@org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, tooltip, url);
    }    

  }-*/;

  public Widget getCurrentTab(int tabIndex) {
    Widget tabPanel = null;
    if (tabIndex >= 0 && getWidgetCount() > tabIndex) {
      tabPanel = getWidget(tabIndex);
    } else {
      int selectedTabIndex = getTabBar().getSelectedTab();
      if (selectedTabIndex >= 0) {
        tabPanel = getWidget(selectedTabIndex);
      }
    }
    return tabPanel;
  }

  public TabWidget getCurrentTab() {
    return contentTabMap.get(getWidget(getTabBar().getSelectedTab()));
  }

  public TabWidget getTabForWidget(Widget tabWidget) {
    return contentTabMap.get(getWidget(getWidgetIndex(tabWidget)));
  }

  /**
   * This method returns the current frame element id.
   * 
   * @return
   */
  public String getCurrentFrameElementId() {
    int curpos = getTabBar().getSelectedTab();
    final IFrameTabPanel curPanel = (IFrameTabPanel) getWidget(curpos);
    return curPanel.getFrame().getElement().getAttribute("id"); //$NON-NLS-1$
  }

  public IFrameTabPanel getCurrentFrame() {
    int curpos = getTabBar().getSelectedTab();
    if (curpos == -1) {
      return null;
    }
    final IFrameTabPanel curPanel = (IFrameTabPanel) getWidget(curpos);
    return curPanel;
  }

  public void setCurrentTabSaveEnabled(boolean enabled) {
    IFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.setSaveEnabled(enabled);
    }
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, getTabBar().getSelectedTab());
  }

  /*
   * registerContentOverlay - register the overlay with the panel. Once the registration is done it fires a soultion browser event passing the current tab index
   * and the type of event
   */
  public void registerContentOverlay(String id) {
    IFrameTabPanel panel = getCurrentFrame();
    panel.addOverlay(id);
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, getTabBar().getSelectedTab());
  }

  public void enableContentEdit(boolean enable) {
    IFrameTabPanel panel = getCurrentFrame();
    panel.setEditEnabled(enable);
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, getTabBar().getSelectedTab());
  }

  public void setContentEditSelected(boolean selected) {
    IFrameTabPanel panel = getCurrentFrame();
    panel.setEditSelected(selected);
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, getTabBar().getSelectedTab());
  }

  // Content frames can register a Javascript object to receive various PUC notifications. We broker that out
  // to the appropriate IFrameTabPanel here.
  public void setCurrentTabJSCallback(JavaScriptObject obj) {
    IFrameTabPanel panel = getCurrentFrame();
    panel.setContentCallback(obj);
  }

  public void hideFrame(int tabIndex) {
    Frame frame = ((IFrameTabPanel) getWidget(tabIndex)).getFrame();
    frame.setVisible(false);
  }

  public boolean existingTabMatchesName(String name) {
    String key = "title=\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$

    NodeList<com.google.gwt.dom.client.Element> divs = getTabBar().getElement().getElementsByTagName("div"); //$NON-NLS-1$

    for (int i = 0; i < divs.getLength(); i++) {
      String tabHtml = divs.getItem(i).getInnerHTML();
      // TODO: remove once a more elegant tab solution is in place
      if (tabHtml.indexOf(key) > -1) {
        return true;
      }
    }
    return false;
  }

  public String getUniqueFrameName() {
    return FRAME_ID_PRE + frameIdCount++;
  }

  /**
   * Called by JSNI call from parameterized xaction prompt pages to "cancel". The only 'key' to pass up is the URL. To handle the possibility of multiple tabs
   * with the same url, this method first checks the assumption that the current active tab initiates the call. Otherwise it checks from tail up for the first
   * tab with a matching url and closes that one. *
   * 
   * @param url
   */
  @SuppressWarnings("unused")
  private void closeTab(String url) {
    int curpos = getTabBar().getSelectedTab();
    if (StringUtils.isEmpty(url)) {
      // if the url was not provided, simply remove the currently selected tab and then remove
      if (curpos >= 0 && getWidgetCount() > 0) {
        remove(curpos);
      }
      if (getWidgetCount() == 0) {
        allTabsClosed();
      }
      return;
    }
    IFrameTabPanel curPanel = (IFrameTabPanel) getWidget(curpos);
    if (url.contains(curPanel.getUrl())) {
      remove(curpos);
      if (getWidgetCount() == 0) {
        allTabsClosed();
      }
      return;
    }

    for (int i = getWidgetCount() - 1; i >= 0; i--) {
      curPanel = (IFrameTabPanel) getWidget(i);

      if (url.contains(curPanel.getUrl())) {
        remove(i);
        if (getWidgetCount() == 0) {
          allTabsClosed();
        }
        return;
      }
    }
  }

  private native boolean isPDF(com.google.gwt.dom.client.Element frame)
  /*-{
    return (frame.contentDocument != null && frame.contentDocument.getElementsByTagName('embed').length > 0);
  }-*/;

  public void refreshIfPDF() {
    // There's a bug when re-showing a tab containing a PDF. Under Firefox it doesn't render, so we force a reload
    int selectedTab = getTabBar().getSelectedTab();
    if (selectedTab > -1) {
      final Widget tabContent = getWidget(selectedTab);
      if (tabContent instanceof IFrameTabPanel) {
        Timer t = new Timer() {
          public void run() {
            IFrameTabPanel frame = ((IFrameTabPanel) tabContent);
            if (isPDF(frame.getFrame().getElement())) {
              frame.reload();
            }
          }
        };
        t.schedule(250);
      }
    }
  }

  /**
   * This method will check if the given frame(by id) is jpivot.
   * 
   * @param elementId
   */
  public static native boolean isPivot(String elementId)
  /*-{
    var frame = $doc.getElementById(elementId);
    if (!frame) { 
      return false; 
    }
    frame = frame.contentWindow;
    return true == frame.pivot_initialized;
  }-*/;

  /**
   * Store representation of file in the frame for reference later when save is called
   * 
   * @param selectedFileItem
   */
  public void setFileInfoInFrame(FileItem selectedFileItem) {
    IFrameTabPanel tp = getCurrentFrame();
    if (tp != null && selectedFileItem != null) {
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      tp.setFileInfo(fileInfo);
    }
  }

  public void allTabsClosed() {
    // show the "launch" panel
    SolutionBrowserPerspective.getInstance().showContent();
    SolutionBrowserPerspective.getInstance().fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.CLOSE, CURRENT_SELECTED_TAB);
  }

}
