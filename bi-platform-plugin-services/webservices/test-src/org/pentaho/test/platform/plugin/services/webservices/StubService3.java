package org.pentaho.test.platform.plugin.services.webservices;

public class StubService3 {

  public static String str;
  
  public void setString( String str ) {
    StubService3.str = str;
  }
  
  public String getString() {
    return "test result"; //$NON-NLS-1$
  }
  
}
