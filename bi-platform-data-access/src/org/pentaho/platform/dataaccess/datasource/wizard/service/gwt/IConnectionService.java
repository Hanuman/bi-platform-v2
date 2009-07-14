package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * The IConnectionService interface is used as a GWT and XML webservice.  It 
 * is also used by the Datasource Service to map a name to an IConnection. 
 */
public interface IConnectionService {
  List<IConnection> getConnections() throws ConnectionServiceException;
  IConnection getConnectionByName(String name) throws ConnectionServiceException;
  boolean addConnection(IConnection connection) throws ConnectionServiceException;
  boolean updateConnection(IConnection connection) throws ConnectionServiceException;
  boolean deleteConnection(IConnection connection) throws ConnectionServiceException;
  boolean deleteConnection(String name) throws ConnectionServiceException;
  boolean testConnection(IConnection connection) throws ConnectionServiceException;
  IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException;
}
