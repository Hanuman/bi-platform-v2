package org.pentaho.platform.dataaccess.datasource.wizard.models;


/**
 * Event listener interface for datasource model validation events.
 */

public interface ICsvModelValidationListener {

   /**
     * Fired when the the model is valid
     * 
     */
    void onCsvModelValid();
    
    /**
     * Fired when the the model is valid
     * 
     */
    void onCsvModelInValid();
  }

