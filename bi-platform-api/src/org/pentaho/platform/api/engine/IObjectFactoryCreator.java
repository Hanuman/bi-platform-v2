package org.pentaho.platform.api.engine;

public interface IObjectFactoryCreator {

  public void configure( String configFilePath ) throws ObjectFactoryException;
  public IPentahoObjectFactory getFactory();
}
