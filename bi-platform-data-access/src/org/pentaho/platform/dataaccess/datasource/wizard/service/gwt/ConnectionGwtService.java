
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
     
public interface ConnectionGwtService extends RemoteService{
  public List<IConnection> getConnections();
  public IConnection getConnectionByName(String name);
  public Boolean addConnection(IConnection connection);
  public Boolean updateConnection(IConnection connection);
  public Boolean deleteConnection(IConnection connection);
  public Boolean deleteConnection(String name);
  public Boolean testConnection(IConnection connection) throws ConnectionServiceException;
}

  