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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.dialogs.usersettings.UserPreferencesDialog;
import org.pentaho.mantle.client.images.MantleImages;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.Bookmark;
import org.pentaho.mantle.client.objects.ReportContainer;
import org.pentaho.mantle.client.objects.RolePermission;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.objects.UserPermission;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.RefreshPerspectiveCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel.CustomFrame;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.FilePropertiesDialog;
import org.pentaho.mantle.client.perspective.solutionbrowser.fileproperties.PermissionsPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.reporting.ReportView;
import org.pentaho.mantle.client.perspective.solutionbrowser.scheduling.NewScheduleDialog;
import org.pentaho.mantle.client.perspective.workspace.IWorkspaceCallback;
import org.pentaho.mantle.client.perspective.workspace.WorkspacePerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.service.Utility;
import org.pentaho.mantle.login.client.MantleLoginDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalSplitPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

public class SolutionBrowserPerspective extends HorizontalPanel implements IPerspective, IFileItemCallback, IWorkspaceCallback {
  protected String BROWSE_LABEL_STYLE_NAME = "browsePanelMenuLabel"; //$NON-NLS-1$

  ClassicNavigatorView classicNavigatorView = new ClassicNavigatorView();
  HorizontalSplitPanel solutionNavigatorAndContentPanel = new HorizontalSplitPanel(MantleImages.images);
  VerticalSplitPanel solutionNavigatorPanel = new VerticalSplitPanel(MantleImages.images);
  SolutionTree solutionTree = new SolutionTree();
  FilesListPanel filesListPanel = new FilesListPanel(this);
  FileItem selectedFileItem;
  DeckPanel contentPanel = new DeckPanel();
  LaunchPanel launchPanel = new LaunchPanel(this);
  WorkspacePerspective workspacePanel = null;

