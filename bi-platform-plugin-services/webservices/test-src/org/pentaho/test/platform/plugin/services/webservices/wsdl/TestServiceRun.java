package org.pentaho.test.platform.plugin.services.webservices.wsdl;

import java.rmi.RemoteException;

import junit.framework.TestCase;

public class TestServiceRun extends TestCase {

  public void testServiceRun() throws RemoteException {
    ServiceStub.ComplexType complex = new ServiceStub.ComplexType();
    complex.setName( "fred" ); //$NON-NLS-1$
    
    ServiceStub.GetDetails getDetails = new ServiceStub.GetDetails();
    
    getDetails.setObject( complex );
    
    ServiceStub stub = new ServiceStub();
    
    ServiceStub.GetDetailsResponse response = stub.getDetails(getDetails);
    
    ServiceStub.ComplexType returnValue = response.get_return();
    assertNotNull( returnValue );
  }
  
}
