package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;

/**
 * This interface represents specific datasource type implementations,
 * which include CSV and SQL for now.
 */
public interface IDatasourceTypeController {
  boolean supportsBusinessData(BusinessData businessData);
  void initializeBusinessData(BusinessData businessData);
}
