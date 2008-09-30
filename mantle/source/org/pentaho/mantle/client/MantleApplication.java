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
package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.CheckForSoftwareUpdatesCommand;
import org.pentaho.mantle.client.commands.ExecuteGlobalActionsCommand;
import org.pentaho.mantle.client.commands.ManageContentEditCommand;
import org.pentaho.mantle.client.commands.ManageContentScheduleCommand;
import org.pentaho.mantle.client.commands.ManageContentShareCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.OpenURLCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.PurgeMondrianSchemaCacheCommand;
import org.pentaho.mantle.client.commands.RefreshMetaDataCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshSystemSettingsCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.dialogs.usersettings.UserPreferencesDialog;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.plugin.PluginPerspective;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.service.Utility;
import org.pentaho.mantle.client.toolbars.MainToolbar;
import org.pentaho.mantle.login.client.MantleLoginDialog;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
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
public class MantleApplication implements EntryPoint, IPerspectiveCallback, SolutionBrowserListener {

  public static final String PRODUCT_NAME = "Pentaho User Console";

  VerticalPanel applicationPanel = new VerticalPanel();

  FlexTable menuAndLogoPanel = new FlexTable();

  MenuBar menuBar = new MenuBar() {
    public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
      super.onPopupClosed(sender, autoClosed);
      this.getSelectedItem().removeStyleDependentName("selected");
    }

