package org.pentaho.mantle.client.messages;

import com.google.gwt.core.client.GWT;

public class Messages {

  private static MantleApplicationConstants constants = (MantleApplicationConstants) GWT.create(MantleApplicationConstants.class);

  public static MantleApplicationConstants getInstance() {
    return constants;
  }

}
