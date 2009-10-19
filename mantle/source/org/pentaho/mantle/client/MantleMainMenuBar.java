package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.mantle.client.commands.AboutCommand;
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
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileCommand;
import org.pentaho.mantle.client.perspective.solutionbrowser.FileItem;
import org.pentaho.mantle.client.perspective.solutionbrowser.IReloadableTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.ReloadableIFrameTabPanel;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserListener;
import org.pentaho.mantle.client.perspective.solutionbrowser.SolutionBrowserPerspective;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class MantleMainMenuBar extends MenuBar implements IViewMenuCallback, SolutionBrowserListener {

  private SolutionBrowserPerspective solutionBrowser;

  private MantleMenuBar viewMenu = new MantleMenuBar(true);
  // menu items (to be enabled/disabled)
  private PentahoMenuItem printMenuItem;
  private PentahoMenuItem saveMenuItem;
  private PentahoMenuItem saveAsMenuItem;
  private PentahoMenuItem propertiesMenuItem;

  private FileCommand propertiesCommand;
  private RefreshRepositoryCommand refreshRepositoryCommand;

  public MantleMainMenuBar() {
    super(false);
    getElement().setId("main_toolbar");
    setAutoOpen(false);
    setHeight("26px"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$
  }

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

  public void buildMenuBar(final HashMap<String, String> settings, final boolean isAdministrator) {
    clearItems();
    propertiesCommand = new FileCommand(FileCommand.COMMAND.PROPERTIES, null, solutionBrowser);
    refreshRepositoryCommand = new RefreshRepositoryCommand();

    solutionBrowser.setAdministrator(isAdministrator);
    printMenuItem = new PentahoMenuItem(Messages.getString("print"), new PrintCommand()); //$NON-NLS-1$
    printMenuItem.getElement().setId("print");
    saveMenuItem = new PentahoMenuItem(Messages.getString("save"), new SaveCommand(false)); //$NON-NLS-1$
    saveMenuItem.getElement().setId("save");
    saveAsMenuItem = new PentahoMenuItem(Messages.getString("saveAsEllipsis"), new SaveCommand(true)); //$NON-NLS-1$
    saveAsMenuItem.getElement().setId("saveAs");
    propertiesMenuItem = new PentahoMenuItem(Messages.getString("propertiesEllipsis"), propertiesCommand); //$NON-NLS-1$
    propertiesMenuItem.getElement().setId("properties");

    MenuBar fileMenu = new MantleMenuBar(true);
    fileMenu.getElement().setId("file_menu");
    MenuBar newMenu = new MantleMenuBar(true);
    newMenu.getElement().setId("new_menu");
    MenuItem waqrMenuItem = new MenuItem(Messages.getString("newAdhocReport"), solutionBrowser.getNewReportCommand());//$NON-NLS-1$
    waqrMenuItem.getElement().setId("waqr_menu_item");
    newMenu.addItem(waqrMenuItem);
    MenuItem analysisMenuItem = new MenuItem(Messages.getString("newAnalysisViewEllipsis"), solutionBrowser.getNewAnalysisViewCommand());//$NON-NLS-1$
    analysisMenuItem.getElement().setId("new_analysis_view_menu_item");
    newMenu.addItem(analysisMenuItem); //$NON-NLS-1$
    // add additions to the file menu
    customizeMenu(newMenu, "file-new", settings); //$NON-NLS-1$

    MenuItem newMenuBar = new MenuItem(Messages.getString("_new"), newMenu); //$NON-NLS-1$
    newMenuBar.getElement().setId("new_menu_bar");
    fileMenu.addItem(newMenuBar);

    MenuItem openFileMenuItem = new MenuItem(Messages.getString("openEllipsis"), new OpenFileCommand());//$NON-NLS-1$
    openFileMenuItem.getElement().setId("open_file_menu_item");
    fileMenu.addItem(openFileMenuItem); //$NON-NLS-1$
    if (MantleApplication.showAdvancedFeatures) {
      fileMenu.addItem(Messages.getString("openURLEllipsis"), new OpenURLCommand()); //$NON-NLS-1$
    }
    fileMenu.addSeparator();

    fileMenu.addItem(saveMenuItem);
    fileMenu.addItem(saveAsMenuItem);
    fileMenu.addSeparator();

    fileMenu.addItem(printMenuItem);
    fileMenu.addSeparator();
    if (MantleApplication.showAdvancedFeatures) {
      fileMenu.addItem(Messages.getString("userPreferencesEllipsis"), new ShowPreferencesCommand()); //$NON-NLS-1$
      fileMenu.addSeparator();
    }
    MenuBar manageContentMenu = new MantleMenuBar(true);
    manageContentMenu.getElement().setId("manage_content_menu");
    MenuItem editContent = new MenuItem(Messages.getString("editEllipsis"), new ManageContentEditCommand());//$NON-NLS-1$
    MenuItem shareContent = new MenuItem(Messages.getString("shareEllipsis"), new ManageContentShareCommand()); //$NON-NLS-1$
    MenuItem scheduleContent = new MenuItem(Messages.getString("scheduleEllipsis"), new ManageContentScheduleCommand()); //$NON-NLS-1$
    
    editContent.getElement().setId("edit_content_menu_item");
    shareContent.getElement().setId("share_content_menu_item");
    scheduleContent.getElement().setId("schedule_content_menu_item");
    
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
    addItem(fileMenuBar);

    // add additions to the view menu
    viewMenu.getElement().setId("view_menu");
    customizeMenu(viewMenu, "view", settings); //$NON-NLS-1$
    MenuItem viewMenuBar = new MenuItem(Messages.getString("view"), viewMenu); //$NON-NLS-1$
    viewMenuBar.getElement().setId("view_menu_bar");
    addItem(viewMenuBar);

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

      Map<String, String> supportedLanguages = Messages.getResourceBundle().getSupportedLanguages();
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
      addItem(toolsMenuBar);
      // add additions to the admin menu
      customizeMenu(toolsMenu, "tools", settings); //$NON-NLS-1$
      customizeMenu(adminMenu, "tools-refresh", settings); //$NON-NLS-1$
    }

    MenuBar helpMenu = new MenuBar(true);
    helpMenu.getElement().setId("help_menu");
    MenuItem docMenuItem = new MenuItem(Messages.getString("documentation"), new OpenDocCommand(settings.get("documentation-url"))); //$NON-NLS-1$ //$NON-NLS-2$
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
    addItem(helpMenuBar);
  }

  private void customizeMenu(final MenuBar menu, final String menuId, final HashMap<String, String> settings) {

    // see if we have any plugins to add
    if (settings.get(menuId + "MenuTitle0") != null) { //$NON-NLS-1$
      // we have at least one so we add a separator first
      // menu.addSeparator();
      // we're going to loop until we don't find any more
      int idx = 0;
      String title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
      String command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
      while (title != null) {
        // create a generic UrlCommand for this
        if (!GWT.isScript() && command.indexOf("content") > -1) {
          int index = command.indexOf("?");
          if (index >= 0) {
            command = "/MantleService?passthru=" + command.substring(command.indexOf("content"), index) + "&" + command.substring(index + 1)
                + "&userid=joe&password=password";
          } else {
            command = "/MantleService?passthru=" + command.substring(command.indexOf("content")) + "&userid=joe&password=password";
          }

        }
        UrlCommand menuCommand = new UrlCommand(command, title);

        MenuItem item = new MenuItem(title, menuCommand);
        //item.getElement().setId(title);

        // add it to the menu
        menu.addItem(item);
        idx++;
        // try to get the next one
        title = settings.get(menuId + "MenuTitle" + idx); //$NON-NLS-1$
        command = settings.get(menuId + "MenuCommand" + idx); //$NON-NLS-1$
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

  public SolutionBrowserPerspective getSolutionBrowser() {
    return solutionBrowser;
  }

  public void setSolutionBrowser(SolutionBrowserPerspective solutionBrowser) {
    this.solutionBrowser = solutionBrowser;
    solutionBrowser.addSolutionBrowserListener(this);
  }

  public void solutionBrowserEvent(SolutionBrowserListener.EventType type, Widget panel, FileItem selectedFileItem) {
    String selectedTabURL = null;
    boolean saveEnabled = false;
    if (panel != null && panel instanceof IReloadableTabPanel) {
      selectedTabURL = ((ReloadableIFrameTabPanel) panel).getUrl();
      saveEnabled = ((ReloadableIFrameTabPanel) panel).isSaveEnabled();
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
  }

}