  TabPanel contentTabPanel = new TabPanel();
  boolean hasBeenLoaded = false;
  IPerspectiveCallback perspectiveCallback;
  Document solutionDocument;
  String defaultSplitPosition = "220px";
  boolean showSolutionBrowser = false;
  boolean explorerMode = false;
  boolean isAdministrator = false;
  MenuBar favoritesGroupMenuBar = new MenuBar(true);
  List<Bookmark> bookmarks;
  List<SolutionBrowserListener> listeners = new ArrayList<SolutionBrowserListener>();

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
          Frame frame = ((ReloadableIFrameTabPanel) contentTabPanel.getWidget(tabIndex)).frame;
          refreshIfPDF(frame.getElement());
        }
      }
    });
    buildUI();
  }

  public void buildUI() {
    clear();
    if (explorerMode) {
      solutionNavigatorPanel.setHeight("100%");
      // ----- Create the top panel ----
      DockPanel topPanel = new DockPanel();
      topPanel.add(solutionTree, DockPanel.CENTER);
      topPanel.setWidth("100%");
      Label browseLabel = new Label(Messages.getInstance().browse());
      browseLabel.setHeight("28px"); //$NON-NLS-1$
      browseLabel.setWidth("100%");
      browseLabel.addStyleName(BROWSE_LABEL_STYLE_NAME);
      topPanel.add(browseLabel, DockPanel.NORTH);
      // --------------------------------
      solutionNavigatorPanel.setTopWidget(topPanel);
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

  public void showWorkspace() {
    workspacePanel.refreshPerspective(false);
    contentPanel.showWidget(contentPanel.getWidgetIndex(workspacePanel));
    fireSolutionBrowserListenerEvent();
  }

  public void showLaunchOrContent() {
    int showIndex = -1;
    if (contentTabPanel.getWidgetCount() == 0) {
      showIndex = contentPanel.getWidgetIndex(launchPanel);
    } else {
      showIndex = contentPanel.getWidgetIndex(contentTabPanel);
    }
    if (showIndex != -1) {
      contentPanel.showWidget(showIndex);
    }
    fireSolutionBrowserListenerEvent();
  }

  private boolean existingTabMatchesName(String name){
    int tabCount = contentTabPanel.getTabBar().getTabCount();
    String startKey = "class=\"gwt-Label\">";

    NodeList<com.google.gwt.dom.client.Element> divs = contentTabPanel.getTabBar().getElement().getElementsByTagName("div");
    
    for(int i=0; i<divs.getLength(); i++){
      String tabHtml = divs.getItem(i).getInnerHTML();
      //TODO: rmove once a more elegent tab solution is in place
      if(tabHtml.indexOf(startKey) == -1){
        continue;
      }
      int startOffset = tabHtml.indexOf(startKey)+startKey.length();
      String tabLabel = tabHtml.substring(startOffset, tabHtml.indexOf("</div>", startOffset));
      
      if(tabLabel.equals(name)){
        return true;
      }
    }
    return false;
  }
  
  public void showNewURLTab(String tabName, String tabTooltip, String url) {
    ReloadableIFrameTabPanel panel = new ReloadableIFrameTabPanel(url);
    Frame frame = panel.getFrame();

    frame.setStyleName("gwt-Frame");
    panel.add(frame);
    frame.setWidth("100%");
    frame.setHeight("100%");

    final int elementId = contentTabPanel.getWidgetCount();
    DOM.setElementAttribute(frame.getElement(), "id", "frameID: " + elementId);

    String finalTabName = tabName;
    //check for other tabs with this name
    if(existingTabMatchesName(tabName)){
      int counter = 2;
      while(true){
        if(existingTabMatchesName(tabName+" ("+counter+")")){ //unique
          counter++;
          continue;
        } else {
          finalTabName = tabName+" ("+counter+")";
          break;
        }
      }
    }
    
    
    contentTabPanel.add(panel, new TabWidget(finalTabName, tabTooltip, this, contentTabPanel, panel));
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
    // wire up client-side javascript to handle mouse events
    ((CustomFrame) frame).attachEventListeners(frame.getElement());

    // update state to workspace state flag
    showWorkspaceMenuItem.setChecked(false);
    // fire
    fireSolutionBrowserListenerEvent();

    perspectiveCallback.activatePerspective(this);
  }

  public void openNewHTMLReport(int mode) {
    final String reportKey = "/" + selectedFileItem.getSolution() + selectedFileItem.getPath() + "/" + selectedFileItem.getName();
    AsyncCallback<ReportContainer> callback = new AsyncCallback<ReportContainer>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
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

  public void openFile(int mode) {
    String name = selectedFileItem.getName();
    if (name.endsWith(".xaction")) {
      executeActionSequence(mode);
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

  public void openFile(String path, String name) {
    List<String> pathSegments = new ArrayList<String>();
    if (path != null) {
      int index = path.indexOf("/", 0);
      while (index >= 0) {
        int oldIndex = index;
        index = path.indexOf("/", oldIndex + 1);
        if (index >= 0) {
          pathSegments.add(path.substring(oldIndex + 1, index));
        }
      }
      pathSegments.add(path.substring(path.lastIndexOf("/") + 1));
    }

    String repoPath = "";
    for (int i = 1; i < pathSegments.size(); i++) {
      repoPath += "/" + pathSegments.get(i);
    }

    selectedFileItem = new FileItem(name, name, false, pathSegments.get(0), repoPath, "", null, null);

    pathSegments.add(name);
    FileTreeItem fileTreeItem = solutionTree.getTreeItem(pathSegments);

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

    openFile(FileCommand.RUN);
  }

  public void editFile() {
    if (selectedFileItem.getName().endsWith(".waqr.xaction")) {
      String filename = selectedFileItem.getName().substring(0, selectedFileItem.getName().indexOf(".waqr.xaction")) + ".waqr.xreportspec";
      String url = "/pentaho/adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename=" + filename;
      if (!GWT.isScript()) {
        url = "http://localhost:8080/pentaho/adhoc/waqr.html?solution=" + selectedFileItem.getSolution() + "&path=" + selectedFileItem.getPath() + "&filename="
            + filename;
      }
      
      //See if it's already loaded
      for(int i=0; i<contentTabPanel.getWidgetCount(); i++){
        Widget w = contentTabPanel.getWidget(i);
        if(w instanceof ReloadableIFrameTabPanel && ((ReloadableIFrameTabPanel) w).url.endsWith(url)){
          //Already up, select and exit
          contentTabPanel.selectTab(i);
          return;
        }
        
      }
      
      showNewURLTab("Editing: "+selectedFileItem.getLocalizedName(), "Editing: "+selectedFileItem.getLocalizedName(), url);
    }
  }

  void executeActionSequence(final int mode) {
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
          url = "/pentaho/ViewAction?solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action=" + selectedFileItem.getName();
        } else {
          url = "http://localhost:8080/pentaho/ViewAction?solution=" + selectedFileItem.getSolution() + "&path=" + path + "&action="
              + selectedFileItem.getName() + "&userid=joe&password=password";
        }

        if (mode == FileCommand.BACKGROUND) {
          MessageDialogBox dialogBox = new MessageDialogBox(
              "Info",
              "Reports that prompt for parameters are not supported with this feature and may result in errors.<BR><BR>  You will be notified when the content is ready.",
              true, false, true);
          dialogBox.center();

          url += "&background=true";
          RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
          try {
            builder.sendRequest(null, new RequestCallback() {

              public void onError(Request request, Throwable exception) {
              }

              public void onResponseReceived(Request request, Response response) {
              }

            });
          } catch (RequestException e) {
          }
        } else if (mode == FileCommand.NEWWINDOW) {
          // popup blockers might attack this
          Window.open(url, "_blank", "");
        } else if (mode == FileCommand.SUBSCRIBE) {
          final String myurl = url + "&subscribepage=yes";
          AsyncCallback<SolutionFileInfo> callback = new AsyncCallback<SolutionFileInfo>() {

            public void onFailure(Throwable caught) {
              MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
              dialogBox.center();
            }

            public void onSuccess(SolutionFileInfo fileInfo) {
              if (fileInfo.isSubscribable) {
                boolean subscribe = false;
                if (fileInfo.supportsAccessControls) {
                  // check perms
                  for (RolePermission role : fileInfo.rolePermissions) {
                    int mask = role.getMask();
                    if ((mask & PermissionsPanel.PERM_SUBSCRIBE) == PermissionsPanel.PERM_SUBSCRIBE) {
                      subscribe = true;
                      break;
                    }
                  }
                  for (UserPermission user : fileInfo.userPermissions) {
                    int mask = user.getMask();
                    if ((mask & PermissionsPanel.PERM_SUBSCRIBE) == PermissionsPanel.PERM_SUBSCRIBE) {
                      subscribe = true;
                      break;
                    }
                  }
                } else {
                  // no perm support, we're ok
                  subscribe = true;
                }
                if (subscribe) {
                  showNewURLTab(selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(), myurl);
                } else {
                  MessageDialogBox dialogBox = new MessageDialogBox("Info", "You do not have permission to subscribe to this action sequence.", false, false,
                      true);
                  dialogBox.center();
                }
              } else {
                MessageDialogBox dialogBox = new MessageDialogBox("Info", "This action sequence is not subscribable.", false, false, true);
                dialogBox.center();
              }
            }
          };
          MantleServiceCache.getService().getSolutionFileInfo(selectedFileItem.getSolution(), selectedFileItem.getPath(), selectedFileItem.getName(), callback);
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
          builder = new RequestBuilder(RequestBuilder.GET, "/pentaho/SolutionRepositoryService?component=getSolutionRepositoryDoc");
        } else {
          builder = new RequestBuilder(RequestBuilder.GET,
              "/MantleService?passthru=SolutionRepositoryService&component=getSolutionRepositoryDoc&userid=joe&password=password");
        }

        RequestCallback callback = new RequestCallback() {

          public void onError(Request request, Throwable exception) {
            Window.alert(exception.toString());
          }

          public void onResponseReceived(Request request, Response response) {
            // ok, we have a repository document, we can build the GUI
            // consider caching the document
            Utility.setDefaultCursor();
            solutionDocument = (Document) XMLParser.parse((String) (String) response.getText());
            // update tree
            solutionTree.buildSolutionTree(solutionDocument);
            // update classic view
            classicNavigatorView.setSolutionDocument(solutionDocument);
            classicNavigatorView.buildSolutionNavigator();
            if (showSuccess) {
              MessageDialogBox dialogBox = new MessageDialogBox("Info", "Solution Navigator Refreshed", false, false, true);
              dialogBox.center();
            }
          }

        };
        try {
          builder.sendRequest(null, callback);
        } catch (RequestException e) {
          Window.alert(e.toString());
        }

        // SafeAsyncCallback callback = new SafeAsyncCallback() {
        //
        // public void onFailure(Object caught) {
        // MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, null, false, true);
        // dialogBox.center();
        // }
        //
        // public void onSuccess(Object result) {
        // Utility.setDefaultCursor();
        // solutionDocument = (Document) XMLParser.parse((String) result);
        // // update tree
        // solutionTree.buildSolutionTree(solutionDocument, true);
        // // update classic view
        // classicNavigatorView.setSolutionDocument(solutionDocument);
        // classicNavigatorView.buildSolutionNavigator();
        // if (showSuccess) {
        // MessageDialogBox dialogBox = new MessageDialogBox("Info", "Solution Navigator Refreshed", false, null, false, true);
        // dialogBox.center();
        // }
        // }
        // };
        // MantleServiceCache.getService().getSolutionRepositoryDoc(new String[] { ".xaction", ".url", ".prc" }, callback);
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
    workspacePanel.refreshPerspective(showStatus);
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
            MessageDialogBox dialogBox = new MessageDialogBox("Error", caught.toString(), false, false, true);
            dialogBox.center();
          }

          public void onSuccess(Object result) {
            MessageDialogBox dialogBox = new MessageDialogBox(
                "Info",
                "The action-sequence has been scheduled successfully.  If the output of the action-sequence is \"response\" the content will be lost.<BR><BR>You can modify your action-sequence to deliver the content via e-mail if necessary.",
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

  public void createSchedule() {
    final AsyncCallback authenticatedCallback = new AsyncCallback() {

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Object result) {
            createSchedule();
          }
        });
      }

      public void onSuccess(Object result) {
        String solutionName = solutionTree.getSolution();
        String path = solutionTree.getPath();
        String actionName = selectedFileItem.getName();

        MantleServiceCache.getService().getSolutionFileInfo(solutionName, path, actionName, new AsyncCallback<SolutionFileInfo>() {

          public void onFailure(Throwable caught) {
          }

          public void onSuccess(SolutionFileInfo fileInfo) {
            if (fileInfo.isSubscribable) {
              executeActionSequence(FileCommand.SUBSCRIBE);
            } else {
              NewScheduleDialog dialog = new NewScheduleDialog(fileInfo.getSolution(), fileInfo.getPath(), fileInfo.getName());
              dialog.center();
            }
          }
        });
      }

    };

    MantleServiceCache.getService().isAuthenticated(authenticatedCallback);
  }

  public void loadPropertiesDialog() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, isAdministrator(), new TabPanel(), null);
    dialog.showTab(FilePropertiesDialog.Tabs.GENERAL);
    dialog.center();
  }

  public void shareFile() {
    FileItem selectedItem = getSelectedFileItem();
    FilePropertiesDialog dialog = new FilePropertiesDialog(selectedItem, isAdministrator(), new TabPanel(), null);
    dialog.showTab(FilePropertiesDialog.Tabs.PERMISSION);
    dialog.center();
  }

  public void setUseLocalizedFileNames(boolean showLocalizedFileNames) {
    solutionTree.setShowLocalizedFileNames(showLocalizedFileNames);
    for (int i = 0; i < filesListPanel.getFileCount(); i++) {
      filesListPanel.toggleLocalizedFileName(i);
    }
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
    favoritesGroupMenuBar.addItem(new MenuItem("Manage Groups", new Command() {
      public void execute() {
        // bring up dialog to edit groups
        UserPreferencesDialog dialog = new UserPreferencesDialog(UserPreferencesDialog.FAVORITES);
        dialog.center();
      }
    }));
  }

  public void loadBookmarks() {
    AsyncCallback<List<Bookmark>> callback = new AsyncCallback<List<Bookmark>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
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
        favoritesGroupMenuBar.setTitle("Favorite Groups");
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
        e.printStackTrace();
        MessageDialogBox dialogBox = new MessageDialogBox("Error", e.toString(), false, false, true);
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
    final Frame currFrame = curPanel.getFrame();
    final String elementId = DOM.getElementAttribute(currFrame.getElement(), "id");
    return elementId;
  }

  private native void refreshIfPDF(com.google.gwt.dom.client.Element frame)
  /*-{
    if(frame.contentDocument != null && frame.contentDocument.getElementsByTagName('embed').length > 0){
      frame.contentWindow.location.href = frame.contentWindow.location.href;
    }
  }-*/;

  public void backgroundExecutionCompleted() {
    showWorkspace();
  }

}