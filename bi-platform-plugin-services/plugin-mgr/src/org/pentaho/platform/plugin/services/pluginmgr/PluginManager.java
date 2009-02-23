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
 */
package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginComponentException;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

public class PluginManager implements IPluginManager {

  private StandaloneObjectFactory objectFactory = new StandaloneObjectFactory();

  private List<IPlatformPlugin> plugins = Collections.synchronizedList(new ArrayList<IPlatformPlugin>());

  /* indexes and cached collections */

  private Map<String, List<IContentGeneratorInfo>> contentGeneratorInfoByTypeMap = Collections
      .synchronizedMap(new HashMap<String, List<IContentGeneratorInfo>>());

  private Map<String, IContentGeneratorInfo> contentInfoMap = Collections
      .synchronizedMap(new HashMap<String, IContentGeneratorInfo>());

  private Map<String, IContentInfo> contentTypeByExtension = Collections
      .synchronizedMap(new HashMap<String, IContentInfo>());

  private Map<String, ClassLoader> classLoaderMap = Collections.synchronizedMap(new HashMap<String, ClassLoader>());

  private List<XulOverlay> overlaysCache = Collections.synchronizedList(new ArrayList<XulOverlay>());

  private List<IMenuCustomization> menuCustomizationsCache = Collections
      .synchronizedList(new ArrayList<IMenuCustomization>());

  private Map<String, String> pluginComponentMap = Collections.synchronizedMap(new HashMap<String, String>());

  public Set<String> getContentTypes() {
    //map.keySet returns a set backed by the map, so we cannot allow modification of the set
    return Collections.unmodifiableSet(contentGeneratorInfoByTypeMap.keySet());
  }

  public List<XulOverlay> getOverlays() {
    return Collections.unmodifiableList(overlaysCache);
  }

  public IContentInfo getContentInfoFromExtension(String extension, IPentahoSession session) {
    return contentTypeByExtension.get(extension);
  }

  public List<IContentGeneratorInfo> getContentGeneratorInfoForType(String type, IPentahoSession session) {
    List<IContentGeneratorInfo> cgInfos = contentGeneratorInfoByTypeMap.get(type);
    return (cgInfos == null) ? null : Collections.unmodifiableList(contentGeneratorInfoByTypeMap.get(type));
  }

  public IContentGenerator getContentGenerator(String id, IPentahoSession session) throws ObjectFactoryException {
    IContentGeneratorInfo info = getContentGeneratorInfo(id, session);
    if (info == null) { //not sure why this is here ??
      return null;
    }
    return objectFactory.get(IContentGenerator.class, id, session);
  }

  public IContentGeneratorInfo getContentGeneratorInfo(String id, IPentahoSession session) {
    IContentGeneratorInfo contentId = contentInfoMap.get(id);
    return contentId;
  }

  public IContentGeneratorInfo getDefaultContentGeneratorInfoForType(String type, IPentahoSession session) {
    List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get(type);
    if (!CollectionUtils.isEmpty(contentIds)) {
      IContentGeneratorInfo info = contentIds.get(0);
      return info;
    }
    return null;
  }

  public String getContentGeneratorIdForType(String type, IPentahoSession session) {
    List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get(type);
    if (!CollectionUtils.isEmpty(contentIds)) {
      IContentGeneratorInfo info = contentIds.get(0);
      return info.getId();
    }
    return null;
  }

  public String getContentGeneratorTitleForType(String type, IPentahoSession session) {
    List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get(type);
    if (!CollectionUtils.isEmpty(contentIds)) {
      IContentGeneratorInfo info = contentIds.get(0);
      return info.getTitle();
    }
    return null;
  }

  public String getContentGeneratorUrlForType(String type, IPentahoSession session) {
    List<IContentGeneratorInfo> contentIds = contentGeneratorInfoByTypeMap.get(type);
    if (!CollectionUtils.isEmpty(contentIds)) {
      IContentGeneratorInfo info = contentIds.get(0);
      return info.getUrl();
    }
    return null;
  }

  public IContentGenerator getContentGeneratorForType(String type, IPentahoSession session)
      throws ObjectFactoryException {
    // return the default content generator for the given type
    // for now we'll assume the first in the list is the default
    List<IContentGeneratorInfo> contentGenerators = contentGeneratorInfoByTypeMap.get(type);
    if (!CollectionUtils.isEmpty(contentGenerators)) {
      String id = contentGenerators.get(0).getId();
      return objectFactory.get(IContentGenerator.class, id, session);
    }
    return null;
  }

  public List<IMenuCustomization> getMenuCustomizations() {
    return Collections.unmodifiableList(menuCustomizationsCache);
  }

