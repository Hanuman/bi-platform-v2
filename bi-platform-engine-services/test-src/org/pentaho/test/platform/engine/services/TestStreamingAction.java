package org.pentaho.test.platform.engine.services;

import java.io.OutputStream;
import java.util.Date;

import org.pentaho.platform.api.action.IStreamingAction;

@SuppressWarnings("nls")
public class TestStreamingAction implements IStreamingAction {

  private OutputStream outputStream;
  private OutputStream myContentOutput;

  private String message;

  private boolean executeWasCalled = false;

  public OutputStream getOutputStream() {
    return outputStream;
  }

  public String getMimeType() {
    return "text/html";
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isExecuteWasCalled() {
    return executeWasCalled;
  }

  public void execute() throws Exception {
    StringBuilder html = new StringBuilder("<html><h1>TestStreamingAction was here @ " + new Date().toString()
        + "!  Your message is \"" + message + "\"<h1>");
    html.append("</html>");
    outputStream.write(html.toString().getBytes());
  }

  public void setMyContentOutput(OutputStream myContentOutput) {
    this.myContentOutput = myContentOutput;
  }

  public OutputStream getMyContentOutput() {
    return myContentOutput;
  }
}
