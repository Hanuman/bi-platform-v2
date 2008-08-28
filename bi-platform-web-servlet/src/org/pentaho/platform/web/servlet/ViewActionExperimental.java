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
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.actionsequence.dom.IActionDefinition;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ActionSequenceContentGenerator;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction" web.servlet-mapping url-pattern="/ViewAction"
 * web.servlet-init-param name="A parameter" value="A value"
 */
public class ViewActionExperimental extends ServletBase {

  private static final long serialVersionUID = 4680027723733552639L;

  private static final Log logger = LogFactory.getLog(ViewAction.class);

  @Override
  public Log getLogger() {
    return ViewActionExperimental.logger;
  }

  /**
   * 
   */
  public ViewActionExperimental() {
    super();
  }

  protected void setupSession(final IPentahoSession userSession) {

    PentahoSystem.sessionStartup(userSession);

  }

  protected void removeUserSession(final IPentahoSession userSession) {
    userSession.destroy();
  }

  protected boolean doBackgroundExecution(final HttpServletRequest request, final HttpServletResponse response,
      final IPentahoSession userSession) throws ServletException, IOException {
    if ("true".equals(request.getParameter("background"))) { //$NON-NLS-1$ //$NON-NLS-2$
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.getBackgroundExecutionHandler(userSession);
      if (backgroundExecutionHandler != null) {
        HttpRequestParameterProvider parameterProvider = new HttpRequestParameterProvider(request);
        String backgroundResponse = backgroundExecutionHandler.backgroundExecuteAction(userSession, parameterProvider);
        String intro = ""; //$NON-NLS-1$
        String footer = ""; //$NON-NLS-1$
        IUITemplater templater = PentahoSystem.getUITemplater(userSession);
        if (templater != null) {
          String sections[] = templater.breakTemplate("template-dialog.html", "", userSession); //$NON-NLS-1$ //$NON-NLS-2$ 
          if ((sections != null) && (sections.length > 0)) {
            intro = sections[0];
          }
          if ((sections != null) && (sections.length > 1)) {
            footer = sections[1];
          }
        } else {
          intro = Messages.getString("ViewAction.ERROR_0002_BAD_TEMPLATE_OBJECT"); //$NON-NLS-1$
        }

        response.getWriter().print(intro);
        response.getWriter().print(backgroundResponse);
        response.getWriter().print(footer);
        return true;
      } else {
        error(Messages.getErrorString("ViewAction.ERROR_0001_BACKGROUND_EXECUTE_NOT_SUPPORTED")); //$NON-NLS-1$
      }
    }
    return false;
  }

  protected OutputStream getOutputStream(final HttpServletResponse response, final boolean doMessages)
      throws ServletException, IOException {
    OutputStream outputStream = null;
    if (doMessages) {
      outputStream = new ByteArrayOutputStream();
    } else {
      outputStream = response.getOutputStream();
    }

    return outputStream;
  }

  protected boolean doMessages(final HttpServletRequest request) {
    return "true".equalsIgnoreCase(request.getParameter("debug")); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected void handleActionRequest(final HttpServletRequest request, final HttpServletResponse response,
      final HttpOutputHandler outputHandler, OutputStream outputStream,
      final IContentItem contentItem, final IPentahoSession userSession) throws ServletException, IOException {
    try {

    	// create the parameter providers
        HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
        HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider(request);
        Map<String,IParameterProvider> parameterProviders = new HashMap<String,IParameterProvider>();
        parameterProviders.put( IParameterProvider.SCOPE_REQUEST , requestParameters);
        parameterProviders.put( IParameterProvider.SCOPE_SESSION , sessionParameters);
        
        // create the URL factory
        SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl()
                + "ViewAction?"); //$NON-NLS-1$

        // create the message list
    	List<String> messages = new ArrayList<String>();
    	
    	// create the content generator and give it the things it needs
    	IContentGenerator contentGenerator = new ActionSequenceContentGenerator();
    	contentGenerator.setOutputHandler(outputHandler);
    	contentGenerator.setMessagesList(messages);
    	contentGenerator.setParameterProviders(parameterProviders);
    	contentGenerator.setSession(userSession);
    	contentGenerator.setUrlFactory(urlFactory);
    	contentGenerator.createContent();
    	
    } catch (Exception e) {
    }
    if (contentItem != null) {
      contentItem.closeOutputStream();
    }
  }

