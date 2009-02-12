package org.pentaho.test.platform.plugin.services.webservices;

import java.util.ArrayList;

import org.pentaho.platform.plugin.services.webservices.BaseWebServiceWrapper;


public class StubService2Wrapper extends BaseWebServiceWrapper {

  public StubService2Wrapper() {
    setEnabled( false );
  }

  public Class getServiceClass() {
    return StubService2.class;
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
