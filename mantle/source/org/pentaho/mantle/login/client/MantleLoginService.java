package org.pentaho.mantle.login.client;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface MantleLoginService extends RemoteService {
  public List<String> getAllUsers();
  public boolean isAuthenticated();
  public boolean isShowUsersList();
}

  