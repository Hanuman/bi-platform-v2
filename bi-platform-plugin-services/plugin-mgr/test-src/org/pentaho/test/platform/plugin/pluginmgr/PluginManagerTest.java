package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginComponentException;
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

  @Before
  public void init0() {
    //setup the most common platform config for these tests. Some tests may choose 
    //to override some of these settings locally or not init the platform at all
    //if they are true unit tests
    microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);
  }

  @Test
  public void test1_Reload() {
    microPlatform.init();
    
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new PluginManager();
    pluginManager.reload(session);

    //one of the plugins serves a content generator with id=test1.  Make sure we can load it.
    assertNotNull("The plugin serving content generator with id=test1 was not loaded", pluginManager
        .getContentGeneratorInfo("test1", session));
  }

  
  @Test
  public void test2_Plugin1ReceivesLifecycleEvents() {
    microPlatform.define(IPluginProvider.class, Tst2PluginProvider.class);
    microPlatform.init();
    
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new PluginManager();
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
    microPlatform.define(IPluginProvider.class, Tst3PluginProvider.class);
    microPlatform.init();
    
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new PluginManager();
    
    PluginMessageLogger.clear();
    
    pluginManager.reload(session);
    
    System.err.println(PluginMessageLogger.prettyPrint());
    
    assertEquals("bad plugin Plugin 3 did not fail to load", 1, PluginMessageLogger
        .count("PluginManager.ERROR_0011"));
  }

  @Test
  public void test4_GetOverlays() throws Exception {
    microPlatform.init();
    
    IPentahoSession session = new StandaloneSession("test user"); //$NON-NLS-1$
    IPluginManager pluginManager = new PluginManager();
    assertNotNull(pluginManager);

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
  
//    private class Tst5ComponentPluginManager extends PluginManager {
//      Tst5ComponentPluginManager() {
//      super();
//      this.reload(new StandaloneSession());
//      pluginComponentMap.put("TestMockComponent", "org.pentaho.test.platform.engine.core.MockComponent");
//      pluginComponentMap.put("TestPojo", "java.lang.String");
//      pluginComponentMap.put("TestClassNotFoundComponent", "org.pentaho.test.NotThere");
//    }
//  }
  
  @Ignore
  @Test
  //This test will not work until I get the pluginManager to register beans
  public void test5_ComponentMethods() {
//    IPluginManager pluginManager = new Tst5ComponentPluginManager();
    IPluginManager pluginManager = null;
    
    assertTrue(pluginManager.isObjectRegistered("TestMockComponent"));
    assertTrue(pluginManager.isObjectRegistered("TestPojo"));
    
    assertFalse(pluginManager.isObjectRegistered("IDoNotExist"));

    try {
      Object obj = pluginManager.getRegisteredObject("TestMockComponent");
      assertTrue(obj instanceof IComponent);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }

    try {
      Object pojo = pluginManager.getRegisteredObject("TestPojo");
      assertTrue(pojo instanceof String);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }
    
    try {
      Object bogus = pluginManager.getRegisteredObject("IDoNotExist");
      assertNull(bogus);
    } catch (PluginComponentException ex) {
      assertFalse("Exception was not expected", true);
    }
    
    try {
      pluginManager.getRegisteredObject("TestClassNotFoundComponent");
      assertFalse("Exception was not expected", true);
    } catch (PluginComponentException ex) {
      assertTrue("Exception was expected", true);
    }
    
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
      return Arrays.asList((IPlatformPlugin)p);
    }
  }
  
  public static class Tst2PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setLifecycleListenerClassname(CheckingLifecycleListener.class.getName());
      return Arrays.asList((IPlatformPlugin)p);
    }
  }
}
