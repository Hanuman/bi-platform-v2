package org.pentaho.samples;

import java.io.OutputStream;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.pentaho.platform.engine.services.solution.SimpleContentGenerator;
import org.pentaho.platform.util.messages.LocaleHelper;

public class EchoContentGenerator extends SimpleContentGenerator {

  @Override
  public void createContent(OutputStream out) throws Exception {
    try {
      StringBuilder html = new StringBuilder();
      html.append("<html>");
      html.append("<h1>EchoContentGenerator is live!</h1>");
      html.append("</html>");
      out.write(html.toString().getBytes(LocaleHelper.getSystemEncoding()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getMimeType() {
    return "text/html";
  }

  @Override
  public Log getLogger() {
    return LogFactory.getLog(EchoContentGenerator.class);
  }

}
