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


public class TestServiceSetup extends BaseServiceSetup {

  private static final long serialVersionUID = 3383802441135983726L;

  protected static TestServiceSetup instance = null;
  
  private static final Log logger = LogFactory.getLog(TestServiceSetup.class);

  public TestServiceSetup() {
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
    wrappers.add( new TestServiceWrapper() );
    wrappers.add( new TestService2Wrapper() );
    wrappers.add( new TestService3Wrapper() );
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
