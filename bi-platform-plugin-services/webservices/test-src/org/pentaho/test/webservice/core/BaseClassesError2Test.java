package org.pentaho.test.webservice.core;

import junit.framework.TestCase;

import org.pentaho.webservice.core.AxisConfig;

public class BaseClassesError2Test extends TestCase {
  
  public void testBadInit4() {
    
    AxisConfig config = AxisConfig.getInstance( new TestServiceSetup3() );
    assertNull( config );
  }
  
  
}
