package org.pentaho.samples.gecho.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("echo")
public interface GEchoService extends RemoteService {
  String echo(String name);
}