  protected HttpOutputHandler createOutputHandler(final HttpServletResponse response, final OutputStream outputStream) {
    return new HttpOutputHandler(response, outputStream, true);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    PentahoSystem.systemEntryPoint();
    IContentItem contentItem = null;
    try {
      IPentahoSession userSession = getPentahoSession(request);
      if (!doBackgroundExecution(request, response, userSession)) {
        OutputStream outputStream = getOutputStream(response, doMessages(request));
//        ISolutionRepository repository = PentahoSystem.getSolutionRepository(userSession);
//        String solutionName = request.getParameter("solution"); //$NON-NLS-1$
 //       String actionPath = request.getParameter("path"); //$NON-NLS-1$
//        String actionName = request.getParameter("action"); //$NON-NLS-1$
 //       IActionSequence actionSequence = repository.getActionSequence(solutionName, actionPath, actionName,
 //           PentahoSystem.loggingLevel, ISolutionRepository.ACTION_EXECUTE);

        HttpOutputHandler outputHandler = createOutputHandler(response, outputStream);
        outputHandler.setSession(userSession);
//        if ((contentItem != null) && (fileName != null)) {
//          outputHandler.setOutputContent(contentItem);
//        }

        IMimeTypeListener listener = new HttpMimeTypeListener(request, response);
        outputHandler.setMimeTypeListener(listener);
        SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl()
            + "ViewAction?"); //$NON-NLS-1$
        IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
        if (!handleSubscriptions(request, response, userSession, requestParameters, outputStream, urlFactory)) {
          handleActionRequest(request, response, outputHandler, outputStream, contentItem, userSession );
        }
      }
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {

    doGet(request, response);
  }

  protected boolean handleSubscriptions(final HttpServletRequest request, final HttpServletResponse response,
      final IPentahoSession userSession, final IParameterProvider requestParameters, OutputStream outputStream,
      final SimpleUrlFactory urlFactory) throws ServletException, IOException {
    // see if we have any subscription information to process
    String subscribeAction = request.getParameter("subscribe"); //$NON-NLS-1$

    if ("run".equals(subscribeAction)) { //$NON-NLS-1$
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
      HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
      SubscriptionHelper.runSubscription(name, userSession, sessionParameters, urlFactory, outputHandler);
      return true;
    } else if ("archived".equals(subscribeAction)) { //$NON-NLS-1$
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      int pos = name.lastIndexOf(':');
      if (pos != -1) {
        String fileId = name.substring(pos + 1);
        name = name.substring(0, pos);
        HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
        SubscriptionHelper.getArchived(name, fileId, userSession, outputHandler);
      }
      return true;
    } else if ("archive".equals(subscribeAction)) { //$NON-NLS-1$
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider(userSession);
      String resp = SubscriptionHelper.createSubscriptionArchive(name, userSession, null, sessionParameters);
      HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
      IContentItem contentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT,
          null, null, "text/html"); //$NON-NLS-1$
      outputStream = contentItem.getOutputStream(name);
      outputStream.write(resp.getBytes());
      contentItem.closeOutputStream();
      return true;
    } else if ("save".equals(subscribeAction)) { //$NON-NLS-1$
      String solutionName = requestParameters.getStringParameter("solution", null); //$NON-NLS-1$
      String actionPath = requestParameters.getStringParameter("path", null); //$NON-NLS-1$
      String actionName = requestParameters.getStringParameter("action", null); //$NON-NLS-1$
      String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
      // HttpSessionParameterProvider sessionParameters = new
      // HttpSessionParameterProvider( userSession );
      String result = SubscriptionHelper.saveSubscription(requestParameters, actionReference, userSession);
      outputStream.write(result.getBytes());
      return true;
    } else if ("edit".equals(subscribeAction)) { //$NON-NLS-1$
      // TODO
      // get the action information from the subscription
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$ 
      SubscriptionHelper.editSubscription(name, userSession, urlFactory, outputStream);
      /*
       * 
       * SimpleParameterSetter parameters = new SimpleParameterSetter(); String result = SubscriptionHelper.getSubscriptionParameters( name, parameters,
       * userSession ); outputPreference = IOutputHandler.OUTPUT_TYPE_PARAMETERS; requestParameters = parameters; SubscriptionHelper.editSubscription if( result !=
       * null ) { outputStream.write(result.getBytes()); return; }
       */
      return true;
    } else if ("delete".equals(subscribeAction)) { //$NON-NLS-1$
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      String result = SubscriptionHelper.deleteSubscription(name, userSession);
      outputStream.write(result.getBytes());
      return true;
    } else if ("delete-archived".equals(subscribeAction)) { //$NON-NLS-1$
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      int pos = name.lastIndexOf(':');
      if (pos != -1) {
        String fileId = name.substring(pos + 1);
        name = name.substring(0, pos);
        String result = SubscriptionHelper.deleteSubscriptionArchive(name, fileId, userSession);
        outputStream.write(result.getBytes());
        return true;
      }
    }
    return false;
  }
  
  /*
  // TODO: Test Code Only!!! DM - returns an actionsequence as a string
  private String getActionSequence(IPentahoSession session) {
    try {
      return (PentahoSystem.getSolutionRepository(session).getResourceAsString("test/metadata/EmbeddedReport.xaction")); //$NON-NLS-1$
    } catch (Throwable t) {
    }
    return (""); //$NON-NLS-1$
    // File f = new File( "test/metadata/EmbeddedReport.xaction" );
    // return( FileHelper.getStringFromFile(f) );
  }
  */

}