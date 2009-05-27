package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;

public class CsvModelValidationListenerCollection extends ArrayList<ICsvModelValidationListener> {
  private static final long serialVersionUID = 1L;

  /**
  * Fires a csv model valid event to all listeners.
  * 
  */
  public void fireCsvModelValid() {
    for (ICsvModelValidationListener listener : this) {
      listener.onCsvModelValid();
    }
  }
  
  /**
   * Fires a csv model valid event to all listeners.
   * 
   */
   public void fireCsvModelInValid() {
     for (ICsvModelValidationListener listener : this) {
       listener.onCsvModelInValid();
     }
   }
}
