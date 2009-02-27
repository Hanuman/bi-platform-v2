package org.pentaho.platform.plugin.action.chartbeans;

/**
 * This exception wraps the generic exception thrown
 * by the ChartBeans boot process so that developers
 * can properly account for boot errors.
 * 
 * @author cboyden
 *
 */
public class ChartBootException extends Exception {
  
  private static final long serialVersionUID = 4840561957831529L;
  

  public ChartBootException(Throwable t){
    super(t);
  }
}
