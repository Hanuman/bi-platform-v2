package org.pentaho.test.platform.engine.core;

import java.io.File;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

import junit.framework.TestCase;

@SuppressWarnings({"all"})
public class StandaloneSpringPentahoObjectFactoryTest extends TestCase {

  public void testInitFromXml() throws Exception {
    
    StandaloneSession session = new StandaloneSession();
    StandaloneSession session2 = new StandaloneSession();
    
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    factory.init("test-src/solution/system/pentahoObjects.spring.xml", null );
    
    IPentahoSession session3 = factory.get(IPentahoSession.class, "systemStartupSession", session);
    assertNotNull( session3 );
    
    GoodObject goodObject = factory.get( GoodObject.class, session);
    assertNotNull( goodObject );
    
    try {
      factory.get(BadObject.class, null);
      assertFalse( true );
    } catch (ObjectFactoryException e) {
      
    }
    
    
    GoodObject goodObject1 = factory.get(GoodObject.class, session);
    GoodObject goodObject2 = factory.get(GoodObject.class, session);
    
    assertEquals( goodObject1, goodObject2 );
    assertEquals( session, goodObject1.initSession );
    
    GoodObject goodObject3 = factory.get(GoodObject.class, session2);
    assertNotSame( goodObject1, goodObject3 );
    assertEquals( session2, goodObject3.initSession );
    
  }
  
  public void testInitFromObject() throws Exception {
    
    StandaloneSession session = new StandaloneSession();
    
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    
    File f = new File("test-src/solution/system/pentahoObjects.spring.xml");
    FileSystemResource fsr = new FileSystemResource(f);
    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);
    xmlReader.loadBeanDefinitions(fsr);
    
    factory.init("test-src/solution/system/pentahoObjects.spring.xml", appCtx );
    
    IPentahoSession session2 = factory.get(IPentahoSession.class, "systemStartupSession", session);
    assertNotNull( session2 );
    
    assertEquals( SystemStartupSession.class, factory.getImplementingClass( "systemStartupSession" ) );
    
    Object obj = factory.get(Object.class, "systemStartupSession", session);
    assertNotNull( obj );

    assertTrue( obj instanceof IPentahoSession );
    
  }
  
  public void testNoInit() throws Exception {
    
    StandaloneSession session = new StandaloneSession();
    
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    
    try {
      IPentahoSession session2 = factory.get(IPentahoSession.class, "systemStartupSession", session);
      assertNotNull( session2 );
      assertFalse( true );
    } catch (ObjectFactoryException e) {
      assertTrue( true );
    }
    
  }

  public void testBadInit() throws Exception {
    
    StandaloneSession session = new StandaloneSession();
    
    StandaloneSpringPentahoObjectFactory factory = new StandaloneSpringPentahoObjectFactory( );
    
    try {
      factory.init( null, "bogus" );
      assertFalse( true );
    } catch (IllegalArgumentException e) {
      assertTrue( true );
    }
    
    try {
      IPentahoSession session2 = factory.get(IPentahoSession.class, "systemStartupSession", session);
      assertNotNull( session2 );
      assertFalse( true );
    } catch (ObjectFactoryException e) {
      assertTrue( true );
    }
    
  }
  
}
