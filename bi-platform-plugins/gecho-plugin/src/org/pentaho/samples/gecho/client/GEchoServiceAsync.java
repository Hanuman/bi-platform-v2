package org.pentaho.samples.gecho.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GEchoServiceAsync {
  void echo(String input, AsyncCallback<String> callback);
}
