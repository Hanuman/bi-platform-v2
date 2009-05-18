package org.pentaho.samples;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class EchoComponent {
  
  private OutputStream outputStream;


  public boolean execute() throws IOException {
    String html = "<html><h1>EchoComponent is live at "+new Date().toString()+"!<h1></html>";
    outputStream.write(html.getBytes());
    return true;
  }
  
  public void setOutputStream(OutputStream outStream) {
    outputStream = outStream;
  }

  public String getMimeType() {
    return "text/html";
  }
}