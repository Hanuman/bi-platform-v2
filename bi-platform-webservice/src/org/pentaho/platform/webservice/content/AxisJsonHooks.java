package org.pentaho.platform.webservice.content;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.pentaho.platform.plugin.services.webservices.AxisServletHooks;

public class AxisJsonHooks extends AxisServletHooks {
  
  private static final long serialVersionUID = -9145400143858554729L;

  protected String messageType = null;
  
  @Override
  public MessageContext createMessageContext(HttpServletRequest request,
      HttpServletResponse response,
      boolean invocationType) throws IOException {
    messageContext = super.createMessageContext(request, response, invocationType);
    
    if( messageType != null ) {
      messageContext.setProperty(Constants.Configuration.MESSAGE_TYPE, messageType);
    }
    return messageContext;
  }

  public String getMessageType() {
    return messageType;
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }
/*
  protected class RestHooks extends RestRequestProcessor {
    private String messageType;
    private HttpServletRequest request;
    private HttpServletResponse response;
    
    public RestHooks (String httpMethodString,
        HttpServletRequest request,
        HttpServletResponse response, String messageType) throws IOException {
      super(httpMethodString, request, response);
      this.request = request;
      this.response = response;
      this.messageType = messageType;
    }
    
    @Override
    public void processURLRequest() throws IOException, ServletException {
      try {
          RESTUtil.processURLRequest(messageContext, response.getOutputStream(),
                  request.getContentType());
          this.checkResponseWritten();
      } catch (AxisFault e) {
          setResponseState(messageContext, response);
          processFault(e);
      }
      closeStaxBuilder(messageContext);

    }
    
    private void checkResponseWritten() {
      if (!TransportUtils.isResponseWritten(messageContext)) {
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        }
    }

    private void processFault(AxisFault e) throws ServletException, IOException {
        log.debug(e);
        if (messageContext != null) {
            processAxisFault(messageContext, response, response.getOutputStream(), e);
        } else {
            throw new ServletException(e);
        }

    }
  }
*/
}
