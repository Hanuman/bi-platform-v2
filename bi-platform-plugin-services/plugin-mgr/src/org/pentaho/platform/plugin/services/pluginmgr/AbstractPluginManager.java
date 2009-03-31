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
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

/**
 * This class implements the boilerplate plugin indexing code that any {@link IPluginManager} 
 * implementation would have to support.  It helps cut the noise out of the subclass that 
 * handles plugin registration.
 * @author aphillips
 */
public abstract class AbstractPluginManager implements IPluginManager {
  protected StandaloneObjectFactory objectFactory = new StandaloneObjectFactory();

  protected List<IPlatformPlugin> plugins = Collections.synchronizedList(new ArrayList<IPlatformPlugin>());

  /* indexes and cached collections */
  
  protected Map<String, List<IContentGeneratorInfo>> contentGeneratorInfoByTypeMap = Collections
      .synchronizedMap(new HashMap<String, List<IContentGeneratorInfo>>());

  protected Map<String, IContentGeneratorInfo> contentInfoMap = Collections
      .synchronizedMap(new HashMap<String, IContentGeneratorInfo>());

  protected Map<String, IContentInfo> contentTypeByExtension = Collections
      .synchronizedMap(new HashMap<String, IContentInfo>());

  protected List<XulOverlay> overlaysCache = Collections.synchronizedList(new ArrayList<XulOverlay>());

  protected List<IMenuCustomization> menuCustomizationsCache = Collections
      .synchronizedList(new ArrayList<IMenuCustomization>());

  public Set<String> getContentTypes() {
    //map.keySet returns a set backed by the map, so we cannot allow modification of the set
    return Collections.unmodifiableSet(contentTypeByExtension.keySet());
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
}
