package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.Iterator;
import java.util.List;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseConnectionService;

public class DataAccessDatabaseConnectionService extends DatabaseConnectionService {
  
  /**
   * This service method overrides it's parent and removes all the database types
   * other than native.
   */
  @Override
  public List<IDatabaseType> getDatabaseTypes() {
     List<IDatabaseType> databaseTypes = super.getDatabaseTypes();
     for (IDatabaseType type : databaseTypes) {
       Iterator<DatabaseAccessType> iter = type.getSupportedAccessTypes().iterator();
       while (iter.hasNext()) {
         DatabaseAccessType accessType = iter.next();
         if (accessType != DatabaseAccessType.NATIVE) {
           iter.remove();
         }
       }
     }
     return databaseTypes;
  }
}
