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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created June 25, 2008
 * @author Steven Barkdull
 * 
 */
package org.pentaho.platform.api.engine;



/**
 * <span style="color:red; font-weight:bold">NOTICE: This class is deprecated and will not exist in platform version 2.1.</span>  <br>Below is a guide for how to use the new API for factory-ing dynamic objects:
 * 
 * The old way of creating objects via a factory was:
 * <p>
 * <code>
 * 1. PentahoObjectFactory fac = new PentahoObjectFactory();<br>
 * //set the object specifications (as a map of object creators)<br>
 * //the object creator classes provided scoping to objects, <br>
 * //allowing us to bind object to particular scopes <br>
 * 2. SessionObjectCreator creator = SessionObjectCreator("org.pentaho.platform.api.repository.ISolutionRepository") <br>
 * 3. Map objCreatorMap = new HashMap() <br>
 * 4. objCreatorMap.put("ISolutionRepository", creator)<br>
 * 5. fac.setObjectCreators(objCreatorMap);<br>
 * 6. ISolutionEngine eng = (ISolutionEngine)fac.getObject("ISolutionEngine", session);<br>
 * </code>
 * <p>
 * The new way of factory-ing objects is:
 * <p>
 * <code>
 * 1. IPentahoObjectFactory fac = new MyPentahoObjectFactory();<br>
 * //configure the factory with an object specification file and/or a runtime context object<br>
 * 2. fac.init(objectSpecFile, contextObject) {@link IPentahoObjectFactory#init(String, Object)}<br>
 * 3. ISolutionEngine eng = fac.get(ISolutionEngine.class, session) {@link IPentahoObjectFactory#get(Class, IPentahoSession)}
 * </code>
 * <p>
 * 
 * You will notice that the new way of serving up objects does not expose an API for scoping of objects like the old way did.
 * This behavior is now delegated to the particular {@link IPentahoObjectFactory} implementation, which means a factory
 * implementation has total freedom to be as simple or sophisticated at it wants without being required to handle object 
 * scoping.  Any kind of object binding/scoping or any other rules for the creation and management of objects is totally 
 * up the implementation.  Typically, a factory implementation would be made aware of it's rules for object creation by way of a well-known objectSpecFile (see
 * new way step #2 in the instructions above).
 * 
 * @deprecated
 */
public interface IObjectCreator {

  /**
   * Please use the new way of factory-ing dynamic PentahoObjects.  
   * See an explanation of how to switch from the old way to the new way here: {@link IObjectCreator}
   * @deprecated
   */
  public Object getInstance( String key, IPentahoSession session ) throws ObjectFactoryException;
  
  /**
   * Please use the new way of factory-ing dynamic PentahoObjects.  
   * See an explanation of how to switch from the old way to the new way here: {@link IObjectCreator}
   * <p>
   * Sets the classloader to be used to create objects. If this is not set the 
   * implementor can use a classloader of its choosing
   * @param classLoader
   * @deprecated
   */
  public void setClassLoader( ClassLoader classLoader );
  
}
