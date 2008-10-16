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

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class StandaloneSpringPentahoObjectFactory extends AbstractSpringPentahoObjectFactory {

  public void init(String configFile, Object context) {
    if (context == null) {
      //      beanFactory = new FileSystemXmlApplicationContext(configFile);
      File f = new File(configFile);
      FileSystemResource fsr = new FileSystemResource(f);
      GenericApplicationContext appCtx = new GenericApplicationContext();
      XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);
      xmlReader.loadBeanDefinitions(fsr);

      beanFactory = appCtx;
    } else {
      beanFactory = (ApplicationContext) context;
    }
  }
}