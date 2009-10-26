package org.pentaho.mantle.client.solutionbrowser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.mantle.client.commands.AnalysisViewCommand;
import org.pentaho.mantle.client.commands.UrlCommand;
import org.pentaho.mantle.client.commands.WAQRCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;

import com.google.gwt.user.client.Command;

public class PluginOptionsHelper {

  private static List<FileTypeEnabledOptions> enabledOptionsList = new ArrayList<FileTypeEnabledOptions>();
  private static List<ContentTypePlugin> contentTypePluginList = new ArrayList<ContentTypePlugin>();
  private static String newAnalysisViewOverrideCommandUrl;
  private static String newAnalysisViewOverrideCommandTitle;
  private static String newReportOverrideCommandUrl;
  private static String newReportOverrideCommandTitle;
  
  public static void buildEnabledOptionsList(Map<String, String> settings) {
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
    // <new-analysis-view>
    // <command-url>http://www.google.com</command-url>
    // <command-title>Marc Analysis View</command-title>
    // </new-analysis-view>
    // <new-report>
    // <command-url>http://www.yahoo.com</command-url>
    // <command-title>Marc New Report</command-title>
    // </new-report>
    // 
    if (settings.containsKey("new-report-command-url")) { //$NON-NLS-1$
      newReportOverrideCommandUrl = settings.get("new-report-command-url"); //$NON-NLS-1$
      newReportOverrideCommandTitle = settings.get("new-report-command-title"); //$NON-NLS-1$
    }
    // Another way to override is from a plugin.xml...
    // 
    // <menu-item id="waqr_menu_item" anchor="file-new-submenu-waqr_menu_item" label="New WAQR" command="http://www.amazon.com" type="MENU_ITEM" how="REPLACE"/>
    // <menu-item id="new_analysis_view_menu_item" anchor="file-new-submenu-new_analysis_view_menu_item" label="New Analysis" command="http://www.dogpile.com"
    // type="MENU_ITEM" how="REPLACE"/>

    if (settings.get("file-newMenuOverrideTitle0") != null) { //$NON-NLS-1$
      // For now, only support override of these two menus
      for (int i = 0; i < 2; i++) {
        String title = settings.get("file-newMenuOverrideTitle" + i); //$NON-NLS-1$
        String command = settings.get("file-newMenuOverrideCommand" + i); //$NON-NLS-1$
        String menuItem = settings.get("file-newMenuOverrideMenuItem" + i); //$NON-NLS-1$
        if ((menuItem != null) && (command != null) && (title != null)) {
          if (menuItem.equals("waqr_menu_item")) { //$NON-NLS-1$
            newReportOverrideCommandUrl = command;
            newReportOverrideCommandTitle = title;
          } else if (menuItem.equals("new_analysis_view_menu_item")) { //$NON-NLS-1$
            newAnalysisViewOverrideCommandUrl = command;
            newAnalysisViewOverrideCommandTitle = title;
          }
        }
      }
    }

    // load plugins
    int index = 0;
    String pluginSetting = "plugin-content-type-" + index; //$NON-NLS-1$
    while (settings.containsKey(pluginSetting)) {
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

  public static FileTypeEnabledOptions getEnabledOptions(String filename) {
    for (FileTypeEnabledOptions option : enabledOptionsList) {
      if (option.isSupportedFile(filename)) {
        return option;
      }
    }
    return null;
  }

  public static ContentTypePlugin getContentTypePlugin(String filename) {
    for (ContentTypePlugin plugin : contentTypePluginList) {
      if (plugin.isSupportedFile(filename)) {
        return plugin;
      }
    }
    return null;
  }  
  
  public static Command getNewAnalysisViewCommand() {
    if (newAnalysisViewOverrideCommandUrl == null) {
      return new AnalysisViewCommand();
    } else {
      return new UrlCommand(newAnalysisViewOverrideCommandUrl, newAnalysisViewOverrideCommandTitle);
    }
  }

  public static Command getNewReportCommand() {
    if (newReportOverrideCommandUrl == null) {
      return new WAQRCommand();
    } else {
      return new UrlCommand(newReportOverrideCommandUrl, newReportOverrideCommandTitle);
    }
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
