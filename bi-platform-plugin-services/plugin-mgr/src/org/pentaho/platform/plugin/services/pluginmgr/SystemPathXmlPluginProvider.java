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
 * Created Dec 19, 2008 
 * @author aphillips
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.IMenuCustomization.CustomizationType;
import org.pentaho.ui.xul.IMenuCustomization.ItemType;
import org.pentaho.ui.xul.impl.DefaultXulOverlay;
import org.pentaho.ui.xul.util.MenuCustomization;

/**
 * An implmentation of {@link IPluginProvider} that searches for plugin.xml files in the Pentaho
 * system path and instantiates {@link IPlatformPlugin}s from the information in those files.
 * @author aphillips
 */
public class SystemPathXmlPluginProvider implements IPluginProvider {

  public static final String ERROR_0001_FAILED_TO_PROCESS_PLUGIN = "SystemPathXmlPluginProvider.ERROR_0001_FAILED_TO_PROCESS_PLUGIN"; //$NON-NLS-1$

  /**
   * Gets the list of plugins that this provider class has discovered.
   * 
   * @return an read-only list of plugins
   * @see IPluginProvider#getPlugins()
   * @throws PlatformPluginRegistrationException if there is a problem preventing the impl from looking for plugins
   */
  public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
    List<IPlatformPlugin> plugins = new ArrayList<IPlatformPlugin>();

    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, session);
    if (repo == null) {
      throw new PlatformPluginRegistrationException(Messages
          .getErrorString("PluginManager.ERROR_0008_CANNOT_GET_REPOSITORY")); //$NON-NLS-1$
    }
    // look in each of the system setting folders looking for plugin.xml files
    String systemPath = PentahoSystem.getApplicationContext().getSolutionPath("system"); //$NON-NLS-1$
    File systemDir = new File(systemPath);
    if (!systemDir.exists() || !systemDir.isDirectory()) {
      throw new PlatformPluginRegistrationException(Messages
          .getErrorString("PluginManager.ERROR_0004_CANNOT_FIND_SYSTEM_FOLDER")); //$NON-NLS-1$
    }
    File kids[] = systemDir.listFiles();
    // look at each child to see if it is a folder
    for (File kid : kids) {
      if (kid.isDirectory()) {
        try {
          processDirectory(plugins, kid, repo, session);
        } catch (Throwable t) {
          //don't throw an exception.  we need to continue to process any remaining good plugins
          String msg = Messages.getErrorString(ERROR_0001_FAILED_TO_PROCESS_PLUGIN, kid.getAbsolutePath());
          Logger.error(getClass().toString(), msg, t);
          PluginMessageLogger.add(msg);
        }
      }
    }

    return Collections.unmodifiableList(plugins);
  }

  protected void processDirectory(List<IPlatformPlugin> plugins, File folder, ISolutionRepository repo,
      IPentahoSession session) throws PlatformPluginRegistrationException {
    // see if there is a plugin.xml file
    FilenameFilter filter = new NameFileFilter("plugin.xml", IOCase.SENSITIVE); //$NON-NLS-1$
    File kids[] = folder.listFiles(filter);
    if (kids == null || kids.length == 0) {
      return;
    }
    boolean hasLib = false;
    filter = new NameFileFilter("lib", IOCase.SENSITIVE); //$NON-NLS-1$
    kids = folder.listFiles(filter);
    if (kids != null && kids.length > 0) {
      hasLib = kids[0].exists() && kids[0].isDirectory();
    }
    // we have found a plugin.xml file
    // get the file from the repository
    String path = "system" + ISolutionRepository.SEPARATOR + folder.getName() + ISolutionRepository.SEPARATOR + "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    Document doc = null;
    try {
      doc = repo.getResourceAsDocument(path);
      if (doc != null) {
        plugins.add(createPlugin(doc, session, folder.getName(), repo, hasLib));
      }
    } catch (Exception e) {
      throw new PlatformPluginRegistrationException(Messages.getErrorString(
          "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path), e); //$NON-NLS-1$
    }
    if (doc == null) {
        throw new PlatformPluginRegistrationException(Messages.getErrorString(
            "PluginManager.ERROR_0005_CANNOT_PROCESS_PLUGIN_XML", path)); //$NON-NLS-1$
    }
  }

  protected PlatformPlugin createPlugin(Document doc, IPentahoSession session, String folder, ISolutionRepository repo,
      boolean hasLib) {
    PlatformPlugin plugin = new PlatformPlugin();

    processPluginInfo(plugin, doc, folder, session);
    processMenuItems(plugin, doc, session);
    processContentTypes(plugin, doc, session);
    processContentGenerators(plugin, doc, session, folder, repo, hasLib);
    processOverlays(plugin, doc, session);

    String msg = Messages.getString("SystemPathXmlPluginProvider.PLUGIN_PROVIDES",  //$NON-NLS-1$
        Integer.toString(plugin.getMenuCustomizations().size()),
        Integer.toString(plugin.getContentInfos().size()), 
        Integer.toString(plugin.getContentGenerators().size()), 
        Integer.toString(plugin.getOverlays().size())
        );
    PluginMessageLogger.add(msg);

    plugin.setSourceDescription(folder);

    return plugin;
  }

  protected void processPluginInfo(PlatformPlugin plugin, Document doc, String folder, IPentahoSession session) {
    Element node = (Element) doc.selectSingleNode("/plugin"); //$NON-NLS-1$
    if (node != null) {
      String name = node.attributeValue("title"); //$NON-NLS-1$
      plugin.setName(name);
      PluginMessageLogger.add(Messages.getString("SystemPathXmlPluginProvider.DISCOVERED_PLUGIN", name, folder)); //$NON-NLS-1$
    }
  }

  protected void processMenuItems(PlatformPlugin plugin, Document doc, IPentahoSession session) {
    // look for menu system customizations
    List<?> nodes = doc.selectNodes("//menu-item"); //$NON-NLS-1$
    for (Object obj : nodes) {
      Element node = (Element) obj;

      String id = node.attributeValue("id"); //$NON-NLS-1$
      String label = node.attributeValue("label"); //$NON-NLS-1$
      try {
        // create an IMenuCustomization object 
        String anchorId = node.attributeValue("anchor"); //$NON-NLS-1$
        String command = node.attributeValue("command"); //$NON-NLS-1$
        CustomizationType customizationType = CustomizationType.valueOf(node.attributeValue("how")); //$NON-NLS-1$
        ItemType itemType = ItemType.valueOf(node.attributeValue("type")); //$NON-NLS-1$
        IMenuCustomization custom = new MenuCustomization();
        custom.setAnchorId(anchorId);
        custom.setId(id);
        custom.setCommand(command);
        custom.setLabel(label);
        custom.setCustomizationType(customizationType);
        custom.setItemType(itemType);
        // store it
        //        menuCustomizations.add( custom );
        plugin.addMenuCustomization(custom);
        if (customizationType == CustomizationType.DELETE) {
          PluginMessageLogger.add(Messages.getString("PluginManager.USER_MENU_ITEM_DELETE", id)); //$NON-NLS-1$
        } else if (customizationType == CustomizationType.REPLACE) {
          PluginMessageLogger.add(Messages.getString("PluginManager.USER_MENU_ITEM_REPLACE", id, label)); //$NON-NLS-1$
        } else {
          PluginMessageLogger.add(Messages.getString("PluginManager.USER_MENU_ITEM_ADDITION", id, label)); //$NON-NLS-1$
        }
      } catch (Exception e) {
        PluginMessageLogger.add(Messages.getString("PluginManager.ERROR_0009_MENU_CUSTOMIZATION_ERROR", id, label)); //$NON-NLS-1$
        Logger.error(getClass().toString(), Messages.getErrorString(
            "PluginManager.ERROR_0009_MENU_CUSTOMIZATION_ERROR", id, label), e); //$NON-NLS-1$
      }
    }
  }

  protected void processOverlays(PlatformPlugin plugin, Document doc, IPentahoSession session) {
    // look for content types
    List<?> nodes = doc.selectNodes("//overlays/overlay"); //$NON-NLS-1$
    for (Object obj : nodes) {
      Element node = (Element) obj;
      String xml = null;
      String id = node.attributeValue("id"); //$NON-NLS-1$
      String resourceBundleUri = node.attributeValue("resourcebundle"); //$NON-NLS-1$
      if (node.elements() != null && node.elements().size() > 0) {
        xml = ((Element) node.elements().get(0)).asXML();
      }
      if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(xml)) {
        XulOverlay overlay = new DefaultXulOverlay(id, null, xml, resourceBundleUri);
        plugin.addOverlay(overlay);
      }
    }
  }

  protected void processContentTypes(PlatformPlugin plugin, Document doc, IPentahoSession session) {
    // look for content types
    List<?> nodes = doc.selectNodes("//content-type"); //$NON-NLS-1$
    for (Object obj : nodes) {
      Element node = (Element) obj;

      // create an IMenuCustomization object 
      String title = XmlDom4JHelper.getNodeText("title", node); //$NON-NLS-1$
      String extension = node.attributeValue("type"); //$NON-NLS-1$

      if (title != null && extension != null) {
        String description = XmlDom4JHelper.getNodeText("description", node, ""); //$NON-NLS-1$ //$NON-NLS-2$
        String mimeType = node.attributeValue("mime-type", ""); //$NON-NLS-1$ //$NON-NLS-2$
        String iconUrl = XmlDom4JHelper.getNodeText("icon-url", node, ""); //$NON-NLS-1$ //$NON-NLS-2$
        ContentInfo contentInfo = new ContentInfo();
        contentInfo.setDescription(description);
        contentInfo.setTitle(title);
        contentInfo.setExtension(extension);
        contentInfo.setMimeType(mimeType);
        contentInfo.setIconUrl(iconUrl);

        List<?> operationNodes = node.selectNodes("operations/operation"); //$NON-NLS-1$
        for (Object operationObj : operationNodes) {
          Element operationNode = (Element) operationObj;
          String id = XmlDom4JHelper.getNodeText("id", operationNode, ""); //$NON-NLS-1$ //$NON-NLS-2$
          String command = XmlDom4JHelper.getNodeText("command", operationNode, ""); //$NON-NLS-1$ //$NON-NLS-2$
          if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(command)) {
            IPluginOperation operation = new PluginOperation(id, command);
            contentInfo.addOperation(operation);
          }
        }

        plugin.addContentInfo(contentInfo);
        PluginMessageLogger.add(Messages.getString("PluginManager.USER_CONTENT_TYPE_REGISTERED", extension, title)); //$NON-NLS-1$
      } else {
        PluginMessageLogger.add(Messages.getString("PluginManager.USER_CONTENT_TYPE_NOT_REGISTERED", extension, title)); //$NON-NLS-1$
      }
    }
  }

  protected void processContentGenerators(PlatformPlugin plugin, Document doc, IPentahoSession session, String folder,
      ISolutionRepository repo, boolean hasLib) {
    // look for content generators
    List<?> nodes = doc.selectNodes("//content-generator"); //$NON-NLS-1$
    for (Object obj : nodes) {
      Element node = (Element) obj;

      // create an IMenuCustomization object 
      String className = XmlDom4JHelper.getNodeText("classname", node, null); //$NON-NLS-1$
      String fileInfoClassName = XmlDom4JHelper.getNodeText("fileinfo-classname", node, null); //$NON-NLS-1$
      String scope = node.attributeValue("scope"); //$NON-NLS-1$
      String id = node.attributeValue("id"); //$NON-NLS-1$
      String type = node.attributeValue("type"); //$NON-NLS-1$
      String url = node.attributeValue("url"); //$NON-NLS-1$
      String title = XmlDom4JHelper.getNodeText("title", node, null); //$NON-NLS-1$ 
      String description = XmlDom4JHelper.getNodeText("description", node, ""); //$NON-NLS-1$ //$NON-NLS-2$
      try {

        if (id != null && type != null && scope != null && className != null && title != null) {
          try {
            IContentGeneratorInfo info = createContentGenerator(plugin, id, title, description, type, url, scope,
                className, fileInfoClassName, session, folder);
            plugin.addContentGenerator(info);
          } catch (Exception e) {
            PluginMessageLogger.add(Messages.getString(
                "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder)); //$NON-NLS-1$
          }
        } else {
          PluginMessageLogger
              .add(Messages.getString("PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder)); //$NON-NLS-1$
        }
      } catch (Exception e) {
        PluginMessageLogger.add(Messages.getString("PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", id, folder)); //$NON-NLS-1$
        Logger.error(getClass().toString(), Messages.getErrorString(
            "PluginManager.ERROR_0006_CANNOT_CREATE_CONTENT_GENERATOR_FACTORY", folder), e); //$NON-NLS-1$
      }
    }
  }

  private static IContentGeneratorInfo createContentGenerator(PlatformPlugin plugin, String id, String title,
      String description, String type, String url, String scopeStr, String className, String fileInfoClassName,
      IPentahoSession session, String location) throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {

    ContentGeneratorInfo info = new ContentGeneratorInfo();
    info.setId(id);
    info.setTitle(title);
    info.setDescription(description);
    info.setUrl((url != null) ? url : ""); //$NON-NLS-1$
    info.setType(type);
    info.setFileInfoGeneratorClassname(fileInfoClassName);
    info.setClassname(className);
    info.setScope(scopeStr);

    return info;
  }

}
