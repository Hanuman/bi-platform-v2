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
 * Copyright 2008-2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFileMetaProvider;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanDefinition;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.api.engine.PluginServiceDefinition;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.engine.ServiceInitializationException;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.FileInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.servicemgr.ServiceConfig;
import org.pentaho.platform.util.logging.Logger;

@SuppressWarnings("deprecation")
public class DefaultPluginManager extends AbstractPluginManager {

  private Map<String, ClassLoader> classLoaderMap = Collections.synchronizedMap(new HashMap<String, ClassLoader>());

  public DefaultPluginManager() {
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
    contentGeneratorInfoByIdMap.clear();
    contentGeneratorInfoByTypeMap.clear();
    contentTypeByExtension.clear();
    objectFactory.init(null, null);
    //we do not need to synchronize here since unloadPlugins 
    //is called within the synchronized block in reload
    for (IPlatformPlugin plugin : registeredPlugins.values()) {
      try {
        plugin.unLoaded();
      } catch (Throwable t) {
        //we do not want any type of exception to leak out and cause a problem here
        //A plugin unload should not adversely affect anything downstream, it should
        //log an error and otherwise fail silently
        String msg = Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0014_PLUGIN_FAILED_TO_PROPERLY_UNLOAD", plugin.getId()); //$NON-NLS-1$
        Logger.error(getClass().toString(), msg, t);
        PluginMessageLogger.add(msg);
      }
    }
    registeredPlugins.clear();
  }

