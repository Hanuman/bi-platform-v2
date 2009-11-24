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
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A custom implementation of {@link URLClassLoader} for Pentaho Platform Plugins.
 * It is used to load plugin jars and classes and aids in retrieving resources by providing
 * a root directory to search for resources related to plugins.
 * <p>
 * Note: {@link PluginClassLoader} will search for jar files in a 'lib' subdirectory
 * under the pluginDir provided in the constructor.
 * @author aphillips
 */
public class PluginClassLoader extends URLClassLoader {
  private static Log log = LogFactory.getLog(PluginClassLoader.class);

  private File pluginDir;

  private boolean overrideLoad = true;

  /**
   * Creates a class loader for loading plugin classes and discovering resources.
   * Jars must be located in [pluginDir]/lib.
   * @param pluginDir the root directory of the plugin
   * @param parent the parent classloader
   */
  public PluginClassLoader(final File pluginDir, ClassLoader parent) {
    super(getPluginUrls(pluginDir), parent);
    this.pluginDir = pluginDir;
    if (log.isDebugEnabled()) {
      log.debug("URLs for this classloader:"); //$NON-NLS-1$
      for (URL url : getURLs()) {
        log.debug(url);
      }
    }
  }

  protected static URL[] getPluginUrls(File pluginDir) {
    List<URL> urls = new ArrayList<URL>();
    File libDir = new File(pluginDir, "lib"); //$NON-NLS-1$
    try {
      urls.add(pluginDir.toURI().toURL());
      urls.add(libDir.toURI().toURL());
      addJars(urls, libDir);
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return urls.toArray(new URL[urls.size()]);
  }

  protected static void addJars(List<URL> urls, File folder) {
    if (folder.exists() && folder.isDirectory()) {
      // get a list of all the JAR files
      FilenameFilter filter = new WildcardFileFilter("*.jar"); //$NON-NLS-1$
      File jarFiles[] = folder.listFiles(filter);
      if (jarFiles != null && jarFiles.length > 0) {

        for (File file : jarFiles) {
          URL url = null;
          try {
            url = file.toURI().toURL();
            if (log.isDebugEnabled()) {
              log.debug("adding jar to plugin classloader: " + url.toString()); //$NON-NLS-1$
            }
            urls.add(url);
          } catch (MalformedURLException e) {
            if (log.isDebugEnabled()) {
              log.debug(MessageFormat.format("failed to add jar file {0} to classpath. Exception: {1}", file //$NON-NLS-1$
                  .getAbsolutePath(), e.getMessage()));
            }
          }
        }
      }
    }
  }

  public File getPluginDir() {
    return pluginDir;
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (log.isDebugEnabled()) {
      log.debug("loadClass(" + name + ")... "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    Class<?> t = null;
    t = findLoadedClass(name);
    if (t != null) {
      if (log.isDebugEnabled())
        log.debug(MessageFormat.format("{0} loaded by {1}", name, t.getClassLoader())); //$NON-NLS-1$
      return t;
    }

    if (overrideLoad) {
      try {
        t = findClass(name);
        if (t != null) {
          if (log.isDebugEnabled()) {
            log.debug(MessageFormat.format("{0} loaded by {1}", name, this)); //$NON-NLS-1$
          }
          if (resolve)
            resolveClass(t);
          return t;
        }
      } catch (ClassNotFoundException e) {
        if (log.isTraceEnabled()) {
          log.trace(MessageFormat.format("class {0} not found in loader {1}. Trying parent loader", name, this)); //$NON-NLS-1$
        }
      }
    }

    t = super.loadClass(name, resolve);
    if (log.isDebugEnabled()) {
      log.debug(MessageFormat.format("{0} loaded by {1}", name, t.getClassLoader())); //$NON-NLS-1$
    }
    return t;
  }
  
  @Override
  public String toString() {
    return super.toString() + ((pluginDir != null)?" at "+pluginDir.getAbsolutePath():""); //$NON-NLS-1$ //$NON-NLS-2$
  }
}