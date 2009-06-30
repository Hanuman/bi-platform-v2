
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface ConnectionGwtService extends RemoteService{
  List<IConnection> getConnections() throws ConnectionServiceException;
  IConnection getConnectionByName(String name) throws ConnectionServiceException;
  Boolean addConnection(IConnection connection) throws ConnectionServiceException;
  Boolean updateConnection(IConnection connection) throws ConnectionServiceException;
  Boolean deleteConnection(IConnection connection) throws ConnectionServiceException;
  Boolean deleteConnection(String name) throws ConnectionServiceException;
  Boolean testConnection(IConnection connection) throws ConnectionServiceException;
  IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException;
  IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException;

}

  