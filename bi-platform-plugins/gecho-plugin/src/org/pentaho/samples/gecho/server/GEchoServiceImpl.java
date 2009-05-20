package org.pentaho.samples.gecho.server;

import java.util.Date;

import org.pentaho.samples.gecho.client.GEchoService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GEchoServiceImpl extends RemoteServiceServlet implements
    GEchoService {

  public String echo(String input) {
    return "Hi "+input+", Gecho service is running at "+new Date();
  }
}