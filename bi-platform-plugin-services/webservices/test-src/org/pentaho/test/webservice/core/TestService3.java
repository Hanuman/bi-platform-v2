package org.pentaho.test.webservice.core;

public class TestService3 {

  public static String str;
  
  public void setString( String str ) {
    TestService3.str = str;
  }
  
  public String getString() {
    return "test result"; //$NON-NLS-1$
  }
  
}
