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

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * The default implementation of the {@link IPluginResourceLoader}.  Obtains resources
 * by searching the root directory of a {@link PluginClassLoader}.  This behavior can
 * be overridden such that the {@link PluginResourceLoader} will search a specified directory
 * instead.  See {@link #setRootDir(File)}
 * @author aphillips
 *
 */
public class PluginResourceLoader implements IPluginResourceLoader {

  private File rootDir = null;

  /**
   * Force the resource loader to look for resources in this root directory.  
   * If null, the resource loader will consult the {@link PluginClassLoader} 
   * for the root directory.
   * @param rootDir  the root directory in which to search for resources
   */
  public void setRootDir(File rootDir) {
    this.rootDir = rootDir;
  }

  public byte[] getResourceAsBytes(Class<? extends Object> clazz, String resourcePath) throws IOException {
    InputStream in = getResourceAsStream(clazz, resourcePath);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(in);
    return out.toByteArray();
  }

  public String getResourceAsString(Class<? extends Object> clazz, String resourcePath)
      throws UnsupportedEncodingException, IOException {
    return new String(getResourceAsBytes(clazz, resourcePath), LocaleHelper.getSystemEncoding());
  }

  private File getRootDir(ClassLoader classLoader) {
    if (rootDir != null) {
      return rootDir;
    }
    if (classLoader instanceof PluginClassLoader) {
      return new File(((PluginClassLoader) classLoader).getPluginDir());
    }
    return null;
  }

  public InputStream getResourceAsStream(Class<?> clazz, String resourcePath) throws IOException {
    ClassLoader classLoader = clazz.getClassLoader();
    if (rootDir == null && !PluginClassLoader.class.isAssignableFrom(clazz)) {
      Logger
          .warn(
              this,
              "Class ["
                  + clazz.getName()
                  + "] was not loaded from a "
                  + PluginClassLoader.class.getSimpleName()
                  + ".  Is this really a plugin class?  "
                  + "If "+clazz.getSimpleName()+" is part of your plugin, but will not be loaded with a "
                  + PluginClassLoader.class.getSimpleName()
                  + " (such as in a test environment), you might consider using setRootDir() to set an artificial plugin base directory."
                  + "  Look higher up in the log for warnings from "+PluginClassLoader.class.getSimpleName());
    }
    InputStream in = null;

    File root = getRootDir(classLoader);
    if (root != null) {
      //TODO use jdk ResourceBundle to resolve localized properties
      //      ResourceBundle resourceBundle = ResourceBundle.getBundle(resourcePath);

      //can we find it on the filesystem?
      File f = new File(root, resourcePath);
      if (f.exists()) {
        in = new FileInputStream(new File(root, resourcePath));
      }
      //if not on filesystem, ask the classloader
      else {
        in = classLoader.getResourceAsStream(resourcePath);
        if (in == null) {
          String msg = "Cannot find resource defined by path [" + resourcePath + "]";
          Logger.error(PluginResourceLoader.class.getName(), msg);
          throw new FileNotFoundException(msg);
        }
      }
    }
    return in;
  }
}