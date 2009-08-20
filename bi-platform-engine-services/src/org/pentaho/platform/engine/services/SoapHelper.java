/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Jul 17, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.engine.services;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class SoapHelper {
  private static final Log logger = LogFactory.getLog(SoapHelper.class);

  public static void generateSoapResponse(final IRuntimeContext context, final OutputStream outputStream,
      final SimpleOutputHandler outputHandler, final OutputStream contentStream, final List messages) {

    StringBuffer messageBuffer = new StringBuffer();
    SoapHelper.generateSoapResponse(context, outputHandler, contentStream, messageBuffer, messages);
    try {
      outputStream.write(messageBuffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
    } catch (IOException e) {
      SoapHelper.logger.error(null, e);
    }
  }

  public static String getSoapHeader() {
    return "<SOAP-ENV:Envelope " + //$NON-NLS-1$
        "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" " + //$NON-NLS-1$
        "SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n " + //$NON-NLS-1$
        "<SOAP-ENV:Body>\n"; //$NON-NLS-1$

  }

  public static String getSoapFooter() {
    return "</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>"; //$NON-NLS-1$

  }

  public static void generateSoapError(final StringBuffer messageBuffer, final List messages) {

    // TODO mlowery begin hack: copied in getFirstError code from MessageFormatter
    // to avoid needing an IPentahoSession
    String message = null;
    String errorStart = PentahoMessenger.getUserString("ERROR"); //$NON-NLS-1$
    int pos = errorStart.indexOf('{');
    if (pos != -1) {
      errorStart = errorStart.substring(0, pos);
    }
    Iterator msgIterator = messages.iterator();
    while (msgIterator.hasNext()) {
      String msg = (String) msgIterator.next();
      if (msg.indexOf(errorStart) == 0) {
        message = msg;
      }
    }
    // TODO mlowery end hack

    messageBuffer.append("<SOAP-ENV:Fault>\n"); //$NON-NLS-1$

    if (message == null) {
      message = Messages.getErrorString("SoapHelper.ERROR_0001_UNKNOWN_ERROR"); //$NON-NLS-1$
    }

    // Envelope envelope = new Envelope();
    // Fault fault = new Fault( );
    // TODO: Generate the following message using the envelope and fault objects

    // TODO determine if this is a reciever or a sender problem by examining
    // the error code
    boolean senderFault = (message.indexOf("SolutionEngine.ERROR_0002") != -1) || //$NON-NLS-1$ // solution not specifed
        (message.indexOf("SolutionEngine.ERROR_0003") != -1) || //$NON-NLS-1$ // Path not specifeid
        (message.indexOf("SolutionEngine.ERROR_0004") != -1) || //$NON-NLS-1$ // Action not specified
        (message.indexOf("SolutionEngine.ERROR_0005") != -1); //$NON-NLS-1$ // Action not found
    // send the error code
    // TODO parse out the error code
    messageBuffer
        .append("<SOAP-ENV:faultcode>\n <SOAP-ENV:Subcode>\n<SOAP-ENV:Value><![CDATA[" + message + "]]></SOAP-ENV:Value>\n </SOAP-ENV:Subcode>\n </SOAP-ENV:faultcode>"); //$NON-NLS-1$ //$NON-NLS-2$

    if (senderFault) {
      messageBuffer.append("<SOAP-ENV:faultactor>SOAP-ENV:Client</SOAP-ENV:faultactor>\n"); //$NON-NLS-1$
    } else {
      messageBuffer.append("<SOAP-ENV:faultactor>SOAP-ENV:Server</SOAP-ENV:faultactor>\n"); //$NON-NLS-1$
    }

    // send the error reason
    messageBuffer
        .append("<SOAP-ENV:faultstring><SOAP-ENV:Text xml:lang=\"" + LocaleHelper.getDefaultLocale().toString() + "\"><![CDATA[" + message + "]]></SOAP-ENV:Text>\n </SOAP-ENV:faultstring>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // send the details
    messageBuffer.append("<SOAP-ENV:Detail>"); //$NON-NLS-1$

    Iterator messageIterator = messages.iterator();
    while (messageIterator.hasNext()) {
      messageBuffer.append("<message name=\"trace\"><![CDATA[" + (String) messageIterator.next() + "]]></message>\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    messageBuffer.append("</SOAP-ENV:Detail>"); //$NON-NLS-1$

    messageBuffer.append("</SOAP-ENV:Fault>\n"); //$NON-NLS-1$

  }

  public static String openSoapResponse() {
    return "<ExecuteActivityResponse xmlns:m=\"http://pentaho.org\">\n"; //$NON-NLS-1$
  }

  public static String closeSoapResponse() {
    return "</ExecuteActivityResponse>\n"; //$NON-NLS-1$
  }

  public static void generateSoapResponse(final IRuntimeContext context, final SimpleOutputHandler outputHandler,
      final OutputStream contentStream, final StringBuffer messageBuffer, final List messages) {
    // we need to generate a soap package for this
    // the outputs of the action need to be put into the package

    /*
     * This is the format of the response message package
     * 
     * 
     * <SOAP-ENV:Envelope
     * xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
     * SOAP-ENV:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
     * <SOAP-ENV:Body> <m:ExecuteActivityResponse
     * xmlns:m="http://pentaho.org"> <result>...</result> <result>...</result>
     * <result>...</result> </m:ExecuteActivityResponse> </SOAP-ENV:Body>
     * </SOAP-ENV:Envelope>
     * 
     */

    if ((context == null) || (context.getStatus() != IRuntimeContext.RUNTIME_STATUS_SUCCESS)) {
      SoapHelper.generateSoapError(messageBuffer, messages);
    } else {

      messageBuffer.append(SoapHelper.openSoapResponse());

      IContentItem contentItem = outputHandler.getFeedbackContentItem();

      // hmm do we need this to be ordered?
      Set outputNames = context.getOutputNames();

      Iterator outputNameIterator = outputNames.iterator();
      while (outputNameIterator.hasNext()) {
        String outputName = (String) outputNameIterator.next();
        contentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, context
            .getSolutionName(), context.getInstanceId(), "text/xml"); //$NON-NLS-1$
        if ((outputNames.size() == 1) && (contentItem != null)) {
          String mimeType = contentItem.getMimeType();
          if ((mimeType != null) && mimeType.startsWith("text/")) { //$NON-NLS-1$
            if (mimeType.equals("text/xml")) { //$NON-NLS-1$
              // this should be ok to embed directly, any CDATA
              // sections in the XML will be ok
              messageBuffer.append("<").append(outputName).append(">") //$NON-NLS-1$//$NON-NLS-2$
                  .append(contentStream.toString()).append("</").append(outputName).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (mimeType.startsWith("text/")) { //$NON-NLS-1$
              // put this is a CDATA section and hope it does not
              // contain the string ']]>'
              messageBuffer.append("<").append(outputName).append("><![CDATA[") //$NON-NLS-1$ //$NON-NLS-2$
                  .append(contentStream.toString()).append("]]></").append(outputName).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
            }
          } else {
            Object value = context.getOutputParameter(outputName).getValue();
            if (value == null) {
              value = ""; //$NON-NLS-1$
            }
            messageBuffer.append(SoapHelper.toSOAP(outputName, value));
          }
        } else {
          Object value = context.getOutputParameter(outputName).getValue();
          if (value == null) {
            value = ""; //$NON-NLS-1$
          }
          messageBuffer.append(SoapHelper.toSOAP(outputName, value));
        }
      }
      messageBuffer.append(SoapHelper.closeSoapResponse());
    }
  }

  public static String toSOAP(final String name, final Object item) {

    if (item instanceof String) {
      return SoapHelper.toSOAP(name, (String) item);
    } else if (item instanceof List) {
      return SoapHelper.toSOAP(name, (List) item);
    } else if (item instanceof IPentahoResultSet) {
      return SoapHelper.toSOAP(name, (IPentahoResultSet) item);
    } else if (item instanceof IContentItem) {
      return SoapHelper.toSOAP(name, ((IContentItem) item).getId());
    }
    return null;
  }

  @SuppressWarnings("deprecation")
  private static String toSOAP(final String name, final IPentahoResultSet resultSet) {
    StringBuffer messageBuffer = new StringBuffer();
    messageBuffer.append("<" + name + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$
    Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
    Object[][] rowHeaders = resultSet.getMetaData().getRowHeaders();
    boolean hasColumnHeaders = columnHeaders != null;
    boolean hasRowHeaders = rowHeaders != null;

    if (hasColumnHeaders) {
      for (Object[] element : columnHeaders) {
        messageBuffer.append("<COLUMN-HDR-ROW>\n"); //$NON-NLS-1$
        for (int column = 0; column < element.length; column++) {
          messageBuffer.append("<COLUMN-HDR-ITEM><![CDATA[").append(element[column]).append("]]></COLUMN-HDR-ITEM>\n"); //$NON-NLS-1$//$NON-NLS-2$
        }
        messageBuffer.append("</COLUMN-HDR-ROW>\n"); //$NON-NLS-1$
      }
    }

    if (hasRowHeaders) {
      for (Object[] element : rowHeaders) {
        messageBuffer.append("<ROW-HDR-ROW>\n"); //$NON-NLS-1$
        for (int column = 0; column < element.length; column++) {
          messageBuffer.append("<ROW-HDR-ITEM><![CDATA[").append(element[column]).append("]]></ROW-HDR-ITEM>\n"); //$NON-NLS-1$//$NON-NLS-2$
        }
        messageBuffer.append("</ROW-HDR-ROW>\n"); //$NON-NLS-1$
      }
    }

    Object[] dataRow = resultSet.next();
    while (dataRow != null) {
      messageBuffer.append("<DATA-ROW>\n"); //$NON-NLS-1$
      for (Object element : dataRow) {
        messageBuffer.append("<DATA-ITEM><![CDATA[").append(element).append("]]></DATA-ITEM>\n"); //$NON-NLS-1$//$NON-NLS-2$
      }
      messageBuffer.append("</DATA-ROW>\n"); //$NON-NLS-1$
      dataRow = resultSet.next();
    }
    messageBuffer.append("</" + name + ">\n"); //$NON-NLS-1$ //$NON-NLS-2$

    return messageBuffer.toString();
  }

  public static String toSOAP(final String name, final String value) {
    // example code only, probably bogus
    return "<" + name + "><![CDATA[" + value + "]]></" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public static String toSOAP(final String name, final long value) {
    // example code only, probably bogus
    return "<" + name + "><![CDATA[" + value + "]]></" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public static String toSOAP(final String name, final Date value) {
    // example code only, probably bogus
    return "<" + name + "><![CDATA[" + value + "]]></" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public static String toSOAP(final String name, final List list) {

    return "<" + name + "><![CDATA[" + list.toString() + "]]></" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

}
