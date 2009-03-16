package org.pentaho.platform.api.engine;

public interface IPentahoDefinableObjectFactory extends IPentahoObjectFactory {

  public static enum Scope { GLOBAL, SESSION, REQUEST, THREAD, LOCAL };

  public void defineObject( String key, String className, Scope scope );
  
  public void defineObject( String key, String className, Scope scope, ClassLoader loader );

}
