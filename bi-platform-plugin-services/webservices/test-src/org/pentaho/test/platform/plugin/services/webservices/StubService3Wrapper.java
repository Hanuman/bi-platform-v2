package org.pentaho.test.platform.plugin.services.webservices;

import java.util.ArrayList;

import org.pentaho.platform.plugin.services.webservices.BaseWebServiceWrapper;


public class StubService3Wrapper extends BaseWebServiceWrapper {

  public Class getServiceClass() {
    return StubService3.class;
  }

  public String getTitle() {
    return "test title 3"; //$NON-NLS-1$
  }

  public String getDescription() {
    return "test description 3"; //$NON-NLS-1$
  }

  @Override
  protected ArrayList<Class> getExtraClasses() {
    return null;
  }

}
