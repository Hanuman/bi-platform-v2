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
 * Created Jan 20, 2009 
 * @author aphillips
 */

package org.pentaho.platform.api.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

/**
 * Finds and returns resources for the plugin related to given class.
 * 
 * @author aphillips
 */
public interface IPluginResourceLoader {
  public byte[] getResourceAsBytes(Class<? extends Object> pluginClass, String resourcePath) throws IOException;

  public String getResourceAsString(Class<? extends Object> pluginClass, String resourcePath)
      throws UnsupportedEncodingException, IOException;

  public InputStream getResourceAsStream(Class<?> pluginClass, String resourcePath) throws IOException;

  /**
   * Retrieves a localized resource bundle for the plugin represented by pluginClass.
   * baseName is a fully qualified package name or relative path to a bundle name.
   * For example, a baseName of "resources.messages" might represent:
   * <ul>
   * <li>(localized) class messages.class in the resources package
   * <li>(localized) messages.properties file in the resource package (of a jar)
   * <li>(localized) messages.properties file in the resources folder at the base of the plugin folder
   * </ul>
   * Implementations of {@link IPluginResourceLoader#getResourceBundle(Class, String)} should behave similar to {@link ResourceBundle#getBundle(String)} 
   * @param pluginClass a class that is part of the plugin package
   * @param baseName points to a particular resource bundle
   * @return a {@link ResourceBundle}
   * @see ResourceBundle
   */
  public ResourceBundle getResourceBundle(Class<?> pluginClass, String baseName);
  
  /**
   * Returns the path (within the system solution) to the plugin's root folder
   * @return
   */
  public String getPluginPath(Class<? extends Object> clazz);
}