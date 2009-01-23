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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

/**
 * This class presents an abstraction layer for accessing resources from within a platform plugin.
 * The idea is you do not need to know how to absolutely resolve the resources you need.  For example: 
 * You don't need to know the absolute path if your resources are on a file-system.  All you need to know
 * in order to obtain a resource is the relative path to the resource.  "Relative" means relative to the base
 * location of your plugin.  For a filesystem example, let's say that your plugin is installed in 
 * "/home/user/plugins/myplugin" and you need the file "/home/user/plugins/myplugin/html/my.html", 
 * you would ask this class for the resource "html/my.html" 
 * 
 * @author aphillips
 */
public interface IPluginResourceLoader {
  
  /**
   * Gets a plugin-related resource in the form of an array of bytes.  The relevant plugin is inferred from pluginClass.
   * An example of resource path is "resources/html/my.html".  {@link IPluginResourceLoader} is able to resolve 
   * relative paths as it knows where to look for plugin classes and resources.
   *  
   * @param pluginClass a class that is part of the plugin package
   * @param resourcePath the (relative) path to a resource
   * @return a resource as an array of bytes or null if the resource is not found
   */
  public byte[] getResourceAsBytes(Class<? extends Object> pluginClass, String resourcePath);

  /**
   * Gets a plugin-related resource in the form of a String.  The relevant plugin is inferred from pluginClass.
   * An example of resource path is "resources/html/my.html".  {@link IPluginResourceLoader} is able to resolve 
   * relative paths as it knows where to look for plugin classes and resources.
   * <p>
   * This method defaults the character encoding (how this default is chosen is up to the implementor).
   *  
   * @param pluginClass a class that is part of the plugin package
   * @param resourcePath the (relative) path to a resource
   * @return a resource as a {@link String} or null if the resource is not found
   * @exception UnsupportedEncodingException if there is a problem encoding the string
   */
  public String getResourceAsString(Class<? extends Object> pluginClass, String resourcePath)
      throws UnsupportedEncodingException;
  
  /**
   * Gets a plugin-related resource in the form of a String.  The relevant plugin is inferred from pluginClass.
   * An example of resource path is "resources/html/my.html".  {@link IPluginResourceLoader} is able to resolve 
   * relative paths as it knows where to look for plugin classes and resources.
   *  
   * @param pluginClass a class that is part of the plugin package
   * @param resourcePath the (relative) path to a resource
   * @param charsetName the character set to encode the string
   * @return a resource as a {@link String} or null if the resource is not found
   * @exception UnsupportedEncodingException if there is a problem encoding the string
   */
  public String getResourceAsString(Class<? extends Object> pluginClass, String resourcePath, String charsetName)
  throws UnsupportedEncodingException;

  /**
   * Gets a plugin-related resource in the form of an InputStream.  The relevant plugin is inferred from pluginClass.
   * An example of resource path is "resources/html/my.html".  {@link IPluginResourceLoader} is able to resolve 
   * relative paths as it knows where to look for plugin classes and resources.
   *  
   * @param pluginClass a class that is part of the plugin package
   * @param resourcePath the (relative) path to a resource
   * @return a resource as an {@link InputStream} or null if the resource is not found
   */
  public InputStream getResourceAsStream(Class<?> pluginClass, String resourcePath);

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