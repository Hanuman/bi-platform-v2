package org.pentaho.test.platform.plugin.services.webservices;

import junit.framework.TestCase;

import org.pentaho.platform.plugin.services.webservices.AxisConfig;

public class BaseClassesError2Test extends TestCase {
  
  public void testBadInit4() {
    
    AxisConfig config = AxisConfig.getInstance( new StubServiceSetup3() );
    assertNull( config );
  }
  
  
}
