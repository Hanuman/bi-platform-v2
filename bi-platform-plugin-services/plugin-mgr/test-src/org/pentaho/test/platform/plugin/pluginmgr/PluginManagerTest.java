package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.IPlatformPlugin.BeanDefinition;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.PlatformPlugin;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.ui.xul.XulOverlay;

@SuppressWarnings("nls")
public class PluginManagerTest {

  private MicroPlatform microPlatform;
  StandaloneSession session;
  IPluginManager pluginManager;

  @Before
  public void init0() {
    //setup the most common platform config for these tests. Some tests may choose 
    //to override some of these settings locally or not init the platform at all
    //if they are true unit tests
    microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest");
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);
    
    session = new StandaloneSession();
    pluginManager = new PluginManager();
    
  }

  @Test
  public void INTEGRATION_test1_Reload() {
    microPlatform.init();

    pluginManager.reload(session);

    //one of the plugins serves a content generator with id=test1.  Make sure we can load it.
    assertNotNull("The plugin serving content generator with id=test1 was not loaded", pluginManager
        .getContentGeneratorInfo("test1", session));
  }

  @Test
  public void test2_Plugin1ReceivesLifecycleEvents() {
    microPlatform.define(IPluginProvider.class, Tst2PluginProvider.class).init();

    pluginManager.reload(session);

    assertFalse("unload was called", CheckingLifecycleListener.unloadedCalled);
    assertTrue("init was not called", CheckingLifecycleListener.initCalled);
    assertTrue("loaded was not called", CheckingLifecycleListener.loadedCalled);

    //reload again, this time we expect the plugin to be unloaded first
    pluginManager.reload(session);

    assertTrue("unload was not called", CheckingLifecycleListener.unloadedCalled);
    assertTrue("init was not called", CheckingLifecycleListener.initCalled);
    assertTrue("loaded was not called", CheckingLifecycleListener.loadedCalled);
  }

  @Test
  public void test3_Plugin3FailsToLoad() {
    microPlatform.define(IPluginProvider.class, Tst3PluginProvider.class).init();

    PluginMessageLogger.clear();

    pluginManager.reload(session);

    System.err.println(PluginMessageLogger.prettyPrint());

    assertEquals("bad plugin Plugin 3 did not fail to load", 1, PluginMessageLogger.count("PluginManager.ERROR_0011"));
  }

  @Test
  public void INTEGRATION_test4_GetOverlays() throws Exception {
    microPlatform.init();

    PluginMessageLogger.clear();
    pluginManager.reload(session);
    System.err.println(PluginMessageLogger.prettyPrint());

    List<XulOverlay> overlays = pluginManager.getOverlays();

    assertNotNull("Overlays is null", overlays); //$NON-NLS-1$

    System.err.println(overlays);

    assertEquals("Wrong number of overlays", 2, overlays.size()); //$NON-NLS-1$
    XulOverlay overlay = overlays.get(0);
    assertEquals("Wrong overlay id", "overlay1", overlay.getId()); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals("Wrong overlay resource uri", "uri1", overlay.getResourceBundleUri()); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node1") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node2") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node3") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node4") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull("Overlay URI should be null", overlay.getOverlayUri()); //$NON-NLS-1$

    overlay = overlays.get(1);

    assertEquals("Wrong overlay id", "overlay2", overlay.getId()); //$NON-NLS-1$ //$NON-NLS-2$
    assertEquals("Wrong overlay resource uri", "uri2", overlay.getResourceBundleUri()); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node1") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node2") == -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node3") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertTrue("Wrong overlay content", overlay.getSource().indexOf("<node4") != -1); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull("Overlay URI should be null", overlay.getOverlayUri()); //$NON-NLS-1$
  }

  @Test
  public void test5a_getBean() throws PluginBeanException {
    microPlatform.define(IPluginProvider.class, Tst5PluginProvider.class).init();

    //reload should register the beans
    pluginManager.reload(new StandaloneSession());

    assertTrue("TestMockComponent should have been registered", pluginManager.isBeanRegistered("TestMockComponent"));
    assertTrue("TestPojo should have been registered", pluginManager.isBeanRegistered("TestPojo"));

    Object obj = pluginManager.getBean("TestMockComponent");
    assertTrue(obj instanceof IComponent);

    Object pojo = pluginManager.getBean("TestPojo");
    assertTrue(pojo instanceof String);
  }

  @Test(expected = PluginBeanException.class)
  public void test5b_getUnregisteredBean() throws PluginBeanException {
    microPlatform.define(IPluginProvider.class, Tst5PluginProvider.class).init();

    //reload should register the beans
    pluginManager.reload(new StandaloneSession());

    assertFalse("IWasNotRegistered should not have been registered", pluginManager
        .isBeanRegistered("IWasNotRegistered"));

    pluginManager.getBean("IWasNotRegistered");
  }

  @Test(expected = PluginBeanException.class)
  public void test5c_getBeanBadClassname() throws PluginBeanException {
    microPlatform.define(IPluginProvider.class, Tst5PluginProvider.class).init();

    //reload should register the beans
    pluginManager.reload(new StandaloneSession());

    assertTrue("TestClassNotFoundComponent should have been registered", pluginManager
        .isBeanRegistered("TestClassNotFoundComponent"));

    assertNotNull(pluginManager.getBean("TestClassNotFoundComponent"));
  }

  @Test
  public void test6_beanNameCollision() throws PluginBeanException {
    microPlatform.define(IPluginProvider.class, Tst6PluginProvider.class).init();

    PluginMessageLogger.clear();
    
    //reload should register the beans
    pluginManager.reload(new StandaloneSession());

    assertNotNull(pluginManager.getBean("bean1"));
    assertTrue(
        "The first plugin to register by this id is a String, it should have remained the registered bean for this id",
        pluginManager.getBean("bean1") instanceof String);
    
    //TODO: we should be able to test that the plugin was not loaded, indicated by bean1 not being registered, but
    //we cannot until plugin registration becomes transactional
  }
  
  @Test
  public void test8_getBeanFromPluginClassloader() throws PluginBeanException {
    microPlatform.define(IPluginProvider.class, Tst5PluginProvider.class).init();
    
    //reload should register the beans
    pluginManager.reload(new StandaloneSession());
    
    assertTrue("PluginOnlyClass should have been registered", pluginManager.isBeanRegistered("PluginOnlyClass"));
    assertNotNull("PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory", pluginManager.getBean("PluginOnlyClass"));
  }
  
  @Test
  public void INTEGRATION_getBeanFromPluginClassloader() throws PluginBeanException {
    microPlatform.init();

    //reload should register the beans
    pluginManager.reload(new StandaloneSession());

    assertTrue("PluginOnlyClass should have been registered", pluginManager.isBeanRegistered("PluginOnlyClass"));
    assertNotNull("PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory", pluginManager.getBean("PluginOnlyClass"));
    assertTrue("TestClassForClassloader should have been registered", pluginManager.isBeanRegistered("TestClassForClassloader"));
    assertNotNull("TestClassForClassloader bean should have been loaded from test-jar.jar in the plugin lib directory", pluginManager.getBean("TestClassForClassloader"));
  }
  

  public static class CheckingLifecycleListener implements IPluginLifecycleListener {
    public static boolean initCalled, loadedCalled, unloadedCalled;

    public void init() {
      initCalled = true;
      loadedCalled = false;
    }

    public void loaded() {
      loadedCalled = true;
    }

    public void unLoaded() {
      unloadedCalled = true;
      loadedCalled = false;
      initCalled = false;
    }
  }

  public static class Tst3PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setName("Plugin 3");
      p.setLifecycleListenerClassname("bogus.classname");
      return Arrays.asList((IPlatformPlugin) p);
    }
  }

  public static class Tst2PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setLifecycleListenerClassname(CheckingLifecycleListener.class.getName());
      return Arrays.asList((IPlatformPlugin) p);
    }
  }

  public static class Tst5PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      //need to set source description - classloader needs it
      p.setSourceDescription("good-plugin1");
      p.addBean(new BeanDefinition("TestMockComponent", "org.pentaho.test.platform.engine.core.MockComponent"));
      p.addBean(new BeanDefinition("TestPojo", "java.lang.String"));
      p.addBean(new BeanDefinition("TestClassNotFoundComponent", "org.pentaho.test.NotThere"));
      p.addBean(new BeanDefinition("PluginOnlyClass", "org.pentaho.nowhere.PluginOnlyClass"));
      return Arrays.asList((IPlatformPlugin) p);
    }
  }

  public static class Tst6PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setName("test6Plugin");
      p.addBean(new BeanDefinition("bean1", "java.lang.String"));
      p.addBean(new BeanDefinition("bean1", "java.lang.Object"));
      return Arrays.asList((IPlatformPlugin) p);
    }
  }
}
