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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.pluginmgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * The default implementation of the {@link IPluginResourceLoader}.  Obtains resources
 * by searching the root directory of a {@link PluginClassLoader}.  This behavior can
 * be overridden such that an instance of this class will search a specified directory
 * instead, bypassing the classloader.  See {@link #setRootDir(File)}  
 * (this is typically only used in test environments.)
 * 
 * <h3>Resource discovery</h3>
 * {@link PluginResourceLoader} will search the following places for plugin classes:
 * <ul>
 * <li> the /lib folder under the plugin's root directory, e.g. "myplugin/lib"
 * </ul>
 * {@link PluginResourceLoader} will search for non-class resources in several locations:
 * <ul>
 * <li> inside jar files located in the lib directory
 * <li> from the filesystem relative to the root directory of the plugin
 * </ul>
 * 
 * <h3>resourcePath</h3> This class requires
 * resource paths to be the relative paths to plugin resources, relative the root directory
 * of the plugin.  A resource path can be specified either using '/' or '.' (or both) in the path, depending
 * on the particular method you are using.  It is usually best to specify the path using '/' since
 * both the filesystem and the classloader can handle this delimiter, whereas '.' will not be handled
 * correctly if you are trying to load a resource from the filesystem.
 * 
 * <h3>Plugin Settings</h3>: this class backs the plugin settings APIs with the PentahoSystem settings service.
 * See {@link PentahoSystem#getSystemSetting(String, String)} and {@link ISystemSettings}.  System
 * settings are expected in a file named settings.xml in the root of the plugin directory.
 * 
 * @author aphillips
 *
 */
public class PluginResourceLoader implements IPluginResourceLoader {

  private File rootDir = null;

  private String settingsPath = ISolutionRepository.SEPARATOR + "settings.xml"; //$NON-NLS-1$

  public void setSettingsPath(String settingsPath) {
    this.settingsPath = settingsPath;
  }

  /**
   * Force the resource loader to look for resources in this root directory.  
   * If null, the resource loader will consult the {@link PluginClassLoader} 
   * for the root directory.
   * @param rootDir  the root directory in which to search for resources
   */
  public void setRootDir(File rootDir) {
    this.rootDir = rootDir;
  }

  public byte[] getResourceAsBytes(Class<? extends Object> clazz, String resourcePath) {
    InputStream in = getResourceAsStream(clazz, resourcePath);
    if (in == null) {
      return null;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      out.write(in);
    } catch (IOException e) {
      Logger.debug(this, "Cannot open stream to resource", e); //$NON-NLS-1$
      return null;
    }
    return out.toByteArray();
  }

  public String getResourceAsString(Class<? extends Object> clazz, String resourcePath)
      throws UnsupportedEncodingException {
    return getResourceAsString(clazz, resourcePath, LocaleHelper.getSystemEncoding());
  }

  public String getResourceAsString(Class<? extends Object> clazz, String resourcePath, String charsetName)
      throws UnsupportedEncodingException {
    byte[] bytes = getResourceAsBytes(clazz, resourcePath);
    if (bytes == null) {
      return null;
    }
    return new String(bytes, charsetName);
  }

  public String getSystemRelativePluginPath(Class<? extends Object> clazz) {
    File dir = getPluginDir(getClassLoader(clazz));
    if (dir == null) {
      return null;
    }
    // get the full path with \ converted to /
    String path = dir.getAbsolutePath().replace('\\', ISolutionRepository.SEPARATOR);
    int pos = path.lastIndexOf(ISolutionRepository.SEPARATOR + "system" + ISolutionRepository.SEPARATOR); //$NON-NLS-1$
    if (pos != -1) {
      path = path.substring(pos + 8);
    }
    return path;
  }

  protected File getPluginDir(ClassLoader classLoader) {
    if (rootDir != null) {
      return rootDir;
    }
    if (classLoader instanceof PluginClassLoader) {
      return new File(((PluginClassLoader) classLoader).getPluginAbsPath());
    }
    return null;
  }

  /*
   * It is important for this method to exist since it provides a way to override the classloader
   * which is particularly useful in test cases
   */
  protected ClassLoader getClassLoader(Class<?> clazz) {
    ClassLoader classLoader = clazz.getClassLoader();
    if (!PluginClassLoader.class.isAssignableFrom(classLoader.getClass())) {
      Logger
          .warn(
              this,
              "getClassLoader must return an instance of "
                  + PluginClassLoader.class.getSimpleName()
                  + ".  If you are running in a unit test environment you may wish use a subclass of "
                  + PluginResourceLoader.class.getSimpleName() + " that overrides this method to return a test classloader.");
    }
    return clazz.getClassLoader();
  }

  public InputStream getResourceAsStream(Class<?> clazz, String resourcePath) {
    ClassLoader classLoader = getClassLoader(clazz);

    //display a warning message if a plugin class is not being loaded by a PluginClassLoader
    if (rootDir == null && !PluginClassLoader.class.isAssignableFrom(classLoader.getClass())) {
      Logger
          .warn(
              this,
              "Class ["
                  + clazz.getName()
                  + "] was not loaded from a "
                  + PluginClassLoader.class.getSimpleName()
                  + ".  Is this really a plugin class?  "
                  + "If "
                  + clazz.getSimpleName()
                  + " is part of your plugin, but will not be loaded with a "
                  + PluginClassLoader.class.getSimpleName()
                  + " (such as in a test environment), you might consider using setRootDir() to set an artificial plugin base directory."
                  + "  Look higher up in the log for warnings from " + PluginClassLoader.class.getSimpleName());
    }
    InputStream in = null;

    File root = getPluginDir(classLoader);
    if (root != null) {

      //can we find it on the filesystem?
      File f = new File(root, resourcePath);
      if (f.exists()) {
        try {
          in = new FileInputStream(new File(root, resourcePath));
        } catch (FileNotFoundException e) {
          Logger.debug(this, "Cannot open stream to resource", e); //$NON-NLS-1$
        }
      }
      //if not on filesystem, ask the classloader
      else {
        in = classLoader.getResourceAsStream(resourcePath);
        if (in == null) {
          Logger.debug(this, "Cannot find resource defined by path [" + resourcePath + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    }
    return in;
  }

  public List<URL> findResources(Class<?> clazz, String namePattern) {

    WildcardFileFilter fileFilter = new WildcardFileFilter(namePattern);
    Collection<?> files = FileUtils
        .listFiles(getPluginDir(getClassLoader(clazz)), fileFilter, TrueFileFilter.INSTANCE);
    Iterator<?> fileIter = files.iterator();
    List<URL> urls = new ArrayList<URL>(files.size());
    while (fileIter.hasNext()) {
      try {
        urls.add(((File) fileIter.next()).toURL());
      } catch (MalformedURLException e) {
        Logger.warn(this, "Could not create url", e); //$NON-NLS-1$
      }
    }
    return urls;
  }

  public ResourceBundle getResourceBundle(Class<?> clazz, String resourcePath) {
    ResourceBundle bundle = ResourceBundle.getBundle(resourcePath, LocaleHelper.getLocale(), getClassLoader(clazz));
    return bundle;
  }

  public String getPluginSetting(Class<?> pluginClass, String key) {
    return PentahoSystem.getSystemSetting(getSystemRelativePluginPath(pluginClass) + settingsPath, key, null);
  }

  public String getPluginSetting(Class<?> pluginClass, String key, String defaultVal) {
    return PentahoSystem.getSystemSetting(getSystemRelativePluginPath(pluginClass) + settingsPath, key, defaultVal);
  }
}