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
 * Copyright 2009 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

/**
 * A self-contained and very easy to configure platform initializer which requires
 * zero sidecar files.  Yes, that means no xml of any kind.
 * Use it in your tests like this:
 * <pre>
 * &#064;Before
 * public void init() {
 *   MicroPlatform mp = new MicroPlatform("path/to/system/folder");
 *   //setup your required object definitions
 *   mp.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
 *   
 *   //setup your required system settings
 *   mp.set("MySetting", "true");
 *   
 *   //initialize the minimal platform
 *   mp.init();
 * }
 * </pre>
 * @author aphillips
 */
@SuppressWarnings("nls")
public class MicroPlatform extends PentahoSystemBoot {
  private String baseUrl;

  public MicroPlatform(String solutionPath) {
    this(solutionPath, "http://localhost:8080/pentaho/");
  }

  public MicroPlatform(String solutionPath, String baseUrl) {
    this(solutionPath, baseUrl, new StandaloneObjectFactory());
  }

  public MicroPlatform(String solutionPath, IPentahoDefinableObjectFactory factory) {
    this(solutionPath, "http://localhost:8080/pentaho/", factory);
  }

  public MicroPlatform(String solutionPath, String baseUrl, IPentahoDefinableObjectFactory factory) {
    this.baseUrl = baseUrl;
    setFilePath(solutionPath);
    setObjectFactory(factory);
  }

  public void init() {
    boolean success = start();
    //TODO: //    applicationContext.setBaseUrl(baseUrl);
    if (!success) {
      throw new RuntimeException("platform initialization failed");
    }
  }

  public MicroPlatform set(String settingName, String settingVal) {
    ((SimpleSystemSettings)getSettingsProvider()).addSetting(settingName, settingVal);
    return this;
  }
  
  /**
   * Define a locally scoped object (aka prototype scope -- unique instance for each request for the class)
   * @param interfaceClass  the key to retrieval of this object
   * @param implClass  the actual type that is served back to you when requested.
   * @return  the current {@link MicroPlatform} instance, for chaining
   */
  public MicroPlatform define(Class<?> interfaceClass, Class<?> implClass) {
    return (MicroPlatform)define(interfaceClass.getSimpleName(), implClass.getName(), Scope.LOCAL);
  }
  
  /**
   * Define a locally scoped object (aka prototype scope -- unique instance for each request for the class)
   * @param key  the key to retrieval of this object
   * @param implClass  the actual type that is served back to you when requested.
   * @return  the current {@link MicroPlatform} instance, for chaining
   */
  public MicroPlatform define(String key, Class<?> implClass) {
    return (MicroPlatform)define(key, implClass.getName(), Scope.LOCAL);
  }
}
