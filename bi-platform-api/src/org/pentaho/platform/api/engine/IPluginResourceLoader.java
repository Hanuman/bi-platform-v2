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

/**
 * Finds and returns resources for the plugin related to given class.
 * 
 * @author aphillips
 */
public interface IPluginResourceLoader {
  public byte[] getResourceAsBytes(Class<? extends Object> clazz, String resourcePath) throws IOException;

  public String getResourceAsString(Class<? extends Object> clazz, String resourcePath)
      throws UnsupportedEncodingException, IOException;

  public InputStream getResourceAsStream(Class<?> clazz, String resourcePath) throws IOException;
}
