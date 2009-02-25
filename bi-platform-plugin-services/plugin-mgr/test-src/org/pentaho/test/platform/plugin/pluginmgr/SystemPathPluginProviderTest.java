package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.engine.IPlatformPlugin.BeanDefinition;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class SystemPathPluginProviderTest {
  private MicroPlatform microPlatform = null;

  SystemPathXmlPluginProvider provider = null;

  @Before
  public void init() {
    microPlatform = new MicroPlatform("plugin-mgr/test-res/SystemPathPluginProviderTest/");

    provider = new SystemPathXmlPluginProvider();
  }

  @Test
  public void testLoad_Good() throws PlatformPluginRegistrationException {
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class).init();

    PluginMessageLogger.clear();

    List<IPlatformPlugin> plugins = provider.getPlugins(new StandaloneSession());

    //should successfully load good-plugin1 and good-plugin2 and not load bad-plugin.  The fact 
    //that bad-plugin does not load should not prevent the good ones from being loaded
    assertTrue("plugin1 was not found", CollectionUtils.exists(plugins, new PluginNameMatcherPredicate("Plugin 1")));
    assertTrue("plugin2 was not found", CollectionUtils.exists(plugins, new PluginNameMatcherPredicate("Plugin 2")));

    //make sure that the bad plugin caused an error message to be logged
    assertEquals("bad plugin did not log an error message", 1, PluginMessageLogger
        .count("SystemPathXmlPluginProvider.ERROR_0001"));

    for (String msg : PluginMessageLogger.getAll()) {
      System.err.println(msg);
    }
  }

  @Test(expected = PlatformPluginRegistrationException.class)
  public void testLoad_NoSolutionRepo() throws PlatformPluginRegistrationException {
    microPlatform.init();

    provider.getPlugins(new StandaloneSession());
  }

  @Test(expected = PlatformPluginRegistrationException.class)
  public void testLoad_BadSolutionPath() throws PlatformPluginRegistrationException {
    MicroPlatform mp = new MicroPlatform("plugin-mgr/test-res/SystemPathPluginProviderTest/system");
    mp.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    mp.init();

    provider.getPlugins(new StandaloneSession());
  }

  class PluginNameMatcherPredicate implements Predicate {
    private String pluginNameToMatch;

    public PluginNameMatcherPredicate(String pluginNameToMatch) {
      this.pluginNameToMatch = pluginNameToMatch;
    }

    public boolean evaluate(Object object) {
      return pluginNameToMatch.equals(((IPlatformPlugin) object).getName());
    }

  }

  @Test
  public void tesLoadtLifecycleListener() throws PlatformPluginRegistrationException {
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class).init();

    PluginMessageLogger.clear();

    List<IPlatformPlugin> plugins = provider.getPlugins(new StandaloneSession());

    //first make sure Plugin 1 was loaded, otherwise our check for lifcycle class will never happen
    assertTrue("plugin1 was not found", CollectionUtils.exists(plugins, new PluginNameMatcherPredicate("Plugin 1")));

    for (IPlatformPlugin plugin : plugins) {
      if (plugin.getName().equals("Plugin 1")) {
        assertEquals("org.pentaho.test.platform.plugin.pluginmgr.FooInitializer", plugin
            .getLifecycleListenerClassname());
      }
      if (plugin.getName().equals("Plugin 2")) {
        //no listener defined to for Plugin 2
        assertNull(plugin.getLifecycleListenerClassname());
      }
    }
  }

  @Test
  public void testLoadBeanDefinition() throws PlatformPluginRegistrationException {
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class).init();
    
    List<IPlatformPlugin> plugins = provider.getPlugins(new StandaloneSession());

    IPlatformPlugin plugin = (IPlatformPlugin) CollectionUtils.find(plugins, new PluginNameMatcherPredicate("Plugin 1"));
    assertNotNull("Plugin 1 should have been found", plugin);
    
    Collection<BeanDefinition> beans = plugin.getBeans();
    
    assertEquals("FooComponent was not loaded", 1, CollectionUtils.countMatches(beans, new Predicate() {
      public boolean evaluate(Object object) {
        BeanDefinition bean = (BeanDefinition)object;
        return bean.beanId.equals("FooComponent") && bean.classname.equals("org.pentaho.test.platform.plugin.pluginmgr.FooComponent");
      }
    }));
    assertEquals("genericBean was not loaded", 1, CollectionUtils.countMatches(beans, new Predicate() {
      public boolean evaluate(Object object) {
        BeanDefinition bean = (BeanDefinition)object;
        return bean.beanId.equals("genericBean") && bean.classname.equals("java.lang.Object");
      }
    }));
  }
  
  @Test
  public void testLoadLifeCycleListener() throws PlatformPluginRegistrationException {
    List<IPlatformPlugin> plugins = provider.getPlugins(new StandaloneSession());
    
    IPlatformPlugin plugin = (IPlatformPlugin) CollectionUtils.find(plugins, new PluginNameMatcherPredicate("Plugin 1"));
    assertNotNull("Plugin 1 should have been found", plugin);
    
    assertEquals("org.pentaho.test.platform.plugin.pluginmgr.FooInitializer", plugin.getLifecycleListenerClassname());
  }
}