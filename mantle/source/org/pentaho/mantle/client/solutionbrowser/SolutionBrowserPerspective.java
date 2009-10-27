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

import java.util.ArrayList;
import java.util.Collections;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.IViewMenuCallback;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.commands.CommandCallback;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshWorkspaceCommand;
import org.pentaho.mantle.client.commands.ShareFileCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.ToggleWorkspaceCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.dialogs.FileDialog;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.service.EmptyCallback;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper.ContentTypePlugin;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.launcher.LaunchPanel;
import org.pentaho.mantle.client.solutionbrowser.scheduling.ScheduleHelper;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.MantleTabPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.TabWidget;
import org.pentaho.mantle.client.solutionbrowser.toolbars.BrowserToolbar;
import org.pentaho.mantle.client.solutionbrowser.tree.FileTreeItem;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTreeWrapper;
import org.pentaho.mantle.client.solutionbrowser.workspace.WorkspacePanel;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;

public class SolutionBrowserPerspective extends HorizontalPanel {

  private static final String defaultSplitPosition = "220px"; //$NON-NLS-1$

  private HorizontalSplitPanel solutionNavigatorAndContentPanel = new HorizontalSplitPanel(MantleImages.images);
  private VerticalSplitPanel solutionNavigatorPanel = new VerticalSplitPanel(MantleImages.images);
  private SolutionTree solutionTree = new SolutionTree();
  private FilesListPanel filesListPanel = new FilesListPanel();
  private DeckPanel contentPanel = new DeckPanel();
  private LaunchPanel launchPanel = new LaunchPanel();
  private WorkspacePanel workspacePanel = null;

  private MantleTabPanel contentTabPanel = new MantleTabPanel();
  private IViewMenuCallback viewMenuCallback;
  private boolean showSolutionBrowser = false;
  private boolean isAdministrator = false;
  private ArrayList<SolutionBrowserListener> listeners = new ArrayList<SolutionBrowserListener>();

  private Command ToggleLocalizedNamesCommand = new Command() {
    public void execute() {
      solutionTree.setShowLocalizedFileNames(!solutionTree.isShowLocalizedFileNames());

      // update view menu
      updateViewMenu();

      // update setting
      MantleServiceCache.getService().setShowLocalizedFileNames(solutionTree.isShowLocalizedFileNames(), EmptyCallback.getInstance());
    }
  };

  private Command ShowHideFilesCommand = new Command() {
    public void execute() {
      solutionTree.setShowHiddenFiles(!solutionTree.isShowHiddenFiles());
      solutionTree.setSelectedItem(solutionTree.getSelectedItem(), true);

      // update view menu
      updateViewMenu();

      // update setting
      MantleServiceCache.getService().setShowHiddenFiles(solutionTree.isShowHiddenFiles(), EmptyCallback.getInstance());
    }
  };

  private Command UseDescriptionCommand = new Command() {
    public void execute() {
      solutionTree.setUseDescriptionsForTooltip(!solutionTree.isUseDescriptionsForTooltip());
      solutionTree.setSelectedItem(solutionTree.getSelectedItem(), true);

      // update view menu
      updateViewMenu();

      // update setting
      MantleServiceCache.getService().setUserSetting(IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS,
          "" + solutionTree.isUseDescriptionsForTooltip(), EmptyCallback.getInstance());
    }
  };

  private TreeListener treeListener = new TreeListener() {

    public void onTreeItemSelected(TreeItem item) {
      filesListPanel.populateFilesList(SolutionBrowserPerspective.this, solutionTree, item);
      filesListPanel.getToolbar().setEnabled(false);
    }

    public void onTreeItemStateChanged(TreeItem item) {
      solutionTree.setSelectedItem(item, false);
    }

  };

  private static SolutionBrowserPerspective instance;

  private SolutionBrowserPerspective(final IViewMenuCallback viewMenuCallback) {
    instance = this;
    this.viewMenuCallback = viewMenuCallback;

    SolutionBrowserPerspective.setupNativeHooks(this);

    solutionTree.addTreeListener(treeListener);
    buildUI();
  }

