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
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
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
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.dialogs.FileDialog;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.menus.MantleMenuBar;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.IPerspective;
import org.pentaho.mantle.client.perspective.IPerspectiveCallback;
import org.pentaho.mantle.client.perspective.plugin.PluginPerspective;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IReloadableTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.login.client.MantleLoginDialog;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
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
public class MantleApplication implements EntryPoint, IPerspectiveCallback, SolutionBrowserListener, IResourceBundleLoadCallback {

  public static boolean showAdvancedFeatures = false;

  private DeckPanel perspectivesPanel = new DeckPanel();
  private VerticalPanel mainApplicationPanel = new VerticalPanel();
  private FlexTable menuAndLogoPanel = new FlexTable();
  private LogoPanel logoPanel;

  // menu items (to be enabled/disabled)
  private MenuBar menuBar;
  private MenuBar viewMenu;
  //private MainToolbar mainToolbar;
  private XulMain main;

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
    // just some quick sanity setting of the platform effective locale based on the override
    // which comes from the url parameter
    if (!StringUtils.isEmpty(Window.Location.getParameter("locale"))) {
      MantleServiceCache.getService().setLocaleOverride(Window.Location.getParameter("locale"), null);
    }
    ResourceBundle messages = new ResourceBundle();
    Messages.setResourceBundle(messages); 
    messages.loadBundle("messages/", "messages", true, MantleApplication.this); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void bundleLoaded(String bundleName) {
    solutionBrowserPerspective = new SolutionBrowserPerspective(this);
    propertiesCommand = new FileCommand(FileCommand.COMMAND.PROPERTIES, null, solutionBrowserPerspective);
    refreshRepositoryCommand = new RefreshRepositoryCommand(solutionBrowserPerspective);

    viewMenu = new MantleMenuBar(true);
    viewMenu.getElement().setId("view_menu");
    // menu items (to be enabled/disabled)
    printMenuItem = new PentahoMenuItem(Messages.getString("print"), new PrintCommand(solutionBrowserPerspective)); //$NON-NLS-1$
    printMenuItem.getElement().setId("print");
    saveMenuItem = new PentahoMenuItem(Messages.getString("save"), new SaveCommand(solutionBrowserPerspective, false)); //$NON-NLS-1$
    saveMenuItem.getElement().setId("save");
    saveAsMenuItem = new PentahoMenuItem(Messages.getString("saveAsEllipsis"), new SaveCommand(solutionBrowserPerspective, true)); //$NON-NLS-1$
    saveAsMenuItem.getElement().setId("saveAs");
    propertiesMenuItem = new PentahoMenuItem(Messages.getString("propertiesEllipsis"), propertiesCommand); //$NON-NLS-1$
    propertiesMenuItem.getElement().setId("properties");

    main = XulMain.instance(solutionBrowserPerspective);
    logoPanel = new LogoPanel("http://www.pentaho.com"); //$NON-NLS-1$
    
    // first things first... make sure we've registered our native hooks
    setupNativeHooks(this, main, solutionBrowserPerspective);

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
    menuBar = new MantleMenuBar() {
      @Override
      public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER: {
          if (!"DIV".equals(event.getTarget().getNodeName()) && getSelectedItem() != null) { //$NON-NLS-1$ 
            getSelectedItem().addStyleDependentName("selected"); //$NON-NLS-1$
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
    menuBar.getElement().setId("main_toolbar");
    menuBar.setAutoOpen(false);
    menuBar.setHeight("26px"); //$NON-NLS-1$
    menuBar.setWidth("100%"); //$NON-NLS-1$

    // load mantle settings
    loadAndApplyMantleSettings();
    

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
  
  public native void setupNativeHooks(MantleApplication mantle, XulMain main, SolutionBrowserPerspective solutionNavigator)
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
    
    $wnd.mantle_confirmBackgroundExecutionDialog = function(url) {
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::confirmBackgroundExecutionDialog(Ljava/lang/String;)(url);      
    }
    
    $wnd.enableAdhocSave = function(enable) {
      mantle.@org.pentaho.mantle.client.MantleApplication::enableAdhocSave(Z)(enable);
    }
    
    $wnd.enableContentEdit = function(enable) { 
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::enableContentEdit(Z)(enable);      
    }
    
    $wnd.setContentEditSelected = function(enable) { 
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::setContentEditSelected(Z)(enable);      
    }
    
    $wnd.registerContentOverlay = function(id) { 
      solutionNavigator.@org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective::registerContentOverlay(Ljava/lang/String;)(id);      
    }
    
    $wnd.registerContentCallback = function(callback) { 
      main.@org.pentaho.mantle.client.XulMain::registerContentCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);      
    }
    
    
    $wnd.openFileDialog = function(callback,title, okText, fileTypes) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, title, okText, fileTypes);      
    }
    
    $wnd.openFileDialogWithPath = function(callback, path, title, okText, fileTypes) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::showOpenFileDialog(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, path, title, okText, fileTypes);      
    }
    
