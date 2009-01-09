package org.pentaho.test.platform.engine.core;

public class BadObjectRuntime {

  public BadObjectRuntime() {
    throw new RuntimeException( "BadObjectRuntime constructor" ); //$NON-NLS-1$
  }
  
}
