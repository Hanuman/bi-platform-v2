package org.pentaho.test.platform.plugin.pluginmgr;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;

public class PluginClassLoaderTest {
  
  private PluginClassLoader loader;
  
  @Before
  public void init() {
    // now load a class
    loader = new PluginClassLoader( new File("./plugin-mgr/test-res/plugin-classloader-test/"), this ); //$NON-NLS-1$
  }

  @Test
  public void testLoadClass() throws IOException, ClassNotFoundException {
    // test the byte array first
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader" ); //$NON-NLS-1$
    assertNotNull( "Could not find class TestClassForClassloader in jar file", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "method is missing", classBytes.contains( "org/pentaho/test/platform/engine/services/TestClassForClassloader" )); //$NON-NLS-1$ //$NON-NLS-2$
    
    // now try getting it as a class
    Class testClass = loader.loadClass("org.pentaho.test.platform.engine.services.TestClassForClassloader"); //$NON-NLS-1$
    assertNotNull( "class is null", testClass ); //$NON-NLS-1$
    assertEquals( "wrong class", "org.pentaho.test.platform.engine.services.TestClassForClassloader", testClass.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  private byte[] toBytes( InputStream in ) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte b[] = new byte[2048];
    int n = in.read(b);
    while( n != -1 ) {
      out.write(b, 0, n);
      n = in.read(b);
    }
    return out.toByteArray();
  }
  
  @Test
  public void testLoadXml() throws IOException {
    InputStream in = loader.getResourceAsStream( "test1.xml" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" )); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  @Test
  public void testLoadBadResource() throws IOException {
    InputStream in = loader.getResourceAsStream( "bogus.xml" ); //$NON-NLS-1$
    assertNull( "input stream should be null", in ); //$NON-NLS-1$
  }
  
  @Test
  public void testLoadBadClass() throws IOException {
    // now try getting it as a class
    try {
      loader.loadClass("bogus"); //$NON-NLS-1$
      assertFalse( "Exception expected", true ); //$NON-NLS-1$
    } catch ( ClassNotFoundException e ) {
      assertTrue( "Exception expected", true ); //$NON-NLS-1$
    }
  }
  
  @Test
  public void testLoadProperties_fromJar() throws IOException {
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.test.properties" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "test_setting=test" )); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
  @Test
  public void testLoadProperties_fromDir() throws IOException {
    InputStream in = loader.getResourceAsStream( "resources/plugin-classloader-test-inresourcesdir.properties" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "name=" )); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
//  @Test
//  public void urlTest() throws IOException {
//    URL url = new URL("file:/home/aaron/workspaces/pentaho/bi-platform-plugin-services/plugin-mgr/test-res/plugin-classloader-test/resources/plugin-classloader-test-inresourcesdir.properties");
//    System.err.println("trying "+url);
//    url.openConnection().connect();
//  }

  @Test
  public void testFindXmlResource() throws IOException {
    URL url = loader.getResource( "test1.xml" ); //$NON-NLS-1$
    
    assertNotNull( "URL is null", url ); //$NON-NLS-1$

    InputStream in = url.openStream();
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" )); //$NON-NLS-1$ //$NON-NLS-2$

  }

  @Test
  public void testFindClassResource() throws IOException {
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader.class" ); //$NON-NLS-1$
    
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "TestClassForClassloader" )); //$NON-NLS-1$ //$NON-NLS-2$

  }
  
  @Test
  public void testFindBadResource() throws IOException {
    URL url = loader.getResource( "bogus.xml" ); //$NON-NLS-1$
    
    assertNull( "URL should be null", url ); //$NON-NLS-1$
    
  }

  @Test
  public void testFindResources() throws IOException {
    Enumeration<URL> urls = loader.getResources( "test1.xml" ); //$NON-NLS-1$
    
    assertNotNull( "URLS is null",  urls ); //$NON-NLS-1$
    
    int count = 0;
    while( urls.hasMoreElements() ) {
      URL url = urls.nextElement();
      InputStream in = url.openStream();
      assertNotNull( "input stream is null", in ); //$NON-NLS-1$
      
      byte b[] = toBytes( in );
      String xml = new String( b );
      assertTrue( "xml is wrong", xml.contains( "<test1>" )); //$NON-NLS-1$ //$NON-NLS-2$
      count++;
    }

    assertEquals( "Wrong number of URLS", 1, count ); //$NON-NLS-1$
  }

  @Test
  public void testFindBadResources() throws IOException {
    Enumeration<URL> urls = loader.getResources( "bogus.xml" ); //$NON-NLS-1$
    
    assertNotNull( "URLS is null",  urls ); //$NON-NLS-1$
    
    int count = 0;
    while( urls.hasMoreElements() ) {
      count++;
    }

    assertEquals( "Wrong number of URLS", 0, count ); //$NON-NLS-1$
  }

  @Test
  public void testCatalog() throws ClassNotFoundException {
    List<String> jarNames = loader.listLoadedJars();

    assertNotNull( jarNames );
    assertEquals( 1, jarNames.size() );
    assertTrue("test-jar.jar not found in classloader", StringUtils.contains(jarNames.get(0), "test-jar.jar"));
  }
  
  @Test
  public void testIsPluginClass() throws ClassNotFoundException {
    Class testClass = loader.loadClass("org.pentaho.test.platform.engine.services.TestClassForClassloader");
    assertTrue("Class should have been identified as a plugin class", loader.isPluginClass(testClass));
    
    assertFalse("Class should NOT have been identified as a plugin class", loader.isPluginClass(String.class));
  }
}
