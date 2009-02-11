package org.pentaho.test.plugin.services.webservices;

import java.util.ArrayList;

import org.pentaho.platform.plugin.webservices.BaseWebServiceWrapper;


public class TestService2Wrapper extends BaseWebServiceWrapper {

  public TestService2Wrapper() {
    setEnabled( false );
  }

  public Class getServiceClass() {
    return TestService2.class;
  }

  public String getTitle() {
    return null;
  }

  public String getDescription() {
    return null;
  }

  @Override
  protected ArrayList<Class> getExtraClasses() {
    return null;
  }

}
