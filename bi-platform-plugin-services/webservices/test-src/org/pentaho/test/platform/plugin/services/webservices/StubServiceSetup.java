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
package org.pentaho.test.platform.plugin.services.webservices;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.webservices.BaseServiceSetup;
import org.pentaho.platform.plugin.services.webservices.IWebServiceWrapper;
import org.pentaho.platform.plugin.services.webservices.content.WebServiceConst;


public class StubServiceSetup extends BaseServiceSetup {

  private static final long serialVersionUID = 3383802441135983726L;

  protected static StubServiceSetup instance = null;
  
  private static final Log logger = LogFactory.getLog(StubServiceSetup.class);

  public StubServiceSetup() {
    super();
  }
  
  public void init() {
    
  }
  
  public Log getLogger() {
    return logger;
  }

  @Override
  public InputStream getConfigXml( ) {

    WebServiceConst.baseUrl = "http://testhost:8080/testcontext/"; //$NON-NLS-1$
    try {
      File f = new File( "webservices/test-src/solution/system/"+WebServiceConst.AXIS_CONFIG_FILE); //$NON-NLS-1$
      return new FileInputStream( f );
    } catch (Exception e) {
      // TODO log this
      e.printStackTrace();
    }
    return null;
  }
  
  @Override
  public boolean setEnabled( String name, boolean enabled ) {
    IWebServiceWrapper wrapper = getServiceWrapper( name );
    wrapper.setEnabled(enabled);
    AxisService axisService = wrapper.getService( );
    axisService.setActive( enabled );
    return true;
  }
  
  @Override
  protected List<IWebServiceWrapper> getWebServiceWrappers() {
    List<IWebServiceWrapper> wrappers = new ArrayList<IWebServiceWrapper>();
    wrappers.add( new StubServiceWrapper() );
    wrappers.add( new StubService2Wrapper() );
    wrappers.add( new StubService3Wrapper() );
    return wrappers;
  }
  
  @Override
  protected void addTransports( AxisService axisService ) {
      
      ArrayList<String> transports = new ArrayList<String>();
      transports.add( "http" ); //$NON-NLS-1$
      axisService.setExposedTransports(transports);
  }

  @Override
  protected void addServiceEndPoints( AxisService axisService ) {
    String endPoint1 = WebServiceConst.getExecuteUrl()+"/"+axisService.getName(); //$NON-NLS-1$
      String endPoint2 = "http:test"; //$NON-NLS-1$
      
      ArrayList<String> transports = new ArrayList<String>();
      transports.add( "http" ); //$NON-NLS-1$
      axisService.setExposedTransports(transports);
      axisService.setEPRs(new String[] { endPoint1, endPoint2 } );
  }

}
