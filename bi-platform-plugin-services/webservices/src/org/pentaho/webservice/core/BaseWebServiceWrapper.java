package org.pentaho.webservice.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.xml.WSDLReader;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.pentaho.webservice.core.content.WebServiceConst;
import org.xml.sax.InputSource;

import com.ibm.wsdl.factory.WSDLFactoryImpl;

/**
 * The base web service wrapper class. Subclasses of this class need to
 * implement:
 * 1) getServiceClass
 * 2) getTitle
 * 3) getDescription()
 * 4) getExtraClasses();
 *
 * @author jamesdixon
 *
 */
public abstract class BaseWebServiceWrapper implements IWebServiceWrapper {

  private boolean enabled = true;

  public abstract Class getServiceClass();

  private AxisService axisService;
  
  /**
   * Returns a list additional classes that need to be included
   * in the WSDL for the web service. 
   * @return List of class names to be defined in the WSDL
   */
  protected abstract ArrayList<Class> getExtraClasses();
  
  public Definition getDefinition( AxisConfiguration axisConfig ) throws Exception {
    
    String wsdlStr = getWsdl( axisConfig );
    InputStream in = new ByteArrayInputStream( wsdlStr.getBytes() );
    InputSource source = new InputSource( in );
    
    WSDLFactoryImpl factory = new WSDLFactoryImpl();
    WSDLReader reader = factory.newWSDLReader();
    Definition def = reader.readWSDL("", source); //$NON-NLS-1$
    return def;
    
  }
  
  public String getWsdl( AxisConfiguration axisConfig ) throws Exception {
    
    Class clazz = getServiceClass();
    String name = clazz.getSimpleName();
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    Java2WSDLBuilder java2WsdlBuilder = new Java2WSDLBuilder(
        out,
        clazz.getName(),
        getClass().getClassLoader() );

    // convert the extra classes into a list of class names
    List<Class> extraClasses = getExtraClasses();
    if( extraClasses != null ) {
      ArrayList<String> extraClassNames = new ArrayList<String>();
      for( Class extraClass : extraClasses ) {
        extraClassNames.add( extraClass.getName() );
      }
      java2WsdlBuilder.setExtraClasses( extraClassNames );
    }
    java2WsdlBuilder.setSchemaTargetNamespace( "http://webservice.pentaho.com" ); //$NON-NLS-1$
    java2WsdlBuilder.setLocationUri( WebServiceConst.getExecuteUrl()+name );
    java2WsdlBuilder.setTargetNamespacePrefix( "pho" ); //$NON-NLS-1$
    java2WsdlBuilder.setServiceName( name );
    java2WsdlBuilder.setAttrFormDefault( "unqualified" ); //$NON-NLS-1$
    java2WsdlBuilder.setElementFormDefault( "unqualified" ); //$NON-NLS-1$
    java2WsdlBuilder.setGenerateDocLitBare( false );
    java2WsdlBuilder.generateWSDL();

    return new String( out.toByteArray() );
    
  }
  
  public String getName() {
    Class serviceClass = getServiceClass();
    return serviceClass.getSimpleName();
  }
  
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setService( AxisService axisService ) {
    this.axisService = axisService;
  }
  
  public AxisService getService() {
    return axisService;
  }

}
