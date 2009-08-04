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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Sept 15, 2008 
 * @author jdixon
 */

package org.pentaho.platform.api.engine;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

/**
 * The contract API between the platform and platform plugins.  The plugin
 * manager provides the ability to load and register plugins as well as utility methods
 * for getting particular extension implementations from the set of registered plugins. 
 * For more information on platform plugins, visit the wiki link below.
 * 
 * @see <a href="http://wiki.pentaho.com/display/ServerDoc2x/BI+Platform+Plugins+in+V2">BI Platform Plugins</a>
 * @author jamesdixon
 */
public interface IPluginManager {

  /**
   * Returns a set of the content types that the registered plugins can
   * process. If the plugin is intended to handle the processing of a 
   * document in the solution repository the types need to match with
   * the filename extension.
   * @return
   */
  public Set<String> getContentTypes();

  public IContentInfo getContentInfoFromExtension(String extension, IPentahoSession session);

  /**
   * Returns a list of info objects that can be used to create content
   * generators for a given type. In most cases there will only be one
   * content generator for any one type.
   * @param type
   * @param session A session used for storing objects from session-scoped factories
   * @return List of IObjectCreator objects
   */
  public List<IContentGeneratorInfo> getContentGeneratorInfoForType(String type, IPentahoSession session);

  public IContentGenerator getContentGenerator(String id, IPentahoSession session) throws ObjectFactoryException;

  public IContentGeneratorInfo getContentGeneratorInfo(String id, IPentahoSession session);

  public IContentGeneratorInfo getDefaultContentGeneratorInfoForType(String type, IPentahoSession session);

  public String getContentGeneratorIdForType(String type, IPentahoSession session);

  public String getContentGeneratorTitleForType(String type, IPentahoSession session);

  public String getContentGeneratorUrlForType(String type, IPentahoSession session);

  public IContentGenerator getContentGeneratorForType(String type, IPentahoSession session)
      throws ObjectFactoryException;

  /**
   * Returns a list of menu customization objects.
   * @return List of IMenuCustomization objects
   */
  public List<IMenuCustomization> getMenuCustomizations();

  /**
  * Causes the plug-in manager object to re-register all of the plug-ins that
  * it knows about.  A {@link IPluginProvider} may be invoked to discover plugins
  * from various sources.
  * @param session the current session
  * @return true if no errors were encountered
  */
  public boolean reload(IPentahoSession session);

  /**
   * Returns a list of the XUL overlays that are defined by all the plug-ins. The overlays are
   * XML fragments.
   * @return List of XML XUL overlays
   */
  public List<XulOverlay> getOverlays();

  /**
   * If any plugins have registered a bean by id beanId, this method will return a new instance 
   * of the object.  The correct classloader must be used to instantiate the object.
   * @param beanId a unique identifier for a particular bean (cannot be null)
   * @return an instance of the bean registered under beanId
   * @throws PluginBeanException if there was a problem retrieving the bean instance
   */
  public Object getBean(String beanId) throws PluginBeanException;
  
  /**
   * Returns a loaded class for the bean registered as beanId.  The class will have 
   * been loaded by the proper classloader, so it indirectly provides the caller
   * with it's classloader by class.getClassLoader().  This is often helpful since
   * plugin bean classes are often not available through the caller's classloader.
   * @param beanId a unique identifier for a particular bean (cannot be null)
   * @return a loaded class for the registered bean
   * @throws PluginBeanException if there was a problem loading the class
   */
  public Class<?> loadClass(String beanId) throws PluginBeanException;

  /**
   * Returns true if a bean with id beanId has been registered with the plugin manager,
   * i.e. you can get a bean instance by calling {@link #getBean(String)}
   * @param beanId Cannot be null
   * @return true if the bean is registered
   */
  public boolean isBeanRegistered(String beanId);
  
  /**
   * Unloads all the plugins. Called when the context shuts down.
   */
  public void unloadAllPlugins();

  /**
   * Retrieves meta information for the given solution file.
   * @param extension used to identify the appropriate plugin that handles this type of content
   * @param session the current session
   * @param file the solution file we want to know something about
   * @param in the content of the solution file as an {@link InputStream}
   * @return meta information about the solution file
   */
  public IFileInfo getFileInfo(String extension, IPentahoSession session, ISolutionFile file, InputStream in);

  /**
   * @deprecated use {@link #getPluginSetting(String, String, String)}
   */
  public Object getPluginSetting(IPlatformPlugin plugin, String key, String defaultValue);
  
  /**
   * Retrieves a plugin setting for a given plugin and key.
   * @param pluginId the ID of the plugin to find settings for
   * @param key the setting name to lookup
   * @param defaultValue the default to use if the setting key is not found
   * @return the plugin setting
   */
  public Object getPluginSetting(String pluginId, String key, String defaultValue);
  
  /**
   * @deprecated This is a poorly named method, use {@link #getServicePlugin(String)} instead.
   */
  public IPlatformPlugin isResourceLoadable(String path);
  
  /**
   * Returns the plugin that can handle a request for the resource at "path".  A plugin is 
   * determined to be able to serve the request if it either a content generator or a static resource
   * of the plugin is configured to handle the path.  In other words,
   * if a plugin has a static resource of "/my-plugin/resources", then a request to 
   * "/my-plugin/resources/images/file.png" can be handled by the plugin.
   * If ultimately, no plugin can handle the resource path, <code>null</code> is returned.
   * @param path the path to the plugin resource
   * @return the ID of the plugin which owns the resource or <code>null</code> if one cannot be found
   */
  public String getServicePlugin(String path);
  
  /**
   * @deprecated use {@link #getClassLoader(String)}
   */
  public ClassLoader getClassLoader(IPlatformPlugin plugin);
  
  /**
   * Returns the classloader instance that was assigned by the plugin manager to load all 
   * classes for the specified plugin.  Used in combination with 
   * {@link #getServicePlugin(String)}, this method can provide you with a way to load resources 
   * from a plugin when all you have is a request URL/path, such as in a servlet environment.
   * @param plugin the plugin for which we want to get the assigned classloader
   * @return the classloader assigned to this plugin, or <code>null</code> if the plugin is not
   * known by the plugin manager, or for some reason a classloader was not assigned to the plugin
   * (an error condition).
   */
  public ClassLoader getClassLoader(String pluginId);
  
  /**
   * returns true if the path is a reference to a potential static resource.
   * Note that this does not guarantee that the resource exists, just that it maps
   * to a static resource location.
   * 
   * @param path static resource path
   * 
   * @return true if path begins as a static resource
   */
  public boolean isStaticResource(String path);
  
  /**
   * Returns and InputStream to the specified resource path.
   * @param path the path to the plugin resource
   * @return the InputStream which may be used to read the plugin resource
   */
  public InputStream getStaticResource(String path);
}
