package org.pentaho.test.platform.engine.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ISolutionEngine;
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
    SolutionClassLoader loader = new SolutionClassLoader( "lib", this ); //$NON-NLS-1$
    
    // test the byte array first
    InputStream in = loader.getResourceAsStream( "org.pentaho.platform.util.StringUtil" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "method is missing", classBytes.contains( "tokenStringToArray" )); //$NON-NLS-1$ //$NON-NLS-2$
    
    // now try getting it as a class
    Class testClass = loader.loadClass("org.pentaho.platform.util.StringUtil"); //$NON-NLS-1$
    
    assertNotNull( "class is null", testClass ); //$NON-NLS-1$
    
    assertEquals( "wrong class", "org.pentaho.platform.util.StringUtil", testClass.getName() ); //$NON-NLS-1$ //$NON-NLS-2$
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
    // now load a properties file
    SolutionClassLoader loader = new SolutionClassLoader( "lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "kettle-steps.xml" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String xml = new String( b );
    assertTrue( "message is missing", xml.contains( "<steps>" )); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public void testLoadProperties() throws IOException {
    // now load a properties file
    SolutionClassLoader loader = new SolutionClassLoader( "lib", this ); //$NON-NLS-1$
    
    InputStream in = loader.getResourceAsStream( "org.pentaho.platform.util.messages.messages.properties" ); //$NON-NLS-1$
    assertNotNull( "input stream is null", in ); //$NON-NLS-1$
    
    byte b[] = toBytes( in );
    String classBytes = new String( b );
    assertTrue( "message is missing", classBytes.contains( "Logger.CODE_LOG_UNKNOWN" )); //$NON-NLS-1$ //$NON-NLS-2$
    
  }
  
}
