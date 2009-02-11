package org.pentaho.test.platform.plugin.services.webservices.wsdl;

import junit.framework.TestCase;

public class TestServiceRun extends TestCase {

  public static void main( String args[] ) throws Exception {
    

    TestServiceStub.ComplexType complex = new TestServiceStub.ComplexType();
    complex.setName( "fred" ); //$NON-NLS-1$
    
    TestServiceStub.GetDetails getDetails = new TestServiceStub.GetDetails();
    
    getDetails.setObject( complex );
    
    TestServiceStub stub = new TestServiceStub();
    
    TestServiceStub.GetDetailsResponse response = stub.getDetails(getDetails);
    
    TestServiceStub.ComplexType returnValue = response.get_return();
    assertNotNull( returnValue );
    
    
  }
  
}
