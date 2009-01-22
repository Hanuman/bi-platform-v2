package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;

public class PluginResourceLoaderTest {
  
  private PluginResourceLoader resLoader;
  private Class<?> pluginClass;
  
  @Before
  public void init() throws ClassNotFoundException {
    resLoader = new PluginResourceLoader();
    PluginClassLoader classLoader = new PluginClassLoader(new File("./plugin-mgr/test-res"), this);
//    resLoader.setRootDir(new File("./plugin-mgr/test-res"));
    pluginClass = classLoader.loadClass("PluginResLoaderDummyClass");
  }
  
  @Test
  public void testGetResourceAsStream_fromFileSystem() throws IOException {
    InputStream in = resLoader.getResourceAsStream(pluginClass, "pluginResourceTest.properties");
    assertNotNull(in);
  }
  
  @Test(expected=FileNotFoundException.class)
  public void testGetResourceAsStream_FileDNE() throws IOException {
    InputStream in = resLoader.getResourceAsStream(pluginClass, "non-existent-file");
    assertNotNull(in);
  }
  
  @Test
  public void ItestGetResource_fromClassLoader() throws ClassNotFoundException, IOException {
    //find a properties file included in a jar
    assertNotNull("Could not find the properties file embededd in the jar", resLoader.getResourceAsStream(pluginClass, "pluginResourceTest-injar.properties"));
    //find a properties file at the classloader root directory
    assertNotNull("Could not find the properties file on the classloader root dir", resLoader.getResourceAsStream(pluginClass, "pluginResourceTest.properties"));
  }
  
  @Test
  public void testPluginPath() {
    String path = resLoader.getPluginPath( pluginClass );
    assertTrue( "Plugin path is not correct", path.endsWith( "plugin-mgr/test-res" ) ); //$NON-NLS-2$
  }
}
