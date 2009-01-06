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
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.pentaho.platform.util.messages.LocaleHelper;

public class WebServiceUtil {

  public static void writeDocument(final OutputStream outputStream, final org.w3c.dom.Document doc, final boolean wrapWithSOAP) throws IOException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new StringWriter());
    TransformerFactory.newInstance().newTransformer().transform(source, result);
    String theXML = result.getWriter().toString();
    WebServiceUtil.writeString(outputStream, theXML, wrapWithSOAP);
  }

  public static void writeDocument(final OutputStream outputStream, final Document doc, final boolean wrapWithSOAP) throws IOException {
    WebServiceUtil.writeString(outputStream, doc.asXML(), wrapWithSOAP);
  }

  public static void writeDocument(final OutputStream outputStream, final Document doc) throws IOException {
    WebServiceUtil.writeString(outputStream, doc.asXML(), false);
  }

  public static void writeString(final OutputStream outputStream, final String strXml) throws IOException {
    WebServiceUtil.writeString(outputStream, strXml, false);
  }

  public static void writeString(final OutputStream outputStream, String strXml, final boolean wrapWithSOAP) throws IOException {
    String xmlProcessingInstruction = null;
    int headerPos = strXml.indexOf("?>"); //$NON-NLS-1$
    if (headerPos > -1) {
      xmlProcessingInstruction = strXml.substring(0, headerPos + 2);
      strXml = strXml.substring(headerPos + 2);
    }
    if ( null != xmlProcessingInstruction )
    {
      outputStream.write(xmlProcessingInstruction.getBytes(LocaleHelper.getSystemEncoding()));
    }
    if (wrapWithSOAP) {
      outputStream.write(SoapHelper.getSoapHeader().getBytes(LocaleHelper.getSystemEncoding()));
      outputStream.write(SoapHelper.openSoapResponse().getBytes(LocaleHelper.getSystemEncoding()));
      outputStream.write("<content>".getBytes(LocaleHelper.getSystemEncoding())); //$NON-NLS-1$
    }
    outputStream.write(strXml.getBytes(LocaleHelper.getSystemEncoding()));
    if (wrapWithSOAP) {
      outputStream.write("</content>".getBytes(LocaleHelper.getSystemEncoding())); //$NON-NLS-1$
      outputStream.write(SoapHelper.closeSoapResponse().getBytes(LocaleHelper.getSystemEncoding()));
      outputStream.write(SoapHelper.getSoapFooter().getBytes(LocaleHelper.getSystemEncoding()));
    }
  }

  public static String getStatusXml(String statusMsg) {
    statusMsg = StringEscapeUtils.escapeXml(statusMsg);
    return "<web-service><status msg=\"" + statusMsg + "\"/></web-service>"; //$NON-NLS-1$ //$NON-NLS-2$
  }
  
  public static String getErrorXml(String errorMsg) {
    errorMsg = StringEscapeUtils.escapeXml(errorMsg);
    return "<web-service><error msg=\"" + errorMsg + "\"/></web-service>"; //$NON-NLS-1$ //$NON-NLS-2$
  }
}
