package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginProvider;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.services.pluginmgr.PluginManager;
import org.pentaho.platform.plugin.services.pluginmgr.SystemPathXmlPluginProvider;
import org.pentaho.platform.repository.solution.filebased.FileBasedSolutionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;

@SuppressWarnings("nls")
public class PluginManagerTest {

  @Before
  public void init() {
    MicroPlatform microPlatform = new MicroPlatform("plugin-mgr/test-res/PluginManagerTest/");
    microPlatform.define(ISolutionRepository.class, FileBasedSolutionRepository.class);
    microPlatform.define(IPluginProvider.class, SystemPathXmlPluginProvider.class);

    microPlatform.init();
  }

  @Test
  public void testReload() {
    StandaloneSession session = new StandaloneSession();
    IPluginManager pluginManager = new PluginManager();
    pluginManager.reload(session);

    //one of the plugins serves a content generator with id=test1.  Make sure we can load it.
    assertNotNull("The plugin serving content generator with id=test1 was not loaded", pluginManager
        .getContentGeneratorInfo("test1", session));
  }

}
