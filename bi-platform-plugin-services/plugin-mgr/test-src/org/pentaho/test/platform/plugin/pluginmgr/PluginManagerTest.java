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
 *
 */
package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IComponent;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.engine.IPlatformPlugin.BeanDefinition;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ContentGeneratorInfo;
import org.pentaho.platform.engine.core.solution.ContentInfo;
import org.pentaho.platform.engine.core.solution.PluginOperation;
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
    microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
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
    microPlatform.define(IPluginProvider.class, Tst8PluginProvider.class).init();
    
    //reload should register the beans
    pluginManager.reload(new StandaloneSession());
    
    try {
      Class.forName("org.pentaho.nowhere.PluginOnlyClass");
      fail("PluginOnlyClass needs to be available only through the plugin lib dir in order for this test to be valid");
    } catch (ClassNotFoundException e) { }
    
    assertTrue("PluginOnlyClass should have been registered", pluginManager.isBeanRegistered("PluginOnlyClass"));
    assertNotNull("PluginOnlyClass bean should have been loaded from test-jar.jar in the plugin lib directory", pluginManager.getBean("PluginOnlyClass"));
  }
  
  @Test
  public void test8b_getBeanFromPluginClassloader_altSolutionPath() throws PluginBeanException {
    //This test is to validate a bug that had existed where a solution path ending in '/' was causing
    //the PluginClassLoader to not be able to open plugin jars, thus you would get ClassNotFound exceptions
    //when accessing plugin classes.
    MicroPlatform mp = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
    mp.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    mp.define(IPluginProvider.class, Tst8PluginProvider.class).init();
    
    //reload should register the beans
    pluginManager.reload(new StandaloneSession());
    
    try {
      Class.forName("org.pentaho.nowhere.PluginOnlyClass");
      fail("PluginOnlyClass needs to be available only through the plugin lib dir in order for this test to be valid");
    } catch (ClassNotFoundException e) { }
    
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
  
  @Test
  public void test9_ContentGenerationRegistration() throws ObjectFactoryException {
    microPlatform.define(IPluginProvider.class, Tst9PluginProvider.class).init();
    
    PluginMessageLogger.clear();
    
    pluginManager.reload(session);

    System.err.println(PluginMessageLogger.prettyPrint());

    //lookup cg infos by id
    assertNotNull("Should be able to get cg for test9id", pluginManager.getContentGeneratorInfo("test9id", session));
    assertNotNull("Should be able to get cg for test9bid", pluginManager.getContentGeneratorInfo("test9bid", session));

    
    
    //see if we have the expected number of cg's that support the test content type
    List<IContentGeneratorInfo> cgInfos = pluginManager.getContentGeneratorInfoForType("test9type", session);
    assertEquals("There should be 2 content generators that support the test9type", 2, cgInfos.size());
    
    //see if we can get a content generator instance by id
    assertNotNull("Could not get content generator test9 by id", pluginManager.getContentGenerator("test9id", session));    
    assertNotNull("Could not get content generator test9b by id", pluginManager.getContentGenerator("test9bid", session));    
    
    //see if we can access the content generator by type
    IContentGenerator contentGenerator = pluginManager.getContentGeneratorForType("test9type", session);
    assertNotNull("Should have gotten an instance of a cg for content type", contentGenerator);
    assertTrue(contentGenerator instanceof ContentGenerator1);
    
    //see if we can lookup content generator ids by content type
    assertEquals("test9id", pluginManager.getContentGeneratorIdForType("test9type", session));
    
    //see if we can lookup content generator titles by content type
    assertEquals("Test Generator 9", pluginManager.getContentGeneratorTitleForType("test9type", session));

    //see if we can lookup content generator urls by content type
    assertEquals("/test9url", pluginManager.getContentGeneratorUrlForType("test9type", session));
  }
  
  @Test
  public void test10_ContentTypeRegistration() {
    microPlatform.define(IPluginProvider.class, Tst10PluginProvider.class).init();
    
    pluginManager.reload(session);

    Set<String> types = pluginManager.getContentTypes();
    //FIXME: getContentTypes returns the list of types configured by content generators, not the list
    //of types defined by IContentInfo's.  Is this really what we want?  If a type has no content
    //generator configured, then it is invisible through this API.
    assertTrue("test10type1 should be registered", types.contains("test10type1-ext"));
    assertTrue("test10type2 should be registered", types.contains("test10type2-ext"));
    
    IContentInfo contentInfo = pluginManager.getContentInfoFromExtension("test10type1-ext", session);
    assertNotNull("type should be registered for extension test10type1-ext", contentInfo);
    assertEquals("test10type1-mimeType", contentInfo.getMimeType());
    assertEquals("test10type1-title", contentInfo.getTitle());
    assertEquals("test10type1-description", contentInfo.getDescription());
    assertEquals("test10type1-ext", contentInfo.getExtension());
    assertEquals("test10type1-url", contentInfo.getIconUrl());
    
    List<IPluginOperation> ops = contentInfo.getOperations();
    assertNotNull("Operations are null", ops);
    assertEquals("Wrong number of ops", 2, ops.size());

    assertEquals("Operation name is wrong", "test10type1-oper1-id", ops.get(0).getId());
    assertEquals("Operation command is wrong", "test10type1-oper1-cmd", ops.get(0).getCommand());

    assertEquals("Operation name is wrong", "test10type1-oper2-id", ops.get(1).getId());
    assertEquals("Operation command is wrong", "test10type1-oper2-cmd", ops.get(1).getCommand());
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
  
  public static class Tst8PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      //need to set source description - classloader needs it
      p.setSourceDescription("good-plugin1");
      p.addBean(new BeanDefinition("PluginOnlyClass", "org.pentaho.nowhere.PluginOnlyClass"));
      return Arrays.asList((IPlatformPlugin) p);
    }
  }
  
  public static class Tst9PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setName("test9Plugin");
      
      ContentGeneratorInfo cg1 = new ContentGeneratorInfo();
      cg1.setDescription("test 9 plugin description");
      cg1.setId("test9id");
      cg1.setType("test9type");
      cg1.setTitle("Test Generator 9");
      cg1.setUrl("/test9url");
      cg1.setClassname("org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1");
      cg1.setFileInfoGeneratorClassname("org.pentaho.test.platform.plugin.pluginmgr.FileInfoGenerator");
      p.addContentGenerator(cg1);
      
      ContentGeneratorInfo cg2 = new ContentGeneratorInfo();
      cg2.setDescription("test 9b plugin description");
      cg2.setId("test9bid");
      cg2.setType("test9type");
      cg2.setTitle("Test Generator 9b");
      cg2.setClassname("org.pentaho.test.platform.plugin.pluginmgr.ContentGenerator1");
      cg2.setFileInfoGeneratorClassname("org.pentaho.test.platform.plugin.pluginmgr.FileInfoGenerator");
      p.addContentGenerator(cg2);
      return Arrays.asList((IPlatformPlugin) p);
    }
  }
  
  public static class Tst10PluginProvider implements IPluginProvider {
    public List<IPlatformPlugin> getPlugins(IPentahoSession session) throws PlatformPluginRegistrationException {
      PlatformPlugin p = new PlatformPlugin();
      p.setName("test10Plugin");
      
      ContentInfo type = new ContentInfo();
      type.setDescription("test10type1-description");
      type.setExtension("test10type1-ext");
      type.setMimeType("test10type1-mimeType");
      type.setTitle("test10type1-title");
      type.setIconUrl("test10type1-url");
      
      type.addOperation(new PluginOperation("test10type1-oper1-id", "test10type1-oper1-cmd"));
      type.addOperation(new PluginOperation("test10type1-oper2-id", "test10type1-oper2-cmd"));
      
      p.addContentInfo(type);
      
      type = new ContentInfo();
      type.setExtension("test10type2-ext");
      p.addContentInfo(type);
      
      return Arrays.asList((IPlatformPlugin) p);
    }
  }
}
