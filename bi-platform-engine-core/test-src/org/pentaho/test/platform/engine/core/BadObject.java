package org.pentaho.test.platform.engine.core;

import org.pentaho.platform.api.engine.ComponentException;

public class BadObject {

  public BadObject() throws ComponentException {
    throw new ComponentException( "BadObject constructor" ); //$NON-NLS-1$
  }
  
}
