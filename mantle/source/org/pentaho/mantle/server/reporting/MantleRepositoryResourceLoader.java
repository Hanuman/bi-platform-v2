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
 */
package org.pentaho.mantle.server.reporting;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jfree.resourceloader.ResourceData;
import org.jfree.resourceloader.ResourceKey;
import org.jfree.resourceloader.ResourceKeyCreationException;
import org.jfree.resourceloader.ResourceLoader;
import org.jfree.resourceloader.ResourceLoadingException;
import org.jfree.resourceloader.ResourceManager;

/**
 * This class is implemented to support loading solution files
 * from the pentaho repository into JFreeReport
 *
 * @author Will Gorman/Michael D'Amour
 */
public class MantleRepositoryResourceLoader implements ResourceLoader {

  public static final String SOLUTION_SCHEMA_NAME = "mantle"; //$NON-NLS-1$

  public static final String SCHEMA_SEPARATOR = ":/"; //$NON-NLS-1$

  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  public static final String WIN_PATH_SEPARATOR = "\\"; //$NON-NLS-1$

  /** keep track of the resource manager */
  private ResourceManager manager;

  /**
   * default constructor
   */
  public MantleRepositoryResourceLoader() {
  }

  /**
   * set the resource manager
   *  
   * @param manager resource manager
   */
  public void setResourceManager(ResourceManager manager) {
    this.manager = manager;
  }

  /**
   * get the resource manager
   * 
   * @return resource manager
   */
  public ResourceManager getManager() {
    return manager;
  }

  /**
   * get the schema name, in this case it's always "solution"
   * 
   * @return the schema name
   */
  public String getSchema() {
    return SOLUTION_SCHEMA_NAME;
  }

  /**
   * create a resource data object
   * 
   * @param key resource key
   * @return resource data
   * @throws ResourceLoadingException
   */
  public ResourceData load(ResourceKey key) throws ResourceLoadingException {
    return new MantleRepositoryResourceData(key);
  }

  /**
   * see if the pentaho resource loader can support the content key path
   * 
   * @param values map of values to look in
   * @return true if class supports the content key.
   */
  public boolean isSupportedKey(ResourceKey key) {
    if (key.getSchema().equals(getSchema())) {
      return true;
    }
    return false;
  }

  /**
   * create a new key based on the values provided
   * 
   * @param values map of values
   * @return new resource key
   * @throws ResourceKeyCreationException
   */
  public ResourceKey createKey(Object value, Map factoryKeys) throws ResourceKeyCreationException {
    if (value instanceof String) {
      String valueString = (String) value;
      if (valueString.startsWith(getSchema() + SCHEMA_SEPARATOR)) {
        String path = valueString.substring(getSchema().length() + SCHEMA_SEPARATOR.length());
        return new ResourceKey(getSchema(), path, factoryKeys);
      }
    }
    return null;
  }

  /**
   * derive a key from an existing key, used when a relative path is given.
   * 
   * @param parent the parent key
   * @param data the new data to be keyed
   * @return derived key
   * @throws ResourceKeyCreationException
   */
  public ResourceKey deriveKey(ResourceKey parent, String path, Map data) throws ResourceKeyCreationException {

    // update url to absolute path if currently a relative path
    if (!path.startsWith(getSchema() + SCHEMA_SEPARATOR)) {
      // we are looking for the current directory specified by the parent.  currently
      // the pentaho system uses the native File.separator, so we need to support it
      // we're simply looking for the last "/" or "\" in the parent's url.
      int winindex = ((String) parent.getIdentifier()).lastIndexOf(WIN_PATH_SEPARATOR);
      int regindex = ((String) parent.getIdentifier()).lastIndexOf(PATH_SEPARATOR);
      int dirindex = 0;
      if (winindex > regindex) {
        dirindex = winindex + WIN_PATH_SEPARATOR.length();
      } else {
        dirindex = regindex + PATH_SEPARATOR.length();
      }
      path = ((String) parent.getIdentifier()).substring(0, dirindex) + path;
    }
    final Map derivedValues = new HashMap(parent.getFactoryParameters());
    derivedValues.putAll(data);
    return new ResourceKey(getSchema(), path, derivedValues);
  }

  public URL toURL(ResourceKey key) {
    // not supported ..
    return null;
  }

}
