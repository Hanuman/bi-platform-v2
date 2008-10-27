package org.pentaho.test.platform.plugin.pluginmgr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoPublisher;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystem;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginSettings;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.engine.core.TestManager;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class PluginSettingsNotConfiguredTest extends BaseTest {
  private static final String SOLUTION_PATH = "plugin-mgr/test-res/solution1-no-config"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-res/solution1-no-config"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$

  final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  @Override
  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  private boolean initOk;

  public void setUp() {

    List<?> messages = TestManager.getMessagesList();
    if (messages == null) {
      messages = new ArrayList<String>();
    }

    if (initOk) {
      return;
    }
    
    PentahoSystem.setSystemSettingsService(new PathBasedSystemSettings());
    if (PentahoSystem.getApplicationContext() == null) {
      StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(getSolutionPath(), ""); //$NON-NLS-1$
      // set the base url assuming there is a running server on port 8080
      applicationContext.setBaseUrl(getBaseUrl());
      String inContainer = System.getProperty("incontainer", "false"); //$NON-NLS-1$ //$NON-NLS-2$
      if (inContainer.equalsIgnoreCase("false")) { //$NON-NLS-1$
        // Setup simple-jndi for datasources
        System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      ApplicationContext springApplicationContext = getSpringApplicationContext();

      IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
      pentahoObjectFactory.init(null, springApplicationContext);
      PentahoSystem.setObjectFactory(pentahoObjectFactory);

      //force Spring to populate PentahoSystem
      springApplicationContext.getBean("pentahoSystemProxy"); //$NON-NLS-1$
      initOk = PentahoSystem.init(applicationContext);
    } else {
      initOk = true;
    }
    initOk = true;
    //	    assertTrue(Messages.getString("BaseTest.ERROR_0001_FAILED_INITIALIZATION"), initOk); //$NON-NLS-1$

  }

  private ApplicationContext getSpringApplicationContext() {

    String[] fns = { "pentahoObjects.spring.xml", "adminPlugins.xml", "sessionStartupActions.xml", "systemListeners.xml", "pentahoSystemConfig.xml" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);

    for (String fn : fns) {
      File f = new File(getSolutionPath() + SYSTEM_FOLDER + "/" + fn); //$NON-NLS-1$
      if (f.exists()) {
        FileSystemResource fsr = new FileSystemResource(f);
        xmlReader.loadBeanDefinitions(fsr);
      }
    }
    
    return appCtx;
  }

  public void testBadConfig1() {
    startTest();

    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$
    IPluginSettings pluginSettings = (IPluginSettings) PentahoSystem.getObject(session, "IPluginSettings"); //$NON-NLS-1$
    assertNull(pluginSettings);

    finishTest();
  }

  public void testPluginSettingsStaticMethods() {
    startTest();

    assertNull(PluginSettings.getInstance());
    assertEquals("", PluginSettings.getContentGeneratorIdForType("test-type-1")); //$NON-NLS-1$//$NON-NLS-2$
    assertEquals("", PluginSettings.getContentGeneratorTitleForType("test-type-1")); //$NON-NLS-1$//$NON-NLS-2$
    assertEquals("", PluginSettings.getContentGeneratorUrlForType("test-type-1")); //$NON-NLS-1$//$NON-NLS-2$

    finishTest();
  }

  public void testPluginManagerViaPublish() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$

    String str = PentahoSystem.publish(session, "org.pentaho.platform.plugin.services.pluginmgr.PluginManager"); //$NON-NLS-1$
    assertEquals(str, Messages.getString("PluginManager.ERROR_0001_PLUGIN_SETTINGS_NOT_CONFIGURED")); //$NON-NLS-1$
    finishTest();
  }

  @SuppressWarnings("cast")
  public void testPluginManagerViaPublisherAPI() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$

    PluginManager mgr = new PluginManager();
    assertTrue(mgr instanceof IPentahoPublisher);
    IPentahoPublisher publisher = (IPentahoPublisher) mgr;

    assertEquals(Messages.getString("PluginManager.USER_PLUGIN_MANAGER"), publisher.getName()); //$NON-NLS-1$
    assertNotSame("!PluginManager.USER_PLUGIN_MANAGER!", publisher.getName()); //$NON-NLS-1$

    assertEquals(Messages.getString("PluginManager.USER_REFRESH_PLUGINS"), publisher.getDescription()); //$NON-NLS-1$
    assertNotSame("!PluginManager.USER_REFRESH_PLUGINS!", publisher.getName()); //$NON-NLS-1$

    String str = publisher.publish(session, ILogger.DEBUG);
    assertEquals(str, Messages.getString("PluginManager.ERROR_0001_PLUGIN_SETTINGS_NOT_CONFIGURED")); //$NON-NLS-1$
    finishTest();
  }

  @SuppressWarnings("cast")
  public void testPluginManagerViaSystemListenerAPI() throws Exception {
    startTest();

    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$

    PluginManager mgr = new PluginManager();
    assertTrue(mgr instanceof IPentahoSystemListener);

    IPentahoSystemListener listener = (IPentahoSystemListener) mgr;

    assertFalse(listener.startup(session));

    // this does not do anything but it shouldn't error
    listener.shutdown();

    finishTest();
  }

}
