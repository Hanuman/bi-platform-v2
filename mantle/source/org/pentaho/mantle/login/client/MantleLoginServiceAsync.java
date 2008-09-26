package org.pentaho.mantle.login.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MantleLoginServiceAsync {
  public void getAllUsers(AsyncCallback callback);
  public void isAuthenticated(AsyncCallback callback);
  public void isSubscription(AsyncCallback callback);
}

  