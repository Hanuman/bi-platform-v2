package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.PlatformPluginRegistrationException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.PluginMessageLogger;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class SystemPathPluginProviderTest {

  @Test
  public void testLoad_Good() throws PlatformPluginRegistrationException {
    MicroPlatform miniPlatform = new MicroPlatform("plugin-mgr/test-res/SystemPathPluginProviderTest/");
    miniPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);

    miniPlatform.init();

    SystemPathXmlPluginProvider provider = new SystemPathXmlPluginProvider();
    StandaloneSession testSession = new StandaloneSession();

    PluginMessageLogger.clear();

    List<IPlatformPlugin> plugins = provider.getPlugins(testSession);

    //should successfully load good-plugin1 and good-plugin2 and not load bad-plugin.  The fact 
    //that bad-plugin does not load should not prevent the good ones from being loaded
    assertTrue("plugin1 was not found", CollectionUtils.exists(plugins, new PluginMatcherPredicate("Plugin 1")));
    assertTrue("plugin2 was not found", CollectionUtils.exists(plugins, new PluginMatcherPredicate("Plugin 2")));

    //make sure that the bad plugin caused an error message to be logged
    assertEquals("bad plugin did not log an error message", 1, PluginMessageLogger
        .count("SystemPathXmlPluginProvider.ERROR_0001"));

    for (String msg : PluginMessageLogger.getAll()) {
      System.err.println(msg);
    }
  }

  @Test(expected = PlatformPluginRegistrationException.class)
  public void testLoad_NoSolutionRepo() throws PlatformPluginRegistrationException {
    MicroPlatform miniPlatform = new MicroPlatform("plugin-mgr/test-res/SystemPathPluginProviderTest");

    miniPlatform.init();
    SystemPathXmlPluginProvider provider = new SystemPathXmlPluginProvider();
    provider.getPlugins(new StandaloneSession());
  }

  @Test(expected = PlatformPluginRegistrationException.class)
  public void testLoad_BadSolutionPath() throws PlatformPluginRegistrationException {
    MicroPlatform miniPlatform = new MicroPlatform("plugin-mgr/test-res/SystemPathPluginProviderTest/system");
    miniPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);

    miniPlatform.init();
    SystemPathXmlPluginProvider provider = new SystemPathXmlPluginProvider();
    provider.getPlugins(new StandaloneSession());
  }

  class PluginMatcherPredicate implements Predicate {
    private String pluginNameToMatch;

    public PluginMatcherPredicate(String pluginNameToMatch) {
      this.pluginNameToMatch = pluginNameToMatch;
    }

    public boolean evaluate(Object object) {
      return pluginNameToMatch.equals(((IPlatformPlugin) object).getName());
    }

  }
}
