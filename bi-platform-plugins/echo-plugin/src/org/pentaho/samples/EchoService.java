package org.pentaho.samples;
import java.util.Date;

@SuppressWarnings("nls")
public class EchoService {

  public String echo(String message) {
    return new Date().toString() + ":" + message;
  }
  
  public String now() {
    return "The current time is: "+new Date().toString();
  }

}
