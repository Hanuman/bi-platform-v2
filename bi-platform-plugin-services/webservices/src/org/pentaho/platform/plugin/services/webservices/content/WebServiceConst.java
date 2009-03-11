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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.services.webservices.content;

/**
 * A class that holds global constants for the webservice system
 * @author jamesdixon
 *
 */
public class WebServiceConst {

  public static final String PLUGIN_NAME = "webservices"; //$NON-NLS-1$
  
  public static final String AXIS_CONFIG_FILE = "axis2_config.xml"; //$NON-NLS-1$
  
  public static String baseUrl = null;
  
  /**
   * Returns the URL that can be used to get a list of the web services
   * @return Services list URL
   */
  public static String getDiscoveryUrl() {
    return WebServiceConst.baseUrl + "content/ws-services"; //$NON-NLS-1$
  }

  /**
   * Returns the URL to the used as the base for the WSDL content generator URLs
   * @return WSDL URL base
   */
  public static String getWsdlUrl() {
    return baseUrl + "content/ws-wsdl"; //$NON-NLS-1$
  }

  /**
   * Returns the URL to the used as the base for the service execution URLs
   * @return Execution URL base
   */
  public static String getExecuteUrl() {
    return baseUrl + "content/ws-run"; //$NON-NLS-1$
  }

}
