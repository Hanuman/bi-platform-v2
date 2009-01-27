package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.util.messages.LocaleHelper;

@RunWith(JMock.class)
public class PluginResourceLoaderTest {
  
  private Mockery mockery = new Mockery();

  
  private PluginResourceLoader resLoader;
  private Class<?> pluginClass;
  private PluginClassLoader classLoader;
  
  @Before
  public void init() throws ClassNotFoundException {
    resLoader = new PluginResourceLoader();
    classLoader = new PluginClassLoader(new File("./plugin-mgr/test-res/PluginResourceLoaderTest"), this);
    pluginClass = classLoader.loadClass("PluginResLoaderDummyClass");
  }
  
  @Test
  public void testGetResource_fromFileSystem() throws UnsupportedEncodingException {
    InputStream in = resLoader.getResourceAsStream(pluginClass, "pluginResourceTest.properties");
    assertNotNull("Failed to get resource as stream", in);
    
    byte[] bytes = resLoader.getResourceAsBytes(pluginClass, "pluginResourceTest.properties");
    assertNotNull("Failed to get resource as bytes", bytes);
    
    String s = resLoader.getResourceAsString(pluginClass, "pluginResourceTest.properties");
    assertNotNull("Failed to get resource as string", s);
    
    s = resLoader.getResourceAsString(pluginClass, "pluginResourceTest.properties", "UTF-8");
    assertNotNull("Failed to get resource as string", s);
    
    //load a resource from a subdirectory
    in = resLoader.getResourceAsStream(pluginClass, "resources/pluginResourceTest-inresources.properties");
    assertNotNull("Failed to get resource as stream", in);
  }
  
  @Test
  public void testGetResource_FileDNE() throws UnsupportedEncodingException {
    InputStream in = resLoader.getResourceAsStream(pluginClass, "non-existent-file");
    assertNull("InputStream should have been null indicating resource not found", in);
    
    byte[] bytes = resLoader.getResourceAsBytes(pluginClass, "non-existent-file");
    assertNull("byte array should have been null indicating resource not found", bytes);
    
    String s = resLoader.getResourceAsString(pluginClass, "non-existent-file");
    assertNull("InputStream should have been null indicating resource not found", s);
  }
  
  @Test(expected=UnsupportedEncodingException.class)
  public void testBadStringEncoding() throws UnsupportedEncodingException {
    String s = resLoader.getResourceAsString(pluginClass, "pluginResourceTest.properties", "bogus encoding");
  }

  @Test
  public void testGetResourceBundleFromInsideJar() {
    ResourceBundle.getBundle("pluginResourceTest-injar", LocaleHelper.getLocale(), classLoader);
  }
  
  @Test
  public void testGetResourceBundleFromResourcesDir() {
    //this properties file lives in the "resources" directory under the plugin root dir
    
    //test that retrieving a resource bundle works the same by in the resource loader and the java ResourceBundle api
    ResourceBundle.getBundle("resources/pluginResourceTest-inresources", LocaleHelper.getLocale(), classLoader);
    ResourceBundle.getBundle("resources.pluginResourceTest-inresources", LocaleHelper.getLocale(), classLoader);
    
    resLoader.getResourceBundle(pluginClass, "resources/pluginResourceTest-inresources");
    resLoader.getResourceBundle(pluginClass, "resources.pluginResourceTest-inresources");
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
    String path = resLoader.getSystemRelativePluginPath( pluginClass );
    assertTrue( "Plugin path is not correct", path.endsWith( "plugin-mgr/test-res/PluginResourceLoaderTest" ) ); //$NON-NLS-2$
  }
  
  @Test
  public void testGetPluginSettings() {
    
    final ISystemSettings mockSettings = mockery.mock(ISystemSettings.class);
    
    final String fullPathToSettingsFile = resLoader.getSystemRelativePluginPath(pluginClass)+"/settings.xml";
    
    mockery.checking(new Expectations() {{
      oneOf(mockSettings).getSystemSetting(fullPathToSettingsFile, "testsetting", null); will(returnValue("false"));
      oneOf(mockSettings).getSystemSetting(fullPathToSettingsFile, "bogussetting", null); will(returnValue(null));
      oneOf(mockSettings).getSystemSetting(fullPathToSettingsFile, "bogussetting", "true"); will(returnValue("true"));
    }});
    
    PentahoSystem.setSystemSettingsService( mockSettings );
    
    
    assertEquals("Cache value incorrect", "false", resLoader.getPluginSetting(pluginClass, "testsetting"));
    
    assertNull("Bogus value should not have been found", resLoader.getPluginSetting(pluginClass, "bogussetting" ));
    
    assertEquals("Bogus value should have a default of true", "true", resLoader.getPluginSetting(pluginClass, "bogussetting", "true" ));
  }
  
  @Test
  public void testFindResources() {
    List<URL> urls = resLoader.findResources(pluginClass, "*properties*");
    boolean propFile1Found = false, propFile2Found = false;
    for( URL url : urls) {
      String fileName = url.getPath().substring(1 + url.getPath().lastIndexOf(File.separator));
      if("pluginResourceTest-inresources.properties".equals(fileName)) {
        propFile1Found = true;
      }
      if("pluginResourceTest.properties".equals(fileName)) {
        propFile2Found = true;
      }
    }
    assertTrue("pluginResourceTest-inresources.properties was not found", propFile1Found);
    assertTrue("pluginResourceTest.properties was not found", propFile2Found);
  }
}
