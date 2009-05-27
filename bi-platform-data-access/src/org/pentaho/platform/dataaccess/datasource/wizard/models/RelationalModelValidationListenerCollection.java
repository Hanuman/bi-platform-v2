package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;

public class RelationalModelValidationListenerCollection extends ArrayList<IRelationalModelValidationListener> {
  private static final long serialVersionUID = 1L;

  /**
  * Fires a relational model valid event to all listeners.
  * 
  */
  public void fireRelationalModelValid() {
    for (IRelationalModelValidationListener listener : this) {
      listener.onRelationalModelValid();
    }
  }
  
  /**
   * Fires a relational model valid event to all listeners.
   * 
   */
   public void fireRelationalModelInValid() {
     for (IRelationalModelValidationListener listener : this) {
       listener.onRelationalModelInValid();
     }
   }
}
