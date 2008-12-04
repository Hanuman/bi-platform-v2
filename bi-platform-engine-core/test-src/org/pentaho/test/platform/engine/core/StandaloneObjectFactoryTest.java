package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.StandaloneObjectFactory;

import junit.framework.TestCase;

public class StandaloneObjectFactoryTest extends TestCase {

  public void testNoCreator() {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    try {
      factory.get( Object1.class , session1);
    assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testBadScope() {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "bogus"); //$NON-NLS-1$

    try {
      factory.get( Object1.class , session1);
    assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testImplementingClass() throws Exception {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "global"); //$NON-NLS-1$
    factory.addObject( "bogus2", "bogus", "global"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    Object1 obj = (Object1) factory.getObject(Object1.class.getSimpleName(), session1);
    assertNotNull( obj );
    
    assertEquals( Object1.class.getName(), factory.getImplementingClass( Object1.class.getSimpleName() ).getName() );
    
    assertNull( factory.getImplementingClass( "bogus" ) ); //$NON-NLS-1$

    assertNull( factory.getImplementingClass( "bogus2" ) ); //$NON-NLS-1$

  }
  
  public void testInit() throws Exception {

    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    StandaloneObjectFactory factory = new StandaloneObjectFactory();

    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "global"); //$NON-NLS-1$

    assertTrue( "Object is not defined", factory.objectDefined( Object1.class.getSimpleName() ) ); //$NON-NLS-1$
    Object1 obj1 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$

    factory.init(null, null);
    assertFalse( "Object is defined", factory.objectDefined( Object1.class.getSimpleName() ) ); //$NON-NLS-1$

    assertNull( factory.getImplementingClass( Object1.class.getSimpleName() ) );
    
    try {
      factory.get( Object1.class , session1);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testGlobalObject1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "global"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object1 obj2 = factory.get( Object1.class, session2);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$
    
  }
  
  public void testGlobalObject2() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object2.class.getSimpleName(), Object2.class.getName(), "global"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object2 obj2 = factory.get( Object2.class, session2);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$
    
  }
  
  public void testGlobalObjectFail() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), "bogus", "global"); //$NON-NLS-1$ //$NON-NLS-2$ 
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testSessionObject1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "session"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object1 obj2 = factory.get( Object1.class, session2);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1);
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$

  }
  
  public void testSessionObject2() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object2.class.getSimpleName(), Object2.class.getName(), "session"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$
    IPentahoSession session2 = new StandaloneSession( "test user 2" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object2 obj2 = factory.get( Object2.class, session2);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$
    
  }
  
  public void testSessionObjectFail1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), "bogus", "session"); //$NON-NLS-1$ //$NON-NLS-2$ 
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  
  public void testSessionObjectFail2() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "session"); //$NON-NLS-1$ 
        
    try {
      factory.get( Object1.class, null);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( IllegalArgumentException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }

  public void testLocalObject1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "local"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object1 obj2 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1);
    assertTrue( "Objects are same", obj1 != obj3 ); //$NON-NLS-1$

  }
  
  public void testLocalObject2() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object2.class.getSimpleName(), Object2.class.getName(), "local"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object2 obj2 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are same", obj1 != obj2 ); //$NON-NLS-1$

    Object2 obj3 = factory.get( Object2.class, session1);
    assertTrue( "Objects are same", obj1 != obj3 ); //$NON-NLS-1$
    
  }
  
  public void testLocalObjectFail1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), "bogus", "local"); //$NON-NLS-1$ //$NON-NLS-2$ 
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }

  public void testThreadObject1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), Object1.class.getName(), "thread"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object1 obj1 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object1 obj2 = factory.get( Object1.class, session1);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

    Object1 obj3 = factory.get( Object1.class, session1);
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$

  }
  
  public void testThreadObject2() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object2.class.getSimpleName(), Object2.class.getName(), "thread"); //$NON-NLS-1$
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    Object2 obj1 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj1 ); //$NON-NLS-1$
    
    Object2 obj2 = factory.get( Object2.class, session1);
    assertNotNull( "Object is null", obj2 ); //$NON-NLS-1$
    
    assertTrue( "Objects are not same", obj1 == obj2 ); //$NON-NLS-1$

    Object2 obj3 = factory.get( Object2.class, session1);
    assertTrue( "Objects are not same", obj1 == obj3 ); //$NON-NLS-1$
    
  }
  
  public void testThreadObjectFail1() throws Exception {
    
    StandaloneObjectFactory factory = new StandaloneObjectFactory();
    
    factory.addObject( Object1.class.getSimpleName(), "bogus", "thread"); //$NON-NLS-1$ //$NON-NLS-2$ 
        
    IPentahoSession session1 = new StandaloneSession( "test user 1" ); //$NON-NLS-1$

    try {
      factory.get( Object1.class, session1);
      assertFalse( "exception expected", true ); //$NON-NLS-1$
    } catch ( ObjectFactoryException e ) {
      assertTrue( "exception expected", true ); //$NON-NLS-1$
    }
    
  }
  

}
