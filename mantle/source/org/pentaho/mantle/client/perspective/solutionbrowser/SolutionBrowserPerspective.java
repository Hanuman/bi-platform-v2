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
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.dialogs.usersettings.UserPreferencesDialog;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand.COMMAND;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.reporting.ReportView;
import org.pentaho.mantle.client.perspective.solutionbrowser.scheduling.NewScheduleDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.toolbars.BrowserToolbar;
import org.pentaho.mantle.client.perspective.workspace.IWorkspaceCallback;
import org.pentaho.mantle.client.perspective.workspace.WorkspacePerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
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
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.RootPanel;
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
  private static final String defaultSplitPosition = "220px";

  private ClassicNavigatorView classicNavigatorView = new ClassicNavigatorView();
  private HorizontalSplitPanel solutionNavigatorAndContentPanel = new HorizontalSplitPanel(MantleImages.images);
  private VerticalSplitPanel solutionNavigatorPanel = new VerticalSplitPanel(MantleImages.images);
  private SolutionTree solutionTree = new SolutionTree(this);
  private FilesListPanel filesListPanel = new FilesListPanel(this);
  private FileItem selectedFileItem;
  private DeckPanel contentPanel = new DeckPanel();
  private LaunchPanel launchPanel = new LaunchPanel(this);
  private WorkspacePerspective workspacePanel = null;

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

  // commands
  Command ShowWorkSpaceCommand = new Command() {
    public void execute() {
      showWorkspaceMenuItem.setChecked(!showWorkspaceMenuItem.isChecked());
      if (showWorkspaceMenuItem.isChecked()) {
        showWorkspace();
      } else {
        showLaunchOrContent();
      }
    }
  };
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

  // menu items
  CheckBoxMenuItem showWorkspaceMenuItem = new CheckBoxMenuItem(Messages.getInstance().workspace(), ShowWorkSpaceCommand);
  CheckBoxMenuItem showHiddenFilesMenuItem = new CheckBoxMenuItem(Messages.getInstance().showHiddenFiles(), ShowHideFilesCommand);
  CheckBoxMenuItem showLocalizedFileNamesMenuItem = new CheckBoxMenuItem(Messages.getInstance().showLocalizedFileNames(), ToggleLocalizedNamesCommand);
  CheckBoxMenuItem showSolutionBrowserMenuItem = new CheckBoxMenuItem(Messages.getInstance().showSolutionBrowser(), new ShowBrowserCommand(this));

  TreeListener treeListener = new TreeListener() {

    @SuppressWarnings("unchecked")
    public void onTreeItemSelected(TreeItem item) {
      filesListPanel.populateFilesList(SolutionBrowserPerspective.this, solutionTree, selectedFileItem, item);
      filesListPanel.getToolbar().setEnabled(false);
    }

    public void onTreeItemStateChanged(TreeItem item) {
      solutionTree.setSelectedItem(item, false);
    }

  };

  public SolutionBrowserPerspective(final IPerspectiveCallback perspectiveCallback) {
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
        fireSolutionBrowserListenerEvent();
        if (previousIndex != tabIndex) {
          ReloadableIFrameTabPanel tabPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(tabIndex);

          NamedFrame frame = tabPanel.getFrame();

          Window.setTitle(getCurrentTab().getText() + " - " + MantleApplication.PRODUCT_NAME);

          frame.setVisible(true);
          refreshIfPDF(tabPanel);
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

  public void hideFrame(int tabIndex) {
    Frame frame = ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(tabIndex)).getFrame();
    frame.setVisible(false);
  }

  public void buildUI() {
    clear();
    if (explorerMode) {

      solutionNavigatorPanel.setHeight("100%");
      // ----- Create the top panel ----

      BrowserToolbar browserToolbar = new BrowserToolbar(this);
      browserToolbar.setHeight("28px"); //$NON-NLS-1$
      browserToolbar.setWidth("100%");

      FlowPanel topPanel = new FlowPanel();
      SimplePanel toolbarWrapper = new SimplePanel();
      toolbarWrapper.add(browserToolbar);
      toolbarWrapper.setStyleName("files-toolbar");
      topPanel.add(toolbarWrapper);

      SimplePanel filesListWrapper = new SimplePanel();
      filesListWrapper.add(solutionTree);
      filesListWrapper.setStyleName("files-list-panel");
      topPanel.add(filesListWrapper);
      solutionTree.getElement().getStyle().setProperty("marginTop", "29px");

      this.setStyleName("panelWithTitledToolbar"); //$NON-NLS-1$  

      // --------------------------------

      solutionNavigatorPanel.setTopWidget(topPanel);
      filesListPanel.setWidth("100%");
      solutionNavigatorPanel.setBottomWidget(filesListPanel);
      solutionNavigatorPanel.setSplitPosition("60%");
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
        solutionNavigatorAndContentPanel.setSplitPosition("0px");
      }
      contentPanel.setHeight("100%");
      contentPanel.setWidth("100%");
      contentTabPanel.setHeight("100%");
      contentTabPanel.setWidth("100%");
      setHeight("100%");
      setWidth("100%");
      add(solutionNavigatorAndContentPanel);

      ElementUtils.removeScrollingFromSplitPane(solutionNavigatorPanel);

      ElementUtils.removeScrollingFromUpTo(solutionNavigatorAndContentPanel.getLeftWidget().getElement(), solutionNavigatorAndContentPanel.getElement());
    } else {
      // load classic view
      // we've got the tree
      setHeight("100%");
      setWidth("100%");
      classicNavigatorView.setHeight("100%");
      classicNavigatorView.setWidth("100%");
      add(classicNavigatorView);
    }
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
    fireSolutionBrowserListenerEvent();
  }

  public void showLaunchOrContent() {
    int showIndex = -1;
    if (contentTabPanel.getWidgetCount() == 0) {
      showIndex = contentPanel.getWidgetIndex(launchPanel);
      Window.setTitle(MantleApplication.PRODUCT_NAME);
    } else {
      showIndex = contentPanel.getWidgetIndex(contentTabPanel);
    }
    if (showIndex != -1) {
      contentPanel.showWidget(showIndex);
    }
    fireSolutionBrowserListenerEvent();
  }

  private boolean existingTabMatchesName(String name) {
    String key = "title=\"" + name + "\"";

    NodeList<com.google.gwt.dom.client.Element> divs = contentTabPanel.getTabBar().getElement().getElementsByTagName("div");

    for (int i = 0; i < divs.getLength(); i++) {
      String tabHtml = divs.getItem(i).getInnerHTML();
      // TODO: remove once a more elegant tab solution is in place
      if (tabHtml.indexOf(key) > -1) {
        return true;
      }
    }
    return false;
  }

  public void showNewURLTab(String tabName, String tabTooltip, String url) {
    final int elementId = contentTabPanel.getWidgetCount();
    String frameName = "frameID: " + elementId;
    ReloadableIFrameTabPanel panel = new ReloadableIFrameTabPanel(frameName, url);

    Frame frame = panel.getFrame();
    frame.getElement().setAttribute("id", frameName);
    frame.setStyleName("gwt-Frame");
    panel.add(frame);
    frame.setWidth("100%");
    frame.setHeight("100%");

    String finalTabName = tabName;
    String finalTabTooltip = tabTooltip;
    // check for other tabs with this name
    if (existingTabMatchesName(tabName)) {
      int counter = 2;
      while (true) {
        // Loop until a unique tab name is not found
        // i.e. get the last counter number and then add 1 to it for the new tab name
        if (existingTabMatchesName(tabName + " (" + counter + ")")) { // unique
          counter++;
          continue;
        } else {
          finalTabName = tabName + " (" + counter + ")";
          finalTabTooltip = tabTooltip + " (" + counter + ")";
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
      parentList.get(i).getStyle().setProperty("height", "100%");
    }
    showLaunchOrContent();

    // update state to workspace state flag
    showWorkspaceMenuItem.setChecked(false);
    // fire
    fireSolutionBrowserListenerEvent();

    perspectiveCallback.activatePerspective(this);
  }

  public TabWidget getCurrentTab() {
    return contentTabMap.get(contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab()));
  }

  public TabWidget getTabForWidget(Widget tabWidget) {
    return contentTabMap.get(contentTabPanel.getWidget(contentTabPanel.getWidgetIndex(tabWidget)));
  }

  public void openNewHTMLReport(COMMAND mode) {
    final String reportKey = "/" + selectedFileItem.getSolution() + selectedFileItem.getPath() + "/" + selectedFileItem.getName();
    AsyncCallback<ReportContainer> callback = new AsyncCallback<ReportContainer>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), "Could not get logical report page.", false, false, true);
        dialogBox.center();
      }

      public void onSuccess(ReportContainer reportContainer) {
        Widget tabContent = new ReportView(reportKey, reportContainer);
        contentTabPanel.add(tabContent, new TabWidget(selectedFileItem.getName(), selectedFileItem.getLocalizedName(), SolutionBrowserPerspective.this,
            contentTabPanel, tabContent));
        contentTabPanel.selectTab(contentTabPanel.getWidgetCount() - 1);
      }
    };
    MantleServiceCache.getService().getLogicalReportPage(null, reportKey, 0, callback);
    showLaunchOrContent();
  }

  public void openFile(final FileCommand.COMMAND mode) {
    String name = selectedFileItem.getName();
    if (name.endsWith(".xaction")) {
      if (mode == FileCommand.COMMAND.RUN) {
        final Widget openAnalysisView = getOpenAnalysisView();
        if (openAnalysisView != null) {
          String actionName = getTabForWidget(openAnalysisView).getText();
          Widget content = new HTML(Messages.getInstance().analysisViewIsOpen(actionName));
          PromptDialogBox dialog = new PromptDialogBox("Open", "OK", "Cancel", false, true, content);
          dialog.setCallback(new IDialogCallback() {

            public void cancelPressed() {
              // TODO Auto-generated method stub

            }

            public void okPressed() {
              contentTabPanel.remove(openAnalysisView);
              executeActionSequence(mode);
            }

          });
          dialog.center();
          dialog.show();
          return;
        } else {
          executeActionSequence(mode);
        }
      }
    } else if (name.endsWith(".url")) {
      showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, selectedFileItem.getURL());
    } else if (name.endsWith(".prc")) {
      // open jfreereport!!
      openNewHTMLReport(mode);
    } else {
      // see if this file has a URL
      String url = selectedFileItem.getURL();
      if (url != null && !"".equals(url)) {
        // we have a URL so open it in a new tab
        showNewURLTab(selectedFileItem.localizedName, selectedFileItem.localizedName, selectedFileItem.getURL());
      }
    }
  }

  public enum OPEN_METHOD {
    OPEN, EDIT, SHARE, SCHEDULE
  }

  public void openFile(String path, String name, String localizedFileName, OPEN_METHOD openMethod) {
    List<String> pathSegments = new ArrayList<String>();
    if (path != null) {
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      StringTokenizer st = new StringTokenizer(path, '/');
      for (int i = 0; i < st.countTokens(); i++) {
        pathSegments.add(st.tokenAt(i));
      }
    }

    String repoPath = "";
    for (int i = 1; i < pathSegments.size(); i++) {
      repoPath += "/" + pathSegments.get(i);
    }

    final boolean fileExists = solutionTree.doesFileExist(pathSegments, name);
    if (!fileExists) {
      final MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().open(), Messages.getInstance().fileDoesNotExist(name), false, false, true);

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

    selectedFileItem = new FileItem(name, localizedFileName, true, pathSegments.get(0), repoPath, "", null, null);

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
        if (name.equals(fileElement.getAttribute("name")) || name.equals(fileElement.getAttribute("localized-name"))) {
          selectedFileItem.setURL(fileElement.getAttribute("url"));
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
    if (selectedFileItem.getName().endsWith(".waqr.xaction")) {
      String filename = selectedFileItem.getName().substring(0, selectedFileItem.getName().indexOf(".waqr.xaction")) + ".waqr.xreportspec";
      String url = "adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename=" + filename;
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename="
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
      showNewURLTab("Editing: " + selectedFileItem.getLocalizedName(), "Editing: " + selectedFileItem.getLocalizedName(), url);

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      this.getCurrentFrame().setFileInfo(fileInfo);

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
      if (selectedFileItem.getSolution().endsWith("/") || selectedFileItem.getPath().startsWith("/")) {
        fullPath = selectedFileItem.getSolution() + selectedFileItem.getPath() + "/" + selectedFileItem.getName();
      } else {
        fullPath = selectedFileItem.getSolution() + "/" + selectedFileItem.getPath() + "/" + selectedFileItem.getName();
      }
      String url = "actioneditor/actioneditor.html?actionSequence=" + fullPath;
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/actioneditor/actioneditor.html?actionSequence=" + fullPath;
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
      showNewURLTab("Editing: " + selectedFileItem.getLocalizedName(), "Editing: " + selectedFileItem.getLocalizedName(), url);

      // Store representation of file in the frame for reference later when save is called
      SolutionFileInfo fileInfo = new SolutionFileInfo();
      fileInfo.setName(selectedFileItem.getName());
      fileInfo.setSolution(selectedFileItem.getSolution());
      fileInfo.setPath(selectedFileItem.getPath());
      this.getCurrentFrame().setFileInfo(fileInfo);
    }
  }

  void executeActionSequence(final FileCommand.COMMAND mode) {
    // open in content panel
    // http://localhost:8080/pentaho/ViewAction?solution=samples&path=reporting&action=JFree_XQuery_report.xaction

    final AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        // if we are still authenticated, perform the action, otherwise present login

        String url = null;
        String path = selectedFileItem.getPath();
        if (path.startsWith("/")) {
          path = path.substring(1);
        }
        if (GWT.isScript()) {
          url = "ViewAction?solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action=" + selectedFileItem.getName();
          String mypath = Window.Location.getPath();
          if (!mypath.endsWith("/")) {
            mypath = mypath.substring(0, mypath.lastIndexOf("/") + 1);
          }
          mypath = mypath.replaceAll("/mantle/", "/");
          if (!mypath.endsWith("/")) {
            mypath = "/" + mypath;
          }
          url = mypath + url;
        } else {
          url = "/MantleService?passthru=ViewAction&solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action=" + selectedFileItem.getName()
              + "&userid=joe&password=password";
        }

        if (mode == FileCommand.COMMAND.BACKGROUND) {
          MessageDialogBox dialogBox = new MessageDialogBox(
              Messages.getInstance().info(),
              "Reports that prompt for parameters are not supported with this feature and may result in errors.<BR><BR>  You will be notified when the content is ready.",
              true, false, true);
          dialogBox.center();

          url += "&background=true";

          RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
          try {
            builder.sendRequest(null, new RequestCallback() {

              public void onError(Request request, Throwable exception) {
                Window.alert(exception.getMessage());
              }

              public void onResponseReceived(Request request, Response response) {
              }

            });
          } catch (RequestException e) {
          }
        } else if (mode == FileCommand.COMMAND.NEWWINDOW) {
          // popup blockers might attack this
          Window.open(url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no");
        } else if (mode == FileCommand.COMMAND.SUBSCRIBE) {
          final String myurl = url + "&subscribepage=yes";
          AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

            public void onFailure(Throwable caught) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), "Could not get file properties.", false, false, true);
              dialogBox.center();
            }

            public void onSuccess(Boolean subscribable) {
                 
                if (subscribable) {
                  showNewURLTab(selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(), myurl);

                  // Store representation of file in the frame for reference later when save is called
//                  getCurrentFrame().setFileInfo(fileInfo);

                } else {
                  MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(),
                      "You do not have permission to subscribe to this action sequence.", false, false, true);
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
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            executeActionSequence(mode);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void fetchSolutionDocument(final boolean showSuccess) {
    final AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean result) {
        RequestBuilder builder = null;
        if (GWT.isScript()) {
          String path = Window.Location.getPath();
          if (!path.endsWith("/")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
          }
          builder = new RequestBuilder(RequestBuilder.GET, path + "SolutionRepositoryService?component=getSolutionRepositoryDoc");
        } else {
          builder = new RequestBuilder(RequestBuilder.GET,
              "/MantleService?passthru=SolutionRepositoryService&component=getSolutionRepositoryDoc&userid=joe&password=password");
        }

        RequestCallback callback = new RequestCallback() {

          public void onError(Request request, Throwable exception) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotGetRepositoryDocument(), false,
                false, true);
            dialogBox.center();
          }

          public void onResponseReceived(Request request, Response response) {
            // ok, we have a repository document, we can build the GUI
            // consider caching the document
            solutionDocument = (Document) XMLParser.parse((String) (String) response.getText());
            // update tree
            solutionTree.buildSolutionTree(solutionDocument);
            // update classic view
            classicNavigatorView.setSolutionDocument(solutionDocument);
            classicNavigatorView.buildSolutionNavigator();
            if (showSuccess) {
              MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().info(), Messages.getInstance().solutionBrowserRefreshed(), false, false,
                  true);
              dialogBox.center();
            }
          }

        };
        try {
          builder.sendRequest(null, callback);
        } catch (RequestException e) {
          Window.alert(e.toString());
        }
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(Boolean result) {
            fetchSolutionDocument(showSuccess);
          }

        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
  }

  public void loadPerspective(boolean force, boolean showStatus) {
    if (!hasBeenLoaded || force) {
      fetchSolutionDocument(showStatus);
      hasBeenLoaded = true;
    }
    workspacePanel.refreshWorkspace();
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

  public void createSchedule(final String cronExpression) {
    final AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        String solutionName = solutionTree.getSolution();
        String path = solutionTree.getPath();
        if (path.startsWith("/")) {
          path = path.substring(1);
        }
        String actionName = selectedFileItem.getName();

        AsyncCallback callback = new AsyncCallback() {

          public void onFailure(Throwable caught) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotCreateSchedule(), false, false,
                true);
            dialogBox.center();
          }

          public void onSuccess(Object result) {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getInstance().info(),
                Messages.getInstance().actionSequenceScheduledSuccess(),
                true, false, true);
            dialogBox.center();
          }
        };
        MantleServiceCache.getService().createCronJob(solutionName, path, actionName, cronExpression, callback);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            createSchedule(cronExpression);
          }
        });
      }
    };
    MantleServiceCache.getService().isAuthenticated(callback);
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
      currentItem.setStyleName("fileLabel");
      FileItem nextItem = filesListPanel.getFileItem(myIndex + 1);
      nextItem.setStyleName("fileLabelSelected");
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
      currentItem.setStyleName("fileLabel");
      FileItem nextItem = filesListPanel.getFileItem(myIndex - 1);
      nextItem.setStyleName("fileLabelSelected");
      setSelectedFileItem(nextItem);
    }
  }

  public void createSchedule() {
    AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(Object result) {
            createSchedule();
          }
        });
      }

      public void onSuccess(SolutionFileInfo fileInfo) {
        if (fileInfo.isSubscribable) {
          executeActionSequence(FileCommand.COMMAND.SUBSCRIBE);
        } else {
          NewScheduleDialog dialog = new NewScheduleDialog(fileInfo.getSolution(), fileInfo.getPath(), fileInfo.getName());
          dialog.center();
        }
      }
    };
    MantleServiceCache.getService().getSolutionFileInfo(selectedFileItem.getSolution(), selectedFileItem.getPath(), selectedFileItem.getName(), callback);

  }

  public void loadPropertiesDialog() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, isAdministrator(), new TabPanel(), null, FilePropertiesDialog.Tabs.GENERAL);
    dialog.showTab(FilePropertiesDialog.Tabs.GENERAL);
    dialog.center();
  }

  public void shareFile() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, isAdministrator(), new TabPanel(), null, FilePropertiesDialog.Tabs.PERMISSION);
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
    favoritesGroupMenuBar.addItem(new MenuItem(Messages.getInstance().manageGroups(), new Command() {
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
        MessageDialogBox dialog = new MessageDialogBox(Messages.getInstance().error(), Messages.getInstance().couldNotLoadBookmarks(), true, false, true);
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

    if (solutionTree.showLocalizedFileNames) {
      showLocalizedFileNamesMenuItem.setChecked(true);
    } else {
      showLocalizedFileNamesMenuItem.setChecked(false);
    }
    if (solutionTree.showHiddenFiles) {
      showHiddenFilesMenuItem.setChecked(true);
    } else {
      showHiddenFilesMenuItem.setChecked(false);
    }
    if (showSolutionBrowser) {
      showSolutionBrowserMenuItem.setChecked(true);
    } else {
      showSolutionBrowserMenuItem.setChecked(false);
    }

    if (explorerMode) {
      // viewMenuItems.add(showLocalizedFileNamesMenuItem);
      viewMenuItems.add(showSolutionBrowserMenuItem);
      viewMenuItems.add(showWorkspaceMenuItem);
      // viewMenuItems.add(showHiddenFilesMenuItem);
      if (MantleApplication.showAdvancedFeatures) {
        favoritesGroupMenuBar.setTitle(Messages.getInstance().favoriteGroups());
        viewMenuItems.add(favoritesGroupMenuBar);
      }
    }

    viewMenuItems.add(new MenuItemSeparator());

    viewMenuItems.add(new MenuItem(Messages.getInstance().refresh(), new RefreshPerspectiveCommand(this)));
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
      solutionNavigatorAndContentPanel.setSplitPosition("0px");
    }
    // update view menu
    installViewMenu(perspectiveCallback);
  }

  public void toggleShowSolutionBrowser() {
    if (!showSolutionBrowser) {
      solutionNavigatorAndContentPanel.setSplitPosition(defaultSplitPosition);
    } else {
      solutionNavigatorAndContentPanel.setSplitPosition("0px");
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
    fireSolutionBrowserListenerEvent();
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

  public void fireSolutionBrowserListenerEvent() {
    // does this take parameters? or should it simply return the state
    for (SolutionBrowserListener listener : listeners) {
      try {
        if (showWorkspaceMenuItem.isChecked()) {
          // cause all menus to be disabled for the selected file/tab
          listener.solutionBrowserEvent(null, null);
        } else {
          String selectedTabURL = null;
          if (contentTabPanel.getTabBar().getTabCount() > 0) {
            selectedTabURL = ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab())).getUrl();
          }
          listener.solutionBrowserEvent(selectedTabURL, selectedFileItem);
        }
      } catch (Exception e) {
        // don't let this fail, it will disturb normal processing
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().error(), e.toString(), false, false, true);
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
    return curPanel.getFrame().getElement().getAttribute("id");
  }

  public ReloadableIFrameTabPanel getCurrentFrame() {
    int curpos = contentTabPanel.getTabBar().getSelectedTab();
    final ReloadableIFrameTabPanel curPanel = (ReloadableIFrameTabPanel) contentTabPanel.getWidget(curpos);
    return curPanel;
  }

  private native boolean isPDF(com.google.gwt.dom.client.Element frame)
  /*-{
    return (frame.contentDocument != null && frame.contentDocument.getElementsByTagName('embed').length > 0);
  }-*/;

  private void refreshIfPDF(ReloadableIFrameTabPanel frame) {
    if (isPDF(frame.getElement())) {
      frame.reload();
    }
  }

  public void backgroundExecutionCompleted() {
    showWorkspace();
  }

  public void handleWAQRPreview(String url, String xml) {
    showNewURLTab(Messages.getInstance().preview(), "Ad Hoc Report Preview", "about:blank");
    NamedFrame namedFrame = ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab())).getFrame();
    final FormPanel form = new FormPanel(namedFrame);
    RootPanel.get().add(form);
    form.setMethod(FormPanel.METHOD_POST);
    form.setAction(url);
    form.add(new Hidden("reportXml", xml));
    form.submit();
    ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(contentTabPanel.getTabBar().getSelectedTab())).setForm(form);
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
      Frame frame = ((ReloadableIFrameTabPanel) currentWidget).getFrame();
      String url = frame.getUrl();
      if (url.contains(".analysis.xaction")) {
        return currentWidget;
      }
    }
    return null;
  }
}