  public final boolean reload(IPentahoSession session) {

    boolean anyErrors = false;
    IPluginProvider pluginProvider = PentahoSystem.get(IPluginProvider.class, session);
    List<IPlatformPlugin> providedPlugins = null;
    try {
      synchronized (registeredPlugins) {
        this.unloadPlugins();
      }
      //the plugin may fail to load during getPlugins without an exception thrown if the provider
      //is capable of discovering the plugin fine but there are structural problems with the plugin
      //itself. In this case a warning should be logged by the provider, but, again, no exception 
      //is expected.
      providedPlugins = pluginProvider.getPlugins(session);
      
    } catch (PlatformPluginRegistrationException e1) {
      String msg = Messages.getInstance().getErrorString("PluginManager.ERROR_0012_PLUGIN_DISCOVERY_FAILED"); //$NON-NLS-1$
      Logger.error(getClass().toString(), msg, e1);
      PluginMessageLogger.add(msg);
      anyErrors = true;
    }
    objectFactory.init(null, null);

    synchronized (providedPlugins) {
      for (IPlatformPlugin plugin : providedPlugins) {
        try {
          registerPlugin(plugin, session);
          registeredPlugins.put(plugin.getId(), plugin);
        } catch (Throwable t) {
          // this has been logged already
          anyErrors = true;
          String msg = Messages.getInstance().getErrorString("PluginManager.ERROR_0011_FAILED_TO_REGISTER_PLUGIN", plugin.getId()); //$NON-NLS-1$
          Logger.error(getClass().toString(), msg, t);
          PluginMessageLogger.add(msg);
        }
      }
    }

    IServiceManager svcManager = PentahoSystem.get(IServiceManager.class, null);
    try {
      svcManager.initServices();
    } catch (ServiceInitializationException e) {
      String msg = Messages.getInstance().getErrorString("PluginManager.ERROR_0022_SERVICE_INITIALIZATION_FAILED"); //$NON-NLS-1$
      Logger.error(getClass().toString(), msg, e);
      PluginMessageLogger.add(msg);
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
      throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0017_COULD_NOT_LOAD_PLUGIN_LIFECYCLE_LISTENER", plugin.getId(), plugin //$NON-NLS-1$
              .getLifecycleListenerClassname()), t);
    }

    if (listener != null) {
      if (!IPluginLifecycleListener.class.isAssignableFrom(listener.getClass())) {
        throw new PlatformPluginRegistrationException(
            Messages.getInstance()
                .getErrorString(
                    "PluginManager.ERROR_0016_PLUGIN_LIFECYCLE_LISTENER_WRONG_TYPE", plugin.getId(), plugin.getLifecycleListenerClassname())); //$NON-NLS-1$
      }
      plugin.addLifecycleListener((IPluginLifecycleListener) listener);
    }
  }

  @SuppressWarnings("unchecked")
  private void registerPlugin(final IPlatformPlugin plugin, IPentahoSession session)
      throws PlatformPluginRegistrationException, PluginLifecycleException {
    //TODO: we should treat the registration of a plugin as an atomic operation
    //with rollback if something is broken
    
    if(StringUtils.isEmpty(plugin.getId())) {
      throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0026_PLUGIN_INVALID", plugin.getSourceDescription())); //$NON-NLS-1$
    }

    if (registeredPlugins.containsKey(plugin.getId())) {
      throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0024_PLUGIN_ALREADY_LOADED_BY_SAME_NAME", plugin.getId())); //$NON-NLS-1$
    }

    ClassLoader loader = setPluginClassLoader(plugin);

    bootStrapPlugin(plugin, loader);

    plugin.init();

    registerContentTypes(plugin, loader);

    registerContentGenerators(plugin, loader);

    //cache overlays
    overlaysCache.addAll(plugin.getOverlays());

    //cache menu customizations
    menuCustomizationsCache.addAll(plugin.getMenuCustomizations());

    registerBeans(plugin, loader, session);

    //service registry must take place after bean registry since
    //a service class may be configured as a plugin bean
    registerServices(plugin, loader);

    PluginMessageLogger.add(Messages.getInstance().getString("PluginManager.PLUGIN_REGISTERED", plugin.getId())); //$NON-NLS-1$
    try {
      plugin.loaded();
    } catch (Throwable t) {
      //The plugin has already been loaded, so there is really no logical response to any type
      //of failure here except to log an error and otherwise fail silently
      String msg = Messages.getInstance().getErrorString("PluginManager.ERROR_0015_PLUGIN_LOADED_HANDLING_FAILED", plugin.getId()); //$NON-NLS-1$
      Logger.error(getClass().toString(), msg, t);
      PluginMessageLogger.add(msg);
    }
  }

  private void registerContentTypes(IPlatformPlugin plugin, ClassLoader loader)
      throws PlatformPluginRegistrationException {
    //index content types and define any file meta providers
    for (IContentInfo info : plugin.getContentInfos()) {
      contentTypeByExtension.put(info.getExtension(), info);

      String metaProviderClass = plugin.getMetaProviderMap().get(info.getExtension());

      //if a meta-provider is defined for this content type, then register it...
      if (!StringUtils.isEmpty(metaProviderClass)) {
        Class<?> clazz = null;
        String defaultErrMsg = Messages.getInstance()
            .getErrorString(
                "PluginManager.ERROR_0013_FAILED_TO_SET_CONTENT_TYPE_META_PROVIDER", metaProviderClass, info.getExtension()); //$NON-NLS-1$

        try {
          //do a test load to fail early if class not found
          clazz = loader.loadClass(metaProviderClass);
        } catch (Exception e) {
          throw new PlatformPluginRegistrationException(defaultErrMsg, e);
        }

        //check that the class is an accepted type
        if (!(ISolutionFileMetaProvider.class.isAssignableFrom(clazz) || IFileInfoGenerator.class
            .isAssignableFrom(clazz))) {
          throw new PlatformPluginRegistrationException(
              Messages.getInstance()
                  .getErrorString(
                      "PluginManager.ERROR_0019_WRONG_TYPE_FOR_CONTENT_TYPE_META_PROVIDER", metaProviderClass, info.getExtension())); //$NON-NLS-1$
        }

        //the class is ok, so register it with the factory
        objectFactory.defineObject(info.getExtension(), metaProviderClass, Scope.LOCAL, loader);

        try {
          //check that the class can be instantiated
          //we have to tell the factory to return us an Object since the instance could be one of 2 permissible types
          objectFactory.get(Object.class, info.getExtension(), null); //solution file meta providers cannot be session scoped, so null is ok here
        } catch (Exception e) {
          throw new PlatformPluginRegistrationException(defaultErrMsg, e);
        }
      }
    }
  }

  private void registerBeans(IPlatformPlugin plugin, ClassLoader loader, IPentahoSession session)
      throws PlatformPluginRegistrationException {
    //we do not have to synchronize on the bean set here because the
    //map that backs the set is never modified after the plugin has 
    //been made available to the plugin manager
    for (PluginBeanDefinition def : plugin.getBeans()) {
      //register by classname if id is null
      def.setBeanId((def.getBeanId() == null) ? def.getClassname() : def.getBeanId());
      registerClass(plugin, def.getBeanId(), def.getClassname(), loader);
    }
  }

  /*
   * A utility method that wraps plugin class registration with proper error handling and messaging
   */
  private void registerClass(IPlatformPlugin plugin, String id, String classname, ClassLoader loader)
      throws PlatformPluginRegistrationException {
    if (objectFactory.objectDefined(id)) {
      throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0018_BEAN_ALREADY_REGISTERED", id, plugin.getId())); //$NON-NLS-1$
    }
    //right now we support only prototype scope for beans
    objectFactory.defineObject(id, classname, Scope.LOCAL, loader);
  }

  private void registerServices(IPlatformPlugin plugin, ClassLoader loader) throws PlatformPluginRegistrationException {
    IServiceManager svcManager = PentahoSystem.get(IServiceManager.class, null);

    for (PluginServiceDefinition pws : plugin.getServices()) {
      for (ServiceConfig ws : createServiceConfigs(pws, plugin, loader)) {
        try {
          svcManager.registerService(ws);
        } catch (ServiceException e) {
          throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
              "PluginManager.ERROR_0025_SERVICE_REGISTRATION_FAILED", ws.getId(), plugin.getId()), e); //$NON-NLS-1$
        }
      }
    }
  }

  /* 
   * A utility method to convert plugin version of webservice definition to the official engine version
   * consumable by an IServiceManager 
   */
  private Collection<ServiceConfig> createServiceConfigs(PluginServiceDefinition pws,
      IPlatformPlugin plugin, ClassLoader loader) throws PlatformPluginRegistrationException {
    Collection<ServiceConfig> services = new ArrayList<ServiceConfig>();

    //Set the service type (one service config instance created per service type)
    //
    if (pws.getTypes() == null || pws.getTypes().length < 1) {
      throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
          "PluginManager.ERROR_0023_SERVICE_TYPE_UNSPECIFIED", pws.getId())); //$NON-NLS-1$
    }
    for (String type : pws.getTypes()) {
      ServiceConfig ws = new ServiceConfig();
      
      ws.setServiceType(type);
      ws.setTitle(pws.getTitle());
      ws.setDescription(pws.getDescription());
      String serviceClassName = (StringUtils.isEmpty(pws.getServiceClass())) ? pws.getServiceBeanId() : pws.getServiceClass();

      String serviceId;
      if (!StringUtils.isEmpty(pws.getId())) {
        serviceId = pws.getId();
      } else {
        serviceId = serviceClassName;
        if (serviceClassName.indexOf('.') > 0) {
          serviceId = serviceClassName.substring(serviceClassName.lastIndexOf('.') + 1);
        }
      }
      ws.setId(serviceId);

      //Register the service class
      //
      final String serviceClassKey = ws.getServiceType() + "-" + ws.getId() + "/" + serviceClassName; //$NON-NLS-1$ //$NON-NLS-2$
      registerClass(plugin, serviceClassKey, serviceClassName, loader);

      if (!this.isBeanRegistered(serviceClassKey)) {
        throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0020_NO_SERVICE_CLASS_REGISTERED", serviceClassKey)); //$NON-NLS-1$
      }

      //Load/set the service class and supporting types
      //
      try {
        ws.setServiceClass(loadClass(serviceClassKey));

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        if (pws.getExtraClasses() != null) {
          for (String extraClass : pws.getExtraClasses()) {
            classes.add(loadClass(extraClass));
          }
        }
        ws.setExtraClasses(classes);
      } catch (PluginBeanException e) {
        throw new PlatformPluginRegistrationException(Messages.getInstance().getErrorString(
            "PluginManager.ERROR_0021_SERVICE_CLASS_LOAD_FAILED", serviceClassKey), e); //$NON-NLS-1$
      }
      services.add(ws);
    }

    return services;
  }

  private ClassLoader setPluginClassLoader(IPlatformPlugin plugin) {
    ClassLoader loader = classLoaderMap.get(plugin.getId());
    if (loader == null) {
      String pluginDir = PentahoSystem.getApplicationContext().getSolutionPath(
          "system/" + plugin.getSourceDescription()); //$NON-NLS-1$
      //need to scrub out duplicate file delimeters otherwise we will 
      //not be able to locate resources in jars.  This classloader ultimately
      //needs to be made less fragile
      pluginDir = pluginDir.replace("//", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      Logger.debug(this, "plugin dir for " + plugin.getId() + " is [" + pluginDir + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      loader = new PluginClassLoader(pluginDir, this);
      classLoaderMap.put(plugin.getId(), loader);
    }
    return loader;
  }

  public ClassLoader getClassLoader(IPlatformPlugin plugin) {
    return getClassLoader(plugin.getId());
  }
  
  public ClassLoader getClassLoader(String pluginId) {
    return classLoaderMap.get(pluginId);
  }

  private void registerContentGenerators(IPlatformPlugin plugin, ClassLoader loader)
      throws PlatformPluginRegistrationException {
    //register the content generators
    for (IContentGeneratorInfo cgInfo : plugin.getContentGenerators()) {
      String errorMsg = Messages.getInstance().getString(
          "PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", cgInfo.getId(), plugin.getId()); //$NON-NLS-1$

      //test load the content generator
      try {
        Class<?> clazz = Class.forName(cgInfo.getClassname(), false, loader);
        objectFactory.defineObject(clazz.getSimpleName(), cgInfo.getClassname(), Scope.LOCAL, loader);
        objectFactory.defineObject(cgInfo.getId(), cgInfo.getClassname(), Scope.LOCAL, loader);
      } catch (Exception e) {
        throw new PlatformPluginRegistrationException(errorMsg, e);
      }

      // do a test load of the content generator so we can fail now if the class cannot be found
      // this tests class loading and cast class issues
      Object tmpObject;
      try {
        tmpObject = objectFactory.get(Object.class, cgInfo.getId(), null); //content generators cannot be session scoped, so null is ok here
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
          objectFactory.defineObject(cgInfo.getType(), cgInfo.getFileInfoGeneratorClassname(), Scope.LOCAL, loader);
        } catch (Exception e) {
          throw new PlatformPluginRegistrationException(
              Messages.getInstance()
                  .getErrorString(
                      "PluginManager.ERROR_0013_FAILED_TO_CREATE_FILE_INFO_GENERATOR", cgInfo.getFileInfoGeneratorClassname(), cgInfo.getType()), e); //$NON-NLS-1$
        }
      }
      contentGeneratorInfoByIdMap.put(cgInfo.getId(), cgInfo);

      List<IContentGeneratorInfo> generatorList = contentGeneratorInfoByTypeMap.get(cgInfo.getType());
      if (generatorList == null) {
        generatorList = new ArrayList<IContentGeneratorInfo>();
        contentGeneratorInfoByTypeMap.put(cgInfo.getType(), generatorList);
      }
      generatorList.add(cgInfo);

      PluginMessageLogger.add(Messages.getInstance().getString(
          "PluginManager.USER_CONTENT_GENERATOR_REGISTERED", cgInfo.getId(), plugin.getId())); //$NON-NLS-1$
    }
  }

  public IPentahoObjectFactory getBeanFactory() {
    return objectFactory;
  }

  public Object getBean(String beanId) throws PluginBeanException {
    if (beanId == null) {
      throw new IllegalArgumentException("beanId cannot be null"); //$NON-NLS-1$
    }
    if (objectFactory.objectDefined(beanId)) {
      Object bean = null;
      try {
        //TODO: should we allow session scoped beans?, if so we need to pass in the session
        //It looks ugly to pass Object.class to the object factory.  This is the way it must
        //be unless we want to support null interfaceClass in which case the factory will not 
        //cast the resultant object.
        bean = objectFactory.get(Object.class, beanId, null);
        return bean;
      } catch (Throwable ex) { // Catching throwable on purpose
        throw new PluginBeanException(ex);
      }
    } else {
      throw new PluginBeanException(Messages.getInstance().getString("PluginManager.WARN_CLASS_NOT_REGISTERED")); //$NON-NLS-1$
    }
  }

  public Class<?> loadClass(String beanId) throws PluginBeanException {
    if (beanId == null) {
      throw new IllegalArgumentException("beanId cannot be null"); //$NON-NLS-1$
    }
    if (objectFactory.objectDefined(beanId)) {
      try {
        return objectFactory.getImplementingClass(beanId);
      } catch (Throwable ex) { // Catching throwable on purpose
        throw new PluginBeanException(ex);
      }
    } else {
      throw new PluginBeanException(Messages.getInstance().getString("PluginManager.WARN_CLASS_NOT_REGISTERED", beanId)); //$NON-NLS-1$
    }
  }

  public boolean isBeanRegistered(String beanId) {
    if (beanId == null) {
      throw new IllegalArgumentException("beanId cannot be null"); //$NON-NLS-1$
    }
    return objectFactory.objectDefined(beanId);
  }

  public void unloadAllPlugins() {
    synchronized (registeredPlugins) {
      this.unloadPlugins();
    }
  }

  public IFileInfo getFileInfo(String extension, IPentahoSession session, ISolutionFile solutionFile, InputStream in) {
    IFileInfo fileInfo = null;
    try {
      IFileInfoGenerator fileInfoGenerator = null;
      if (objectFactory.objectDefined(extension)) {
        fileInfoGenerator = objectFactory.get(IFileInfoGenerator.class, extension, null); //session scope not supported for meta providers
      }
      if (fileInfoGenerator instanceof ISolutionFileMetaProvider) {
        // new good stuff
        ISolutionFileMetaProvider provider = (ISolutionFileMetaProvider) fileInfoGenerator;
        fileInfo = provider.getFileInfo(solutionFile, in);
      } else {
        // old nasty stuff
        fileInfo = getLegacyFileInfo(fileInfoGenerator, session, solutionFile);
      }
    } catch (Throwable t) {
      // we cannot allow a plugin to break our application, so we *MUST* catch
      // throwable here (think about AbstractMethodError for example)
      // fileInfo will remain null if we hit an exception here, but just to make
      // sure, we'll make sure to set it
      t.printStackTrace();
      fileInfo = null;
    }
    if (fileInfo == null) {
      /* create a fallback file info based on the information from the solution file */
      fileInfo = new FileInfo();
      fileInfo.setTitle(solutionFile.getFileName());
      fileInfo.setDescription("failsafe: " + solutionFile.getFullPath()); //$NON-NLS-1$
      fileInfo.setDisplayType(solutionFile.getExtension());
    }
    return fileInfo;
  }

  private static IFileInfo getLegacyFileInfo(IFileInfoGenerator fileInfoGenerator, IPentahoSession session,
      ISolutionFile solutionFile) {
    IFileInfo fileInfo = null;
    try {
      String fullPath = solutionFile.getFullPath();
      String solution = solutionFile.getSolution();
      String fileName = solutionFile.getFileName();
      String path = solutionFile.getSolutionPath();
      IFileInfoGenerator.ContentType contentType = fileInfoGenerator.getContentType();
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
      if (contentType == IFileInfoGenerator.ContentType.INPUTSTREAM) {
        InputStream in = repository.getResourceInputStream(fullPath, true, ISolutionRepository.ACTION_EXECUTE);
        fileInfo = fileInfoGenerator.getFileInfo(solution, path, fileName, in);
      } else if (contentType == IFileInfoGenerator.ContentType.DOM4JDOC) {
        Document doc = repository.getResourceAsDocument(fullPath, ISolutionRepository.ACTION_EXECUTE);
        fileInfo = fileInfoGenerator.getFileInfo(solution, path, fileName, doc);
      } else if (contentType == IFileInfoGenerator.ContentType.BYTES) {
        byte bytes[] = repository.getResourceAsBytes(fullPath, true, ISolutionRepository.ACTION_EXECUTE);
        fileInfo = fileInfoGenerator.getFileInfo(solution, path, fileName, bytes);
      } else if (contentType == IFileInfoGenerator.ContentType.STRING) {
        String str = repository.getResourceAsString(fullPath, ISolutionRepository.ACTION_EXECUTE);
        fileInfo = fileInfoGenerator.getFileInfo(solution, path, fileName, str);
      }
    } catch (Exception e) {
      //we don't care if an error occurred, we'll just return null and the caller will handle it
    }
    return fileInfo;
  }

  public Object getPluginSetting(IPlatformPlugin plugin, String key, String defaultValue) {
    return getPluginSetting(plugin.getId(), key, defaultValue);
  }
  
  public Object getPluginSetting(String pluginId, String key, String defaultValue) {
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    ClassLoader classLoader = classLoaderMap.get(pluginId);
    return resLoader.getPluginSetting(classLoader, key, defaultValue);
  }

  public IPlatformPlugin isResourceLoadable(String path) {
    PlatformPlugin p = new PlatformPlugin();
    p.setId(getServicePlugin(path));
    return p;
  }

  public String getServicePlugin(String path) {
    //normalize path for comparison
    path = (path.startsWith("/")) ? path.substring(1) : path; //$NON-NLS-1$

    for (IPlatformPlugin plugin : registeredPlugins.values()) {
      String pluginId = getStaticResourcePluginId(plugin, path);
      if (pluginId != null) {
        return pluginId;
      }

      for (IContentGeneratorInfo contentGenerator : plugin.getContentGenerators()) {
        String cgId = contentGenerator.getId();
        //content generator ids cannot start with '/', so no need to normalize cg ids
        if (path.startsWith(cgId)) {
          return plugin.getId();
        }
      }
    }

    return null;
  }
  
  private String getStaticResourcePluginId(IPlatformPlugin plugin, String path) {
    Map<String, String> resourceMap = plugin.getStaticResourceMap();
    for (String url : resourceMap.keySet()) {
      //normalize static url for comparison
      url = (url.startsWith("/")) ? url.substring(1) : url; //$NON-NLS-1$
      if (path.startsWith(url)) {
        return plugin.getId();
      }
    }
    return null;
  }

  public boolean isStaticResource(String path) {
    // normalize path for comparison
    path = (path.startsWith("/")) ? path.substring(1) : path; //$NON-NLS-1$
    for (IPlatformPlugin plugin : registeredPlugins.values()) {
      String pluginId = getStaticResourcePluginId(plugin, path);
      if (pluginId != null) {
        return true;
      }
    }
    return false;
  }
  
  public InputStream getStaticResource(String path) {
    for (IPlatformPlugin plugin : registeredPlugins.values()) {
      Map<String, String> resourceMap = plugin.getStaticResourceMap();
      for (String url : resourceMap.keySet()) {
        if (path.startsWith(url, 1) || path.startsWith(url)) {
          IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
          ClassLoader classLoader = classLoaderMap.get(plugin.getId());
          String resourcePath = path.replace(url, resourceMap.get(url));
          return resLoader.getResourceAsStream(classLoader, resourcePath);
        }
      }
    }
    return null;
  }

}