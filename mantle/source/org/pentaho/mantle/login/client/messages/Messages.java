package org.pentaho.mantle.login.client.messages;

import com.google.gwt.core.client.GWT;

public class Messages {

  private static MantleLoginMessages constants = (MantleLoginMessages) GWT.create(MantleLoginMessages.class);

  public static MantleLoginMessages getInstance() {
    return constants;
  }

}


  