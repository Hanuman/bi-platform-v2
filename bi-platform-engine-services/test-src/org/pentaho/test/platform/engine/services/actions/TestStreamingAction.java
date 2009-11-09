package org.pentaho.test.platform.engine.services.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.pentaho.platform.api.action.IStreamingAction;

@SuppressWarnings("nls")
public class TestStreamingAction implements IStreamingAction {

  private OutputStream myContentOutput;

  private String message;

  private boolean executeWasCalled = false;

  public void setMyContentOutputStream(OutputStream myContentOutput) {
    this.myContentOutput = myContentOutput;
  }

  public OutputStream getMyContentOutputStream() {
    return myContentOutput;
  }

  public ByteArrayOutputStream getMyContentOutput() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      bos.write("this content is not written out as a normal non-streamed output".getBytes());
    } catch (IOException e) {
    }
    return bos;
  }

  public String getMimeType(String streamPropertyName) {
    return "text/html";
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
    if (myContentOutput != null) {
      myContentOutput.write(html.toString().getBytes());
    }
  }

}
