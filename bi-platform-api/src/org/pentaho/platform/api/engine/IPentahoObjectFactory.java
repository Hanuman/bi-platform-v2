package org.pentaho.platform.api.engine;

import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoSession;

public interface IPentahoObjectFactory {

  public Object getObject( String key, final IPentahoSession session ) throws ObjectFactoryException;
  public boolean hasObject( String key );
  public void setObjectCreators( Map objectMap );
}
