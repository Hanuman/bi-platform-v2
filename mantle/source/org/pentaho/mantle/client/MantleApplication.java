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

import java.util.HashMap;
import java.util.List;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPerspective;
import org.pentaho.mantle.client.usersettings.IMantleSettingsListener;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleApplication implements IUserSettingsListener, IMantleSettingsListener {

  public static boolean showAdvancedFeatures = false;

  private LogoPanel logoPanel;

  // menu items (to be enabled/disabled)
  private MantleMainMenuBar menuBar;

  // solution browser view
  private SolutionBrowserPerspective solutionBrowserPerspective;

  private XulMain main;

  private CommandExec commandExec = GWT.create(CommandExec.class);

  public void loadApplication() {
    logoPanel = new LogoPanel("http://www.pentaho.com"); //$NON-NLS-1$;
    menuBar = new MantleMainMenuBar();
    solutionBrowserPerspective = SolutionBrowserPerspective.getInstance(menuBar);
    main = XulMain.instance(solutionBrowserPerspective);
    menuBar.setSolutionBrowser(solutionBrowserPerspective);

    // registered our native JSNI hooks
    setupNativeHooks(this);

    // listen to any reloads of user settings
    UserSettingsManager.getInstance().addUserSettingsListener(this);

    // listen to any reloads of mantle settings
    MantleSettingsManager.getInstance().addMantleSettingsListener(this);

    ElementUtils.convertPNGs();
  }

  public native void setupNativeHooks(MantleApplication mantle)
  /*-{
    $wnd.mantle_initialized = true;
    $wnd.mantle_showMessage = function(title, message) {
      mantle.@org.pentaho.mantle.client.MantleApplication::showMessage(Ljava/lang/String;Ljava/lang/String;)(title, message);
    }
    
    $wnd.addGlassPaneListener = function(callback) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);      
    }
    
    $wnd.executeCommand = function(commandName) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::executeCommand(Ljava/lang/String;)(commandName);      
    }    
  }-*/;

  private void executeCommand(String commandName) {
    commandExec.execute(commandName);
  }

  private void addGlassPaneListener(JavaScriptObject obj) {
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
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

  public void onFetchUserSettings(List<IUserSetting> settings) {
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

  public void onFetchMantleSettings(HashMap<String, String> settings) {
    FlexTable menuAndLogoPanel = new FlexTable();
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

    VerticalPanel mainApplicationPanel = new VerticalPanel();
    mainApplicationPanel.setStyleName("applicationShell"); //$NON-NLS-1$
    mainApplicationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    mainApplicationPanel.add(menuAndLogoPanel);
    mainApplicationPanel.setCellHeight(menuAndLogoPanel, "1px"); //$NON-NLS-1$

    // load user bookmarks
    solutionBrowserPerspective.loadBookmarks();

    // update supported file types
    solutionBrowserPerspective.buildEnabledOptionsList(settings);

    // show stuff we've created/configured
    mainApplicationPanel.add(solutionBrowserPerspective);

    // menubar=no,location=no,resizable=yes,scrollbars=no,status=no,width=1200,height=800
    RootPanel.get().clear();
    RootPanel.get().add(mainApplicationPanel);
    RootPanel.get().add(WaitPopup.getInstance());

    showAdvancedFeatures = "true".equals(settings.get("show-advanced-features")); //$NON-NLS-1$ //$NON-NLS-2$

    boolean isAdministrator = "true".equals(settings.get("is-administrator"));
    solutionBrowserPerspective.setAdministrator(isAdministrator);
    menuBar.buildMenuBar(settings, isAdministrator);

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

}
