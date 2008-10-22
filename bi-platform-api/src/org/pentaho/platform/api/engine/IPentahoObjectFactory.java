/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Oct 11, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.api.engine;

/**
 * The way the BI platform creates and manages system objects.  The {@link IPentahoObjectFactory}
 * acts like a service locator of objects rather than services.  It is typically accessed via a static
 * method in PentahoSystem, PentahoSystem.getObjectFactory()
 * 
 * @author Aaron Phillips
 */
public interface IPentahoObjectFactory {
  /**
   * Retrieves an instance of a Pentaho BI Server API interface using the simple interface name
   * (interfaceClass name without the package) as the object key.  If an appropriate 
   * implementation does not exist the factory implementation should create it. 
   * 
   * @param interfaceClass  the type of object to retrieve (retrieved object will be 
   *        returned as this type)
   * @param session  the Pentaho session object.  Can be used to associate an object 
   *        instance to a Pentaho session.  Value will be null if request to getObject 
   *        does not originate in a session context.
   * @return the implementation object typed to interfaceClass
   * @throws ObjectFactoryException if the object cannot be retrieved
   */
  public <T> T get(Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException;
  
  /**
   * Retrieves an instance of a Pentaho BI Server API interface by the given object key.  
   * If an appropriate implementation does not exist the factory implementation should create it. 
   * 
   * @param interfaceClass  the type of object to retrieve (retrieved object will be 
   *        returned as this type)
   * @param key  the object identifier, typically the interface name
   * @param session  the Pentaho session object.  Can be used to associate an object 
   *        instance to a Pentaho session.  Value will be null if request to getObject 
   *        does not originate in a session context.
   * @return the implementation object typed to interfaceClass
   * @throws ObjectFactoryException if the object cannot be retrieved
   */
  public <T> T get(Class<T> interfaceClass, String key, final IPentahoSession session) throws ObjectFactoryException;

  @Deprecated
  public Object getObject(String key, final IPentahoSession session) throws ObjectFactoryException;

  /**
   * Checks if the implementation for the given interface is defined.
   * @param key  the object identifier, typically the interface name
   * @return true if the object is defined
   */
  public boolean objectDefined(String key);
  
  /**
   * Provides the concrete Class defined for the given object key.
   * @param key  the object identifier, typically the interface name
   * @return  the type of object associated with key
   */
  @SuppressWarnings("unchecked")
  public Class getImplementingClass(String key);

  /**
   * Initialize the factory with optional configuration file and runtime context.
   * @param configFile  an object configuration definition file understandable by the 
   *        factory implementation
   * @param context  a context object whereby the factory implementation can access 
   *        runtime information, type of object varies depending on the framework
   *        used by the factory and the environment in which the application is running.
   */
  public void init(String configFile, Object context);
}