    @Override
    public void onBrowserEvent(Event e) {
      super.onBrowserEvent(e);

      if ("mouseover".equals(e.getType()) && !"DIV".equals(e.getTarget().getNodeName())) {
        this.getSelectedItem().addStyleDependentName("selected");
      }
    }
  };

  DeckPanel perspectivesPanel = new DeckPanel();

  SolutionBrowserPerspective solutionBrowserPerspective = new SolutionBrowserPerspective(this);

  // menu items (to be enabled/disabled)
  MenuBar viewMenu = new MenuBar(true);

  PentahoMenuItem printMenuItem = new PentahoMenuItem(Messages.getInstance().print(), new PrintCommand(solutionBrowserPerspective));

  PentahoMenuItem saveMenuItem = new PentahoMenuItem(Messages.getInstance().save(), new SaveCommand(solutionBrowserPerspective, false));

  PentahoMenuItem saveAsMenuItem = new PentahoMenuItem(Messages.getInstance().saveAs(), new SaveCommand(solutionBrowserPerspective, true));

  final Command propertiesCommand = new FileCommand(FileCommand.PROPERTIES, null, MantleApplication.this.solutionBrowserPerspective);
  
  PentahoMenuItem propertiesMenuItem = new PentahoMenuItem(Messages.getInstance().properties(), propertiesCommand);

  MainToolbar mainToolbar = new MainToolbar(solutionBrowserPerspective);

  public boolean isAdministrator = false;

  public static boolean showAdvancedFeatures;

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

  Command jiraCommand = new Command() {
    public void execute() {
      Window.open("http://jira.pentaho.org", "_blank", "");
    }
  };

  Command pentahoCommand = new Command() {
    public void execute() {
      Window.open("http://www.pentaho.com", "_blank", "");
    }
  };

  Command aboutCommand = new Command() {
    public void execute() {
      AsyncCallback<String> callback = new AsyncCallback<String>() {
        public void onFailure(Throwable caught) {
        }

        public void onSuccess(String version) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getInstance().about(), version, false, false, true);
          dialogBox.center();
        }
      };
      MantleServiceCache.getService().getVersion(callback);
    }
  };

  Command demosCommand = new Command() {
    public void execute() {
      Window.open("http://www.pentaho.com/products/demos/", "_blank", "");
    }
  };

  Command downloadsCommand = new Command() {
    public void execute() {
      Window.open("http://www.pentaho.com/download/", "_blank", "");
    }
  };

  Command nightlyBuildsCommand = new Command() {
    public void execute() {
      Window.open("ftp://download.pentaho.org", "_blank", "");
    }
  };

  Command forumsCommand = new Command() {
    public void execute() {
      Window.open("http://forums.pentaho.org/", "_blank", "");
    }
  };

  Command logoutCommand = new Command() {
    public void execute() {
      String location = Window.Location.getPath().substring(0, Window.Location.getPath().lastIndexOf('/')) + "/Logout";
      Window.open(location, "_top", "");
    }
  };

  Command cmd = new Command() {
    public void execute() {
      MessageDialogBox dialogBox = new MessageDialogBox("Title", "Message", false, false, true);
      dialogBox.center();
    }
  };

  Command preferencesCommand = new Command() {
    public void execute() {
      // read solution engine interactivity service
      UserPreferencesDialog dialog = new UserPreferencesDialog(UserPreferencesDialog.STYLES);
      dialog.center();
    }
  };

  Command refreshRepositoryCommand = new RefreshRepositoryCommand(solutionBrowserPerspective);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // first things first... make sure we've registered our native hooks
    setupNativeHooks(this, solutionBrowserPerspective);

    Window.setTitle(PRODUCT_NAME);

    Timer timer = new Timer() {

      public void run() {
        RootPanel loadingPanel = RootPanel.get("loading");
        if (loadingPanel != null) {
          loadingPanel.removeFromParent();
          loadingPanel.setVisible(false);
          loadingPanel.setHeight("0px");
        }
      }
    };
    timer.schedule(3000);

    applicationPanel.setStyleName("applicationShell");
    applicationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    menuBar.setAutoOpen(false);
    menuBar.setHeight("26px");
    menuBar.setWidth("100%");

    menuAndLogoPanel.setCellPadding(0);
    menuAndLogoPanel.setCellSpacing(0);
    menuAndLogoPanel.setStyleName("menuBarAndLogoPanel");
    menuAndLogoPanel.setWidth("100%");
    menuAndLogoPanel.setWidget(0, 0, menuBar);
    menuAndLogoPanel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    menuAndLogoPanel.setWidget(1, 0, mainToolbar);
    menuAndLogoPanel.setWidget(0, 1, new LogoPanel("http://www.pentaho.com"));
    menuAndLogoPanel.getFlexCellFormatter().setRowSpan(0, 1, 2);
    menuAndLogoPanel.getFlexCellFormatter().setWidth(0, 1, "180px");
    menuAndLogoPanel.getFlexCellFormatter().setHeight(0, 1, "71px");

    mainToolbar.setHeight("46px");
    mainToolbar.setWidth("100%");
    applicationPanel.add(menuAndLogoPanel);
    applicationPanel.setCellHeight(menuAndLogoPanel, "70px");

    perspectivesPanel.setAnimationEnabled(true);
    perspectivesPanel.setHeight("100%");
    perspectivesPanel.setWidth("100%");

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
    applicationPanel.add(perspectivesPanel);

    ElementUtils.convertPNGs();
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
  }-*/;

  public void loadAndApplyUserSettings() {
    AsyncCallback<List<IUserSetting>> callback = new AsyncCallback<List<IUserSetting>>() {

      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }

      public void onSuccess(List<IUserSetting> settings) {
        if (settings == null) {
          return;
        }
        for (IUserSetting setting : settings) {
          try {
            if (IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals(setting.getSettingName())) {
              boolean showNavigator = "true".equals(setting.getSettingValue());
              solutionBrowserPerspective.setNavigatorShowing(showNavigator);
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_LOCALIZED_FILENAMES.equals(setting.getSettingName())) {
              boolean showLocalizedFileNames = "true".equals(setting.getSettingValue());
              solutionBrowserPerspective.setUseLocalizedFileNames(showLocalizedFileNames);
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getSettingName())) {
              boolean showHiddenFiles = "true".equals(setting.getSettingValue());
              solutionBrowserPerspective.setShowHiddenFiles(showHiddenFiles);
            } else if (IMantleUserSettingsConstants.MANTLE_LOGO_LAUNCH_URL.equals(setting.getSettingName())) {
              String url = setting.getSettingValue();
              menuAndLogoPanel.setWidget(0, 1, new LogoPanel(url));
              menuAndLogoPanel.getFlexCellFormatter().setRowSpan(0, 1, 2);
              menuAndLogoPanel.getFlexCellFormatter().setWidth(0, 1, "180px");
              menuAndLogoPanel.getFlexCellFormatter().setHeight(0, 1, "71px");
            }
          } catch (Exception e) {
            MessageDialogBox dialogBox = new MessageDialogBox("Error", e.getMessage(), false, false, true);
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
        RootPanel.get().add(applicationPanel);

        Window.addWindowCloseListener(new WindowCloseListener() {

          public void onWindowClosed() {
          }

          public String onWindowClosing() {
            if (solutionBrowserPerspective.getContentTabPanel().getTabBar().getTabCount() > 0) {
              return "Navigating away from this page may terminate your session.";
            }
            return null;
          }
        });

        boolean showExplorerViewOnStartup = "true".equals(settings.get("show-explorer-view-on-startup"));
        showAdvancedFeatures = "true".equals(settings.get("show-advanced-features"));
        buildMenuBar(settings);
        solutionBrowserPerspective.setExplorerViewShowing(showExplorerViewOnStartup);

        int numStartupURLs = Integer.parseInt(settings.get("num-startup-urls"));
        for (int i = 0; i < numStartupURLs; i++) {
          String url = settings.get("startup-url-" + (i + 1));
          String name = settings.get("startup-name-" + (i + 1));
          if (url != null && !"".equals(url)) {
            solutionBrowserPerspective.showNewURLTab(name != null ? name : url, url, url);
          }
        }
        if (solutionBrowserPerspective.getContentTabPanel().getWidgetCount() > 0) {
          solutionBrowserPerspective.getContentTabPanel().selectTab(0);
        }

        // startup-url on the URL for the app, wins over user-settings
        String startupURL = Utility.getRequestParameter("startup-url");
        if (startupURL != null && !"".equals(startupURL)) {
          startupURL = URL.decodeComponent(startupURL);
          solutionBrowserPerspective.showNewURLTab(startupURL, startupURL, startupURL);
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

        MenuBar fileMenu = new MenuBar(true);
        MenuBar newMenu = new MenuBar(true);
        newMenu.addItem("Report", new WAQRCommand(solutionBrowserPerspective));
        newMenu.addItem("Analysis View", new AnalysisViewCommand(solutionBrowserPerspective));
        // add additions to the file menu
        customizeMenu(newMenu, "file-new", settings); //$NON-NLS-1$

        fileMenu.addItem("New", newMenu);
        fileMenu.addItem("Open...", new OpenFileCommand(solutionBrowserPerspective));
        if (showAdvancedFeatures) {
          fileMenu.addItem("Open URL..", new OpenURLCommand(solutionBrowserPerspective));
        }
        fileMenu.addSeparator();

        fileMenu.addItem(saveMenuItem);
        fileMenu.addItem(saveAsMenuItem);
        fileMenu.addSeparator();

        fileMenu.addItem(printMenuItem);
        fileMenu.addSeparator();
        if (showAdvancedFeatures) {
          fileMenu.addItem("Preferences...", preferencesCommand);
          fileMenu.addSeparator();
        }
        MenuBar manageContentMenu = new MenuBar(true);
        manageContentMenu.addItem(new MenuItem(Messages.getInstance().edit(), new ManageContentEditCommand(solutionBrowserPerspective)));
        manageContentMenu.addItem(new MenuItem(Messages.getInstance().share(), new ManageContentShareCommand(solutionBrowserPerspective)));
        manageContentMenu.addItem(new MenuItem(Messages.getInstance().schedule(), new ManageContentScheduleCommand(solutionBrowserPerspective)));
        customizeMenu(manageContentMenu, "file-manage", settings); //$NON-NLS-1$
        fileMenu.addItem(Messages.getInstance().manage(), manageContentMenu);
        fileMenu.addSeparator();
        fileMenu.addItem(propertiesMenuItem);
        fileMenu.addSeparator();
        fileMenu.addItem(Messages.getInstance().logout(), true, logoutCommand);

        // add additions to the file menu
        customizeMenu(fileMenu, "file", settings); //$NON-NLS-1$

        menuBar.addItem(Messages.getInstance().file(), fileMenu);

        // add plugin perspectives (urls)
        int numPluginPerspectives = Integer.parseInt(settings.get("num-plugin-perspectives"));
        for (int i = 0; i < numPluginPerspectives; i++) {
          String url = settings.get("plugin-perspective-url-" + (i + 1));
          PluginPerspective plugin = new PluginPerspective(MantleApplication.this, url);
          perspectivesPanel.add(plugin);
          // add menu item
          viewMenu.addItem(settings.get("plugin-perspective-name-" + (i + 1)), plugin);
        }

        // add additions to the view menu
        customizeMenu(viewMenu, "view", settings); //$NON-NLS-1$
        menuBar.addItem(Messages.getInstance().view(), viewMenu);

        MenuBar toolsMenu = new MenuBar(true);
        if (isAdministrator) {
          MenuBar adminMenu = new MenuBar(true);
          adminMenu.addItem(Messages.getInstance().refreshRepository(), refreshRepositoryCommand);
          adminMenu.addItem(Messages.getInstance().refreshSystemSettings(), new RefreshSystemSettingsCommand());
          adminMenu.addItem(Messages.getInstance().refreshReportingMetadata(), new RefreshMetaDataCommand());
          adminMenu.addItem(Messages.getInstance().executeGlobalActions(), new ExecuteGlobalActionsCommand());
          adminMenu.addItem(Messages.getInstance().purgeMondrianSchemaCache(), new PurgeMondrianSchemaCacheCommand());
          // add additions to the admin menu

          toolsMenu.addItem(Messages.getInstance().refresh(), adminMenu);
          toolsMenu.addSeparator();
          toolsMenu.addItem(Messages.getInstance().softwareUpdates(), new CheckForSoftwareUpdatesCommand());
          menuBar.addItem(Messages.getInstance().tools(), toolsMenu);
          // add additions to the admin menu
          customizeMenu(toolsMenu, "tools", settings); //$NON-NLS-1$
          customizeMenu(adminMenu, "tools-refresh", settings); //$NON-NLS-1$
        }

        MenuBar helpMenu = new MenuBar(true);
        helpMenu.addItem("Documentation...", new OpenDocCommand(solutionBrowserPerspective));
        helpMenu.addSeparator();
        helpMenu.addItem("Pentaho.com...", pentahoCommand);
        helpMenu.addSeparator();
        helpMenu.addItem(Messages.getInstance().about(), aboutCommand);
        // add additions to the help menu
        customizeMenu(helpMenu, "help", settings); //$NON-NLS-1$
        menuBar.addItem(Messages.getInstance().help(), helpMenu);
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
    final boolean isEnabled = (selectedTabURL != null && !"".equals(selectedTabURL));
    
    printMenuItem.setEnabled(isEnabled);
    saveMenuItem.setEnabled(isEnabled);
    saveAsMenuItem.setEnabled(isEnabled);
    propertiesMenuItem.setEnabled(isEnabled);
    
    // Properties menu item should have a command associated with it ONLY when it is enabled. 
    if (isEnabled) {
      propertiesMenuItem.setCommand(propertiesCommand);
    } else {
      propertiesMenuItem.setCommand(null);
    }
    
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

}
