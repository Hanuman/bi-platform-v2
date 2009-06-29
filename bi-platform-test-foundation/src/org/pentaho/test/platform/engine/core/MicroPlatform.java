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

import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.boot.PentahoSystemBoot;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

/**
 * This class is a convenience wrapper class around {@link PentahoSystemBoot} to aid
 * in simple integration/unit testing against the BI platform. Please first think about 
 * making any changes to {@link PentahoSystemBoot} before changing {@link MicroPlatform}.
 * 
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
 * @see PentahoSystemBoot
 */
@SuppressWarnings("nls")
public class MicroPlatform extends PentahoSystemBoot {
  private String baseUrl;

  /**
   * Creates a minimal ready-to-run platform.  Use this constructor if you don't need to load
   * any files from a solutions folder, i.e. you require only an in-memory platform.
   */
  public MicroPlatform() {
    this(".");
  }
  
  /**
   * Creates a minimal ready-to-run platform with a specified solution path.  Use this constructor if
   * your test needs to access system or other solution files from a particular directory.
   * Note that MicroPlatform's default behavior is to load zero files from the filesystem.
   * In fact, it is completely up to you to define a solution path at all.
   * @param solutionPath full path to the pentaho_solutions folder 
   */
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

  /**
   * Initializes the platform.  You only need to call this method if you need the runtime aspects of 
   * the platform in your test.  If you only require the certain system objects to be defined, you may only
   * need to create a MicroPlatform and define a few objects.  First try your test without calling {@link #init()}.
   */
  public void init() {
    if (!start()) {
      throw new RuntimeException("platform initialization failed");
    }
  }
  
  @Override
  protected IApplicationContext createApplicationContext() {
    StandaloneApplicationContext appCtxt = new StandaloneApplicationContext(getFilePath(), "");
    appCtxt.setBaseUrl(baseUrl);
    return appCtxt;
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
