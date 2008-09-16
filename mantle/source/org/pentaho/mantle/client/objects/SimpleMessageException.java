package org.pentaho.mantle.client.objects;

import java.io.Serializable;

public class SimpleMessageException extends Exception implements Serializable {
  public String message;

  public SimpleMessageException() {

  }

  public SimpleMessageException(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
  
  public String toString() {
    return message;
  }
  
}