    $wnd.addGlassPaneListener = function(callback) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);      
    }
    
    
  }-*/;

  public void addGlassPaneListener(JavaScriptObject obj){
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
  }
  
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
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals(setting.getSettingName())) {
              boolean useDescriptions = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
              solutionBrowserPerspective.setUseDescriptions(useDescriptions);
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
      }
    };
    MantleServiceCache.getService().getUserSettings(callback);
  }

  public void loadAndApplyMantleSettings() {
    AsyncCallback<HashMap<String, String>> callback = new AsyncCallback<HashMap<String, String>>() {

      public void onSuccess(HashMap<String, String> settings) {
        menuAndLogoPanel.setCellPadding(0);
        menuAndLogoPanel.setCellSpacing(0);
        menuAndLogoPanel.setStyleName("menuBarAndLogoPanel"); //$NON-NLS-1$
        menuAndLogoPanel.setWidth("100%"); //$NON-NLS-1$
        if ("true".equals(settings.get("show-logo-panel")) && "true".equals(settings.get("show-menu-bar")) && "true".equals(settings.get("show-main-toolbar"))) {
          menuAndLogoPanel.setWidget(0, 1, logoPanel);
          menuAndLogoPanel.getFlexCellFormatter().setRowSpan(0, 1, 2);
          menuAndLogoPanel.getFlexCellFormatter().setWidth(0, 1, "180px"); //$NON-NLS-1$
          menuAndLogoPanel.getFlexCellFormatter().setHeight(0, 1, "100%"); //$NON-NLS-1$
        }
        if ("true".equals(settings.get("show-menu-bar"))) {
          menuAndLogoPanel.setWidget(0, 0, menuBar);
          menuAndLogoPanel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        }
        if ("true".equals(settings.get("show-main-toolbar"))) {
          menuAndLogoPanel.setWidget(1, 0, main);
          main.setHeight("46px"); //$NON-NLS-1$
          main.setWidth("100%"); //$NON-NLS-1$
        }

        mainApplicationPanel.add(menuAndLogoPanel);
        mainApplicationPanel.setCellHeight(menuAndLogoPanel, "1px"); //$NON-NLS-1$

        perspectivesPanel.setAnimationEnabled(true);
        perspectivesPanel.setHeight("100%"); //$NON-NLS-1$
        perspectivesPanel.setWidth("100%"); //$NON-NLS-1$

        solutionBrowserPerspective.addSolutionBrowserListener(MantleApplication.this);
        perspectivesPanel.add(solutionBrowserPerspective);
        // perspectivesPanel.add(desktopPerspective);
        // perspectivesPanel.add(halogenPerspective);
        showNavigatorCommand.execute();

        // load user settings
        loadAndApplyUserSettings();

        // load user bookmarks
        solutionBrowserPerspective.loadBookmarks();

        // update supported file types
        solutionBrowserPerspective.buildEnabledOptionsList(settings);

        // show stuff we've created/configured
        mainApplicationPanel.add(perspectivesPanel);

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
//      menu.addSeparator();
      // we're going to loop until we don't find any more
      int idx = 0;
      String title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
      String command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      while (title != null) {
        // create a generic UrlCommand for this
        if(!GWT.isScript() && command.indexOf("content") > -1) {
          int index = command.indexOf("?");
          if( index >=0) {
            command = "/MantleService?passthru=" + command.substring(command.indexOf("content"), index) + "&" + command.substring(index+1) + "&userid=joe&password=password"; ;            
          } else  {
            command = "/MantleService?passthru=" + command.substring(command.indexOf("content")) + "&userid=joe&password=password"; ;  
          }
          
        }
        UrlCommand menuCommand = new UrlCommand(solutionBrowserPerspective, command, title);
        
        MenuItem item = new MenuItem(title, menuCommand);
        item.getElement().setId(title);
        
        // add it to the menu
        menu.addItem(item);
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
        fileMenu.getElement().setId("file_menu");
        MenuBar newMenu = new MantleMenuBar(true);
        newMenu.getElement().setId("new_menu");
        MenuItem waqrMenuItem = new MenuItem(Messages.getString("newAdhocReport"), new WAQRCommand(solutionBrowserPerspective));//$NON-NLS-1$
        waqrMenuItem.getElement().setId("waqr_menu_item");
        newMenu.addItem(waqrMenuItem);
        MenuItem analysisMenuItem = new MenuItem(Messages.getString("newAnalysisViewEllipsis"), new AnalysisViewCommand(solutionBrowserPerspective));//$NON-NLS-1$
        analysisMenuItem.getElement().setId("new_analysis_view_menu_item");
        newMenu.addItem(analysisMenuItem); //$NON-NLS-1$
        // add additions to the file menu
        customizeMenu(newMenu, "file-new", settings); //$NON-NLS-1$
        
        MenuItem newMenuBar = new MenuItem(Messages.getString("_new"), newMenu); //$NON-NLS-1$
        newMenuBar.getElement().setId("new_menu_bar");
        fileMenu.addItem(newMenuBar);

        MenuItem openFileMenuItem = new MenuItem(Messages.getString("openEllipsis"), new OpenFileCommand(solutionBrowserPerspective));//$NON-NLS-1$
        openFileMenuItem.getElement().setId("open_file_menu_item");
        fileMenu.addItem(openFileMenuItem); //$NON-NLS-1$
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
        manageContentMenu.getElement().setId("manage_content_menu");
        MenuItem editContent = new MenuItem(Messages.getString("editEllipsis"), new ManageContentEditCommand(solutionBrowserPerspective));//$NON-NLS-1$
        MenuItem shareContent = new MenuItem(Messages.getString("shareEllipsis"), new ManageContentShareCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        MenuItem scheduleContent = new MenuItem(Messages.getString("scheduleEllipsis"), new ManageContentScheduleCommand(solutionBrowserPerspective)); //$NON-NLS-1$
        manageContentMenu.addItem(editContent); 
        manageContentMenu.addItem(shareContent);
        manageContentMenu.addItem(scheduleContent);
        customizeMenu(manageContentMenu, "file-manage", settings); //$NON-NLS-1$
        MenuItem manageContentMenuBar = new MenuItem(Messages.getString("manage"), manageContentMenu); //$NON-NLS-1$
        manageContentMenuBar.getElement().setId("manage_content_menu_bar");
        fileMenu.addItem(manageContentMenuBar);
        fileMenu.addSeparator();
        fileMenu.addItem(propertiesMenuItem);
        fileMenu.addSeparator();
        MenuItem logoutMenuItem = new MenuItem(Messages.getString("logout"), true, new LogoutCommand()); //$NON-NLS-1$
        logoutMenuItem.getElement().setId("logout_menu_item");
        fileMenu.addItem(logoutMenuItem); 

        // add additions to the file menu
        customizeMenu(fileMenu, "file", settings); //$NON-NLS-1$

        MenuItem fileMenuBar = new MenuItem(Messages.getString("file"), fileMenu);//$NON-NLS-1$
        fileMenuBar.getElement().setId("file_menu_bar");
        menuBar.addItem(fileMenuBar); 

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
        MenuItem viewMenuBar = new MenuItem(Messages.getString("view"), viewMenu); //$NON-NLS-1$
        viewMenuBar.getElement().setId("view_menu_bar");
        menuBar.addItem(viewMenuBar);

        MenuBar toolsMenu = new MantleMenuBar(true);
        if (isAdministrator) {
          MenuBar adminMenu = new MantleMenuBar(true);
          adminMenu.getElement().setId("admin_menu");
          
          MenuItem refreshRepositoryMenuItem = new MenuItem(Messages.getString("refreshRepository"), refreshRepositoryCommand); //$NON-NLS-1$
          MenuItem refreshSystemSettingsMenuItem = new MenuItem(Messages.getString("refreshSystemSettings"), new RefreshSystemSettingsCommand()); //$NON-NLS-1$
          MenuItem refreshMetadataMenuItem = new MenuItem(Messages.getString("refreshReportingMetadata"), new RefreshMetaDataCommand()); //$NON-NLS-1$
          MenuItem executeGlobalActionsMenuItem = new MenuItem(Messages.getString("executeGlobalActions"), new ExecuteGlobalActionsCommand()); //$NON-NLS-1$
          MenuItem purgeMondrianSchemaCacheMenuItem = new MenuItem(Messages.getString("purgeMondrianSchemaCache"), new PurgeMondrianSchemaCacheCommand()); //$NON-NLS-1$

          refreshRepositoryMenuItem.getElement().setId("refresh_repository_menu_item");
          refreshSystemSettingsMenuItem.getElement().setId("refresh_system_settings_menu_item");
          refreshMetadataMenuItem.getElement().setId("refresh_metadata_menu_item");
          executeGlobalActionsMenuItem.getElement().setId("execute_global_actions_menu_item");
          purgeMondrianSchemaCacheMenuItem.getElement().setId("purge_mondrian_schema_cache_menu_item");
          
          adminMenu.addItem(refreshRepositoryMenuItem); 
          adminMenu.addItem(refreshSystemSettingsMenuItem);
          adminMenu.addItem(refreshMetadataMenuItem);
          adminMenu.addItem(executeGlobalActionsMenuItem);
          adminMenu.addItem(purgeMondrianSchemaCacheMenuItem);
          // add additions to the admin menu

          MenuItem adminMenuBar = new MenuItem(Messages.getString("refresh"), adminMenu);//$NON-NLS-1$
          adminMenuBar.getElement().setId("admin_menu_bar");
          toolsMenu.addItem(adminMenuBar); 
          toolsMenu.addSeparator();

          Map<String,String> supportedLanguages = Messages.getResourceBundle().getSupportedLanguages();
          if (supportedLanguages != null && supportedLanguages.keySet() != null && !supportedLanguages.isEmpty()) {
            MenuBar langMenu = new MantleMenuBar(true);
            langMenu.getElement().setId("languages_menu");
            for (String lang : supportedLanguages.keySet()) {
              MenuItem langMenuItem = new MenuItem(supportedLanguages.get(lang), new SwitchLocaleCommand(lang)); //$NON-NLS-1$
              langMenuItem.getElement().setId(supportedLanguages.get(lang) + "_menu_item");
              langMenu.addItem(langMenuItem);
            }
            MenuItem langMenuBar = new MenuItem(Messages.getString("languages"), langMenu);//$NON-NLS-1$
            langMenuBar.getElement().setId("languages_menu_bar");
            toolsMenu.addItem(langMenuBar); 
            toolsMenu.addSeparator();
          }
          
          MenuItem softwareUpdatesMenuItem = new MenuItem(Messages.getString("softwareUpdates"), new CheckForSoftwareUpdatesCommand()); //$NON-NLS-1$
          softwareUpdatesMenuItem.getElement().setId("software_updates_menu_item");
          toolsMenu.addItem(softwareUpdatesMenuItem);
          
          MenuItem toolsMenuBar = new MenuItem(Messages.getString("tools"), toolsMenu);//$NON-NLS-1$
          toolsMenuBar.getElement().setId("tools_menu_bar");
          menuBar.addItem(toolsMenuBar); 
          // add additions to the admin menu
          customizeMenu(toolsMenu, "tools", settings); //$NON-NLS-1$
          customizeMenu(adminMenu, "tools-refresh", settings); //$NON-NLS-1$
        }

        MenuBar helpMenu = new MenuBar(true);
        helpMenu.getElement().setId("help_menu");
        MenuItem docMenuItem = new MenuItem(Messages.getString("documentation"), new OpenDocCommand(settings.get("documentation-url"), solutionBrowserPerspective)); //$NON-NLS-1$ //$NON-NLS-2$
        docMenuItem.getElement().setId("doc_menu_item"); //$NON-NLS-1$
        helpMenu.addItem(docMenuItem);
        helpMenu.addSeparator();
        MenuItem pentahoHomeMenuItem = new MenuItem(Messages.getString("pentahoHomePageName"), new PentahoHomeCommand());//$NON-NLS-1$
        pentahoHomeMenuItem.getElement().setId("pentaho_home_menu_item"); //$NON-NLS-1$
        helpMenu.addItem(pentahoHomeMenuItem); 
        helpMenu.addSeparator();
        MenuItem aboutMenuItem = new MenuItem(Messages.getString("about"), new AboutCommand()); //$NON-NLS-1$
        aboutMenuItem.getElement().setId("about_menu_item"); //$NON-NLS-1$
        helpMenu.addItem(aboutMenuItem);
        // add additions to the help menu
        customizeMenu(helpMenu, "help", settings); //$NON-NLS-1$
        MenuItem helpMenuBar = new MenuItem(Messages.getString("help"), helpMenu); //$NON-NLS-1$
        helpMenuBar.getElement().setId("help_menu_bar");
        menuBar.addItem(helpMenuBar);
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

  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, Widget panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    if(panel != null && panel instanceof IReloadableTabPanel){
      selectedTabURL = ((ReloadableIFrameTabPanel)panel).getUrl();
      saveEnabled = ((ReloadableIFrameTabPanel)panel).isSaveEnabled();
    }

    final boolean isEnabled = (selectedTabURL != null && !"".equals(selectedTabURL)); //$NON-NLS-1$

    printMenuItem.setEnabled(isEnabled);
    propertiesMenuItem.setEnabled(isEnabled);

    // Properties menu item should have a command associated with it ONLY when it is enabled.
    if (isEnabled) {
      propertiesMenuItem.setCommand(propertiesCommand);
    } else {
      propertiesMenuItem.setCommand(null);
    }    
    
    saveMenuItem.setEnabled(saveEnabled && isEnabled);
    saveAsMenuItem.setEnabled(saveEnabled && isEnabled);
    
    if (panel instanceof ReloadableIFrameTabPanel) {
      if(SolutionBrowserListener.EventType.OPEN.equals(type) || SolutionBrowserListener.EventType.SELECT.equals(type)) {
        if(panel != null) {
          main.applyOverlays(((ReloadableIFrameTabPanel)panel).getOverlayIds());  
        }
      } else if(SolutionBrowserListener.EventType.CLOSE.equals(type) || SolutionBrowserListener.EventType.DESELECT.equals(type)){
        if(panel != null) {
          main.removeOverlays(((ReloadableIFrameTabPanel)panel).getOverlayIds());  
        }
      }
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

  public void enableAdhocSave(boolean enable) {
    this.solutionBrowserPerspective.setCurrentTabSaveEnabled(enable);
  }
  
  public void showOpenFileDialog(JavaScriptObject callback, String title, String okText, String fileTypes){
    FileDialog dialog = new FileDialog(this.solutionBrowserPerspective.getSolutionDocument(), title, okText, fileTypes.split(","));
    openFileDialog(dialog, callback);
  }

  public void showOpenFileDialog(JavaScriptObject callback, String path, String title, String okText, String fileTypes){
    FileDialog dialog = new FileDialog(this.solutionBrowserPerspective.getSolutionDocument(), path, title, okText, fileTypes.split(","));
    openFileDialog(dialog, callback);
  }
  
  private void openFileDialog(FileDialog dialog, final JavaScriptObject callback){
    dialog.addFileChooserListener(new FileChooserListener(){

      public void fileSelected(String solution, String path, String name, String localizedFileName) {
        notifyOpenFileCallback(callback, solution, path, name, localizedFileName);
      }

      public void fileSelectionChanged(String solution, String path, String name) {}
      
    });
    dialog.show();
  }
  
  private native void notifyOpenFileCallback(JavaScriptObject obj, String solution, String path, String name, String localizedFileName)/*-{
    obj.fileSelected(solution, path, name, localizedFileName);
  }-*/;
  
}
