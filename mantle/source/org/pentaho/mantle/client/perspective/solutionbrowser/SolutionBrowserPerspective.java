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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.ExecuteWAQRPreviewCommand;
import org.pentaho.mantle.client.commands.NewFolderCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.dialogs.usersettings.UserPreferencesDialog;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand.COMMAND;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.scheduling.NewScheduleDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.toolbars.BrowserToolbar;
import org.pentaho.mantle.client.perspective.workspace.IWorkspaceCallback;
import org.pentaho.mantle.client.perspective.workspace.WorkspacePerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class SolutionBrowserPerspective extends HorizontalPanel implements IPerspective, IFileItemCallback, IWorkspaceCallback {

  private static final String FRAME_ID_PRE = "frame_"; //$NON-NLS-1$
  private static int frameIdCount = 0;
  private static final String defaultSplitPosition = "220px"; //$NON-NLS-1$
  private static PopupPanel popupMenu = new PopupPanel(true);

  private ClassicNavigatorView classicNavigatorView = new ClassicNavigatorView();
  private HorizontalSplitPanel solutionNavigatorAndContentPanel = new HorizontalSplitPanel(MantleImages.images);
  private VerticalSplitPanel solutionNavigatorPanel = new VerticalSplitPanel(MantleImages.images);
  private SolutionTree solutionTree = new SolutionTree(this);
  private FilesListPanel filesListPanel = new FilesListPanel(this);
  private FileItem selectedFileItem;
  private DeckPanel contentPanel = new DeckPanel();
  private LaunchPanel launchPanel = new LaunchPanel(this);
  private WorkspacePerspective workspacePanel = null;
  private String newAnalysisViewOverrideCommandUrl;
  private String newAnalysisViewOverrideCommandTitle;
  private String newReportOverrideCommandUrl;
  private String newReportOverrideCommandTitle;
  
  protected TabPanel contentTabPanel = new TabPanel();
  private HashMap<Widget, TabWidget> contentTabMap = new HashMap<Widget, TabWidget>();
  private boolean hasBeenLoaded = false;
  private IPerspectiveCallback perspectiveCallback;
  private Document solutionDocument;
  private boolean showSolutionBrowser = false;
  private boolean explorerMode = false;
  private boolean isAdministrator = false;
  private MenuBar favoritesGroupMenuBar = new MenuBar(true);
  private List<Bookmark> bookmarks;
  private List<SolutionBrowserListener> listeners = new ArrayList<SolutionBrowserListener>();

  private List<FileTypeEnabledOptions> enabledOptionsList = new ArrayList<FileTypeEnabledOptions>();
  private List<ContentTypePlugin> contentTypePluginList = new ArrayList<ContentTypePlugin>();
  public static final int CURRENT_SELECTED_TAB = -1;
  // commands
  Command ShowWorkSpaceCommand = new Command() {
    public void execute() {
      toggleWorkspace();
    }
  };

  public void toggleWorkspace() {
    showWorkspaceMenuItem.setChecked(!showWorkspaceMenuItem.isChecked());
    if (showWorkspaceMenuItem.isChecked()) {
      showWorkspace();
    } else {
      showLaunchOrContent();
    }
  }

  Command ToggleClassicViewCommand = new Command() {

    public void execute() {
      // update view menu
      explorerMode = !explorerMode;
      buildUI();
      installViewMenu(perspectiveCallback);
    }

  };
  Command ToggleLocalizedNamesCommand = new Command() {

    public void execute() {
      setUseLocalizedFileNames(!solutionTree.showLocalizedFileNames);
      // update view menu
      installViewMenu(perspectiveCallback);

      // update setting
      AsyncCallback callback = new AsyncCallback() {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(Object result) {
        }

      };
      MantleServiceCache.getService().setShowLocalizedFileNames(solutionTree.showLocalizedFileNames, callback);
    }
  };

  Command ShowHideFilesCommand = new Command() {
    public void execute() {
      setShowHiddenFiles(!solutionTree.showHiddenFiles);
      // update view menu
      installViewMenu(perspectiveCallback);

      // update setting
      AsyncCallback callback = new AsyncCallback() {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(Object result) {
        }

      };
      MantleServiceCache.getService().setShowHiddenFiles(solutionTree.showHiddenFiles, callback);
    }
  };

  Command UseDescriptionCommand = new Command() {
    public void execute() {
      setUseDescriptions(!solutionTree.useDescriptionsForTooltip);
      // update view menu
      installViewMenu(perspectiveCallback);

      // update setting
      AsyncCallback<Void> callback = new AsyncCallback<Void>() {

        public void onFailure(Throwable caught) {
        }

        public void onSuccess(Void result) {
        }

      };
      MantleServiceCache.getService().setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS, ""+solutionTree.useDescriptionsForTooltip, callback);
    }
  };
  
  
  // menu items
  CheckBoxMenuItem showWorkspaceMenuItem = new CheckBoxMenuItem(Messages.getString("workspace"), ShowWorkSpaceCommand); //$NON-NLS-1$
  CheckBoxMenuItem showHiddenFilesMenuItem = new CheckBoxMenuItem(Messages.getString("showHiddenFiles"), ShowHideFilesCommand); //$NON-NLS-1$
  CheckBoxMenuItem showLocalizedFileNamesMenuItem = new CheckBoxMenuItem(Messages.getString("showLocalizedFileNames"), ToggleLocalizedNamesCommand); //$NON-NLS-1$
  CheckBoxMenuItem showSolutionBrowserMenuItem = new CheckBoxMenuItem(Messages.getString("showSolutionBrowser"), new ShowBrowserCommand(this)); //$NON-NLS-1$
  CheckBoxMenuItem useDescriptionsMenuItem = new CheckBoxMenuItem(Messages.getString("useDescriptionsForTooltips"), UseDescriptionCommand); //$NON-NLS-1$

  TreeListener treeListener = new TreeListener() {

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void onTreeItemSelected(TreeItem item) {
      filesListPanel.populateFilesList(SolutionBrowserPerspective.this, solutionTree, selectedFileItem, item);
      filesListPanel.getToolbar().setEnabled(false);
    }

    public void onTreeItemStateChanged(TreeItem item) {
      solutionTree.setSelectedItem(item, false);
    }

  };

  private static SolutionBrowserPerspective instance;

  public SolutionBrowserPerspective(final IPerspectiveCallback perspectiveCallback) {
    instance = this;
    this.perspectiveCallback = perspectiveCallback;
    solutionTree.addTreeListener(treeListener);
    workspacePanel = new WorkspacePerspective(this, perspectiveCallback);
    showWorkspaceMenuItem.setChecked(false);
    contentTabPanel.addTabListener(new TabListener() {
      private int previousIndex;

      public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
        previousIndex = contentTabPanel.getTabBar().getSelectedTab();
        return true;
      }

      public void onTabSelected(SourcesTabEvents sender, int tabIndex) {
        fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.DESELECT, previousIndex);
        fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, tabIndex);
        if (previousIndex != tabIndex) {
          Widget tabPanel = contentTabPanel.getWidget(tabIndex);
          Window.setTitle(Messages.getString("productName") + " - " + getCurrentTab().getText()); //$NON-NLS-1$ //$NON-NLS-2$

          if (tabPanel instanceof ReloadableIFrameTabPanel) {
            NamedFrame frame = ((ReloadableIFrameTabPanel)tabPanel).getFrame();
            frame.setVisible(true);
            refreshIfPDF(((ReloadableIFrameTabPanel)tabPanel));
          }
        }
        for (int i = 0; i < tabIndex; i++) {
          hideFrame(i);
        }
        for (int i = tabIndex + 1; i < contentTabPanel.getTabBar().getTabCount(); i++) {
          hideFrame(i);
        }
      }
    });
    buildUI();
  }
  
  public FileTypeEnabledOptions getEnabledOptions(String filename) {
    for (FileTypeEnabledOptions option : enabledOptionsList) {
      if (option.isSupportedFile(filename)) {
        return option;
      }
    }
    return null;
  }
  
  public ContentTypePlugin getContentTypePlugin(String filename) {
    for (ContentTypePlugin plugin : contentTypePluginList) {
      if (plugin.isSupportedFile(filename)) {
        return plugin;
      }
    }
    return null;
  }

  public void hideFrame(int tabIndex) {
    Frame frame = ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(tabIndex)).getFrame();
    frame.setVisible(false);
  }

  public Command getNewAnalysisViewCommand() {
    if (newAnalysisViewOverrideCommandUrl == null) {
      return new AnalysisViewCommand(this);
    } else {
      return new UrlCommand(this, newAnalysisViewOverrideCommandUrl, newAnalysisViewOverrideCommandTitle);
    }
  }

  public Command getNewReportCommand() {
    if (newReportOverrideCommandUrl == null) {
      return new WAQRCommand(this); 
    } else {
      return new UrlCommand(this, newReportOverrideCommandUrl, newReportOverrideCommandTitle);
    }
  }
  
  public void buildUI() {
    clear();
    if (explorerMode) {

      solutionNavigatorPanel.setHeight("100%"); //$NON-NLS-1$
      // ----- Create the top panel ----

      BrowserToolbar browserToolbar = new BrowserToolbar(this);
      browserToolbar.setHeight("28px"); //$NON-NLS-1$
      browserToolbar.setWidth("100%"); //$NON-NLS-1$

      FlowPanel topPanel = new FlowPanel();
      SimplePanel toolbarWrapper = new SimplePanel();
      toolbarWrapper.add(browserToolbar);
      toolbarWrapper.setStyleName("files-toolbar"); //$NON-NLS-1$
      topPanel.add(toolbarWrapper);

      SimplePanel filesListWrapper = new SimplePanel() {
        public void onBrowserEvent(Event event) {
          if (((DOM.eventGetButton(event) & Event.BUTTON_RIGHT) == Event.BUTTON_RIGHT && (DOM.eventGetType(event) & Event.ONMOUSEUP) == Event.ONMOUSEUP)) {
            // bring up a popup with 'create new folder' option
            final int left = Window.getScrollLeft() + DOM.eventGetClientX(event);
            final int top = Window.getScrollTop() + DOM.eventGetClientY(event);
            handleRightClick(left, top);
            event.cancelBubble(true);
          } else {
            super.onBrowserEvent(event);
          }
        }
      };
      filesListWrapper.sinkEvents(Event.MOUSEEVENTS);
      filesListWrapper.add(solutionTree);
      filesListWrapper.setStyleName("files-list-panel"); //$NON-NLS-1$
      topPanel.add(filesListWrapper);
      solutionTree.getElement().getStyle().setProperty("marginTop", "29px"); //$NON-NLS-1$ //$NON-NLS-2$

      this.setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  

      // --------------------------------

      solutionNavigatorPanel.setTopWidget(topPanel);
      filesListPanel.setWidth("100%"); //$NON-NLS-1$
      solutionNavigatorPanel.setBottomWidget(filesListPanel);
      solutionNavigatorPanel.setSplitPosition("60%"); //$NON-NLS-1$
      solutionNavigatorAndContentPanel.setLeftWidget(solutionNavigatorPanel);
      solutionNavigatorAndContentPanel.setRightWidget(contentPanel);
      contentPanel.setAnimationEnabled(true);
      contentPanel.add(workspacePanel);
      contentPanel.add(launchPanel);
      contentPanel.add(contentTabPanel);
      showLaunchOrContent();
      if (showSolutionBrowser) {
        solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
      } else {
        solutionNavigatorAndContentPanel.setSplitPosition("0px"); //$NON-NLS-1$
      }
      contentPanel.setHeight("100%"); //$NON-NLS-1$
      contentPanel.setWidth("100%"); //$NON-NLS-1$
      contentTabPanel.setHeight("100%"); //$NON-NLS-1$
      contentTabPanel.setWidth("100%"); //$NON-NLS-1$
      setHeight("100%"); //$NON-NLS-1$
      setWidth("100%"); //$NON-NLS-1$
      add(solutionNavigatorAndContentPanel);

      ElementUtils.removeScrollingFromSplitPane(solutionNavigatorPanel);

      ElementUtils.removeScrollingFromUpTo(solutionNavigatorAndContentPanel.getLeftWidget().getElement(), solutionNavigatorAndContentPanel.getElement());
    } else {
      // load classic view
      // we've got the tree
      setHeight("100%"); //$NON-NLS-1$
      setWidth("100%"); //$NON-NLS-1$
      classicNavigatorView.setHeight("100%"); //$NON-NLS-1$
      classicNavigatorView.setWidth("100%"); //$NON-NLS-1$
      add(classicNavigatorView);
    }
  }

  private void handleRightClick(int left, int top) {
    popupMenu.setPopupPosition(left, top);
    MenuBar menuBar = new MenuBar(true);
    menuBar.setAutoOpen(true);
    menuBar.addItem(new MenuItem(Messages.getString("createNewFolderEllipsis"), new FileCommand(FileCommand.COMMAND.CREATE_FOLDER, popupMenu, this)));
    popupMenu.setWidget(menuBar);
    popupMenu.show();
  }

  public boolean isWorkspaceShowing() {
    if (contentPanel.getWidgetCount() > 0) {
      return contentPanel.getWidgetIndex(workspacePanel) == contentPanel.getVisibleWidget();
    }
    return false;
  }

  public void showWorkspace() {
    workspacePanel.refreshWorkspace();
    contentPanel.showWidget(contentPanel.getWidgetIndex(workspacePanel));
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED,CURRENT_SELECTED_TAB); // TODO Not sure what event type to pass
  }

  public void showLaunchOrContent() {
    int showIndex = -1;
    if (contentTabPanel.getWidgetCount() == 0) {
      showIndex = contentPanel.getWidgetIndex(launchPanel);
      Window.setTitle(Messages.getString("productName")); //$NON-NLS-1$
    } else {
      showIndex = contentPanel.getWidgetIndex(contentTabPanel);
    }
    int selectedTab = -1;
    if (showIndex != -1) {
      contentPanel.showWidget(showIndex);

      // There's a bug when re-showing a tab containing a PDF. Under Firefox it doesn't render, so we force a reload
      selectedTab = contentTabPanel.getTabBar().getSelectedTab();
      if (selectedTab > -1) {
        Widget tabContent = contentTabPanel.getWidget(selectedTab);
        if (tabContent instanceof ReloadableIFrameTabPanel) {
          refreshIfPDF((ReloadableIFrameTabPanel)tabContent);
        }
      }

    }
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, selectedTab); // TODO Not sure what event type to pass
  }

  private boolean existingTabMatchesName(String name) {
    String key = "title=\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$

    NodeList<com.google.gwt.dom.client.Element> divs = contentTabPanel.getTabBar().getElement().getElementsByTagName("div"); //$NON-NLS-1$

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
  
  public void showNewURLTab(final String tabName, final String tabTooltip, final String url) {
    final int elementId = contentTabPanel.getWidgetCount();
    String frameName = getUniqueFrameName();
    ReloadableIFrameTabPanel panel = new ReloadableIFrameTabPanel(frameName, url);

    Frame frame = panel.getFrame();
    frame.getElement().setAttribute("id", frameName); //$NON-NLS-1$
    frame.setStyleName("gwt-Frame"); //$NON-NLS-1$
    panel.add(frame);
    frame.setWidth("100%"); //$NON-NLS-1$
    frame.setHeight("100%"); //$NON-NLS-1$

    String finalTabName = tabName;
    String finalTabTooltip = tabTooltip;
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
          finalTabName = tabName + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          finalTabTooltip = tabTooltip + " (" + counter + ")"; //$NON-NLS-1$ //$NON-NLS-2$
          break;
        }
      }
    }

    TabWidget tabWidget = new TabWidget(finalTabName, finalTabTooltip, this, contentTabPanel, panel);
    contentTabMap.put(panel, tabWidget);
    contentTabPanel.add(panel, tabWidget);
    contentTabPanel.selectTab(elementId);

    final List<com.google.gwt.dom.client.Element> parentList = new ArrayList<com.google.gwt.dom.client.Element>();
    com.google.gwt.dom.client.Element parent = frame.getElement();
    while (parent != contentTabPanel.getElement()) {
      parentList.add(parent);
      parent = parent.getParentElement();
    }
    Collections.reverse(parentList);
    for (int i = 1; i < parentList.size(); i++) {
      parentList.get(i).getStyle().setProperty("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    showLaunchOrContent();

    // update state to workspace state flag
    showWorkspaceMenuItem.setChecked(false);
    // fire
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, contentTabPanel.getTabBar().getSelectedTab());

    perspectiveCallback.activatePerspective(this);
  }

  public TabWidget getCurrentTab() {
    return contentTabMap.get(contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab()));
  }

  public TabWidget getTabForWidget(Widget tabWidget) {
    return contentTabMap.get(contentTabPanel.getWidget(contentTabPanel.getWidgetIndex(tabWidget)));
  }

  public void openFile(final FileCommand.COMMAND mode) {
    String name = selectedFileItem.getName();
    if (name.endsWith(".xaction")) { //$NON-NLS-1$
      //
      // Commented out analysis view check, JPivot now supports multiple views
      // in a single session.  Leaving the code here during testing phase.
      //
//      if (mode == FileCommand.COMMAND.RUN) {
//        final Widget openAnalysisView = getOpenAnalysisView();
//        if (openAnalysisView != null && name.endsWith(".analysisview.xaction")) { //$NON-NLS-1$
//          String actionName = getTabForWidget(openAnalysisView).getText();
//          Widget content = new HTML(Messages.getString("analysisViewIsOpen", actionName)); //$NON-NLS-1$
//          PromptDialogBox dialog = new PromptDialogBox(Messages.getString("open"), Messages.getString("ok"), Messages.getString("cancel"), false, true, content); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//          dialog.setCallback(new IDialogCallback() {
//
//            public void cancelPressed() {
//              // do nothing
//            }
//
//            public void okPressed() {
//              contentTabPanel.remove(openAnalysisView);
//              executeActionSequence(mode);
//            }
//
//          });
//          dialog.center();
//          dialog.show();
//          return;
//        } else {
//          executeActionSequence(mode);
//        }
//      } else {
      executeActionSequence(mode);
//      }
    } else if (name.endsWith(".url")) { //$NON-NLS-1$
      if (mode == FileCommand.COMMAND.NEWWINDOW) {
        Window.open(selectedFileItem.getURL(), "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, selectedFileItem.getURL());
      }
    } else {
      
      // see if this file is a plugin
      ContentTypePlugin plugin = getContentTypePlugin(selectedFileItem.getName());
      if (plugin != null && plugin.hasCommand(mode)) {
        // load the editor for this plugin
        String url = plugin.getCommandUrl(selectedFileItem, mode);
        if (GWT.isScript()) {
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            // we have a URL so open it in a new tab
            if (mode == FileCommand.COMMAND.NEWWINDOW) {
              Window.open(url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              UrlCommand cmd = new UrlCommand(this, url, selectedFileItem.localizedName);
              cmd.execute();
            }
          }
        } else {
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            
            // we have a URL so open it in a new tab
            String updateUrl = "/MantleService?passthru=" + url; //$NON-NLS-1$

            if (mode == FileCommand.COMMAND.NEWWINDOW) {
              Window.open(updateUrl, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, updateUrl);
            }
          }          
        }
      } else {
      
        // see if this file has a URL
        String url = selectedFileItem.getURL();
        if (url != null && !"".equals(url)) { //$NON-NLS-1$
          // we have a URL so open it in a new tab
          if (mode == FileCommand.COMMAND.NEWWINDOW) {
            Window.open(selectedFileItem.getURL(), "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, selectedFileItem.getURL());
          }
        }
      }

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      this.getCurrentFrame().setFileInfo(fileInfo);
    }
  }

  public enum OPEN_METHOD {
    OPEN, EDIT, SHARE, SCHEDULE
  }

  public void openFile(String path, String name, String localizedFileName, OPEN_METHOD openMethod) {
    List<String> pathSegments = new ArrayList<String>();
    if (path != null) {
      if (path.startsWith("/")) { //$NON-NLS-1$
        path = path.substring(1);
      }
      StringTokenizer st = new StringTokenizer(path, '/');
      for (int i = 0; i < st.countTokens(); i++) {
        pathSegments.add(st.tokenAt(i));
      }
    }

    String repoPath = ""; //$NON-NLS-1$
    for (int i = 1; i < pathSegments.size(); i++) {
      repoPath += "/" + pathSegments.get(i); //$NON-NLS-1$
    }

    final boolean fileExists = solutionTree.doesFileExist(pathSegments, name);
    if (!fileExists) {
      final MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("open"), Messages.getString("fileDoesNotExist", name), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$

      dialogBox.setCallback(new IDialogCallback() {
        public void cancelPressed() {
        }

        public void okPressed() {
          dialogBox.hide();
          (new OpenFileCommand(SolutionBrowserPerspective.this)).execute();
        }
      });

      dialogBox.center();
      return;
    }

    selectedFileItem = new FileItem(name, localizedFileName, localizedFileName, pathSegments.get(0), repoPath, "", null, null, null, false, null); //$NON-NLS-1$

    // TODO: Create a more dynamic filter interface
    if (openMethod == OPEN_METHOD.SCHEDULE) {
      if (selectedFileItem != null) {
        if (selectedFileItem.getName() != null) {
          if (!selectedFileItem.getName().endsWith(".xaction") || selectedFileItem.getName().endsWith(FileItem.ANALYSIS_VIEW_SUFFIX)) { //$NON-NLS-1$
            final MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("open"), Messages.getString("scheduleInvalidFileType", selectedFileItem.getName()), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$

            dialogBox.setCallback(new IDialogCallback() {
              public void cancelPressed() {
              }

              public void okPressed() {
                dialogBox.hide();
                (new OpenFileCommand(SolutionBrowserPerspective.this)).execute(OPEN_METHOD.SCHEDULE);
              }
            });

            dialogBox.center();
            return;
          }
        }
      }
    }

    FileTreeItem fileTreeItem = solutionTree.getTreeItem(pathSegments);
    pathSegments.add(name);

    List<FileTreeItem> allNodes = solutionTree.getAllNodes();
    for (FileTreeItem item : allNodes) {
      item.setSelected(false);
    }
    solutionTree.setSelectedItem(fileTreeItem, true);

    TreeItem tmpTreeItem = fileTreeItem;
    while (tmpTreeItem != null) {
      tmpTreeItem.setState(true);
      tmpTreeItem = tmpTreeItem.getParentItem();
    }
    fileTreeItem.setSelected(true);

    List<com.google.gwt.xml.client.Element> files = (List<com.google.gwt.xml.client.Element>) fileTreeItem.getUserObject();
    if (files != null) {
      for (com.google.gwt.xml.client.Element fileElement : files) {
        if (name.equals(fileElement.getAttribute("name")) || name.equals(fileElement.getAttribute("localized-name"))) { //$NON-NLS-1$ //$NON-NLS-2$
          selectedFileItem.setURL(fileElement.getAttribute("url")); //$NON-NLS-1$
        }
      }
    }

    if (openMethod == OPEN_METHOD.EDIT) {
      editFile();
    } else if (openMethod == OPEN_METHOD.OPEN) {
      openFile(FileCommand.COMMAND.RUN);
    } else if (openMethod == OPEN_METHOD.SCHEDULE) {
      createSchedule();
    } else if (openMethod == OPEN_METHOD.SHARE) {
      shareFile();
    }
  }

  public void editFile() {
    if (selectedFileItem.getName().endsWith(".waqr.xaction")) { //$NON-NLS-1$
      String filename = selectedFileItem.getName().substring(0, selectedFileItem.getName().indexOf(".waqr.xaction")) + ".waqr.xreportspec"; //$NON-NLS-1$ //$NON-NLS-2$
      String url = "adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename=" + filename; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + filename;
      }

      // See if it's already loaded
      for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
        Widget w = contentTabPanel.getWidget(i);
        if (w instanceof ReloadableIFrameTabPanel && ((ReloadableIFrameTabPanel) w).url.endsWith(url)) {
          // Already up, select and exit
          contentTabPanel.selectTab(i);
          return;
        }
      }
      showNewURLTab(
          Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), url); //$NON-NLS-1$ //$NON-NLS-2$

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      this.getCurrentFrame().setFileInfo(fileInfo);

    } else if (selectedFileItem.getName().endsWith(".analysisview.xaction")) { //$NON-NLS-1$
      openFile(COMMAND.RUN);
    } else {
      // check to see if a plugin supports editing
      ContentTypePlugin plugin = getContentTypePlugin(selectedFileItem.getName());
      if (plugin != null && plugin.hasCommand(COMMAND.EDIT)) {
        
        // load the editor for this plugin
        
        String editUrl = plugin.getCommandUrl(selectedFileItem, COMMAND.EDIT);
        // See if it's already loaded
        for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
          Widget w = contentTabPanel.getWidget(i);
          if (w instanceof ReloadableIFrameTabPanel && ((ReloadableIFrameTabPanel) w).url.endsWith(editUrl)) {
            // Already up, select and exit
            contentTabPanel.selectTab(i);
            return;
          }
        }
        
        
        if (GWT.isScript()) {
          showNewURLTab(
              Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), editUrl); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            // we have a URL so open it in a new tab
            String updateUrl = "/MantleService?passthru=" + editUrl; //$NON-NLS-1$
            showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, updateUrl);
        }
        
        
        
        // Store representation of file in the frame for reference later when save is called
        SolutionFileInfo fileInfo = new SolutionFileInfo();
        fileInfo.setName(selectedFileItem.getName());
        fileInfo.setSolution(selectedFileItem.getSolution());
        fileInfo.setPath(selectedFileItem.getPath());
        this.getCurrentFrame().setFileInfo(fileInfo);

        
      } else {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
            Messages.getString("cannotEditFileType"), //$NON-NLS-1$
            true, false, true);
        dialogBox.center();
      }
    }
  }

  /**
   * this method launches the experimental action editor from Mantle.
   * 
   * Pentaho's action editor is located today at http://code.google.com/p/pentaho-actioneditor
   * 
   */
  public void editActionFile() {
    if (MantleApplication.showAdvancedFeatures) {
      String fullPath = null;
      if (selectedFileItem.getSolution().endsWith("/") || selectedFileItem.getPath().startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
        fullPath = selectedFileItem.getSolution() + selectedFileItem.getPath() + "/" + selectedFileItem.getName(); //$NON-NLS-1$
      } else {
        fullPath = selectedFileItem.getSolution() + "/" + selectedFileItem.getPath() + "/" + selectedFileItem.getName(); //$NON-NLS-1$ //$NON-NLS-2$
      }
      String url = "actioneditor/actioneditor.html?actionSequence=" + fullPath; //$NON-NLS-1$
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/actioneditor/actioneditor.html?actionSequence=" + fullPath; //$NON-NLS-1$
      }

      // See if it's already loaded
      for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
        Widget w = contentTabPanel.getWidget(i);
        if (w instanceof ReloadableIFrameTabPanel && ((ReloadableIFrameTabPanel) w).url.endsWith(url)) {
          // Already up, select and exit
          contentTabPanel.selectTab(i);
          return;
        }
      }
      showNewURLTab(
          Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), Messages.getString("editingColon") + selectedFileItem.getLocalizedName(), url); //$NON-NLS-1$ //$NON-NLS-2$

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      this.getCurrentFrame().setFileInfo(fileInfo);
    }
  }

  public void createNewFolder() {
    // this NewFolderCommand creates new "root" folders, so we do not
    // pass the SolutionTree in (if we did, it would create a folder
    // branched off of anything selected).
    NewFolderCommand cmd = new NewFolderCommand(null);
    cmd.execute();
  }

  public void deleteFile() {
    // delete file
    final FileItem selectedItem = getSelectedFileItem();
    String url = ""; //$NON-NLS-1$
    if (GWT.isScript()) {
      String windowpath = Window.Location.getPath();
      if (!windowpath.endsWith("/")) { //$NON-NLS-1$
        windowpath = windowpath.substring(0, windowpath.lastIndexOf("/") + 1); //$NON-NLS-1$
      }
      url = windowpath + "SolutionRepositoryService?component=delete&solution=" + selectedItem.getSolution() + "&path=" + selectedItem.getPath() + "&name=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + selectedItem.getName();
    } else if (!GWT.isScript()) {
      url = "http://localhost:8080/pentaho/SolutionRepositoryService?component=delete&solution=" + selectedItem.getSolution() + "&path=" //$NON-NLS-1$ //$NON-NLS-2$
          + selectedItem.getPath() + "&name=" + selectedItem.getName(); //$NON-NLS-1$
    }
    final String myurl = url;
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString("deleteQuestion", selectedItem.getLocalizedName()))); //$NON-NLS-1$
    final PromptDialogBox deleteConfirmDialog = new PromptDialogBox(
        Messages.getString("deleteConfirm"), Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final IDialogCallback callback = new IDialogCallback() {

      public void cancelPressed() {
        deleteConfirmDialog.hide();
      }

      public void okPressed() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, myurl);
        try {
          builder.sendRequest(null, new RequestCallback() {

            public void onError(Request request, Throwable exception) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotDelete", selectedItem.getName()), //$NON-NLS-1$ //$NON-NLS-2$
                  false, false, true);
              dialogBox.center();
            }

            public void onResponseReceived(Request request, Response response) {
              Document resultDoc = (Document) XMLParser.parse((String) (String) response.getText());
              boolean result = "true".equals(resultDoc.getDocumentElement().getFirstChild().getNodeValue()); //$NON-NLS-1$
              if (result) {
                RefreshRepositoryCommand cmd = new RefreshRepositoryCommand(SolutionBrowserPerspective.this);
                cmd.execute(false);
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
                    Messages.getString("couldNotDelete", selectedItem.getName()), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }

          });
        } catch (RequestException e) {
        }
      }
    };
    deleteConfirmDialog.setCallback(callback);
    deleteConfirmDialog.center();
  }

  void executeActionSequence(final FileCommand.COMMAND mode) {
    // open in content panel
    // http://localhost:8080/pentaho/ViewAction?solution=samples&path=reporting&action=JFree_XQuery_report.xaction

    final AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        // if we are still authenticated, perform the action, otherwise present login

        String url = null;
        String path = selectedFileItem.getPath();
        if (path.startsWith("/")) { //$NON-NLS-1$
          path = path.substring(1);
        }
        if (GWT.isScript()) {
          url = "ViewAction?solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action=" + selectedFileItem.getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          String mypath = Window.Location.getPath();
          if (!mypath.endsWith("/")) { //$NON-NLS-1$
            mypath = mypath.substring(0, mypath.lastIndexOf("/") + 1); //$NON-NLS-1$
          }
          mypath = mypath.replaceAll("/mantle/", "/"); //$NON-NLS-1$ //$NON-NLS-2$
          if (!mypath.endsWith("/")) { //$NON-NLS-1$
            mypath = "/" + mypath; //$NON-NLS-1$
          }
          url = mypath + url;
        } else {
          url = "/MantleService?passthru=ViewAction&solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action=" + selectedFileItem.getName() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              + "&userid=joe&password=password"; //$NON-NLS-1$
        }

        if (mode == FileCommand.COMMAND.BACKGROUND) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), //$NON-NLS-1$
              Messages.getString("backgroundExecutionWarning"), //$NON-NLS-1$
              true, false, true);
          dialogBox.center();

          url += "&background=true"; //$NON-NLS-1$

          RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
          try {
            builder.sendRequest(null, new RequestCallback() {

              public void onError(Request request, Throwable exception) {
                MessageDialogBox dialogBox = new MessageDialogBox(
                    Messages.getString("error"), Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
                dialogBox.center();
              }

              public void onResponseReceived(Request request, Response response) {
              }

            });
          } catch (RequestException e) {
          }
        } else if (mode == FileCommand.COMMAND.NEWWINDOW) {
          // popup blockers might attack this
          Window.open(url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (mode == FileCommand.COMMAND.SUBSCRIBE) {
          final String myurl = url + "&subscribepage=yes"; //$NON-NLS-1$
          AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              MessageDialogBox dialogBox = new MessageDialogBox(
                  Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
              dialogBox.center();
            }

            public void onSuccess(Boolean subscribable) {

              if (subscribable) {
                showNewURLTab(selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(), myurl);

                // Store representation of file in the frame for reference later when save is called
                // getCurrentFrame().setFileInfo(fileInfo);

              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), //$NON-NLS-1$
                    Messages.getString("noSchedulePermission"), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }
          };
          MantleServiceCache.getService().hasAccess(selectedFileItem.getSolution(), selectedFileItem.getPath(), selectedFileItem.getName(), 3, callback);
        } else {
          showNewURLTab(selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(), url);
        }
      }

      public void onFailure(Throwable caught) {
        doLogin(mode);
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  /**
   * Display the login screen and and validate the credentials supplied by the user
   * if the credentials are correct, the execute method is being invoked other wise
   * error dialog is being display. On clicking ok button on the dialog box, login
   * screen is displayed again and process is repeated until the user click cancel 
   * or user is successfully authenticated
   *  */
  private void doLogin(final FileCommand.COMMAND mode) {
    MantleLoginDialog.performLogin(new AsyncCallback<Object>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(
            Messages.getString("error"), Messages.getString("invalidLogin"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.setCallback(new IDialogCallback() {
          public void cancelPressed() {
            // do nothing
          }

          public void okPressed() {
            doLogin(mode);
          }
          
        });
        dialogBox.center();
      }

      public void onSuccess(Object result) {
        executeActionSequence(mode);
      }

    });
  }

  void showScheduleDialog(final SolutionFileInfo fileInfo) {
    final AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        // if we are still authenticated, perform the action, otherwise present login
          AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetFileProperties"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
              dialogBox.center();
            }

            public void onSuccess(Boolean subscribable) {
                 
                if (subscribable) {
                  NewScheduleDialog dialog = new NewScheduleDialog(fileInfo.getSolution(), fileInfo.getPath(), fileInfo.getName());
                  dialog.center();
                } else {
                  MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), //$NON-NLS-1$
                      Messages.getString("noSchedulePermission"), false, false, true); //$NON-NLS-1$
                  dialogBox.center();
                }
              } 
          };
          MantleServiceCache.getService().hasAccess(selectedFileItem.getSolution(), selectedFileItem.getPath(), selectedFileItem.getName(), 3, callback);

      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            showScheduleDialog(fileInfo);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }
  
  public void fetchSolutionDocument(final boolean showSuccess, final boolean collapse) {
    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        RequestBuilder builder = null;
        if (GWT.isScript()) {
          String path = Window.Location.getPath();
          if (!path.endsWith("/")) { //$NON-NLS-1$
            path = path.substring(0, path.lastIndexOf("/") + 1); //$NON-NLS-1$
          }
          builder = new RequestBuilder(RequestBuilder.GET, path + "SolutionRepositoryService?component=getSolutionRepositoryDoc"); //$NON-NLS-1$
        } else {
          builder = new RequestBuilder(RequestBuilder.GET,
              "/MantleService?passthru=SolutionRepositoryService&component=getSolutionRepositoryDoc&userid=joe&password=password"); //$NON-NLS-1$
        }

        RequestCallback callback = new RequestCallback() {

          public void onError(Request request, Throwable exception) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetRepositoryDocument"), false, //$NON-NLS-1$ //$NON-NLS-2$
                false, true);
            dialogBox.center();
          }

          public void onResponseReceived(Request request, Response response) {
            // ok, we have a repository document, we can build the GUI
            // consider caching the document
            solutionDocument = (Document) XMLParser.parse((String) (String) response.getText());
            
            // flat that we have the document so that other things might start to use it (PDB-500)
            flagSolutionDocumentLoaded();
            
            // update tree
            solutionTree.buildSolutionTree(solutionDocument);

            TreeItem selectedItem = solutionTree.getSelectedItem();
            
            // IE has difficulty rendering the tree if the nodes have changed. We can get around 
            // that by collapsing the top level nodes and then re-opening them.
            for (TreeItem item : solutionTree.getAllNodes()) {
              if(item.getState()){
                item.setState(false);
                if (collapse == false ) {
                  item.setState(true);
                }
              }
            }

            final List<TreeItem> items = new ArrayList<TreeItem>();
            TreeItem tmpItem = selectedItem;
            while (tmpItem != null) {
              items.add(tmpItem);
              tmpItem = tmpItem.getParentItem();
            }
            Collections.reverse(items);
            for (TreeItem item : items) {
              item.setState(true);
              item.setSelected(false);
            }
            selectedItem.setSelected(true);
            
            // update classic view
            classicNavigatorView.setSolutionDocument(solutionDocument);
            classicNavigatorView.buildSolutionNavigator();
            if (showSuccess) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("solutionBrowserRefreshed"), false, false, //$NON-NLS-1$ //$NON-NLS-2$
                  true);
              dialogBox.center();
            }
          }

        };
        try {
          builder.sendRequest(null, callback);
        } catch (RequestException e) {
          MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getString("error"), Messages.getString("couldNotGetRepositoryDocument"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(Boolean result) {
            fetchSolutionDocument(showSuccess, collapse);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void loadPerspective(boolean force, boolean showStatus) {
    if (!hasBeenLoaded) {
      fetchSolutionDocument(showStatus, /*collapse*/true);
      hasBeenLoaded = true;
    } else if (force) {
      fetchSolutionDocument(showStatus, /*collapse*/false);
    }
    installViewMenu(perspectiveCallback);
  }

  public void unloadPerspective() {
  }

  public void refreshPerspective(boolean showStatus) {
    loadPerspective(true, showStatus);
  }

  public FileItem getSelectedFileItem() {
    return selectedFileItem;
  }

  public void setSelectedFileItem(FileItem fileItem) {
    selectedFileItem = fileItem;
  }

  public void selectNextItem(FileItem currentItem) {
    if (currentItem == null) {
      return;
    }
    int myIndex = -1;
    for (int i = 0; i < filesListPanel.getFileCount(); i++) {
      FileItem fileItem = filesListPanel.getFileItem(i);
      if (fileItem == currentItem) {
        myIndex = i;
      }
    }
    if (myIndex >= 0 && myIndex < filesListPanel.getFileCount() - 1) {
      currentItem.setStyleName("fileLabel"); //$NON-NLS-1$
      FileItem nextItem = filesListPanel.getFileItem(myIndex + 1);
      nextItem.setStyleName("fileLabelSelected"); //$NON-NLS-1$
      setSelectedFileItem(nextItem);
    }
  }

  public void selectPreviousItem(FileItem currentItem) {
    if (currentItem == null) {
      return;
    }
    int myIndex = -1;
    for (int i = 0; i < filesListPanel.getFileCount(); i++) {
      FileItem fileItem = filesListPanel.getFileItem(i);
      if (fileItem == currentItem) {
        myIndex = i;
      }
    }
    if (myIndex > 0 && myIndex < filesListPanel.getFileCount()) {
      currentItem.setStyleName("fileLabel"); //$NON-NLS-1$
      FileItem nextItem = filesListPanel.getFileItem(myIndex - 1);
      nextItem.setStyleName("fileLabelSelected"); //$NON-NLS-1$
      setSelectedFileItem(nextItem);
    }
  }

  public void createSchedule() {
    AbstractCommand scheduleCommand = new AbstractCommand() {

      private void schedule() {
        AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

          public void onFailure(Throwable caught) {
            // show error
            final MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), caught.toString(), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }

          public void onSuccess(SolutionFileInfo fileInfo) {
            if (fileInfo.isSubscribable) {
              if (fileInfo.getType().equals(SolutionFileInfo.Type.PLUGIN)) {
                // see if this file is a plugin
                ContentTypePlugin plugin = getContentTypePlugin(fileInfo.getName());
                String url = plugin.getCommandUrl(selectedFileItem, COMMAND.SCHEDULE_NEW);
                String displayName = fileInfo.getLocalizedName();
                if (displayName == null || displayName.length()<1) {
                  displayName = fileInfo.getName();
                }
                showNewURLTab(displayName, displayName, url);
              } else {
                executeActionSequence(FileCommand.COMMAND.SUBSCRIBE);
              }
            } else {
              if (fileInfo.getType().equals(SolutionFileInfo.Type.PLUGIN)) {
                // see if this file is a plugin
                ContentTypePlugin plugin = getContentTypePlugin(fileInfo.getName());
                String url = plugin.getCommandUrl(selectedFileItem, COMMAND.SCHEDULE_NEW);
                if (StringUtils.isEmpty(url)) {
                  // content is not subscribable but the schedule url (subscription) is empty
                  final MessageDialogBox dialogBox = new MessageDialogBox(
                      Messages.getString("open"), Messages.getString("scheduleInvalidFileType", selectedFileItem.getName()), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$

                  dialogBox.setCallback(new IDialogCallback() {
                    public void cancelPressed() {
                    }

                    public void okPressed() {
                      dialogBox.hide();
                    }
                  });

                  dialogBox.center();
                  return;
                }
                // at this point we know that:
                // 1. the file is not subscribable
                // 2. there is a subscribe url in the plugin
                // 3. the intention probably exists for the content to be schedulable
                showScheduleDialog(fileInfo);
              } else {
                showScheduleDialog(fileInfo);
              }
            }
          }
        };
        MantleServiceCache.getService().getSolutionFileInfo(selectedFileItem.getSolution(), selectedFileItem.getPath(), selectedFileItem.getName(), callback);
      }
      
      protected void performOperation() {
        schedule();
      }

      protected void performOperation(boolean feedback) {
        schedule();
      }
      
    };
    scheduleCommand.execute();
  }

  public void loadPropertiesDialog() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, getEnabledOptions(selectedItem.getName()), isAdministrator(), new TabPanel(), null, FilePropertiesDialog.Tabs.GENERAL);
    dialog.showTab(FilePropertiesDialog.Tabs.GENERAL);
    dialog.center();
  }

  public void shareFile() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, getEnabledOptions(selectedItem.getName()), isAdministrator(), new TabPanel(), null, FilePropertiesDialog.Tabs.PERMISSION);
    dialog.showTab(FilePropertiesDialog.Tabs.PERMISSION);
    dialog.center();
  }

  public void setUseLocalizedFileNames(boolean showLocalizedFileNames) {
    solutionTree.setShowLocalizedFileNames(showLocalizedFileNames);
    // update view menu
    installViewMenu(perspectiveCallback);
  }

  public void setShowHiddenFiles(boolean showHiddenFiles) {
    solutionTree.setShowHiddenFiles(showHiddenFiles);
    solutionTree.setSelectedItem(solutionTree.getSelectedItem(), true);

    // update view menu
    installViewMenu(perspectiveCallback);
  }

  public void setUseDescriptions(boolean showDescriptions) {
    solutionTree.setUseDescriptionsForTooltip(showDescriptions);
    solutionTree.setSelectedItem(solutionTree.getSelectedItem(), true);

    // update view menu
    installViewMenu(perspectiveCallback);
  }  
  
  public void installBookmarkGroups(final Map<String, List<Bookmark>> groupMap) {
    favoritesGroupMenuBar.clearItems();
    for (final String groupName : groupMap.keySet()) {
      favoritesGroupMenuBar.addItem(groupName, new Command() {
        public void execute() {
          List<Bookmark> bookmarks = groupMap.get(groupName);
          contentTabPanel.clear();
          for (Bookmark bookmark : bookmarks) {
            showNewURLTab(bookmark.getTitle(), bookmark.getTitle(), bookmark.getUrl());
          }
          contentTabPanel.selectTab(0);
        }
      });
    }
    if (!groupMap.keySet().isEmpty()) {
      favoritesGroupMenuBar.addSeparator();
    }
    favoritesGroupMenuBar.addItem(new MenuItem(Messages.getString("manageGroups"), new Command() { //$NON-NLS-1$
          public void execute() {
            // bring up dialog to edit groups
            UserPreferencesDialog dialog = new UserPreferencesDialog(UserPreferencesDialog.PREFERENCE.FAVORITES);
            dialog.center();
          }
        }));
  }

  public void loadBookmarks() {
    AsyncCallback<List<Bookmark>> callback = new AsyncCallback<List<Bookmark>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotLoadBookmarks"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.center();
      }

      public void onSuccess(List<Bookmark> bookmarks) {
        SolutionBrowserPerspective.this.bookmarks = bookmarks;
        // group these guys in a map
        Map<String, List<Bookmark>> groupMap = new HashMap<String, List<Bookmark>>();
        for (Bookmark bookmark : bookmarks) {
          List<Bookmark> groupList = groupMap.get(bookmark.getGroup());
          if (groupList == null) {
            groupList = new ArrayList<Bookmark>();
            groupMap.put(bookmark.getGroup(), groupList);
          }
          groupList.add(bookmark);
        }
        // install the groupMap on the solution browser perspective
        installBookmarkGroups(groupMap);
      }
    };
    MantleServiceCache.getService().getBookmarks(callback);
  }

  public void installViewMenu(IPerspectiveCallback perspectiveCallback) {
    List<UIObject> viewMenuItems = new ArrayList<UIObject>();

    showLocalizedFileNamesMenuItem.setChecked(solutionTree.showLocalizedFileNames);
    showHiddenFilesMenuItem.setChecked(solutionTree.showHiddenFiles);
    showSolutionBrowserMenuItem.setChecked(showSolutionBrowser);
    useDescriptionsMenuItem.setChecked(solutionTree.useDescriptionsForTooltip);

    if (explorerMode) {
      // viewMenuItems.add(showLocalizedFileNamesMenuItem);
      viewMenuItems.add(showSolutionBrowserMenuItem);
      viewMenuItems.add(showWorkspaceMenuItem);
      // viewMenuItems.add(showHiddenFilesMenuItem);
      viewMenuItems.add(new MenuItemSeparator());
      viewMenuItems.add(useDescriptionsMenuItem);
      if (MantleApplication.showAdvancedFeatures) {
        favoritesGroupMenuBar.setTitle(Messages.getString("favoriteGroups")); //$NON-NLS-1$
        viewMenuItems.add(favoritesGroupMenuBar);
      }
    }

    viewMenuItems.add(new MenuItemSeparator());

    viewMenuItems.add(new MenuItem(Messages.getString("refresh"), new RefreshPerspectiveCommand(this))); //$NON-NLS-1$
    perspectiveCallback.installViewMenu(viewMenuItems);
  }

  public boolean isExplorerViewShowing() {
    return explorerMode;
  }

  public void setExplorerViewShowing(boolean explorerViewShowing) {
    this.explorerMode = explorerViewShowing;
    buildUI();
    // update view menu
    installViewMenu(perspectiveCallback);
  }

  public TabPanel getContentTabPanel() {
    return contentTabPanel;
  }

  public void setContentTabPanel(TabPanel contentTabPanel) {
    this.contentTabPanel = contentTabPanel;
  }

  public Document getSolutionDocument() {
    return solutionDocument;
  }

  public void setSolutionDocument(Document solutionDocument) {
    this.solutionDocument = solutionDocument;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
    solutionTree.setAdministrator(isAdministrator);
  }

  public boolean isNavigatorShowing() {
    return showSolutionBrowser;
  }

  public void setNavigatorShowing(boolean navigatorShowing) {
    this.showSolutionBrowser = navigatorShowing;
    if (navigatorShowing) {
      solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
    } else {
      solutionNavigatorAndContentPanel.setSplitPosition("0px"); //$NON-NLS-1$
    }
    // update view menu
    installViewMenu(perspectiveCallback);
  }

  public void toggleShowSolutionBrowser() {
    if (!showSolutionBrowser) {
      solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
    } else {
      solutionNavigatorAndContentPanel.setSplitPosition("0px"); //$NON-NLS-1$
    }
    showSolutionBrowser = !showSolutionBrowser;
    // update view menu
    installViewMenu(perspectiveCallback);

    // update setting
    AsyncCallback callback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Object result) {
      }

    };
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, CURRENT_SELECTED_TAB);  // TODO not sure what type of event needs to be fired 
    MantleServiceCache.getService().setShowNavigator(showSolutionBrowser, callback);
  }

  public MenuBar getFavoritesGroupMenuBar() {
    return favoritesGroupMenuBar;
  }

  public void setFavoritesGroupMenuBar(MenuBar favoritesGroupMenuBar) {
    this.favoritesGroupMenuBar = favoritesGroupMenuBar;
  }

  public List<Bookmark> getBookmarks() {
    return bookmarks;
  }

  public void mouseUp(Event e) {
    solutionNavigatorAndContentPanel.onBrowserEvent(e);
  }

  public void allTabsClosed() {
    // show the "launch" panel
    showLaunchOrContent();
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.CLOSE, CURRENT_SELECTED_TAB);
  }

  public IPerspectiveCallback getPerspectiveCallback() {
    return perspectiveCallback;
  }

  public void setPerspectiveCallback(IPerspectiveCallback perspectiveCallback) {
    this.perspectiveCallback = perspectiveCallback;
  }

  public void addSolutionBrowserListener(SolutionBrowserListener listener) {
    listeners.add(listener);
  }

  public void removeSolutionBrowserListener(SolutionBrowserListener listener) {
    listeners.remove(listener);
  }

  public void fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType type, int tabIndex) {
    // does this take parameters? or should it simply return the state

    // Get a reference to the current tab
    Widget tabPanel = null;
    if (tabIndex >= 0 && contentTabPanel.getWidgetCount() > tabIndex) {
      tabPanel = contentTabPanel.getWidget(tabIndex);
    } else {
      int selectedTabIndex = contentTabPanel.getTabBar().getSelectedTab();
      if(selectedTabIndex >= 0) {
        tabPanel = contentTabPanel.getWidget(selectedTabIndex);  
      }
      
    }

    for (SolutionBrowserListener listener : listeners) {
      try {
        if (showWorkspaceMenuItem.isChecked()) {
          // cause all menus to be disabled for the selected file/tab
          listener.solutionBrowserEvent(null, null, null);
        } else {
          listener.solutionBrowserEvent(type, tabPanel, selectedFileItem);
        }
      } catch (Exception e) {
        // don't let this fail, it will disturb normal processing
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }
    }
  }

  /**
   * Called by JSNI call from parameterized xaction prompt pages to "cancel". The only 'key' to pass up is the URL. To handle the possibility of multiple tabs
   * with the same url, this method first checks the assumption that the current active tab initiates the call. Otherwise it checks from tail up for the first
   * tab with a matching url and closes that one. *
   * 
   * @param url
   */
  public void closeTab(String url) {
    int curpos = contentTabPanel.getTabBar().getSelectedTab();
    if (StringUtils.isEmpty(url)) {
      // if the url was not provided, simply remove the currently selected tab and then remove
      if (curpos >= 0 && contentTabPanel.getWidgetCount() > 0) {
        contentTabPanel.remove(curpos);
      }
      if (contentTabPanel.getWidgetCount() == 0) {
        allTabsClosed();
      }
      return;
    }
    ReloadableIFrameTabPanel curPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(curpos);
    if (url.contains(curPanel.url)) {
      contentTabPanel.remove(curpos);
      if (contentTabPanel.getWidgetCount() == 0) {
        allTabsClosed();
      }
      return;
    }

    for (int i = contentTabPanel.getWidgetCount() - 1; i >= 0; i--) {
      curPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(i);

      if (url.contains(curPanel.url)) {
        contentTabPanel.remove(i);
        if (contentTabPanel.getWidgetCount() == 0) {
          allTabsClosed();
        }
        return;
      }
    }
  }

  /**
   * This method returns the current frame element id.
   * 
   * @return
   */
  public String getCurrentFrameElementId() {
    int curpos = contentTabPanel.getTabBar().getSelectedTab();
    final ReloadableIFrameTabPanel curPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(curpos);
    return curPanel.getFrame().getElement().getAttribute("id"); //$NON-NLS-1$
  }

  public ReloadableIFrameTabPanel getCurrentFrame() {
    int curpos = contentTabPanel.getTabBar().getSelectedTab();
    if (curpos == -1) {
      return null;
    }
    final ReloadableIFrameTabPanel curPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(curpos);
    return curPanel;
  }

  private native boolean isPDF(com.google.gwt.dom.client.Element frame)
  /*-{
    return (frame.contentDocument != null && frame.contentDocument.getElementsByTagName('embed').length > 0);
  }-*/;

  private void refreshIfPDF(final ReloadableIFrameTabPanel frame) {
    Timer t = new Timer() {
      public void run() {
        if (isPDF(frame.getFrame().getElement())) {
          frame.reload();
        }
      }
    };
    t.schedule(250);
  }

  public void backgroundExecutionCompleted() {
    showWorkspace();
  }

  public void handleWAQRPreview(String url, String xml) {
    ExecuteWAQRPreviewCommand command = new ExecuteWAQRPreviewCommand(contentTabPanel, url, xml, this);
    command.execute();
  }

  /*
   * getOpenAnalysisView
   * 
   * Polls the current open tabs to see if any (and there should be no more than one) is displaying an analysis view.
   * 
   * return null if there is no open analysis view and returns the Widget the view is in if there is an open analysis view.
   */
  public Widget getOpenAnalysisView() {
    for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
      Widget currentWidget = contentTabPanel.getWidget(i);
      ReloadableIFrameTabPanel frame = ((ReloadableIFrameTabPanel) currentWidget);
      String id = frame.getFrame().getElement().getAttribute("id"); //$NON-NLS-1$
      if (isPivot(id)) {
        return currentWidget;
      }
    }
    return null;
  }

  public native void flagSolutionDocumentLoaded()
  /*-{
    $wnd.mantle_repository_loaded = true;
  }-*/;
  
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

  public static SolutionBrowserPerspective getInstance() {
    return instance;
  }

  /**
   * The passed in URL has all the parameters set for background execution. We simply call GET on the URL and handle the response object. If the response object
   * contains a particular string then we display success message box.
   * 
   * @param url
   *          Complete url with all the parameters set for scheduling a job in the background.
   */
  private void runInBackground(final String url) {

    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
          dialogBox.center();
        }

        public void onResponseReceived(Request request, Response response) {
          /*
           *  We are checking for this specific string because if the job was scheduled 
           *  successfully by QuartzBackgroundExecutionHelper then the response is an html 
           *  that contains the specific string. We have coded this way because we did not want to 
           *  touch the old way.   
           */
          if ("true".equals(response.getHeader("background_execution"))) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), Messages.getString("backgroundJobScheduled"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), //$NON-NLS-1$
          Messages.getString("couldNotBackgroundExecute"), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  public void confirmBackgroundExecutionDialog(final String url) {
    final String title = Messages.getString("confirm"); //$NON-NLS-1$
    final String message = Messages.getString("userParamBackgroundWarning"); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    vp.add(new Label(Messages.getString(message))); 

    final PromptDialogBox scheduleInBackground = new PromptDialogBox(title, Messages.getString("yes"), Messages.getString("no"), false, true, vp); //$NON-NLS-1$ //$NON-NLS-2$

    final IDialogCallback callback = new IDialogCallback() {
      public void cancelPressed() {
        scheduleInBackground.hide();
      }

      public void okPressed() {
        runInBackground(url);
      }
    };
    scheduleInBackground.setCallback(callback);
    scheduleInBackground.center();
  }

  public void setCurrentTabSaveEnabled(boolean enabled) {
    ReloadableIFrameTabPanel panel = getCurrentFrame();
    if (panel != null) {
      panel.setSaveEnabled(enabled);
    }
    this.fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.SELECT, contentTabPanel.getTabBar().getSelectedTab());
  }

  /*
   * registerContentOverlay - register the overlay with the panel. Once the registration is done it fires a soultion browser
   * event passing the current tab index and the type of event
   */
  public void registerContentOverlay(String id){
    ReloadableIFrameTabPanel panel = getCurrentFrame();
    panel.addOverlay(id);
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, contentTabPanel.getTabBar().getSelectedTab());
  }

  public void enableContentEdit(boolean enable){
    ReloadableIFrameTabPanel panel = getCurrentFrame();
    panel.setEditEnabled(enable);
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, contentTabPanel.getTabBar().getSelectedTab());
  }

  public void setContentEditSelected(boolean selected){
    ReloadableIFrameTabPanel panel = getCurrentFrame();
    panel.setEditSelected(selected);
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, contentTabPanel.getTabBar().getSelectedTab());
  }
  
  // Content frames can register a Javascript object to receive various PUC notifications. We broker that out 
  // to the appropriate ReloadableIFrameTabPanel here.
  public void setCurrentTabJSCallback(JavaScriptObject obj){
    ReloadableIFrameTabPanel panel = getCurrentFrame();
    panel.setContentCallback(obj);
  }
  
  public void buildEnabledOptionsList(Map<String, String> settings) {
    
    enabledOptionsList.clear();
    contentTypePluginList.clear();

    // Check for override of NewAnalysisView
    // Poked in via pentaho.xml entry
    if (settings.containsKey("new-analysis-view-command-url")) { //$NON-NLS-1$
      newAnalysisViewOverrideCommandUrl = settings.get("new-analysis-view-command-url"); //$NON-NLS-1$
      newAnalysisViewOverrideCommandTitle = settings.get("new-analysis-view-command-title"); //$NON-NLS-1$
    }
    // Check for override of New Report
    // Poked in via pentaho.xml entry
//    <new-analysis-view>
//      <command-url>http://www.google.com</command-url>
//      <command-title>Marc Analysis View</command-title>
//    </new-analysis-view>
//    <new-report>
//      <command-url>http://www.yahoo.com</command-url>
//      <command-title>Marc New Report</command-title>
//    </new-report>
    // 
    if (settings.containsKey("new-report-command-url")) { //$NON-NLS-1$
      newReportOverrideCommandUrl = settings.get("new-report-command-url"); //$NON-NLS-1$
      newReportOverrideCommandTitle = settings.get("new-report-command-title"); //$NON-NLS-1$
    }
    // Another way to override is from a plugin.xml...
    // 
    // <menu-item id="waqr_menu_item" anchor="file-new-submenu-waqr_menu_item" label="New WAQR" command="http://www.amazon.com" type="MENU_ITEM" how="REPLACE"/>
    // <menu-item id="new_analysis_view_menu_item" anchor="file-new-submenu-new_analysis_view_menu_item" label="New Analysis" command="http://www.dogpile.com" type="MENU_ITEM" how="REPLACE"/>

    if (settings.get("file-newMenuOverrideTitle0" )!= null ) { //$NON-NLS-1$
      // For now, only support override of these two menus
      for (int i=0; i<2; i++) {
        String title = settings.get("file-newMenuOverrideTitle" + i); //$NON-NLS-1$
        String command = settings.get("file-newMenuOverrideCommand" + i); //$NON-NLS-1$
        String menuItem = settings.get("file-newMenuOverrideMenuItem" + i); //$NON-NLS-1$
        if ( (menuItem != null) && (command != null) && (title != null) ) {
          if (menuItem.equals("waqr_menu_item")) { //$NON-NLS-1$
            newReportOverrideCommandUrl = command;
            newReportOverrideCommandTitle = title;
          } else if (menuItem.equals("new_analysis_view_menu_item") ) { //$NON-NLS-1$
            newAnalysisViewOverrideCommandUrl = command;
            newAnalysisViewOverrideCommandTitle = title;
          }
        }
      }
    }
    
    
    // load plugins
    int index = 0;
    String pluginSetting = "plugin-content-type-" + index; //$NON-NLS-1$
    while(settings.containsKey(pluginSetting)) {
      String fileExtension = settings.get(pluginSetting);
      String fileIcon = settings.get("plugin-content-type-icon-" + index);
      FileTypeEnabledOptions pluginMenu = new FileTypeEnabledOptions(fileExtension);
      ContentTypePlugin plugin = new ContentTypePlugin(fileExtension, fileIcon);

      int cmdIndex = 0;
      String cmdSetting = pluginSetting + "-command-" + cmdIndex;
      while (settings.containsKey(cmdSetting)) {
        try {
          COMMAND cmd = COMMAND.valueOf(settings.get(cmdSetting));
          String url = settings.get(pluginSetting + "-command-url-" + cmdIndex);
          pluginMenu.addCommand(cmd);
          plugin.addUrlCommand(cmd, url);
          cmdSetting = pluginSetting + "-command-" + (++cmdIndex);
        } catch (Throwable t) {
          cmdSetting = pluginSetting + "-command-" + (++cmdIndex);
          // command is not found, invalid, we cannot let this break
          // the entire application, and it doesn't help to annoy every
          // single user everytime they start their application if
          // a plugin has a poorly configured plugin
        }
      }
      
      // all files can share, delete, and have properties
      pluginMenu.addCommand(COMMAND.SHARE);
      pluginMenu.addCommand(COMMAND.DELETE);
      pluginMenu.addCommand(COMMAND.PROPERTIES);
      
      contentTypePluginList.add(plugin);
      enabledOptionsList.add(pluginMenu);

      // check for another one
      pluginSetting = "plugin-content-type-" + (++index); //$NON-NLS-1$
    }
    
    FileTypeEnabledOptions waqrMenu = new FileTypeEnabledOptions(FileItem.WAQR_VIEW_SUFFIX);
    waqrMenu.addCommand(COMMAND.RUN);
    waqrMenu.addCommand(COMMAND.NEWWINDOW);
    waqrMenu.addCommand(COMMAND.BACKGROUND);
    waqrMenu.addCommand(COMMAND.EDIT);
    waqrMenu.addCommand(COMMAND.EDIT_ACTION);
    waqrMenu.addCommand(COMMAND.DELETE);
    waqrMenu.addCommand(COMMAND.SHARE);
    waqrMenu.addCommand(COMMAND.SCHEDULE_NEW);
    waqrMenu.addCommand(COMMAND.PROPERTIES);
    enabledOptionsList.add(waqrMenu);
    
    FileTypeEnabledOptions analysisMenu = new FileTypeEnabledOptions(FileItem.ANALYSIS_VIEW_SUFFIX);
    analysisMenu.addCommand(COMMAND.RUN);
    analysisMenu.addCommand(COMMAND.NEWWINDOW);
    analysisMenu.addCommand(COMMAND.EDIT);
    analysisMenu.addCommand(COMMAND.EDIT_ACTION);
    analysisMenu.addCommand(COMMAND.DELETE);
    analysisMenu.addCommand(COMMAND.SHARE);    
    analysisMenu.addCommand(COMMAND.PROPERTIES);
    enabledOptionsList.add(analysisMenu);
    
    FileTypeEnabledOptions xactionMenu = new FileTypeEnabledOptions(FileItem.XACTION_SUFFIX);
    xactionMenu.addCommand(COMMAND.RUN);
    xactionMenu.addCommand(COMMAND.NEWWINDOW);
    xactionMenu.addCommand(COMMAND.BACKGROUND);
    xactionMenu.addCommand(COMMAND.EDIT_ACTION);
    xactionMenu.addCommand(COMMAND.DELETE);
    xactionMenu.addCommand(COMMAND.SCHEDULE_NEW);
    xactionMenu.addCommand(COMMAND.SHARE);
    xactionMenu.addCommand(COMMAND.PROPERTIES);
    enabledOptionsList.add(xactionMenu);
    
    FileTypeEnabledOptions defaultMenu = new FileTypeEnabledOptions(null);
    defaultMenu.addCommand(COMMAND.RUN);
    defaultMenu.addCommand(COMMAND.NEWWINDOW);
    defaultMenu.addCommand(COMMAND.DELETE);
    defaultMenu.addCommand(COMMAND.SHARE);
    defaultMenu.addCommand(COMMAND.PROPERTIES);
    enabledOptionsList.add(defaultMenu);
  }
  
  public static class ContentTypePlugin {

    String fileExtension;
    String fileIcon;
    Map<COMMAND, String> urlCommands = new HashMap<COMMAND, String>();
    
    ContentTypePlugin(String fileExtension, String fileIcon) {
      this.fileExtension = fileExtension;
      this.fileIcon = fileIcon;
    }
    
    public void addUrlCommand(COMMAND cmd, String url) {
      urlCommands.put(cmd, url);
    }
    
    public boolean isSupportedFile(String filename) {
        return filename != null && filename.endsWith(fileExtension);
    }
    
    private String replacePattern(String url, FileItem item) {
      if (url == null) {
        return null;
      }
      String newurl = url.replaceAll("\\{solution\\}", item.getSolution()); //$NON-NLS-1$
      newurl = newurl.replaceAll("\\{path\\}", item.getPath()); //$NON-NLS-1$
      return newurl.replaceAll("\\{name\\}", item.getName()); //$NON-NLS-1$
    }
    
    public boolean hasCommand(COMMAND cmd) {
      return urlCommands.containsKey(cmd);
    }
    
    public String getCommandUrl(FileItem item, COMMAND cmd) {
      return replacePattern(urlCommands.get(cmd), item);
    }
    
    public String getFileIcon() {
      return fileIcon;
    }
  }

}