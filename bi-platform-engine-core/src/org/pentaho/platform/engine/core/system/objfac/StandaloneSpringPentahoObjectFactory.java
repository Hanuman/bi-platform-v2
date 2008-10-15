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
 * @created Oct 14, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import java.io.File;

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class StandaloneSpringPentahoObjectFactory implements IPentahoObjectFactory {

  private ApplicationContext beanFactory;

  public <T> T get(Class<T> interfaceClass, final IPentahoSession session) throws ObjectFactoryException {
    return get(interfaceClass, interfaceClass.getSimpleName(), session);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> interfaceClass, String key, final IPentahoSession session) throws ObjectFactoryException {
    return (T) getObject(key, session);
  }

  public Object getObject(String key, final IPentahoSession session) throws ObjectFactoryException {
    System.out.println(getClass().getSimpleName() + " attempting to get an instance of [" + key
        + "] while in session [" + session + "]");

    Object object = null;

    if (session != null) {
      //We are in a test environment with no HttpSession context, so we will handle our
      //own session context apart from Spring.
      object = session.getAttribute(key);

      if ((object == null)) {
        try {
          object = beanFactory.getType(key).newInstance();
        } catch (Exception e) {
          throw new ObjectFactoryException(e);
        }
        session.setAttribute(key, object);
      }
    } else {
      //there is no session. Let Spring create the object
      try {
        object = beanFactory.getBean(key);
      } catch (Throwable t) {
        throw new ObjectFactoryException(t);
      }
      
    }

    //FIXME: what is this doing here??
    if (object instanceof IPentahoInitializer) {
      ((IPentahoInitializer) object).init(session);
    }
    //FIXME: hack to support null IPluginSetting's
    if (object instanceof String) {
      object = null;
    }

    System.out.println(getClass().getSimpleName() + " got an instance of [" + key + "]: " + object);
    return object;
  }

  public boolean objectDefined(String key) {
    return beanFactory.containsBean(key);
  }

  public void init(String configFile, Object context) {
    if(context == null) {
//      beanFactory = new FileSystemXmlApplicationContext(configFile);
      File f = new File( configFile );
      FileSystemResource fsr = new FileSystemResource( f );
      GenericApplicationContext appCtx = new GenericApplicationContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);
      xmlReader.loadBeanDefinitions( fsr );
      
      beanFactory = appCtx;
    }
    else {
      beanFactory = (ApplicationContext)context;
    }
  }

  public Class getImplementingClass(String key) {
    return beanFactory.getType(key);
  }

}