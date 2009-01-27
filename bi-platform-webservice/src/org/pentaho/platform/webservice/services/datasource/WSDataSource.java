package org.pentaho.platform.webservice.services.datasource;

/**
 * A WSDL-friendly datasource definition class
 * @author jamesdixon
 *
 */
public class WSDataSource {

  String name;
  
  public WSDataSource( ) {
  }
  
  /**
   * Returns the name of this datasource
   * @return Datasource name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this datasource
   * @param name
   */
  public void setName( String name ) {
    this.name = name;
  }
  
}
