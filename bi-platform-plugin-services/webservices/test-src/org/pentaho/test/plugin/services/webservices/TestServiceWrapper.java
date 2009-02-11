package org.pentaho.test.plugin.services.webservices;

import java.util.ArrayList;

import org.pentaho.platform.plugin.services.webservices.BaseWebServiceWrapper;


public class TestServiceWrapper extends BaseWebServiceWrapper {

  public Class getServiceClass() {
    return TestService.class;
  }

  public String getTitle() {
    return "test title"; //$NON-NLS-1$
  }

  public String getDescription() {
    return "test description"; //$NON-NLS-1$
  }
  
  @Override
  protected ArrayList<Class> getExtraClasses() {
    ArrayList<Class> extraClasses = new ArrayList<Class>();
    extraClasses.add( ComplexType.class );
    return extraClasses;
  }
}
