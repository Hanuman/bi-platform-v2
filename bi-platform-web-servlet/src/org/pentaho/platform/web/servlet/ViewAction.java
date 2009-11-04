/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Jul 12, 2005 
 * @author James Dixon
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
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
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.repository.IContentItem;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.runtime.ParameterManager;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

/**
 * Servlet Class
 * 
 * web.servlet name="ViewAction" display-name="Name for ViewAction" description="Description for ViewAction" web.servlet-mapping url-pattern="/ViewAction"
 * web.servlet-init-param name="A parameter" value="A value"
 */
public class ViewAction extends ServletBase {

  private static final long serialVersionUID = 4680027723733552639L;

  private static final Log logger = LogFactory.getLog(ViewAction.class);

  @Override
  public Log getLogger() {
    return ViewAction.logger;
  }

  /**
   * 
   */
  public ViewAction() {
    super();
  }

  protected boolean doBackgroundExecution(final HttpServletRequest request, final HttpServletResponse response,
      final IPentahoSession userSession) throws ServletException, IOException {
    if ("true".equals(request.getParameter("background"))) { //$NON-NLS-1$ //$NON-NLS-2$
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, userSession);
      if (backgroundExecutionHandler != null) {
        HttpRequestParameterProvider parameterProvider = new HttpRequestParameterProvider(request);
        String intro = ""; //$NON-NLS-1$
        String footer = ""; //$NON-NLS-1$
        IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession);
        if (templater != null) {
          String sections[] = templater.breakTemplate("template-dialog.html", "", userSession); //$NON-NLS-1$ //$NON-NLS-2$ 
          if ((sections != null) && (sections.length > 0)) {
            intro = sections[0];
          }
          if ((sections != null) && (sections.length > 1)) {
            footer = sections[1];
          }
        } else {
          intro = Messages.getInstance().getString("ViewAction.ERROR_0002_BAD_TEMPLATE_OBJECT"); //$NON-NLS-1$
        }

        response.getWriter().print(intro);
        String backgroundResponse = null;
        try {
          backgroundResponse = backgroundExecutionHandler.backgroundExecuteAction(userSession, parameterProvider);
        } catch (BackgroundExecutionException bex) {
          backgroundResponse = bex.getLocalizedMessage();
          response.getWriter().print(backgroundResponse);
          response.getWriter().print(footer);
          error(Messages.getInstance().getErrorString("ViewAction.ERROR_0004_UNABLE_TO_PERFORM_BACKGROUND_EXECUTION"), bex); //$NON-NLS-1$
          return false;
        }
        response.setHeader("background_execution", "true");
        response.getWriter().print(backgroundResponse);
        response.getWriter().print(footer);
        return true;
      } else {
        error(Messages.getInstance().getErrorString("ViewAction.ERROR_0001_BACKGROUND_EXECUTE_NOT_SUPPORTED")); //$NON-NLS-1$
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
  
  protected boolean hasResponse(IRuntimeContext runtime) {
    boolean hasResponse = false;
    Map returnParamMap = runtime.getParameterManager().getReturnParameters();
    for (Iterator it = returnParamMap.entrySet().iterator(); it.hasNext();) {
      Map.Entry mapEntry = (Map.Entry) it.next();
      ParameterManager.ReturnParameter returnParam = (ParameterManager.ReturnParameter) mapEntry.getValue();
      if (returnParam != null && "response".equals(returnParam.destinationName)) {
        hasResponse = true;
      }
    }
    return hasResponse;
  }

  protected void handleActionRequest(final HttpServletRequest request, final HttpServletResponse response,
      final IOutputHandler outputHandler, final HttpServletRequestHandler requestHandler, OutputStream outputStream,
      final IContentItem contentItem) throws ServletException, IOException {
    IRuntimeContext runtime = null;
    try {
      runtime = requestHandler.handleActionRequest(0, 0);

      /*
       * the flag "hasResponse" should be set if the outputHandler is expected to serve a response back
       * via either the "response.content" output (a final content output), or an intermediate response
       * such as a form to request parameters such as from a SecureFilterComponent.
       */
      boolean hasResponse = outputHandler.isResponseExpected();
      IContentItem responseContentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT, null, null, null);

      boolean success = (runtime.getStatus() == IRuntimeContext.RUNTIME_STATUS_SUCCESS);
      boolean debugMessages = doMessages(request);
      boolean printSuccess = (runtime != null) && success && (!hasResponse || debugMessages);
      boolean printError = (runtime != null) && !success && !response.isCommitted();
      
      if(printSuccess || printError) {
        final String htmlMimeType = "text/html"; //$NON-NLS-1$
        responseContentItem.setMimeType(htmlMimeType);
        //this is going to be the response output stream unless you are in debug mode
        outputStream = responseContentItem.getOutputStream(null);

        response.setContentType(htmlMimeType);
        StringBuffer buffer = new StringBuffer();
        
        IMessageFormatter formatter = PentahoSystem.get(IMessageFormatter.class, PentahoHttpSessionHelper.getPentahoSession(request));
        
        if(printSuccess) {
          boolean doWrapper = !("false".equals(request.getParameter("wrapper"))); //$NON-NLS-1$ //$NON-NLS-2$
          formatter.formatSuccessMessage(htmlMimeType, runtime, buffer, debugMessages, doWrapper);
        }else {
          response.resetBuffer();
          formatter.formatFailureMessage(htmlMimeType, runtime, buffer, requestHandler.getMessages());
        }

        outputStream.write(buffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
        responseContentItem.closeOutputStream();
      }
    } finally {
      if (runtime != null) {
        runtime.dispose();
      }
    }
  }

  protected void setupRequestHandler(final HttpServletRequest request, final HttpServletRequestHandler requestHandler) {
    String prompt = request.getParameter("prompt"); //$NON-NLS-1$
    String solutionName = request.getParameter("solution"); //$NON-NLS-1$
    String actionPath = request.getParameter("path"); //$NON-NLS-1$
    String actionName = request.getParameter("action"); //$NON-NLS-1$
    String processId = this.getClass().getName();
    String instanceId = request.getParameter("instance-id"); //$NON-NLS-1$
    requestHandler.setInstanceId(instanceId);
    requestHandler.setProcessId(processId);
    requestHandler.setAction(actionPath, actionName);
    requestHandler.setSolutionName(solutionName);
    requestHandler.setForcePrompt((prompt != null) && prompt.equalsIgnoreCase("yes")); //$NON-NLS-1$
  }

  protected void setupOutputHandler(final HttpOutputHandler outputHandler, final IParameterProvider requestParameters) {
    int outputPreference = IOutputHandler.OUTPUT_TYPE_DEFAULT;
    if (doSubscribe(requestParameters)) {
      outputPreference = IOutputHandler.OUTPUT_TYPE_PARAMETERS;
    }
    outputHandler.setOutputPreference(outputPreference);
  }

  protected HttpServletRequestHandler getRequestHandler(final HttpServletRequest request,
      final HttpServletResponse response, final IPentahoSession userSession,
      final IParameterProvider requestParameters, final OutputStream outputStream,
      final HttpOutputHandler outputHandler, final SimpleUrlFactory urlFactory) throws ServletException, IOException {
    HttpServletRequestHandler requestHandler = new HttpServletRequestHandler(userSession, null, request, outputHandler,
        urlFactory);
    setupRequestHandler(request, requestHandler, requestParameters, userSession);
    return requestHandler;
  }

  protected HttpOutputHandler createOutputHandler(final HttpServletResponse response, final OutputStream outputStream) {
    return new HttpOutputHandler(response, outputStream, true);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {
    PentahoSystem.systemEntryPoint();
    try {
      IPentahoSession userSession = getPentahoSession(request);
      if (!doBackgroundExecution(request, response, userSession)) {
        OutputStream outputStream = getOutputStream(response, doMessages(request));
        ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
        String solutionName = request.getParameter("solution"); //$NON-NLS-1$
        String actionPath = request.getParameter("path"); //$NON-NLS-1$
        String actionName = request.getParameter("action"); //$NON-NLS-1$
        IActionSequence actionSequence = repository.getActionSequence(solutionName, actionPath, actionName,
            PentahoSystem.loggingLevel, ISolutionRepository.ACTION_EXECUTE);
        String fileName = null;
        if (actionSequence != null) {
          String title = actionSequence.getTitle();
          if ((title != null) && (title.length() > 0)) {
            fileName = title;
          } else {
            String sequenceName = actionSequence.getSequenceName();

            if ((sequenceName != null) && (sequenceName.length() > 0)) {
              fileName = sequenceName;
            } else {
              List actionDefinitionsList = actionSequence.getActionDefinitionsAndSequences();
              int i = 0;
              boolean done = false;

              while ((actionDefinitionsList.size() > i) && !done) {
                IActionDefinition actionDefinition = (IActionDefinition) actionDefinitionsList.get(i);
                String componentName = actionDefinition.getComponentName();
                if ((componentName != null) && (componentName.length() > 0)) {
                  fileName = componentName;
                  done = true;
                } else {
                  i++;
                }
              }
            }
          }
        }

        HttpOutputHandler outputHandler = createOutputHandler(response, outputStream);
        outputHandler.setSession(userSession);

        IMimeTypeListener listener = new HttpMimeTypeListener(request, response, fileName);
        outputHandler.setMimeTypeListener(listener);
        SimpleUrlFactory urlFactory = new SimpleUrlFactory(PentahoSystem.getApplicationContext().getBaseUrl()
            + "ViewAction?"); //$NON-NLS-1$
        IParameterProvider requestParameters = new HttpRequestParameterProvider(request);
        setupOutputHandler(outputHandler, requestParameters);
        if (!handleSubscriptions(request, response, userSession, requestParameters, outputStream, urlFactory)) {
          HttpServletRequestHandler requestHandler = getRequestHandler(request, response, userSession,
              requestParameters, outputStream, outputHandler, urlFactory);
          handleActionRequest(request, response, outputHandler, requestHandler, outputStream, null);
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

  ///////////////////// Merge
  protected boolean doSubscribe(final IParameterProvider requestParameters) {
    return requestParameters.getStringParameter("subscribepage", "no").equalsIgnoreCase("yes"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
      HttpOutputHandler outputHandler = new HttpOutputHandler(response, outputStream, true);
      IContentItem contentItem = outputHandler.getOutputContentItem(IOutputHandler.RESPONSE, IOutputHandler.CONTENT,
          null, null, "text/html"); //$NON-NLS-1$
      outputStream = contentItem.getOutputStream(name);
      String resp = null;
      try {
        resp = SubscriptionHelper.createSubscriptionArchive(name, userSession, null, sessionParameters);
      } catch (BackgroundExecutionException bex) {
        resp = bex.getLocalizedMessage();
        error(Messages.getInstance().getErrorString("ViewAction.ERROR_0003_UNABLE_TO_CREATE_SUBSCRIPTION_ARCHIVE")); //$NON-NLS-1$
        outputStream.write(resp.getBytes());
        contentItem.closeOutputStream();
        return false;
      }

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

  protected void setupRequestHandler(final HttpServletRequest request, final HttpServletRequestHandler requestHandler,
      final IParameterProvider requestParameters, final IPentahoSession userSession) {
    this.setupRequestHandler(request, requestHandler);
    // first take a secondary action
    String actionName = requestParameters.getStringParameter("action2", null); //$NON-NLS-1$
    if (actionName == null) {
      // now look for a primary action
      actionName = requestParameters.getStringParameter("action", null); //$NON-NLS-1$
    }
    /*
    // TODO: DM test code
    if (ISolutionEngine.RUNTIME_SOLUTION_NAME.equals(requestHandler.getSolutionName()) && "preview.xaction".equalsIgnoreCase(actionName)) {
      requestHandler.setActionPath(getActionSequence(userSession));
    }
    */

    //Proposed fix for bug BISERVER-97 by Ezequiel Cuellar
    //Changed to set parameterXsl from the value specified specified in the Pentaho.xml tag "default-parameter-xsl"
    //Proposed fix for bug BISERVER-238 by Ezequiel Cuellar
    //Code refactoring. DefaultParameterForm.xsl was always getting set so I just refactored the code
    //by adding a default value of DefaultParameterForm.xsl when getting the value of default-parameter-xsl
    ISystemSettings systemSettings = PentahoSystem.getSystemSettings();
    String defaultParameterXsl = systemSettings.getSystemSetting("default-parameter-xsl", "DefaultParameterForm.xsl"); //$NON-NLS-1$ //$NON-NLS-2$
    requestHandler.setParameterXsl(defaultParameterXsl);
    if (doSubscribe(requestParameters)) {
      requestHandler.setForcePrompt(true);
      requestHandler.setParameterProvider("PRO_EDIT_SUBSCRIPTION", requestParameters); //$NON-NLS-1$ 
    }
  }

  /*
  // TODO: Test Code Only!!! DM - returns an actionsequence as a string
  private String getActionSequence(IPentahoSession session) {
    try {
      return (PentahoSystem.get(ISolutionRepository.class, session).getResourceAsString("test/metadata/EmbeddedReport.xaction")); //$NON-NLS-1$
    } catch (Throwable t) {
    }
    return (""); //$NON-NLS-1$
    // File f = new File( "test/metadata/EmbeddedReport.xaction" );
    // return( FileHelper.getStringFromFile(f) );
  }
  */

}