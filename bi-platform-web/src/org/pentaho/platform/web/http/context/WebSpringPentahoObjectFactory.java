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
 * @created Oct 11, 2008
 * @author Aaron Phillips
 * 
 */package org.pentaho.platform.web.http.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.objfac.AbstractSpringPentahoObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A Spring-based Pentaho object factory.  Delegates object creation to the Spring
 * {@link ApplicationContext} created during the initialization of the web application.
 * The Spring bean factory supports the binding of objects to particular scopes.  See Spring
 * documentation for description of the scope types: singleton, prototype, session, and request.
 * The latter two apply only in a web context.
 *
 * There is one scenario in which this factory will handle object retrieval and non delegate to Spring,
 * this is {@link StandaloneSession}.  Spring's session scope relates a bean to an {@link HttpSession},
 * it does not know about custom sessions.  The correct approach to solve this problem is to write a
 * custom Spring scope (called something like "pentahosession").  Unfortunately, we cannot implement
 * a custom scope to handle the {@link StandaloneSession} because the custom scope would not be able
 * to access it.  There is currently no way to statically obtain a reference to a pentaho session.
 * So we are left with using custom logic in this factory to execute a different non-Spring logic path
 * when the IPentahoSession is of type StandaloneSession.
 *
 * @author Aaron Phillips
 * @see http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-factory-scopes
 */
public class WebSpringPentahoObjectFactory extends AbstractSpringPentahoObjectFactory {

  /**
   * Retrieves an implementation of a biserver api interface based on a Spring bean definition
   * file (e.g. pentahoObjects.spring.xml).
   *
   * @see IPentahoObjectFactory
   * @param interfaceClass  the type of object to retrieve
   * @param session  the Pentaho session object.  will be null if request to getObject does not originate in a session context
   *
   */
  public void init(String configFile, Object context) {
    assert context instanceof ServletContext : getClass().getSimpleName() + "currently supports only " //$NON-NLS-1$
        + ServletContext.class.getName()+".  You have tried to initialize with "+context.getClass().getName(); //$NON-NLS-1$
    ServletContext servletContext = (ServletContext) context;
    beanFactory = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
  }
}