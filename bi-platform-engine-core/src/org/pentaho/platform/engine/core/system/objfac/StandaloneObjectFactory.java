/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Dec 3, 2008
 * @author James Dixon
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import java.util.HashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;

public class StandaloneObjectFactory implements IPentahoObjectFactory {

  private Map<String,ObjectCreator> creators = new HashMap<String,ObjectCreator>();
  
  public <T> T get(Class<T> interfaceClass, IPentahoSession session) throws ObjectFactoryException {
    return get(interfaceClass, interfaceClass.getSimpleName(), session);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> interfaceClass, String key, IPentahoSession session) throws ObjectFactoryException {
    return (T) retreiveObject(key, session);
  }

  public Class getImplementingClass(String key) {
    if( !objectDefined( key ) ) {
      return null;
    }
    ObjectCreator creator = creators.get(key);
    try {
      return creator.createClass();
    } catch (ObjectFactoryException e) {
      return null;
    }
  }

  public Object getObject(String key, IPentahoSession session) throws ObjectFactoryException {
    return retreiveObject(key, session);
  }

  public void init(String arg0, Object arg1) {
    creators = new HashMap<String,ObjectCreator>();
  }

  public boolean objectDefined(String key) {
    return creators.get(key) != null;
  }

  public void addObject( String key, String className, String scope ) {
    
    addObject( key, className, scope, getClass().getClassLoader() );
  }
  
  public void addObject( String key, String className, String scope, ClassLoader loader ) {
    ObjectCreator creator = new ObjectCreator( className, scope, loader );
    creators.put( key, creator );
  }
  
  protected Object retreiveObject( String key, IPentahoSession session ) throws ObjectFactoryException {

    ObjectCreator creator = creators.get(key);
    if( creator == null ) {
      throw new ObjectFactoryException( "Object creator not found" );
    }
    
    Object instance = creator.getInstance(key, session);

    return instance;
  }

  private class ObjectCreator {

    private String scope = null;
    private String className = null;  
    private ThreadLocal<Object> threadLocalInstance = null;
    private Object globalInstance = null;
    private ClassLoader loader;
    
    public ObjectCreator( String className, String scope, ClassLoader loader ) {
      this.className = className.trim();
      this.scope = scope;
      this.loader = loader;
      if( "thread".equals( scope ) ) { //$NON-NLS-1$
        threadLocalInstance = new ThreadLocal<Object>();
      }
    }
    
    public Object getInstance( String key, IPentahoSession session  ) throws ObjectFactoryException {
      if( "global".equals( scope ) ) { //$NON-NLS-1$
        return getGlobalInstance( key, session );
      }
      else if( "session".equals( scope ) ) { //$NON-NLS-1$
        return getSessionInstance( key, session );
      }
      else if( "local".equals( scope ) ) { //$NON-NLS-1$
        return getLocalInstance( key, session );
      }
      else if( "thread".equals( scope ) ) { //$NON-NLS-1$
        return getThreadInstance( key, session );
      }
      throw new ObjectFactoryException( "Invalid scope: "+scope );
    }
    
    protected Object createObject() throws ObjectFactoryException {
      Object instance = null;
      Class<?> classObject = createClass();
      
      try {
        instance = classObject.newInstance();
      } catch ( Exception e ) {
        if ( e instanceof RuntimeException ) {
          throw (RuntimeException)e;
        }
        throw new ObjectFactoryException( e );
      }
      
      return instance;
    }
    
    protected Class createClass() throws ObjectFactoryException {
      
      try {
        return loader.loadClass( className );
      } catch ( Exception e ) {
        if ( e instanceof RuntimeException ) {
          throw (RuntimeException)e;
        }
        throw new ObjectFactoryException( e );
      }
    }
    
    public Object getGlobalInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      if ( null == globalInstance ) {
        globalInstance = createObject();
      }
      if (globalInstance instanceof IPentahoInitializer) {
        ((IPentahoInitializer) globalInstance).init( session );
      }
      return globalInstance;
    }
    
    public Object getSessionInstance( String key, IPentahoSession session )throws ObjectFactoryException  {

      if ( null == session ) {
        throw new IllegalArgumentException( Messages.getErrorString( "SessionObjectCreator.ERROR_0001_INVALID_SESSION" ) ); //$NON-NLS-1$
      }
      Object instance = session.getAttribute( key );

      if ((instance == null)) {
        instance = createObject();
        session.setAttribute( key, instance );
      }
      if (instance instanceof IPentahoInitializer) {
        ((IPentahoInitializer) instance).init( session );
      }
      return instance;
    }

    public Object getLocalInstance( String key, IPentahoSession session ) throws ObjectFactoryException  {
      Object instance = createObject();

      if (instance instanceof IPentahoInitializer) {
        ((IPentahoInitializer) instance).init( session );
      }
      return instance;
    }
    
    public Object getThreadInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
      Object instance = threadLocalInstance.get();
      
      if ((instance == null)) {
        instance = createObject();
        threadLocalInstance.set( instance );
      }
      if (instance instanceof IPentahoInitializer) {
        ((IPentahoInitializer) instance).init( session );
      }
      return instance;
    }
  }

}
