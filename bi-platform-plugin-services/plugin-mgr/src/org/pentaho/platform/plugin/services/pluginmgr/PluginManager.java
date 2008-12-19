package org.pentaho.platform.plugin.services.pluginmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;
import org.pentaho.platform.engine.services.solution.SolutionClassLoader;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

public class PluginManager implements IPluginManager {

  protected IPluginProvider pluginProvider = new SystemPathXmlPluginProvider();

  protected Map<String, List<IContentGeneratorInfo>> contentGeneratorInfoByTypeMap = new HashMap<String, List<IContentGeneratorInfo>>();

  protected Map<String, IContentGeneratorInfo> contentInfoMap = new HashMap<String, IContentGeneratorInfo>();

  protected Map<String, IContentInfo> contentTypeByExtension = new HashMap<String, IContentInfo>();

  protected StandaloneObjectFactory objectFactory = new StandaloneObjectFactory();

  protected final ThreadLocal<IPentahoSession> sessions = new ThreadLocal<IPentahoSession>();

  public IPentahoObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public Set<String> getContentTypes() {
    return contentGeneratorInfoByTypeMap.keySet();
  }

  public List<XulOverlay> getOverlays() {
    List<XulOverlay> list = new ArrayList<XulOverlay>();
    for (IPlatformPlugin plugin : pluginProvider.getPlugins()) {
      list.addAll(plugin.getOverlays());
    }
    return list;
  }

  public IContentInfo getContentInfoFromExtension(String extension, IPentahoSession session) {
    return contentTypeByExtension.get(extension);
  }

  public List<IContentGeneratorInfo> getContentGeneratorInfoForType(String type, IPentahoSession session) {
    return contentGeneratorInfoByTypeMap.get(type);
  }

  public IContentGenerator getContentGenerator(String id, IPentahoSession session) throws ObjectFactoryException {
    IContentGeneratorInfo info = getContentGeneratorInfo(id, session);
    if (info == null) {
      return null;
    }
    return (IContentGenerator) objectFactory.getObject(id, session);
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
      IContentGenerator generator = (IContentGenerator) objectFactory.getObject(id, session);
      return generator;
    }
    return null;
  }

  public void setSession(IPentahoSession session) {
    sessions.set(session);
  }

  public void removeSession() {
    sessions.remove();
  }

  public List<IMenuCustomization> getMenuCustomizations() {
    List<IMenuCustomization> list = new ArrayList<IMenuCustomization>();
    for (IPlatformPlugin plugin : pluginProvider.getPlugins()) {
      list.addAll(plugin.getMenuCustomizations());
    }
    return list;
  }

  public synchronized boolean reload(IPentahoSession session, List<String> comments) throws PlatformPluginRegistrationException {
    pluginProvider.reload(session, comments);
    
    contentGeneratorInfoByTypeMap.clear();
    contentTypeByExtension.clear();
    objectFactory.init(null, null);
    SolutionClassLoader.clearResourceCache();

    for (IPlatformPlugin plugin : pluginProvider.getPlugins()) {
      registerPlugin(plugin, comments, session);
    }
    return true;
  }

  public void registerPlugin(IPlatformPlugin plugin, List<String> comments, IPentahoSession session) throws PlatformPluginRegistrationException {

    for (IContentInfo info : plugin.getContentInfos()) {
      contentTypeByExtension.put(info.getExtension(), info);
    }

    //register the content generators
    for (IContentGeneratorInfo cgInfo : plugin.getContentGenerators()) {
      contentInfoMap.put(cgInfo.getId(), cgInfo);
      
      List<IContentGeneratorInfo> generatorList = contentGeneratorInfoByTypeMap.get(cgInfo.getTitle());
      if (generatorList == null) {
        generatorList = new ArrayList<IContentGeneratorInfo>();
        contentGeneratorInfoByTypeMap.put(cgInfo.getType(), generatorList);
      }
      generatorList.add(cgInfo);
      
      ClassLoader loader = new SolutionClassLoader(
          "system" + ISolutionRepository.SEPARATOR + plugin.getSourceDescription() + ISolutionRepository.SEPARATOR + "lib", //$NON-NLS-1$ //$NON-NLS-2$
          this);
      
      String errorMsg = Messages.getString("PluginManager.USER_CONTENT_GENERATOR_NOT_REGISTERED", cgInfo.getId(), plugin.getSourceDescription()); //$NON-NLS-1$

      //test load the content generator
      try {
        Class clazz = loader.loadClass(cgInfo.getClassname());
        Scope scope = Scope.valueOf(cgInfo.getScope().toUpperCase());
        objectFactory.defineObject(clazz.getSimpleName(), cgInfo.getClassname(), scope, loader);
        objectFactory.defineObject(cgInfo.getId(), cgInfo.getClassname(), scope, loader);
      } catch (Exception e) {
        throw new PlatformPluginRegistrationException(errorMsg,e);
      }

      // do a test load of the content generator so we can fail now if the class cannot be found
      // this tests class loading and cast class issues
      Object tmpObject;
      try {
        tmpObject = objectFactory.getObject(cgInfo.getId(), session);
      } catch (ObjectFactoryException e) {
        throw new PlatformPluginRegistrationException(errorMsg,e);
      }

      if (!(tmpObject instanceof IContentGenerator)) {
        throw new PlatformPluginRegistrationException(errorMsg);
      }

      //create the file info generator
      if (cgInfo.getFileInfoGeneratorClassname() != null) {
        // try to create the fileinfo generator class

        try {
          Class<?> clazz = loader.loadClass(cgInfo.getFileInfoGeneratorClassname());
          IFileInfoGenerator fileInfoGenerator = (IFileInfoGenerator) clazz.newInstance();
          cgInfo.setFileInfoGenerator(fileInfoGenerator);
        } catch (Exception e) {
          throw new PlatformPluginRegistrationException(errorMsg, e);
        }
      }
      
      comments.add(Messages.getString("PluginManager.USER_CONTENT_GENERATOR_REGISTERED", cgInfo.getId(), plugin.getSourceDescription())); //$NON-NLS-1$
    }
  }

  //
  // deprecated stuff. These all just call reload now.
  //

  public void addOverlay(XulOverlay overlay) {
    try {
      reload(null, new ArrayList<String>());
    } catch (PlatformPluginRegistrationException e) {
      e.printStackTrace();
    }
  }

  public void addOverlay(String id, String xml, String resourceBundleUri) {
    try {
      reload(null, new ArrayList<String>());
    } catch (PlatformPluginRegistrationException e) {
      e.printStackTrace();
    }
  }

  public void addContentInfo(String extension, IContentInfo contentInfo) {
    try {
      reload(null, new ArrayList<String>());
    } catch (PlatformPluginRegistrationException e) {
      e.printStackTrace();
    }
  }

  public void addContentGenerator(String id, String title, String description, String type, String url,
      String scopeStr, String className, String fileInfoClassName, IPentahoSession session, List<String> comments,
      String location, ClassLoader loader) throws ObjectFactoryException, ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    try {
      reload(null, new ArrayList<String>());
    } catch (PlatformPluginRegistrationException e) {
      e.printStackTrace();
    }
  }

  public boolean updatePluginSettings(IPentahoSession session, List<String> comments) {
    try {
      return reload(session, comments);
    } catch (PlatformPluginRegistrationException e) {
      e.printStackTrace();
    }
    return false;
  }
}