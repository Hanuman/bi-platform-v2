package org.pentaho.platform.engine.core.system.objfac;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;

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
 * @created June 25, 2008
 * @author Steven Barkdull
 * 
 */
public abstract class ObjectCreator implements IObjectCreator {

  private String className = null;  
  protected static final Log logger = LogFactory.getLog(ObjectCreator.class);
  
  private ClassLoader classLoader;
  
  public void setClassLoader( ClassLoader classLoader ) {
	  this.classLoader = classLoader;
  }
  
  public ObjectCreator( String className ) {
	  classLoader = getClass().getClassLoader();
    if ( StringUtils.isEmpty( className ) ) {
      throw new IllegalArgumentException( Messages.getErrorString( "ObjectCreator.ERROR_0001_INVALID_CLASSNAME" ) ); //$NON-NLS-1$
    } else {
      this.className = className.trim();
    }
  }
  
  protected Object createObject() throws ObjectFactoryException {
    Object instance = null;
    Class<?> classObject = null;
    
    try {
      classObject = classLoader.loadClass( className );
      instance = classObject.newInstance();
    } catch ( Exception e ) {
      if ( e instanceof RuntimeException ) {
        throw (RuntimeException)e;
      }
      throw new ObjectFactoryException( e );
    }
    
    
//    this is crap, if any of these exceptions trigger, i guess this code will return null
//    it would be  nice to throw a checked exception.
//    
//    try {
//      classObject = ObjectCreator.class.forName( className );
//    } catch (ClassNotFoundException e) {
//      logger.error( "Failed to create class object for class name: " + className, e );
//    }
//    try {
//      if ( null == classObject ) {
//        logger.error( "ObjectCreator has an invalid class object, unable to create object instance." );
//      } else {
//        instance = classObject.newInstance();
//      }
//    } catch (InstantiationException e) {
//      logger.error( "", e );
//    } catch (IllegalAccessException e) {
//      logger.error( "", e );
//    }
    return instance;
  }
  
}
