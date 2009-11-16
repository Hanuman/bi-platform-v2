package org.pentaho.test.platform.engine.core;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.ui.xul.IMenuCustomization;
import org.pentaho.ui.xul.XulOverlay;

/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
public class PluginManagerAdapter implements IPluginManager {

  public Object getBean(String beanId) throws PluginBeanException {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader(IPlatformPlugin plugin) {
    // TODO Auto-generated method stub
    return null;
  }

  public ClassLoader getClassLoader(String pluginId) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGenerator(String id, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGenerator getContentGeneratorForType(String type, IPentahoSession session)
      throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentGeneratorIdForType(String type, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getContentGeneratorInfo(String id, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<IContentGeneratorInfo> getContentGeneratorInfoForType(String type, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentGeneratorTitleForType(String type, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getContentGeneratorUrlForType(String type, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentInfo getContentInfoFromExtension(String extension, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public Set<String> getContentTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public IContentGeneratorInfo getDefaultContentGeneratorInfoForType(String type, IPentahoSession session) {
    // TODO Auto-generated method stub
    return null;
  }

  public IFileInfo getFileInfo(String extension, IPentahoSession session, ISolutionFile file, InputStream in) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPluginSetting(IPlatformPlugin plugin, String key, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object getPluginSetting(String pluginId, String key, String defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  public String getServicePlugin(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public InputStream getStaticResource(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isBeanRegistered(String beanId) {
    // TODO Auto-generated method stub
    return false;
  }

  public IPlatformPlugin isResourceLoadable(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isStaticResource(String path) {
    // TODO Auto-generated method stub
    return false;
  }

  public Class<?> loadClass(String beanId) throws PluginBeanException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean reload(IPentahoSession session) {
    // TODO Auto-generated method stub
    return false;
  }

  public void unloadAllPlugins() {
    // TODO Auto-generated method stub

  }

  public List<IMenuCustomization> getMenuCustomizations() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<XulOverlay> getOverlays() {
    // TODO Auto-generated method stub
    return null;
  }

}
