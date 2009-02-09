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
