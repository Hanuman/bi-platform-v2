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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
//import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;

@SuppressWarnings("nls")
public class PluginClassLoaderTest {
  
  private PluginClassLoader loader;
  
  @Before
  public void init() {
    // now load a class
    loader = new PluginClassLoader( new File("./plugin-mgr/test-res/plugin-classloader-test/"), this ); 
  }

  @Test
  public void testLoadClass() throws IOException, ClassNotFoundException {
    // test the byte array first
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader" ); 
    assertNotNull( "Could not find class TestClassForClassloader in jar file", in ); 
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "method is missing", classBytes.contains( "org/pentaho/test/platform/engine/services/TestClassForClassloader" ));  
    
    // now try getting it as a class
    Class testClass = loader.loadClass("org.pentaho.test.platform.engine.services.TestClassForClassloader"); 
    assertNotNull( "class is null", testClass );
    assertEquals( "wrong class", "org.pentaho.test.platform.engine.services.TestClassForClassloader", testClass.getName() );  
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
    InputStream in = loader.getResourceAsStream( "test1.xml" ); 
    assertNotNull( "input stream is null", in ); 
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" ));  
  }
  
  @Test
  public void testLoadBadResource() throws IOException {
    InputStream in = loader.getResourceAsStream( "bogus.xml" ); 
    assertNull( "input stream should be null", in ); 
  }
  
  @Test
  public void testLoadBadClass() throws IOException {
    // now try getting it as a class
    try {
      loader.loadClass("bogus"); 
      assertFalse( "Exception expected", true ); 
    } catch ( ClassNotFoundException e ) {
      assertTrue( "Exception expected", true ); 
    }
  }
  
  @Test
  public void testLoadProperties_fromJar() throws IOException {
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.test.properties" ); 
    assertNotNull( "input stream is null", in ); 
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "test_setting=test" ));  
    
  }
  
  @Test
  public void testLoadProperties_fromDir() throws IOException {
    InputStream in = loader.getResourceAsStream( "resources/plugin-classloader-test-inresourcesdir.properties" ); 
    assertNotNull( "input stream is null", in ); 
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "name=" ));  
    
  }
  
//  @Test
//  public void urlTest() throws IOException {
//    URL url = new URL("file:/home/aaron/workspaces/pentaho/bi-platform-plugin-services/plugin-mgr/test-res/plugin-classloader-test/resources/plugin-classloader-test-inresourcesdir.properties");
//    System.err.println("trying "+url);
//    url.openConnection().connect();
//  }

  @Test
  public void testFindXmlResource() throws IOException {
    URL url = loader.getResource( "test1.xml" ); 
    
    assertNotNull( "URL is null", url ); 

    InputStream in = url.openStream();
    assertNotNull( "input stream is null", in ); 
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" ));  

  }

  @Test
  public void testFindClassResource() throws IOException {
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader.class" ); 
    
    assertNotNull( "input stream is null", in ); 
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "TestClassForClassloader" ));  

  }
  
  @Test
  public void testFindBadResource() throws IOException {
    URL url = loader.getResource( "bogus.xml" ); 
    
    assertNull( "URL should be null", url ); 
    
  }

  @Test
  public void testFindResources() throws IOException {
    Enumeration<URL> urls = loader.getResources( "test1.xml" ); 
    
    assertNotNull( "URLS is null",  urls ); 
    
    int count = 0;
    while( urls.hasMoreElements() ) {
      URL url = urls.nextElement();
      InputStream in = url.openStream();
      assertNotNull( "input stream is null", in ); 
      
      byte b[] = toBytes( in );
      String xml = new String( b );
      assertTrue( "xml is wrong", xml.contains( "<test1>" ));  
      count++;
    }

    assertEquals( "Wrong number of URLS", 1, count ); 
  }

  @Test
  public void testFindBadResources() throws IOException {
    Enumeration<URL> urls = loader.getResources( "bogus.xml" ); 
    
    assertNotNull( "URLS is null",  urls ); 
    
    int count = 0;
    while( urls.hasMoreElements() ) {
      count++;
    }

    assertEquals( "Wrong number of URLS", 0, count ); 
  }

  @Test
  public void testCatalog() throws ClassNotFoundException {
    loader.loadClass("org.pentaho.test.platform.engine.services.TestClassForClassloader" ); 
    
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
