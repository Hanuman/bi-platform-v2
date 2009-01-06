package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SimpleSystemSettings;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory.Scope;
import org.pentaho.platform.engine.services.solution.SolutionClassLoader;
import org.pentaho.platform.engine.services.solution.SolutionEngine;

import junit.framework.TestCase;

public class SolutionClassLoaderTest extends TestCase {

  public void testLoadClass() throws IOException, ClassNotFoundException {
    
    // create an object factory
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    // specify the objects we will use
    factory.defineObject( ISolutionEngine.class.getSimpleName(), SolutionEngine.class.getName(), Scope.LOCAL );
    factory.defineObject( "systemStartupSession", SystemStartupSession.class.getName(), Scope.LOCAL ); //$NON-NLS-1$
    PentahoSystem.setObjectFactory( factory );

    // create a settings object.
    SimpleSystemSettings settings = new SimpleSystemSettings();
    settings.addSetting( "pentaho-system" , "" ); //$NON-NLS-1$ //$NON-NLS-2$
    PentahoSystem.setSystemSettingsService( settings );
    
    // specify the startup listeners
    List<IPentahoSystemListener> listeners = new ArrayList<IPentahoSystemListener>();
    PentahoSystem.setSystemListeners( listeners );

    // initialize the system
    PentahoSystem.init( new StandaloneApplicationContext(".", "") ); //$NON-NLS-1$ //$NON-NLS-2$

    // now load a class
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    // test the byte array first
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
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
  
  public void testLoadXml() throws IOException {
    // now load a xml file
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "test1.xml" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" )); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public void testLoadBadResource() throws IOException {
    // now load a xml file
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "bogus.xml" ); //$NON-NLS-1$
    assertNull( "input stream should be null", in ); //$NON-NLS-1$
    
  }
  
  public void testLoadBadClass() throws IOException {
    // now load a xml file
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    // now try getting it as a class
    try {
      loader.loadClass("bogus"); //$NON-NLS-1$
      assertFalse( "Exception expected", true ); //$NON-NLS-1$
    } catch ( ClassNotFoundException e ) {
      assertTrue( "Exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testLoadProperties() throws IOException {
    // now load a properties file
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.test.properties" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "property is missing", classBytes.contains( "test_setting=test" )); //$NON-NLS-1$ //$NON-NLS-2$
    
  }

  public void testFindXmlResource() throws IOException {

    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    URL url = loader.getResource( "test1.xml" ); //$NON-NLS-1$
    
    assertNotNull( "URL is null", url ); //$NON-NLS-1$

    InputStream in = url.openStream();
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "<test1>" )); //$NON-NLS-1$ //$NON-NLS-2$

  }

  public void testFindClassResource() throws IOException {

    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "org.pentaho.test.platform.engine.services.TestClassForClassloader.class" ); //$NON-NLS-1$
    
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "xml is wrong", xml.contains( "TestClassForClassloader" )); //$NON-NLS-1$ //$NON-NLS-2$

  }

  public void testFindBadResource() throws IOException {

    // now load a properties file
    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    URL url = loader.getResource( "bogus.xml" ); //$NON-NLS-1$
    
    assertNull( "URL should be null", url ); //$NON-NLS-1$
    
  }

  public void testFindResources() throws IOException {

    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
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

  public void testFindBadResources() throws IOException {

    SolutionClassLoader loader = new SolutionClassLoader( "test-jar-lib", this ); //$NON-NLS-1$
    
    Enumeration<URL> urls = loader.getResources( "bogus.xml" ); //$NON-NLS-1$
    
    assertNotNull( "URLS is null",  urls ); //$NON-NLS-1$
    
    int count = 0;
    while( urls.hasMoreElements() ) {
      count++;
    }

    assertEquals( "Wrong number of URLS", 0, count ); //$NON-NLS-1$
  }

  public void testCatalog() throws ClassNotFoundException {
    
    List<String> jarNames = SolutionClassLoader.listLoadedJars();

    assertNotNull( jarNames );
    assertEquals( 1, jarNames.size() );
    assertEquals( "test-jar-lib"+ISolutionRepository.SEPARATOR+"test-jar.jar", jarNames.get(0) ); //$NON-NLS-1$ //$NON-NLS-2$
        
  }
    
}
