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
package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.IMessageBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.MessageBundle;
import org.pentaho.mantle.client.commands.AboutCommand;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.CheckForSoftwareUpdatesCommand;
import org.pentaho.mantle.client.commands.ExecuteGlobalActionsCommand;
import org.pentaho.mantle.client.commands.LogoutCommand;
import org.pentaho.mantle.client.commands.ManageContentEditCommand;
import org.pentaho.mantle.client.commands.ManageContentScheduleCommand;
import org.pentaho.mantle.client.commands.ManageContentShareCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.OpenURLCommand;
import org.pentaho.mantle.client.commands.PentahoHomeCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.PurgeMondrianSchemaCacheCommand;
import org.pentaho.mantle.client.commands.RefreshMetaDataCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshSystemSettingsCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.ShowPreferencesCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.menus.MantleMenuBar;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.plugin.PluginPerspective;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.toolbars.MainToolbar;
import org.pentaho.mantle.login.client.MantleLoginDialog;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowCloseListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleApplication implements EntryPoint, IPerspectiveCallback, SolutionBrowserListener, IMessageBundleLoadCallback {

  public static boolean showAdvancedFeatures = false;

  private DeckPanel perspectivesPanel = new DeckPanel();
  private VerticalPanel mainApplicationPanel = new VerticalPanel();
  private FlexTable menuAndLogoPanel = new FlexTable();

  MenuBar menuBar = new MantleMenuBar(){

    @Override
    public void onBrowserEvent(Event event) {
      super.onBrowserEvent(event);

      final MenuItem item = getSelectedItem();
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER: {
          if (!"DIV".equals(event.getTarget().getNodeName())) { //$NON-NLS-1$ 
            this.getSelectedItem().addStyleDependentName("selected"); //$NON-NLS-1$
          }
          break;
        }
      }
    }
    
    @Override
    public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
      super.onPopupClosed(sender, autoClosed);
      this.getSelectedItem().removeStyleDependentName("selected"); //$NON-NLS-1$
    }
  };
  
  // menu items (to be enabled/disabled)
  MenuBar viewMenu = new MantleMenuBar(true);
  private MainToolbar mainToolbar;
  private LogoPanel logoPanel;

  private SolutionBrowserPerspective solutionBrowserPerspective;
  private FileCommand propertiesCommand;
  private RefreshRepositoryCommand refreshRepositoryCommand;

  // menu items (to be enabled/disabled)
  private PentahoMenuItem printMenuItem;
  private PentahoMenuItem saveMenuItem;
  private PentahoMenuItem saveAsMenuItem;
  private PentahoMenuItem propertiesMenuItem;

  public boolean isAdministrator = false;

  public void activatePerspective(IPerspective perspective) {
    for (int i = 0; i < perspectivesPanel.getWidgetCount(); i++) {
      Widget unloadWidget = perspectivesPanel.getWidget(i);
      if (unloadWidget instanceof IPerspective) {
        ((IPerspective) unloadWidget).unloadPerspective();
      }
    }
    perspective.loadPerspective(false, false);

    if (perspective instanceof Widget) {
      int index = perspectivesPanel.getWidgetIndex((Widget) perspective);
      perspectivesPanel.showWidget(index);
    }

  }

  Command showNavigatorCommand = new Command() {
    public void execute() {
      int index = perspectivesPanel.getWidgetIndex(solutionBrowserPerspective);
      perspectivesPanel.showWidget(index);
      Widget w = perspectivesPanel.getWidget(perspectivesPanel.getVisibleWidget());
      if (w instanceof IPerspective) {
        activatePerspective((IPerspective) w);
      }
      solutionBrowserPerspective.showLaunchOrContent();
    }
  };

  Command showWorkspaceCommand = new Command() {
    public void execute() {
      showNavigatorCommand.execute();
      solutionBrowserPerspective.showWorkspace();
    }
  };

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // after the Messages are loaded, IMessageBundleLoadCallback is fired and we can proceed
    Messages.setMessageBundle(new MessageBundle("messages/", "messages", this)); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void bundleLoaded(String bundleName) {
    solutionBrowserPerspective = new SolutionBrowserPerspective(this);
    propertiesCommand = new FileCommand(FileCommand.COMMAND.PROPERTIES, null, solutionBrowserPerspective);
    refreshRepositoryCommand = new RefreshRepositoryCommand(solutionBrowserPerspective);

    viewMenu = new MenuBar(true);
    // menu items (to be enabled/disabled)
    printMenuItem = new PentahoMenuItem(Messages.getString("print"), new PrintCommand(solutionBrowserPerspective)); //$NON-NLS-1$
    saveMenuItem = new PentahoMenuItem(Messages.getString("saveAsEllipsis"), new SaveCommand(solutionBrowserPerspective, false)); //$NON-NLS-1$
    saveAsMenuItem = new PentahoMenuItem(Messages.getString("saveAs"), new SaveCommand(solutionBrowserPerspective, true)); //$NON-NLS-1$
    propertiesMenuItem = new PentahoMenuItem(Messages.getString("propertiesEllipsis"), propertiesCommand); //$NON-NLS-1$

    mainToolbar = new MainToolbar(solutionBrowserPerspective);
    logoPanel = new LogoPanel("http://www.pentaho.com"); //$NON-NLS-1$
    
    // first things first... make sure we've registered our native hooks
    setupNativeHooks(this, solutionBrowserPerspective);

    Window.setTitle(Messages.getString("productName")); //$NON-NLS-1$

    Timer timer = new Timer() {

      public void run() {
        RootPanel loadingPanel = RootPanel.get("loading"); //$NON-NLS-1$
        if (loadingPanel != null) {
          loadingPanel.removeFromParent();
          loadingPanel.setVisible(false);
          loadingPanel.setHeight("0px"); //$NON-NLS-1$
        }
      }
    };
    timer.schedule(3000);

    mainApplicationPanel.setStyleName("applicationShell"); //$NON-NLS-1$
    mainApplicationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    menuBar.setAutoOpen(false);
    menuBar.setHeight("26px"); //$NON-NLS-1$
    menuBar.setWidth("100%"); //$NON-NLS-1$

    menuAndLogoPanel.setCellPadding(0);
    menuAndLogoPanel.setCellSpacing(0);
    menuAndLogoPanel.setStyleName("menuBarAndLogoPanel"); //$NON-NLS-1$
    menuAndLogoPanel.setWidth("100%"); //$NON-NLS-1$
    menuAndLogoPanel.setWidget(0, 0, menuBar);
    menuAndLogoPanel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    menuAndLogoPanel.setWidget(1, 0, mainToolbar);
    menuAndLogoPanel.setWidget(0, 1, logoPanel);
    menuAndLogoPanel.getFlexCellFormatter().setRowSpan(0, 1, 2);
    menuAndLogoPanel.getFlexCellFormatter().setWidth(0, 1, "180px"); //$NON-NLS-1$
    menuAndLogoPanel.getFlexCellFormatter().setHeight(0, 1, "100%"); //$NON-NLS-1$

    mainToolbar.setHeight("46px"); //$NON-NLS-1$
    mainToolbar.setWidth("100%"); //$NON-NLS-1$
    mainApplicationPanel.add(menuAndLogoPanel);
    mainApplicationPanel.setCellHeight(menuAndLogoPanel, "70px"); //$NON-NLS-1$

    perspectivesPanel.setAnimationEnabled(true);
    perspectivesPanel.setHeight("100%"); //$NON-NLS-1$
    perspectivesPanel.setWidth("100%"); //$NON-NLS-1$

    solutionBrowserPerspective.addSolutionBrowserListener(mainToolbar);
    solutionBrowserPerspective.addSolutionBrowserListener(this);
    perspectivesPanel.add(solutionBrowserPerspective);
    // perspectivesPanel.add(desktopPerspective);
    // perspectivesPanel.add(halogenPerspective);
    showNavigatorCommand.execute();

    // load mantle settings
    loadAndApplyMantleSettings();

    // load user settings
    loadAndApplyUserSettings();

    // load user bookmarks
    solutionBrowserPerspective.loadBookmarks();

    // show stuff we've created/configured
    mainApplicationPanel.add(perspectivesPanel);

    // add window close listener
    Window.addWindowCloseListener(new WindowCloseListener() {

      public void onWindowClosed() {
      }

      public String onWindowClosing() {
        // close only if we have stuff open
        if (solutionBrowserPerspective.getContentTabPanel().getTabBar().getTabCount() > 0) {
          return Messages.getString("windowCloseWarning"); //$NON-NLS-1$
        }
        return null;
      }
    });
    
    ElementUtils.convertPNGs();
  }

  
  /**
   * This method is used by things like jpivot in order to show a 'mantle' looking alert dialog instead of a standard alert dialog.
   * 
   * @param title
   * @param message
   */
  private void showMessage(String title, String message) {
    MessageDialogBox dialog = new MessageDialogBox(title, message, true, false, true);
    dialog.center();
  }
  
  public native void setupNativeHooks(MantleApplication mantle, SolutionBrowserPerspective solutionNavigator)
  /*-{
    $wnd.mantle_openTab = function(name, title, url) {
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::showNewURLTab(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(name, title, url);
    }
    $wnd.mantle_initialized = true;
    $wnd.sendMouseEvent = function(event) {
      return solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::mouseUp(Lcom/google/gwt/user/client/Event;)(event);
    }
    $wnd.closeTab = function(url) {
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::closeTab(Ljava/lang/String;)(url);
    }
    $wnd.mantle_refreshRepository = function() {
      var cmd = mantle.@org.pentaho.mantle.client.MantleApplication::refreshRepositoryCommand;
      cmd.@org.pentaho.mantle.client.commands.RefreshRepositoryCommand::execute(Z)(false);
    }
    $wnd.mantle_waqr_preview = function(url, xml) {
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::handleWAQRPreview(Ljava/lang/String;Ljava/lang/String;)(url, xml);
    }
    $wnd.mantle_showMessage = function(title, message) {
      mantle.@org.pentaho.mantle.client.MantleApplication::showMessage(Ljava/lang/String;Ljava/lang/String;)(title, message);
    }
    
    $wnd.enableAdhocSave = function(enable) {
      mantle.@org.pentaho.mantle.client.MantleApplication::enableAdhocSave(Z)(enable);
    }
    
  }-*/;

  public void loadAndApplyUserSettings() {
    AsyncCallback<List<IUserSetting>> callback = new AsyncCallback<List<IUserSetting>>() {

      public void onFailure(Throwable caught) {
        MessageDialogBox dialog = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), true, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.center();
      }

      public void onSuccess(List<IUserSetting> settings) {
        if (settings == null) {
          return;
        }
        for (IUserSetting setting : settings) {
          try {
            if (IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals(setting.getSettingName())) {
              boolean showNavigator = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
              solutionBrowserPerspective.setNavigatorShowing(showNavigator);
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals(setting.getSettingName())) {
              boolean showLocalizedFileNames = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
              solutionBrowserPerspective.setUseLocalizedFileNames(showLocalizedFileNames);
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getSettingName())) {
              boolean showHiddenFiles = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
              solutionBrowserPerspective.setShowHiddenFiles(showHiddenFiles);
            } else if (IMantleUserSettingsConstants.MANTLE_LOGO_LAUNCH_URL.equals(setting.getSettingName())) {
              String url = setting.getSettingValue();
              logoPanel.setLaunchURL(url);
            }
          } catch (Exception e) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
        mainToolbar.solutionBrowserEvent(null, null);
      }
    };
    MantleServiceCache.getService().getUserSettings(callback);
  }

  public void loadAndApplyMantleSettings() {
    AsyncCallback callback = new AsyncCallback() {

      public void onSuccess(Object result) {
        HashMap<String, String> settings = (HashMap<String, String>) result;

        // menubar=no,location=no,resizable=yes,scrollbars=no,status=no,width=1200,height=800
        RootPanel.get().add(mainApplicationPanel);
        RootPanel.get().add(WaitPopup.getInstance());

        boolean showExplorerViewOnStartup = "true".equals(settings.get("show-explorer-view-on-startup")); //$NON-NLS-1$ //$NON-NLS-2$
        showAdvancedFeatures = "true".equals(settings.get("show-advanced-features")); //$NON-NLS-1$ //$NON-NLS-2$
        buildMenuBar(settings);
        solutionBrowserPerspective.setExplorerViewShowing(showExplorerViewOnStartup);

        int numStartupURLs = Integer.parseInt(settings.get("num-startup-urls")); //$NON-NLS-1$
        for (int i = 0; i < numStartupURLs; i++) {
          String url = settings.get("startup-url-" + (i + 1)); //$NON-NLS-1$
          String name = settings.get("startup-name-" + (i + 1)); //$NON-NLS-1$
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            solutionBrowserPerspective.showNewURLTab(name != null ? name : url, url, url);
          }
        }
        if (solutionBrowserPerspective.getContentTabPanel().getWidgetCount() > 0) {
          solutionBrowserPerspective.getContentTabPanel().selectTab(0);
        }

        // startup-url on the URL for the app, wins over user-settings
        String startupURL = Window.Location.getParameter("startup-url"); //$NON-NLS-1$
        if (startupURL != null && !"".equals(startupURL)) { //$NON-NLS-1$
          String title = Window.Location.getParameter("name"); //$NON-NLS-1$
          startupURL = URL.decodeComponent(startupURL);
          solutionBrowserPerspective.showNewURLTab(title, title, startupURL);
        }

        mainToolbar.solutionBrowserEvent(null, null);
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Object result) {
            loadAndApplyMantleSettings();
          }

        });
      }
    };
    MantleServiceCache.getService().getMantleSettings(callback);

  }

  private void customizeMenu(final MenuBar menu, final String menuId, final HashMap<String, String> settings) {
    // see if we have any plugins to add
    if (settings.get(menuId + "MenuTitle0") != null) { //$NON-NLS-1$
      // we have at least one so we add a separator first
      menu.addSeparator();
      // we're going to loop until we don't find any more
      int idx = 0;
      String title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
      String command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      while (title != null) {
        // create a generic UrlCommand for this
        UrlCommand menuCommand = new UrlCommand(solutionBrowserPerspective, command, title);
        // add it to the menu
        menu.addItem(title, menuCommand);
        idx++;
        // try to get the next one
        title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
        command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      }
    }
  }

  public void buildMenuBar(final HashMap<String, String> settings) {
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {

      public void onSuccess(Boolean isAdministrator) {
        MantleApplication.this.isAdministrator = isAdministrator;

        solutionBrowserPerspective.setAdministrator(isAdministrator);

        MenuBar fileMenu = new MantleMenuBar(true);
        MenuBar newMenu = new MantleMenuBar(true);
        newMenu.addItem(Messages.getString("newAdhocReport"), new WAQRCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        newMenu.addItem(Messages.getString("newAnalysisViewEllipsis"), new AnalysisViewCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        // add additions to the file menu
        customizeMenu(newMenu, "file-new", settings); //$NON-NLS-1$

        fileMenu.addItem(Messages.getString("_new"), newMenu); //$NON-NLS-1$
        fileMenu.addItem(Messages.getString("openEllipsis"), new OpenFileCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        if (showAdvancedFeatures) {
          fileMenu.addItem(Messages.getString("openURLEllipsis"), new OpenURLCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        }
        fileMenu.addSeparator();

        fileMenu.addItem(saveMenuItem);
        fileMenu.addItem(saveAsMenuItem);
        fileMenu.addSeparator();

        fileMenu.addItem(printMenuItem);
        fileMenu.addSeparator();
        if (showAdvancedFeatures) {
          fileMenu.addItem(Messages.getString("userPreferencesEllipsis"), new ShowPreferencesCommand()); //$NON-NLS-1$
          fileMenu.addSeparator();
        }
        MenuBar manageContentMenu = new MantleMenuBar(true);
        manageContentMenu.addItem(new MenuItem(Messages.getString("editEllipsis"), new ManageContentEditCommand(solutionBrowserPerspective))); //$NON-NLS-1$
        manageContentMenu.addItem(new MenuItem(Messages.getString("shareEllipsis"), new ManageContentShareCommand(solutionBrowserPerspective))); //$NON-NLS-1$
        manageContentMenu.addItem(new MenuItem(Messages.getString("scheduleEllipsis"), new ManageContentScheduleCommand(solutionBrowserPerspective))); //$NON-NLS-1$
        customizeMenu(manageContentMenu, "file-manage", settings); //$NON-NLS-1$
        fileMenu.addItem(Messages.getString("manage"), manageContentMenu); //$NON-NLS-1$
        fileMenu.addSeparator();
        fileMenu.addItem(propertiesMenuItem);
        fileMenu.addSeparator();
        fileMenu.addItem(Messages.getString("logout"), true, new LogoutCommand()); //$NON-NLS-1$

        // add additions to the file menu
        customizeMenu(fileMenu, "file", settings); //$NON-NLS-1$

        menuBar.addItem(Messages.getString("file"), fileMenu); //$NON-NLS-1$

        // add plugin perspectives (urls)
        int numPluginPerspectives = Integer.parseInt(settings.get("num-plugin-perspectives")); //$NON-NLS-1$
        for (int i = 0; i < numPluginPerspectives; i++) {
          String url = settings.get("plugin-perspective-url-" + (i + 1)); //$NON-NLS-1$
          PluginPerspective plugin = new PluginPerspective(MantleApplication.this, url);
          perspectivesPanel.add(plugin);
          // add menu item
          viewMenu.addItem(settings.get("plugin-perspective-name-" + (i + 1)), plugin); //$NON-NLS-1$
        }

        // add additions to the view menu
        customizeMenu(viewMenu, "view", settings); //$NON-NLS-1$
        menuBar.addItem(Messages.getString("view"), viewMenu); //$NON-NLS-1$

        MenuBar toolsMenu = new MantleMenuBar(true);
        if (isAdministrator) {
          MenuBar adminMenu = new MantleMenuBar(true);
          adminMenu.addItem(Messages.getString("refreshRepository"), refreshRepositoryCommand); //$NON-NLS-1$
          adminMenu.addItem(Messages.getString("refreshSystemSettings"), new RefreshSystemSettingsCommand()); //$NON-NLS-1$
          adminMenu.addItem(Messages.getString("refreshReportingMetadata"), new RefreshMetaDataCommand()); //$NON-NLS-1$
          adminMenu.addItem(Messages.getString("executeGlobalActions"), new ExecuteGlobalActionsCommand()); //$NON-NLS-1$
          adminMenu.addItem(Messages.getString("purgeMondrianSchemaCache"), new PurgeMondrianSchemaCacheCommand()); //$NON-NLS-1$
          // add additions to the admin menu

          toolsMenu.addItem(Messages.getString("refresh"), adminMenu); //$NON-NLS-1$
          toolsMenu.addSeparator();
          toolsMenu.addItem(Messages.getString("softwareUpdates"), new CheckForSoftwareUpdatesCommand()); //$NON-NLS-1$
          menuBar.addItem(Messages.getString("tools"), toolsMenu); //$NON-NLS-1$
          // add additions to the admin menu
          customizeMenu(toolsMenu, "tools", settings); //$NON-NLS-1$
          customizeMenu(adminMenu, "tools-refresh", settings); //$NON-NLS-1$
        }

        MenuBar helpMenu = new MenuBar(true);
        helpMenu.addItem(Messages.getString("documentation"), new OpenDocCommand(settings.get("documentation-url"), solutionBrowserPerspective)); //$NON-NLS-1$ //$NON-NLS-2$
        helpMenu.addSeparator();
        helpMenu.addItem(Messages.getString("pentahoHomePageName"), new PentahoHomeCommand()); //$NON-NLS-1$
        helpMenu.addSeparator();
        helpMenu.addItem(Messages.getString("about"), new AboutCommand()); //$NON-NLS-1$
        // add additions to the help menu
        customizeMenu(helpMenu, "help", settings); //$NON-NLS-1$
        menuBar.addItem(Messages.getString("help"), helpMenu); //$NON-NLS-1$
      }

      public void onFailure(Throwable caught) {
        MantleLoginDialog.performLogin(new AsyncCallback<Boolean>() {

          public void onFailure(Throwable caught) {

          }

          public void onSuccess(Boolean result) {
            buildMenuBar(settings);
          }

        });
      }
    };
    MantleServiceCache.getService().isAdministrator(callback);
  }

  public void solutionBrowserEvent(String selectedTabURL, FileItem selectedFileItem) {
    final boolean isEnabled = (selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$

    printMenuItem.setEnabled(isEnabled);
    propertiesMenuItem.setEnabled(isEnabled);

    // Properties menu item should have a command associated with it ONLY when it is enabled.
    if (isEnabled) {
      propertiesMenuItem.setCommand(propertiesCommand);
    } else {
      propertiesMenuItem.setCommand(null);
    }    
    
    // Enable/Disable Save menu items based on content
    String[] saveTypes = new String[] { ".analysisview.xaction"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    boolean saveEnabled = false;
    if (selectedTabURL != null) {
      for (String saveType : saveTypes) {
        if (selectedTabURL.toLowerCase().indexOf(saveType) != -1) {
          saveEnabled = true;
        }
      }
    }
    saveMenuItem.setEnabled(saveEnabled && isEnabled);
    saveAsMenuItem.setEnabled(saveEnabled && isEnabled);

    if (selectedTabURL != null) {
      // Window.alert(selectedTabURL);
    }
    if (selectedFileItem != null) {
      // Window.alert(selectedFileItem.getLocalizedName());
    }
  }

  // Cache menu additions for removal later.
  private List<UIObject> viewMenuAdditions = new ArrayList<UIObject>();

  public void installViewMenu(List<UIObject> viewMenuItems) {
    // clear existing items
    if (viewMenuAdditions != null) {
      for (UIObject widget : viewMenuAdditions) {
        if (widget instanceof MenuItem) {
          viewMenu.removeItem((MenuItem) widget);
        } else if (widget instanceof MenuItemSeparator) {
          viewMenu.removeSeparator((MenuItemSeparator) widget);
        } else if (widget instanceof MenuBar) {
          ((MenuBar) widget).removeFromParent();
        }
      }
    }

    // add new items
    for (UIObject widget : viewMenuItems) {
      if (widget instanceof MenuItem) {
        MenuItem menuItem = (MenuItem) widget;
        viewMenu.addItem(menuItem);
      } else if (widget instanceof MenuItemSeparator) {
        viewMenu.addSeparator((MenuItemSeparator) widget);
      } else if (widget instanceof MenuBar) {
        MenuBar menuBar = (MenuBar) widget;
        viewMenu.addItem(menuBar.getTitle(), menuBar);
      }
    }
    viewMenuAdditions = viewMenuItems;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator(boolean isAdministrator) {
    this.isAdministrator = isAdministrator;
  }

  public void enableAdhocSave(boolean enable){
    saveMenuItem.setEnabled(enable);
    saveAsMenuItem.setEnabled(enable);
    this.mainToolbar.enableAdhocSave(enable);
  }
}