  /**
   * Clears all the lists and maps in preparation for
   * reloading the state from the plugin provider.
   * Fires the plugin unloaded event for each known plugin.
   */
  private void unloadPlugins() {
    overlaysCache.clear();
    menuCustomizationsCache.clear();
    classLoaderMap.clear();
    contentInfoMap.clear();
    contentGeneratorInfoByTypeMap.clear();
    contentTypeByExtension.clear();
    pluginComponentMap.clear();
    //we do not need to synchronize here since unloadPlugins 
    //is called within the synchronized block in reload
    for (IPlatformPlugin plugin : plugins) {
      try {
        plugin.unLoaded();
      } catch (Throwable t) {
        //we do not want any type of exception to leak out and cause a problem here
        //A plugin unload should not adversely affect anything downstream, it should
        //log an error and otherwise fail silently
        String msg = Messages.getErrorString(
            "PluginManager.ERROR_0014_PLUGIN_FAILED_TO_PROPERLY_UNLOAD", plugin.getName()); //$NON-NLS-1$
        Logger.error(getClass().toString(), msg, t);
        PluginMessageLogger.add(msg);
      }
    }
    plugins.clear();
  }

  public final boolean reload(IPentahoSession session) {

    boolean anyErrors = false;
    IPluginProvider pluginProvider = PentahoSystem.get(IPluginProvider.class, session);
    try {
      synchronized (plugins) {
        this.unloadPlugins();
        //the plugin may fail to load during getPlugins without an exception thrown if the provider
        //is capable of discovering the plugin fine but there are structural problems with the plugin
        //itself. In this case a warning should be logged by the provider, but, again, no exception 
        //is expected.
        plugins.addAll(pluginProvider.getPlugins(session));
      }
    } catch (PlatformPluginRegistrationException e1) {
      String msg = Messages.getErrorString("PluginManager.ERROR_0012_PLUGIN_DISCOVERY_FAILED"); //$NON-NLS-1$
      Logger.error(getClass().toString(), msg, e1);
      PluginMessageLogger.add(msg);
      anyErrors = true;
    }
    objectFactory.init(null, null);

    synchronized (plugins) {
      for (IPlatformPlugin plugin : plugins) {
        try {
          registerPlugin(plugin, session);
        } catch (Throwable t) {
          // this has been logged already
          anyErrors = true;
          String msg = Messages.getErrorString("PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getName()); //$NON-NLS-1$
          Logger.error(getClass().toString(), msg, t);
          PluginMessageLogger.add(msg);
        }
      }
    }
    return !anyErrors;
  }

  /**
   * Gets the plugin ready to handle lifecycle events. 
   */
  private static void bootStrapPlugin(IPlatformPlugin plugin, ClassLoader loader)
      throws PlatformPluginRegistrationException {
    Object listener = null;
    try {
      if (!StringUtils.isEmpty(plugin.getLifecycleListenerClassname())) {
        listener = loader.loadClass(plugin.getLifecycleListenerClassname()).newInstance();
      }
    } catch (Throwable t) {
      throw new PlatformPluginRegistrationException(Messages.getErrorString(
          "PluginManager.ERROR_0017_COULD_NOT_LOAD_PLUGIN_LIFECYCLE_LISTENER", plugin.getName(), plugin //$NON-NLS-1$
              .getLifecycleListenerClassname()), t);
    }

    if (listener != null) {
      if (!IPluginLifecycleListener.class.isAssignableFrom(listener.getClass())) {
        throw new PlatformPluginRegistrationException(
            Messages
                .getErrorString(
                    "PluginManager.ERROR_0016_PLUGIN_LIFECYCLE_LISTENER_WRONG_TYPE", plugin.getName(), plugin.getLifecycleListenerClassname())); //$NON-NLS-1$
      }
      plugin.addLifecycleListener((IPluginLifecycleListener) listener);
    }
  }

  @SuppressWarnings("unchecked")
  private void registerPlugin(IPlatformPlugin plugin, IPentahoSession session)
      throws PlatformPluginRegistrationException, PluginLifecycleException {
    //FIXME: shouldn't we treat the registration of a plugin as an atomic operation
    //with rollback if something is broken?

    ClassLoader loader = setPluginClassLoader(plugin);

    bootStrapPlugin(plugin, loader);

    plugin.init();

    //index content types
    for (IContentInfo info : plugin.getContentInfos()) {
      contentTypeByExtension.put(info.getExtension(), info);
    }

    registerContentGenerators(plugin, loader, session);

    //cache overlays
    overlaysCache.addAll(plugin.getOverlays());

    //cache menu customizations
    menuCustomizationsCache.addAll(plugin.getMenuCustomizations());

    PluginMessageLogger.add(Messages.getString("PluginManager.PLUGIN_REGISTERED", plugin.getName())); //$NON-NLS-1$
    try {
      plugin.loaded();
    } catch (Throwable t) {
      //The plugin has already been loaded, so there is really no logical response to any type
      //of failure here except to log an error and otherwise fail silently
      String msg = Messages.getErrorString("PluginManager.ERROR_0015_PLUGIN_LOADED_HANDLING_FAILED", plugin.getName()); //$NON-NLS-1$
      Logger.error(getClass().toString(), msg, t);
      PluginMessageLogger.add(msg);
    }
  }

  private ClassLoader setPluginClassLoader(IPlatformPlugin plugin) {
    ClassLoader loader = classLoaderMap.get(plugin.getSourceDescription());
    if (loader == null) {
      String pluginDir = PentahoSystem.getApplicationContext().getSolutionPath(
          "system" + ISolutionRepository.SEPARATOR + plugin.getSourceDescription()); //$NON-NLS-1$
      loader = new PluginClassLoader(pluginDir, this);
      classLoaderMap.put(plugin.getSourceDescription(), loader);
    }
    return loader;
  }

  private void registerContentGenerators(IPlatformPlugin plugin, ClassLoader loader, IPentahoSession session)
      throws PlatformPluginRegistrationException {
    //register the content generators
    for (IContentGeneratorInfo cgInfo : plugin.getContentGenerators()) {
      String errorMsg = Messages.getString(
          "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", cgInfo.getId(), plugin.getSourceDescription()); //$NON-NLS-1$

      //test load the content generator
      try {
        Class<?> clazz = loader.loadClass(cgInfo.getClassname());
        Scope scope = Scope.valueOf(cgInfo.getScope().toUpperCase());
        objectFactory.defineObject(clazz.getSimpleName(), cgInfo.getClassname(), scope, loader);
        objectFactory.defineObject(cgInfo.getId(), cgInfo.getClassname(), scope, loader);
      } catch (Exception e) {
        throw new PlatformPluginRegistrationException(errorMsg, e);
      }

      // do a test load of the content generator so we can fail now if the class cannot be found
      // this tests class loading and cast class issues
      Object tmpObject;
      try {
        tmpObject = objectFactory.get(Object.class, cgInfo.getId(), session);
      } catch (ObjectFactoryException e) {
        throw new PlatformPluginRegistrationException(errorMsg, e);
      }

      //try to cast it to make sure it's the correct type, we want an exception to be thrown if not
      @SuppressWarnings("unused")
      IContentGenerator cg = (IContentGenerator) tmpObject;

      //create the file info generator
      if (cgInfo.getFileInfoGeneratorClassname() != null) {
        // try to create the fileinfo generator class

        try {
          Class<?> clazz = loader.loadClass(cgInfo.getFileInfoGeneratorClassname());
          clazz.newInstance();
          objectFactory.defineObject(cgInfo.getType(), cgInfo.getFileInfoGeneratorClassname(), Scope.LOCAL, loader);
        } catch (Exception e) {
          throw new PlatformPluginRegistrationException(
              Messages
                  .getErrorString(
                      "PluginManager.ERROR_0013_FAILED_TO_CREATE_FILE_INFO_GENERATOR", cgInfo.getFileInfoGeneratorClassname(), cgInfo.getType()), e); //$NON-NLS-1$
        }
      }
      contentInfoMap.put(cgInfo.getId(), cgInfo);

      List<IContentGeneratorInfo> generatorList = contentGeneratorInfoByTypeMap.get(cgInfo.getType());
      if (generatorList == null) {
        generatorList = new ArrayList<IContentGeneratorInfo>();
        contentGeneratorInfoByTypeMap.put(cgInfo.getType(), generatorList);
      }
      generatorList.add(cgInfo);

      PluginMessageLogger.add(Messages.getString(
          "PluginManager.USER_CONTENT_GENERATOR_REGISTERED", cgInfo.getId(), plugin.getSourceDescription())); //$NON-NLS-1$
    }

  }

  public IPentahoObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public IFileInfoGenerator getFileInfoGeneratorForType(String type, IPentahoSession session)
      throws PlatformPluginRegistrationException {
    IContentGeneratorInfo info = getDefaultContentGeneratorInfoForType(type, session);
    if (info != null) {
      String fileInfoClassName = info.getFileInfoGeneratorClassname();
      if (!StringUtils.isEmpty(fileInfoClassName)) {
        try {
          return objectFactory.get(IFileInfoGenerator.class, type, session);
        } catch (ObjectFactoryException e) {
          throw new PlatformPluginRegistrationException(Messages.getErrorString(
              "PluginManager.ERROR_0013_FAILED_TO_CREATE_FILE_INFO_GENERATOR", fileInfoClassName, type), e); //$NON-NLS-1$
        }
      }
    }
    return null;
  }

  public Object getRegisteredObject(String componentClassName) throws PluginComponentException {
    assert (componentClassName != null);
    String className = pluginComponentMap.get(componentClassName);
    if (className != null) {
      Object componentOrPojo = null;
      Class<?> componentOrPojoClass = null;
      try {
        //
        // TODO - Get the class here
        componentOrPojoClass = Class.forName(className);
        componentOrPojo = componentOrPojoClass.newInstance();
        return componentOrPojo;
      } catch (Throwable ex) { // Catching throwable on purpose
        throw new PluginComponentException(ex);
      }
    } else {
      Logger.warn(getClass().toString(), Messages.getString("PluginManager.WARN_CLASS_NOT_REGISTERED")); //$NON-NLS-1$
      return null;
    }
  }

  public boolean isObjectRegistered(String componentClassName) {
    assert (componentClassName != null);
    return pluginComponentMap.containsKey(componentClassName);
  }

}
