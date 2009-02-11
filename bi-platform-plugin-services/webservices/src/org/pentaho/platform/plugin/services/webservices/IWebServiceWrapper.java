package org.pentaho.platform.plugin.services.webservices;

import javax.wsdl.Definition;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

public interface IWebServiceWrapper {

  /**
   * Returns the WSDL definition for this web service
   * @param axisConfig The current AxisConfiguration
   * @return WSDL definition
   * @throws Exception
   */
  public Definition getDefinition( AxisConfiguration axisConfig ) throws Exception;
  
  /**
   * Returns the WSDL XML text for this web service
   * @param axisConfig The current AxisConfiguration
   * @return WSDL XML
   * @throws Exception
   */
  public String getWsdl( AxisConfiguration axisConfig ) throws Exception;
  
  /**
   * Returns the name of this web service. This should not be localized
   * @return Name
   */
  public String getName();
  
  /**
   * Returns the localized title for this web service. This is shown on the 
   * services list page.
   * @return Title
   */
  public String getTitle();
  
  /**
   * Returns the localized title for this web service. This is shown on the 
   * services list page.
   * @return Description
   */
  public String getDescription();
  
  /**
   * Returns the web service bean class
   * @return Class of the bean
   */
  public Class getServiceClass();
  
  /**
   * Returns the enabled state of this service
   * @return Current enable/disable state
   */
  public boolean isEnabled();
  
  /**
   * Sets the enabled state of this service
   * @param enabled
   */
  public void setEnabled( boolean enabled );
  
  /**
   * Sets the AxisService for this wrapper
   * @param service
   */
  public void setService( AxisService service );
  
  /**
   * Gets the AxisService for this wrapper
   * @return axis service
   */
  public AxisService getService();
  
}
