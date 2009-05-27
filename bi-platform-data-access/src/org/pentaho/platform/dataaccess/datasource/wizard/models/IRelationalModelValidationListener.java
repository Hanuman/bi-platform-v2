package org.pentaho.platform.dataaccess.datasource.wizard.models;


/**
 * Event listener interface for datasource model validation events.
 */

public interface IRelationalModelValidationListener {

   /**
     * Fired when the the model is valid
     * 
     */
    void onRelationalModelValid();
    
    /**
     * Fired when the the model is valid
     * 
     */
    void onRelationalModelInValid();
  }

