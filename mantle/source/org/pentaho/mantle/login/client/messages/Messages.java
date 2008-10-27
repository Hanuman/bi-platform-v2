package org.pentaho.mantle.login.client.messages;

import org.pentaho.gwt.widgets.client.utils.MessageBundle;

public class Messages {

  private static MessageBundle messageBundle;

  public static String getString(String key) {
    return messageBundle.getString(key);
  }

  public static MessageBundle getMessageBundle() {
    return messageBundle;
  }

  public static void setMessageBundle(MessageBundle messageBundle) {
    Messages.messageBundle = messageBundle;
  }

}
