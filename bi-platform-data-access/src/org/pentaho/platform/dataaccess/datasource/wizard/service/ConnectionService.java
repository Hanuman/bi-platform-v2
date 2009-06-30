package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.ui.xul.XulServiceCallback;

public interface ConnectionService {
  void getConnections(XulServiceCallback<List<IConnection>> callback) throws ConnectionServiceException;
  void getConnectionByName(String name, XulServiceCallback<IConnection> callback) throws ConnectionServiceException;
  void addConnection(IConnection connection, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
  void updateConnection(IConnection connection, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
  void deleteConnection(IConnection connection, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
  void deleteConnection(String name, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
  void testConnection(IConnection connection, XulServiceCallback<Boolean> callback) throws ConnectionServiceException;
  void convertToConnection(IDatabaseConnection databaseConnection, XulServiceCallback<IConnection> callback) throws ConnectionServiceException;
  void convertFromConnection(IConnection databaseConnection, XulServiceCallback<IDatabaseConnection> callback) throws ConnectionServiceException;
}
