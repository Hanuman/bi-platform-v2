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
 * Created Dec 19, 2008 
 * @author aphillips
 */
package org.pentaho.platform.api.engine;

import java.util.List;

/**
 * A plugin provider is responsible for serving up {@link IPlatformPlugin}s to whoever is asking, typically
 * {@link IPluginManager}.  The plugin provider is not responsible for integrating the plugins into the platform.
 * It's only role is to render plugin definitions, {@link IPlatformPlugin}s.  A plugin provider might 
 * load plugin definitions from an xml file, or a properties file.  You might also create a plugin provider
 * that creates plugins programmatically.  It won't matter what mechanism you use to define your plugins
 * so long as you implement {@link IPluginProvider}.
 * @author aphillips
 */
public interface IPluginProvider {

  /**
   * Returns a list of {@link IPlatformPlugin}s defined by this plugin provider.  These plugins have not been
   * initialized or registered within the platform at this point.
   * @return a list of platform plugins
   */
  public List<IPlatformPlugin> getPlugins();

  /**
   * Force the plugin provider to renew its list of {@link IPlatformPlugin}s.  A call to {@link #getPlugins()}s
   * after a {@link #reload(IPentahoSession, List)} may result in a different set of {@link IPlatformPlugin}s.  
   * @param session the current session
   * @param comments
   */
  public void reload(IPentahoSession session, List<String> comments);

}
