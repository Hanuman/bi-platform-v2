/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 12, 2005 
 * @author James Dixon, Angelo Rodriguez, Steven Barkdull
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.api.repository.SolutionRepositoryServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SoapHelper;
import org.pentaho.platform.engine.services.WebServiceUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.w3c.dom.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.tree.DefaultElement;
import org.dom4j.Element;

public class SolutionRepositoryService extends ServletBase {

  private static final long serialVersionUID = -5870073658756939643L;
  private static final Log logger = LogFactory.getLog(SolutionRepositoryService.class);

  @Override
  public Log getLogger() {
    return SolutionRepositoryService.logger;
  }

  public SolutionRepositoryService() {
    super();
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    PentahoSystem.systemEntryPoint();
    OutputStream outputStream = response.getOutputStream();
    try {
      boolean wrapWithSoap = "false".equals(request.getParameter("ajax")); //$NON-NLS-1$ //$NON-NLS-2$
      String component = request.getParameter("component"); //$NON-NLS-1$
      String responseEncoding = PentahoSystem.getSystemSetting("web-service-encoding", "utf-8").toUpperCase();
      response.setContentType("text/xml"); //$NON-NLS-1$
      response.setCharacterEncoding(responseEncoding);
      IPentahoSession userSession = getPentahoSession(request);
      // send the header of the message to prevent time-outs while we are working
      response.setHeader("expires", "0"); //$NON-NLS-1$ //$NON-NLS-2$

      dispatch(request, response, component, outputStream, responseEncoding, userSession, wrapWithSoap);

      /**
       * NOTE: PLEASE DO NOT CATCH Exception, since this is the super class of RuntimeException. We do NOT want to catch RuntimeException, only CHECKED
       * exceptions!
       */
    } catch (SolutionRepositoryServiceException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (PentahoAccessControlException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerConfigurationException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (ParserConfigurationException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (DocumentException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerFactoryConfigurationError ex) {
      commonErrorHandler(outputStream, ex.getException());
    } catch (IOException ex) {
      // Use debugErrorHandler for ioException
      debugErrorHandler(outputStream, ex);
    } finally {
      PentahoSystem.systemExitPoint();
    }
    if (ServletBase.debug) {
      debug(Messages.getInstance().getString("HttpWebService.DEBUG_WEB_SERVICE_END")); //$NON-NLS-1$
    }
  }

  /**
   * Used for logging exceptions that happen that aren't necessarily exceptional. It's common that IOExceptions will happen as people begin their transactions
   * and then abandon their web page by closing their browser or current tab. Logging each one fills up the server log needlessly. Setting to debug level allows
   * us visibility without compromising a production deployment.
   * 
   * @param outputStream
   * @param ex
   * @throws IOException
   */
  protected void debugErrorHandler(final OutputStream outputStream, final Exception ex) throws IOException {
    String msg = Messages.getInstance().getErrorString("SolutionRepositoryService.ERROR_0001_ERROR_DURING_SERVICE_REQUEST"); //$NON-NLS-1$;
    debug(msg, ex);
    XmlDom4JHelper.saveDom(WebServiceUtil.createErrorDocument(msg), outputStream, LocaleHelper.getSystemEncoding(), true);
  }

  protected void commonErrorHandler(final OutputStream outputStream, final Exception ex) throws IOException {
    String msg = Messages.getInstance().getErrorString("SolutionRepositoryService.ERROR_0001_ERROR_DURING_SERVICE_REQUEST"); //$NON-NLS-1$;
    error(msg, ex);
    XmlDom4JHelper.saveDom(WebServiceUtil.createErrorDocument(msg), outputStream, LocaleHelper.getSystemEncoding(), true);
  }

  protected String[] getFilters(final HttpServletRequest request) {
    String filter = request.getParameter("filter"); //$NON-NLS-1$
    List<String> filters = new ArrayList<String>();
    if (!StringUtils.isEmpty(filter)) {
      StringTokenizer st = new StringTokenizer(filter, "*.,"); //$NON-NLS-1$
      while (st.hasMoreTokens()) {
        filters.add(st.nextToken());
      }
    }

    return filters.toArray(new String[] {});
  }

  protected void dispatch(final HttpServletRequest request, final HttpServletResponse response, final String component,
      final OutputStream outputStream, String responseEncoding, final IPentahoSession userSession, final boolean wrapWithSOAP)
      throws IOException, SolutionRepositoryServiceException, PentahoAccessControlException,
      ParserConfigurationException, TransformerConfigurationException, TransformerException,
      TransformerFactoryConfigurationError, DocumentException {

    ISolutionRepositoryService service = PentahoSystem.get(ISolutionRepositoryService.class, userSession);
    IParameterProvider parameterProvider = new HttpRequestParameterProvider(request);
    org.dom4j.Document doc = null;
    
    if ("getSolutionRepositoryDoc".equals(component)) { //$NON-NLS-1$
      String[] filters = getFilters(request);
      doc = convertToDom4JDoc(service.getSolutionRepositoryDoc(userSession, filters));
    } else if ("getSolutionRepositoryFileDetails".equals(component)) { //$NON-NLS-1$
      String fullPath = request.getParameter("fullPath"); //$NON-NLS-1$
      doc = convertToDom4JDoc(service.getSolutionRepositoryFileDetails(userSession, fullPath));
    } else if ("createNewFolder".equals(component)) { //$NON-NLS-1$
      String solution = request.getParameter("solution"); //$NON-NLS-1$
      String path = request.getParameter("path"); //$NON-NLS-1$
      String name = request.getParameter("name"); //$NON-NLS-1$
      String desc = request.getParameter("desc"); //$NON-NLS-1$
      boolean result = service.createFolder(userSession, solution, path, name, desc);
      doc = createResultDocument(Boolean.toString(result));
    } else if ("delete".equals(component)) { //$NON-NLS-1$
      String solution = request.getParameter("solution"); //$NON-NLS-1$
      String path = request.getParameter("path"); //$NON-NLS-1$
      String name = request.getParameter("name"); //$NON-NLS-1$
      boolean result = service.delete(userSession, solution, path, name);
      doc = createResultDocument(Boolean.toString(result));
    } else if ("setAcl".equals(component)) { //$NON-NLS-1$
      String solution = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$ 
      String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$ 
      String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$
      String strAclXml = parameterProvider.getStringParameter("aclXml", null); //$NON-NLS-1$
      service.setAcl(solution, path, filename, strAclXml, userSession);
      doc = WebServiceUtil.createStatusDocument(Messages.getInstance().getString("AdhocWebService.ACL_UPDATE_SUCCESSFUL")); //$NON-NLS-1$
    } else if ("getAcl".equals(component)) { //$NON-NLS-1$
      String solution = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$ 
      String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$ 
      String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$
      try {
        doc = DocumentHelper.parseText(service.getAclXml(solution, path, filename, userSession));
      } catch (DocumentException e) {
        throw new RuntimeException(Messages.getInstance().getErrorString("HttpWebService.UNRECOGNIZED_COMPONENT_REQUEST", component)); //$NON-NLS-1$
      }
    } else {
      throw new RuntimeException(Messages.getInstance().getErrorString("HttpWebService.UNRECOGNIZED_COMPONENT_REQUEST", component)); //$NON-NLS-1$
    }
    if (doc != null) {
      doc = wrapWithSOAP ? SoapHelper.createSoapResponseDocument(doc) : doc;
      XmlDom4JHelper.saveDom(doc, outputStream, responseEncoding, true);
    }

  }

  public  org.dom4j.Document convertToDom4JDoc(final org.w3c.dom.Document doc) throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError, DocumentException {
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new StringWriter());
    TransformerFactory.newInstance().newTransformer().transform(source, result);
    String theXML = result.getWriter().toString();
    org.dom4j.Document dom4jDoc = DocumentHelper.parseText(theXML);
    return dom4jDoc;
  }
  
  private org.dom4j.Document createResultDocument(String result) {
    org.dom4j.Document doc = DocumentHelper.createDocument();
    Element rootElement = new DefaultElement("result");
    doc.setRootElement(rootElement);
    rootElement.setText(result);
    return doc;
  }
  
  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    doGet(request, response);
  }

  /**
   * @deprecated use ISolutionRepositoryService instead
   */
  public org.w3c.dom.Document getSolutionRepositoryDoc(IPentahoSession session, String[] filters)
  throws ParserConfigurationException {
    ISolutionRepositoryService service = PentahoSystem.get(ISolutionRepositoryService.class, session);
    return service.getSolutionRepositoryDoc(session, filters);
  }
  
  public static void writeDocument(final OutputStream outputStream, final org.w3c.dom.Document doc, final boolean wrapWithSOAP) throws IOException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new StringWriter());
    TransformerFactory.newInstance().newTransformer().transform(source, result);
    String theXML = result.getWriter().toString();
    writeString(outputStream, theXML, wrapWithSOAP);
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
}
