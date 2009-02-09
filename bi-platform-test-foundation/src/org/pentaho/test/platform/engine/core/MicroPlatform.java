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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;

/**
 * A self-contained and very easy to configure platform initializer.
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
public class MicroPlatform {
  private SimpleSystemSettings settings = new SimpleSystemSettings();

  private String solutionPath;

  private StandaloneObjectFactory factory = new StandaloneObjectFactory();

  public MicroPlatform(String solutionPath) {
    this.solutionPath = solutionPath;
  }

  public void init() {
    PentahoSystem.setSystemSettingsService(settings);

      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(solutionPath, ""); //$NON-NLS-1$
      PentahoSystem.setObjectFactory(factory);
      PentahoSystem.init(applicationContext);
  }

  public MicroPlatform set(String settingName, String settingVal) {
    settings.addSetting(settingName, settingVal);
    return this;
  }

  public MicroPlatform define(Class<?> interfaceClass, Class<?> implClass) {
    factory.defineObject(interfaceClass.getSimpleName(), implClass.getName(), Scope.LOCAL);
    return this;
  }
}