  @SuppressWarnings("unused")
  private void showOpenFileDialog(final JavaScriptObject callback, final String path, final String title, final String okText, final String fileTypes) {
    SolutionDocumentManager.getInstance().fetchSolutionDocument(new AsyncCallback<Document>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Document result) {
        FileDialog dialog = new FileDialog(result, path, title, okText, fileTypes.split(","));
        dialog.addFileChooserListener(new FileChooserListener() {

          public void fileSelected(String solution, String path, String name, String localizedFileName) {
            notifyOpenFileCallback(callback, solution, path, name, localizedFileName);
          }

          public void fileSelectionChanged(String solution, String path, String name) {
          }

        });
        dialog.show();
      }
    }, false);
  }

  private static native void setupNativeHooks(SolutionBrowserPerspective solutionNavigator)
  /*-{
    $wnd.openFileDialog = function(callback,title, okText, fileTypes) { 
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, null, title, okText, fileTypes);      
    }
    $wnd.openFileDialogWithPath = function(callback, path, title, okText, fileTypes) { 
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, path, title, okText, fileTypes);      
    }
    $wnd.mantle_openTab = function(name, title, url) {
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, title, url);
    }    
    $wnd.openURL = function(name, tooltip, url){
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, tooltip, url);
    }    
    $wnd.sendMouseEvent = function(event) {
      return solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective::mouseUp(Lcom/google/gwt/user/client/Event;)(event);
    }
  }-*/;

  private native void notifyOpenFileCallback(JavaScriptObject obj, String solution, String path, String name, String localizedFileName)
  /*-{
    obj.fileSelected(solution, path, name, localizedFileName);
  }-*/;

  private void buildUI() {
    FlowPanel topPanel = new FlowPanel();
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.setWidget(new BrowserToolbar());
    toolbarWrapper.setStyleName("files-toolbar"); //$NON-NLS-1$
    topPanel.add(toolbarWrapper);
    topPanel.add(new SolutionTreeWrapper(solutionTree));

    solutionNavigatorPanel.setHeight("100%"); //$NON-NLS-1$
    solutionNavigatorPanel.setTopWidget(topPanel);
    solutionNavigatorPanel.setBottomWidget(filesListPanel);
    solutionNavigatorPanel.setSplitPosition("60%"); //$NON-NLS-1$
    solutionNavigatorAndContentPanel.setLeftWidget(solutionNavigatorPanel);
    solutionNavigatorAndContentPanel.setRightWidget(contentPanel);
    contentPanel.setAnimationEnabled(true);
    workspacePanel = new WorkspacePanel(isAdministrator);
    contentPanel.add(workspacePanel);
    contentPanel.add(launchPanel);
    contentPanel.add(contentTabPanel);
    if (showSolutionBrowser) {
      solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
    } else {
      solutionNavigatorAndContentPanel.setSplitPosition("0px"); //$NON-NLS-1$
    }
    contentPanel.setHeight("100%"); //$NON-NLS-1$
    contentPanel.setWidth("100%"); //$NON-NLS-1$

    setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  
    setHeight("100%"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$
    add(solutionNavigatorAndContentPanel);

    showContent();
    ElementUtils.removeScrollingFromSplitPane(solutionNavigatorPanel);
    ElementUtils.removeScrollingFromUpTo(solutionNavigatorAndContentPanel.getLeftWidget().getElement(), solutionNavigatorAndContentPanel.getElement());
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
    // TODO Not sure what event type to pass
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, MantleTabPanel.CURRENT_SELECTED_TAB);
    updateViewMenu();
  }

  public void showContent() {
    int showIndex = -1;
    if (contentTabPanel.getWidgetCount() == 0) {
      showIndex = contentPanel.getWidgetIndex(launchPanel);
      Window.setTitle(Messages.getString("productName")); //$NON-NLS-1$
    } else {
      showIndex = contentPanel.getWidgetIndex(contentTabPanel);
    }

    if (showIndex != -1) {
      contentPanel.showWidget(showIndex);
      contentTabPanel.refreshIfPDF();
    }
    // TODO Not sure what event type to pass
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, -1);
    updateViewMenu();
  }

  public void showNewURLTab(String tabName, String tabTooltip, final String url) {
    final int elementId = contentTabPanel.getWidgetCount();
    String frameName = contentTabPanel.getUniqueFrameName();

    // check for other tabs with this name
    if (contentTabPanel.existingTabMatchesName(tabName)) {
      int counter = 2;
      while (true) {
        // Loop until a unique tab name is not found
        // i.e. get the last counter number and then add 1 to it for the new tab name
        if (contentTabPanel.existingTabMatchesName(tabName + " (" + counter + ")")) { // unique //$NON-NLS-1$ //$NON-NLS-2$
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
    contentTabPanel.add(panel, new TabWidget(tabName, tabTooltip, this, contentTabPanel, panel));
    contentTabPanel.selectTab(elementId);

    final ArrayList<com.google.gwt.dom.client.Element> parentList = new ArrayList<com.google.gwt.dom.client.Element>();
    com.google.gwt.dom.client.Element parent = panel.getFrame().getElement();
    while (parent != contentTabPanel.getElement()) {
      parentList.add(parent);
      parent = parent.getParentElement();
    }
    Collections.reverse(parentList);
    for (int i = 1; i < parentList.size(); i++) {
      parentList.get(i).getStyle().setProperty("height", "100%"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    showContent();

    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.OPEN, contentTabPanel.getTabBar().getSelectedTab());

    contentTabPanel.setFileInfoInFrame(filesListPanel.getSelectedFileItem());
  }

  public void openFile(final FileCommand.COMMAND mode) {
    String name = filesListPanel.getSelectedFileItem().getName();
    if (name.endsWith(".xaction")) { //$NON-NLS-1$
      executeActionSequence(mode);
      contentTabPanel.setFileInfoInFrame(filesListPanel.getSelectedFileItem());
    } else if (name.endsWith(".url")) { //$NON-NLS-1$
      if (mode == FileCommand.COMMAND.NEWWINDOW) {
        Window.open(filesListPanel.getSelectedFileItem().getURL(), "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel
            .getSelectedFileItem().getURL());
      }
    } else {
      // see if this file is a plugin
      ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin(filesListPanel.getSelectedFileItem().getName());
      if (plugin != null && plugin.hasCommand(mode)) {
        // load the editor for this plugin
        String url = filesListPanel.getSelectedFileItem().getURL();
        if (StringUtils.isEmpty(url)) {
          url = plugin.getCommandUrl(filesListPanel.getSelectedFileItem(), mode);
        }
        if (GWT.isScript()) {
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            // we have a URL so open it in a new tab
            if (mode == FileCommand.COMMAND.NEWWINDOW) {
              Window.open(url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              UrlCommand cmd = new UrlCommand(url, filesListPanel.getSelectedFileItem().getLocalizedName());
              cmd.execute(new CommandCallback() {
                public void afterExecute() {
                  contentTabPanel.setFileInfoInFrame(filesListPanel.getSelectedFileItem());
                }
              });
            }
          }
        } else {
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            // we have a URL so open it in a new tab
            String updateUrl = "/MantleService?passthru=" + url; //$NON-NLS-1$

            if (mode == FileCommand.COMMAND.NEWWINDOW) {
              Window.open(updateUrl, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
              showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), updateUrl);
            }
          }
        }
      } else {
        // see if this file has a URL
        String url = filesListPanel.getSelectedFileItem().getURL();
        if (url != null && !"".equals(url)) { //$NON-NLS-1$
          // we have a URL so open it in a new tab
          if (mode == FileCommand.COMMAND.NEWWINDOW) {
            Window.open(filesListPanel.getSelectedFileItem().getURL(), "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no"); //$NON-NLS-1$ //$NON-NLS-2$
          } else {
            showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel
                .getSelectedFileItem().getURL());
          }
        }
      }
    }
  }

  public enum OPEN_METHOD {
    OPEN, EDIT, SHARE, SCHEDULE
  }

  public void openFile(String path, String name, String localizedFileName, OPEN_METHOD openMethod) {
    ArrayList<String> pathSegments = new ArrayList<String>();
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
          (new OpenFileCommand()).execute();
        }
      });

      dialogBox.center();
      return;
    }

    FileItem selectedFileItem = new FileItem(name, localizedFileName, localizedFileName, pathSegments.get(0), repoPath, "", null, null, null, false, null); //$NON-NLS-1$
    filesListPanel.setSelectedFileItem(selectedFileItem);

    // TODO: Create a more dynamic filter interface
    if (openMethod == OPEN_METHOD.SCHEDULE) {
      if (selectedFileItem.getName() != null) {
        if (!selectedFileItem.getName().endsWith(".xaction") || selectedFileItem.getName().endsWith(FileItem.ANALYSIS_VIEW_SUFFIX)) { //$NON-NLS-1$
          final MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getString("open"), Messages.getString("scheduleInvalidFileType", selectedFileItem.getName()), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$

          dialogBox.setCallback(new IDialogCallback() {
            public void cancelPressed() {
            }

            public void okPressed() {
              dialogBox.hide();
              (new OpenFileCommand(OPEN_METHOD.SCHEDULE)).execute();
            }
          });

          dialogBox.center();
          return;
        }
      }
    }

    FileTreeItem fileTreeItem = solutionTree.getTreeItem(pathSegments);
    pathSegments.add(name);

    ArrayList<FileTreeItem> allNodes = solutionTree.getAllNodes();
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

    ArrayList<com.google.gwt.xml.client.Element> files = (ArrayList<com.google.gwt.xml.client.Element>) fileTreeItem.getUserObject();
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
      ScheduleHelper.createSchedule(filesListPanel.getSelectedFileItem());
    } else if (openMethod == OPEN_METHOD.SHARE) {
      (new ShareFileCommand()).execute();
    }
  }

  public void editFile() {
    if (filesListPanel.getSelectedFileItem().getName().endsWith(".waqr.xaction")) { //$NON-NLS-1$
      String filename = filesListPanel.getSelectedFileItem().getName().substring(0, filesListPanel.getSelectedFileItem().getName().indexOf(".waqr.xaction")) + ".waqr.xreportspec"; //$NON-NLS-1$ //$NON-NLS-2$
      String url = "adhoc/waqr.html?solution=" + filesListPanel.getSelectedFileItem().getSolution() + "&path=" + filesListPanel.getSelectedFileItem().getPath() + "&filename=" + filename; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/adhoc/waqr.html?solution=" + filesListPanel.getSelectedFileItem().getSolution() + "&path=" + filesListPanel.getSelectedFileItem().getPath() + "&filename=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            + filename;
      }

      // See if it's already loaded
      for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
        Widget w = contentTabPanel.getWidget(i);
        if (w instanceof IFrameTabPanel && ((IFrameTabPanel) w).getUrl().endsWith(url)) {
          // Already up, select and exit
          contentTabPanel.selectTab(i);
          return;
        }
      }
      showNewURLTab(
          Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), url); //$NON-NLS-1$ //$NON-NLS-2$

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(filesListPanel.getSelectedFileItem().getName());
      fileInfo.setSolution(filesListPanel.getSelectedFileItem().getSolution());
      fileInfo.setPath(filesListPanel.getSelectedFileItem().getPath());
      contentTabPanel.getCurrentFrame().setFileInfo(fileInfo);

    } else if (filesListPanel.getSelectedFileItem().getName().endsWith(".analysisview.xaction")) { //$NON-NLS-1$
      openFile(COMMAND.RUN);
    } else {
      // check to see if a plugin supports editing
      ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin(filesListPanel.getSelectedFileItem().getName());
      if (plugin != null && plugin.hasCommand(COMMAND.EDIT)) {
        // load the editor for this plugin
        String editUrl = plugin.getCommandUrl(filesListPanel.getSelectedFileItem(), COMMAND.EDIT);
        // See if it's already loaded
        for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
          Widget w = contentTabPanel.getWidget(i);
          if (w instanceof IFrameTabPanel && ((IFrameTabPanel) w).getUrl().endsWith(editUrl)) {
            // Already up, select and exit
            contentTabPanel.selectTab(i);
            return;
          }
        }

        if (GWT.isScript()) {
          showNewURLTab(
              Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), editUrl); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
          // we have a URL so open it in a new tab
          String updateUrl = "/MantleService?passthru=" + editUrl; //$NON-NLS-1$
          showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), updateUrl);
        }

        // Store representation of file in the frame for reference later when save is called
        SolutionFileInfo fileInfo = new SolutionFileInfo();
        fileInfo.setName(filesListPanel.getSelectedFileItem().getName());
        fileInfo.setSolution(filesListPanel.getSelectedFileItem().getSolution());
        fileInfo.setPath(filesListPanel.getSelectedFileItem().getPath());
        contentTabPanel.getCurrentFrame().setFileInfo(fileInfo);

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
      if (filesListPanel.getSelectedFileItem().getSolution().endsWith("/") || filesListPanel.getSelectedFileItem().getPath().startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
        fullPath = filesListPanel.getSelectedFileItem().getSolution() + filesListPanel.getSelectedFileItem().getPath()
            + "/" + filesListPanel.getSelectedFileItem().getName(); //$NON-NLS-1$
      } else {
        fullPath = filesListPanel.getSelectedFileItem().getSolution()
            + "/" + filesListPanel.getSelectedFileItem().getPath() + "/" + filesListPanel.getSelectedFileItem().getName(); //$NON-NLS-1$ //$NON-NLS-2$
      }
      String url = "actioneditor/actioneditor.html?actionSequence=" + fullPath; //$NON-NLS-1$
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/actioneditor/actioneditor.html?actionSequence=" + fullPath; //$NON-NLS-1$
      }

      // See if it's already loaded
      for (int i = 0; i < contentTabPanel.getWidgetCount(); i++) {
        Widget w = contentTabPanel.getWidget(i);
        if (w instanceof IFrameTabPanel && ((IFrameTabPanel) w).getUrl().endsWith(url)) {
          // Already up, select and exit
          contentTabPanel.selectTab(i);
          return;
        }
      }
      showNewURLTab(
          Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), Messages.getString("editingColon") + filesListPanel.getSelectedFileItem().getLocalizedName(), url); //$NON-NLS-1$ //$NON-NLS-2$

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(filesListPanel.getSelectedFileItem().getName());
      fileInfo.setSolution(filesListPanel.getSelectedFileItem().getSolution());
      fileInfo.setPath(filesListPanel.getSelectedFileItem().getPath());
      contentTabPanel.getCurrentFrame().setFileInfo(fileInfo);
    }
  }

  public void executeActionSequence(final FileCommand.COMMAND mode) {
    // open in content panel
    AbstractCommand authCmd = new AbstractCommand() {
      protected void performOperation() {
        performOperation(false);
      }

      protected void performOperation(boolean feedback) {
        String url = null;
        String path = filesListPanel.getSelectedFileItem().getPath();
        if (path.startsWith("/")) { //$NON-NLS-1$
          path = path.substring(1);
        }
        if (GWT.isScript()) {
          url = "ViewAction?solution=" + filesListPanel.getSelectedFileItem().getSolution() + "&path=" + path + "&action=" + filesListPanel.getSelectedFileItem().getName(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
          url = "/MantleService?passthru=ViewAction&solution=" + filesListPanel.getSelectedFileItem().getSolution() + "&path=" + path + "&action=" + filesListPanel.getSelectedFileItem().getName() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), myurl);

                // Store representation of file in the frame for reference later when save is called
                // getCurrentFrame().setFileInfo(fileInfo);

              } else {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("info"), //$NON-NLS-1$
                    Messages.getString("noSchedulePermission"), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }
            }
          };
          MantleServiceCache.getService().hasAccess(filesListPanel.getSelectedFileItem().getSolution(), filesListPanel.getSelectedFileItem().getPath(),
              filesListPanel.getSelectedFileItem().getName(), 3, callback);
        } else {
          showNewURLTab(filesListPanel.getSelectedFileItem().getLocalizedName(), filesListPanel.getSelectedFileItem().getLocalizedName(), url);
        }
      }

    };
    authCmd.execute();
  }

  public void updateViewMenu() {
    ArrayList<UIObject> viewMenuItems = new ArrayList<UIObject>();

    // menu items
    CheckBoxMenuItem showWorkspaceMenuItem = new CheckBoxMenuItem(Messages.getString("workspace"), new ToggleWorkspaceCommand()); //$NON-NLS-1$
    CheckBoxMenuItem showHiddenFilesMenuItem = new CheckBoxMenuItem(Messages.getString("showHiddenFiles"), ShowHideFilesCommand); //$NON-NLS-1$
    CheckBoxMenuItem showLocalizedFileNamesMenuItem = new CheckBoxMenuItem(Messages.getString("showLocalizedFileNames"), ToggleLocalizedNamesCommand); //$NON-NLS-1$
    CheckBoxMenuItem showSolutionBrowserMenuItem = new CheckBoxMenuItem(Messages.getString("showSolutionBrowser"), new ShowBrowserCommand()); //$NON-NLS-1$
    CheckBoxMenuItem useDescriptionsMenuItem = new CheckBoxMenuItem(Messages.getString("useDescriptionsForTooltips"), UseDescriptionCommand); //$NON-NLS-1$

    showWorkspaceMenuItem.setChecked(isWorkspaceShowing());
    showLocalizedFileNamesMenuItem.setChecked(solutionTree.isShowLocalizedFileNames());
    showHiddenFilesMenuItem.setChecked(solutionTree.isShowHiddenFiles());
    showSolutionBrowserMenuItem.setChecked(showSolutionBrowser);
    useDescriptionsMenuItem.setChecked(solutionTree.isUseDescriptionsForTooltip());

    // viewMenuItems.add(showLocalizedFileNamesMenuItem);
    viewMenuItems.add(showSolutionBrowserMenuItem);
    viewMenuItems.add(showWorkspaceMenuItem);
    // viewMenuItems.add(showHiddenFilesMenuItem);
    viewMenuItems.add(new MenuItemSeparator());
    viewMenuItems.add(useDescriptionsMenuItem);
    viewMenuItems.add(new MenuItemSeparator());

    MenuItem refreshItem = new MenuItem(Messages.getString("refresh"), isWorkspaceShowing() ? new RefreshWorkspaceCommand() : new RefreshRepositoryCommand());
    refreshItem.getElement().setId("view_refresh_menu_item");
    viewMenuItems.add(refreshItem); //$NON-NLS-1$

    viewMenuCallback.installViewMenu(viewMenuItems);
  }

  public WorkspacePanel getWorkspacePanel() {
    return workspacePanel;
  }

  public MantleTabPanel getContentTabPanel() {
    return contentTabPanel;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
    solutionTree.setAdministrator(isAdministrator);
    workspacePanel.setAdministrator(isAdministrator);
  }

  public boolean isNavigatorShowing() {
    return showSolutionBrowser;
  }

  public void setNavigatorShowing(boolean showSolutionBrowser) {
    this.showSolutionBrowser = showSolutionBrowser;
    if (showSolutionBrowser) {
      solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
    } else {
      solutionNavigatorAndContentPanel.setSplitPosition("0px"); //$NON-NLS-1$
    }
    updateViewMenu();
  }

  public void toggleShowSolutionBrowser() {
    setNavigatorShowing(!showSolutionBrowser);

    // update setting
    // TODO not sure what type of event needs to be fired
    fireSolutionBrowserListenerEvent(SolutionBrowserListener.EventType.UNDEFINED, MantleTabPanel.CURRENT_SELECTED_TAB);
    MantleServiceCache.getService().setShowNavigator(showSolutionBrowser, EmptyCallback.getInstance());
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
      if (selectedTabIndex >= 0) {
        tabPanel = contentTabPanel.getWidget(selectedTabIndex);
      }

    }

    for (SolutionBrowserListener listener : listeners) {
      try {
        if (isWorkspaceShowing()) {
          // cause all menus to be disabled for the selected file/tab
          listener.solutionBrowserEvent(null, null, null);
        } else {
          listener.solutionBrowserEvent(type, tabPanel, filesListPanel.getSelectedFileItem());
        }
      } catch (Exception e) {
        // don't let this fail, it will disturb normal processing
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.toString(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }
    }
  }

  /**
   * This method is called via JSNI
   */
  @SuppressWarnings("unused")
  private void mouseUp(Event e) {
    solutionNavigatorAndContentPanel.onBrowserEvent(e);
  }

  public static SolutionBrowserPerspective getInstance(IViewMenuCallback viewMenuCallback) {
    if (instance == null) {
      instance = new SolutionBrowserPerspective(viewMenuCallback);
    }
    return instance;
  }

  public static SolutionBrowserPerspective getInstance() {
    return getInstance(null);
  }

  public SolutionTree getSolutionTree() {
    return solutionTree;
  }

  public FilesListPanel getFilesListPanel() {
    return filesListPanel;
  }

}