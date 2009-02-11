package org.pentaho.test.plugin.services.webservices;

import junit.framework.TestCase;

import org.pentaho.platform.plugin.webservices.AxisConfig;

public class BaseClassesError2Test extends TestCase {
  
  public void testBadInit4() {
    
    AxisConfig config = AxisConfig.getInstance( new TestServiceSetup3() );
    assertNull( config );
  }
  
  
}
