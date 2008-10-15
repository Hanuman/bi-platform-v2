package org.pentaho.platform.api.engine;

@Deprecated //IPentahoObjectFactory is no longer created via a factory
public interface IObjectFactoryCreator {

  public void configure( String configFilePath ) throws ObjectFactoryException;
  public IPentahoObjectFactory getFactory();
}
