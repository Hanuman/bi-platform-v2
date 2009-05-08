
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface ConnectionGwtService extends RemoteService{
  public List<IConnection> getConnections() throws ConnectionServiceException;
  public IConnection getConnectionByName(String name) throws ConnectionServiceException;
  public Boolean addConnection(IConnection connection) throws ConnectionServiceException;
  public Boolean updateConnection(IConnection connection) throws ConnectionServiceException;
  public Boolean deleteConnection(IConnection connection) throws ConnectionServiceException;
  public Boolean deleteConnection(String name) throws ConnectionServiceException;
  public Boolean testConnection(IConnection connection) throws ConnectionServiceException;
}

  