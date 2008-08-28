/*
 * Copyright 2005 Pentaho Corporation.  All rights reserved. 
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
 * @created Jan 10, 2006
 */
package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.messages.Messages;


/**
 * Provides a web interface for the management of users. The service should be
 * called by external systems to notify Pentaho when a user has been remove,
 * allowing for proper clean-up of objects owned by the user.
 * <p>
 * <b>Parameters</b> <table cellspacing='5' cellpadding='3'>
 * <tr>
 * <td valign='top'>command</td>
 * <td valign='top'>the command to exeucte. The only valid value is <i>delete</i>.</td>
 * <td valign='top'>required</td>
 * </tr>
 * <tr>
 * <td valign='top'>user</td>
 * <td valign='top'>the user name of the user for whom the action is taken. For
 * example, this would be the user to delete.</td>
 * <td valign='top'>required</td>
 * </tr>
 * <tr>
 * <td valign='top'>password</td>
 * <td valign='top'>the password for authenticating the system invoking the
 * call. This password must match the value of the <i>parameter</i> optional
 * initialization parameter for the servlet and specified in the web.xml.</td>
 * <td valign='top'>optional</td>
 * </tr>
 * </table>
 * 
 * @author Anthony de Shazor
 * 
 */
public class SubscriptionUserCleanup extends ServletBase {

  private static final int SERVICE_SUCCESS = 0;

  private static final int SERVICE_FAILURE = 1;

  private static final int REQUEST_FAILURE = 2;

  private static final long serialVersionUID = -3249803751706281261L;

  private static final Log logger = LogFactory.getLog(SubscriptionUserCleanup.class);

  private String password;

  /**
   * Constructs an instance.
   */
  public SubscriptionUserCleanup() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.ui.servlet.ServletBase#getLogger()
   */
  @Override
  public Log getLogger() {
    return SubscriptionUserCleanup.logger;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
   */
  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);

    password = config.getInitParameter("password"); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

    PentahoSystem.systemEntryPoint();

    try {
      String userName;
      String passwordParam;
      String command;
      Element soap;

      command = request.getParameter("command"); //$NON-NLS-1$
      userName = request.getParameter("user"); //$NON-NLS-1$
      passwordParam = request.getParameter("password"); //$NON-NLS-1$

      if (command == null) {
        soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.REQUEST_FAILURE, Messages.getErrorString("ManageUsers.ERROR_0004_MISSING_COMMAND")); //$NON-NLS-1$                
      } else if (userName == null) {
        soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.REQUEST_FAILURE, Messages.getErrorString("ManageUsers.ERROR_0001_MISSING_USERNAME")); //$NON-NLS-1$
      } else if ((password != null) && (password.length() > 0) && !password.equals(passwordParam)) {
        soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.REQUEST_FAILURE, Messages
            .getErrorString("ManageUsers.ERROR_0002_UNAUTHORIZED_ACCESS")); //$NON-NLS-1$
      } else {
        if ("delete".equals(command)) { //$NON-NLS-1$
          soap = deleteUser(request, userName);
        } else {
          soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.REQUEST_FAILURE, Messages.getErrorString(
              "ManageUsers.ERROR_0005_UNKNOWN_COMMAND", command)); //$NON-NLS-1$                                    
        }
      }

      generateResponse(response, soap);
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  private void generateResponse(final HttpServletResponse response, final Element soap) throws IOException {
    Writer output;
    XMLWriter xmlWriter;
    OutputFormat format;

    response.setContentType("text/xml"); //$NON-NLS-1$
    response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

    output = response.getWriter();
    format = OutputFormat.createCompactFormat();
    xmlWriter = new XMLWriter(output, format);
    xmlWriter.write(soap);
  }

  private Element deleteUser(final HttpServletRequest request, final String user) {
    Element soap = null;

    try {
      ISubscriptionRepository repository;
      IPentahoSession pentahoSession;

      pentahoSession = getPentahoSession(request);
      repository = PentahoSystem.getSubscriptionRepository(pentahoSession);
      if (repository == null) {
        soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.SERVICE_FAILURE, Messages.getErrorString("ManageUsers.ERROR_0006_REPOSITORY_ERROR")); //$NON-NLS-1$                
      } else {
        repository.deleteUserSubscriptions(user);
        soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.SERVICE_SUCCESS, user);
      }
    } catch (Exception ex) {
      String message = ex.toString();

      soap = SubscriptionUserCleanup.generateSOAPMessage(SubscriptionUserCleanup.SERVICE_FAILURE, Messages.getErrorString(
          "ManageUsers.ERROR_0003_SUBSCRIPTION_DELETE_FAILURE_MESSAGE", user, message)); //$NON-NLS-1$                        
    }

    return soap;
  }

  private static Element generateSOAPMessage(final int status, final String message) {
    DocumentFactory factory = DocumentFactory.getInstance();
    Document soap = factory.createDocument();
    Element envelope;
    Element body;
    QName qname;
    Namespace namespace;

    namespace = new Namespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/"); //$NON-NLS-1$ //$NON-NLS-2$        
    envelope = soap.addElement(new QName("Envelope", namespace)); //$NON-NLS-1$
    qname = new QName("encodingStyle", namespace); //$NON-NLS-1$
    envelope.addAttribute(qname, "http://schemas.xmlsoap.org/soap/encoding/"); //$NON-NLS-1$
    body = envelope.addElement(new QName("Body", namespace)); //$NON-NLS-1$

    if (status == SubscriptionUserCleanup.SERVICE_SUCCESS) {
      Element response = body.addElement("ManageUsers-response"); //$NON-NLS-1$
      response.addCDATA(message);
    } else {
      Element fault = body.addElement(new QName("Fault", namespace)); //$NON-NLS-1$
      Element code = fault.addElement(new QName("Code", namespace)); //$NON-NLS-1$
      Element value = code.addElement(new QName("Value", namespace)); //$NON-NLS-1$
      Element subcode;
      Element reason;
      Element text;

      if (status == SubscriptionUserCleanup.REQUEST_FAILURE) {
        value.setText("SOAP-ENV:Sender"); //$NON-NLS-1$
      } else {
        value.setText("SOAP-ENV:Reciever"); //$NON-NLS-1$
      }

      subcode = code.addElement(new QName("Subcode", namespace)); //$NON-NLS-1$
      value = subcode.addElement(new QName("Value", namespace)); //$NON-NLS-1$
      value.addCDATA(message);
      reason = fault.addElement(new QName("Reason", namespace)); //$NON-NLS-1$
      text = reason.addElement(new QName("Text", namespace)); //$NON-NLS-1$
      text.addAttribute("lang", LocaleHelper.getDefaultLocale().toString()); //$NON-NLS-1$
      text.addCDATA(message);

      // Add empty Detail section
      fault.addElement(new QName("Detail", namespace)); //$NON-NLS-1$
    }

    return envelope;
  }
}
