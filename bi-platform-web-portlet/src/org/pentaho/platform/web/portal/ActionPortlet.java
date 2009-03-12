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
 * @created Sep 22, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IBackgroundExecution;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IUITemplater;
import org.pentaho.platform.api.scheduler.BackgroundExecutionException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.subscription.SubscriptionHelper;
import org.pentaho.platform.uifoundation.component.ActionComponent;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.portal.messages.Messages;

public class ActionPortlet extends ViewPortlet {

  private final static String ACTION = "action"; //$NON-NLS-1$

  private static final Log portletLogger = LogFactory.getLog(ActionPortlet.class);

  @Override
  public Log getLogger() {
    return ActionPortlet.portletLogger;
  }

  @Override
  public void processPortletAction(final ActionRequest request, final ActionResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);
    String solutionName = requestParameters.getStringParameter("solution", null); //$NON-NLS-1$
    String actionPath = requestParameters.getStringParameter("path", null); //$NON-NLS-1$
    String actionName = requestParameters.getStringParameter("action", null); //$NON-NLS-1$
    String subscribeAction = requestParameters.getStringParameter("subscribe", null); //$NON-NLS-1$
    if (subscribeAction != null) {
      String key = "pentaho-message-" + getPortletName(); //$NON-NLS-1$
      if ("save".equals(subscribeAction)) { //$NON-NLS-1$
        String actionReference = solutionName + "/" + actionPath + "/" + actionName; //$NON-NLS-1$ //$NON-NLS-2$
        String result = SubscriptionHelper.saveSubscription(requestParameters, actionReference, userSession);
        userSession.setAttribute(key, result, PortletSession.PORTLET_SCOPE);
      } else if ("delete".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        String result = SubscriptionHelper.deleteSubscription(name, userSession);
        userSession.setAttribute(key, result, PortletSession.PORTLET_SCOPE);
      } else if ("edit".equals(subscribeAction)) { //$NON-NLS-1$
        // TODO
      } else if ("archive".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);
        String result = null;
        try {
          result = SubscriptionHelper.createSubscriptionArchive(name, userSession, null, sessionParameters);  
        } catch(BackgroundExecutionException bex) {
          result = bex.getLocalizedMessage();
          userSession.setAttribute(key, result, PortletSession.PORTLET_SCOPE);
        }
        userSession.setAttribute(key, result, PortletSession.PORTLET_SCOPE);
      } else if ("delete-archived".equals(subscribeAction)) { //$NON-NLS-1$
        String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
        int pos = name.lastIndexOf(':');
        if (pos != -1) {
          String fileId = name.substring(pos + 1);
          name = name.substring(0, pos);
          String result = SubscriptionHelper.deleteSubscriptionArchive(name, fileId, userSession);
          userSession.setAttribute(key, result, PortletSession.PORTLET_SCOPE);
        }
      }

    }

  }

  @Override
  public void doPortletView(final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession)
      throws PortletException, IOException {

    // we don't want to use a portlet url factory as we are probably
    // generating a popup window
    // TODO make this configurable

    String key = "pentaho-message-" + getPortletName(); //$NON-NLS-1$
    String message = (String) userSession.getAttribute(key, PortletSession.PORTLET_SCOPE);
    int outputPreference = IOutputHandler.OUTPUT_TYPE_DEFAULT;
    PortletRequestParameterProvider requestParameters = new PortletRequestParameterProvider(request);

    String subscribeAction = requestParameters.getStringParameter("subscribe", null); //$NON-NLS-1$
    if ("edit".equals(subscribeAction)) { //$NON-NLS-1$
      // TODO
      // get the action information from the subscription
      String name = requestParameters.getStringParameter("subscribe-name", null); //$NON-NLS-1$
      message = SubscriptionHelper.getSubscriptionParameters(name, requestParameters, userSession);
      outputPreference = IOutputHandler.OUTPUT_TYPE_PARAMETERS;
    }

    if (message != null) {
      response.setContentType("text/html"); //$NON-NLS-1$
      PrintWriter writer = response.getWriter();
      writer.println("<span class=\"portlet-font\">Message: " + message + "</span>"); //$NON-NLS-1$ //$NON-NLS-2$
      userSession.removeAttribute("pentaho-message", PortletSession.PORTLET_SCOPE); //$NON-NLS-1$
    }

    doPortletView(outputPreference, requestParameters, request, response, userSession);
  }

  public void doPortletView(final int outputPreference, final PortletRequestParameterProvider requestParameters,
      final RenderRequest request, final RenderResponse response, final PentahoPortletSession userSession) throws IOException {

    // we don't want to use a portlet url factory as we are probably
    // generating a popup window
    // TODO make this configurable

    PortletUrlFactory urlFactory = new PortletUrlFactory(response, request.getWindowState(), request.getPortletMode());
    // SimpleUrlFactory urlFactory = new SimpleUrlFactory(
    // PentahoSystem.getApplicationContext().getBaseUrl() + "ViewAction?" );
    // //$NON-NLS-1$
    PortletPreferences prefs = request.getPreferences();
    PortletPreferencesParameterProvider portletPrefsParameters = new PortletPreferencesParameterProvider(prefs);
    PortletSessionParameterProvider sessionParameters = new PortletSessionParameterProvider(userSession);

    String actionString = request.getParameter(ActionPortlet.ACTION);
    if (actionString == null) {
      // get the default value from the preferences
      actionString = prefs.getValue(ActionPortlet.ACTION, null);
      if (actionString != null) {
        requestParameters.setParameter(ActionPortlet.ACTION, actionString);
        //portletPrefsParameters.setParameter(ACTION, actionString);
      }
    }
    String backgroundExecution = request.getParameter("background"); //$NON-NLS-1$
    if (backgroundExecution == null) {
      backgroundExecution = prefs.getValue("background", null); //$NON-NLS-1$
      if (backgroundExecution != null) {
        requestParameters.setParameter("background", backgroundExecution); //$NON-NLS-1$
      }
    }
    // TODO sbarkdull, not sure the next 4 lines are needed 
    //        String solutionName = request.getParameter("solution");
    //        String actionName = actionString;
    //        String actionPath = request.getParameter("path");
    //        if (actionString != null && solutionName == null && actionPath == null ) {

    String solutionName = null;
    String actionName = null;
    String actionPath = null;
    if (actionString != null) {
    	ActionInfo info = ActionInfo.parseActionString(actionString);
      if (info != null) {
        solutionName = info.getSolutionName();
        actionPath = info.getPath();
        actionName = info.getActionName();
      }

    }

    if ((actionString == null) || (solutionName == null) || (actionPath == null) || (actionName == null)) {
      error(Messages.getString("ActionPortlet.ERROR_0001_COULD_NOT_PARSE_ACTION")); //$NON-NLS-1$
      PrintWriter writer = response.getWriter();
      response.setContentType("text/html"); //$NON-NLS-1$
      writer.print(Messages.getString("ActionPortlet.ERROR_0001_COULD_NOT_PARSE_ACTION")); //$NON-NLS-1$
      return;

    }
    if ("true".equals(backgroundExecution)) { //$NON-NLS-1$
      IBackgroundExecution backgroundExecutionHandler = PentahoSystem.get(IBackgroundExecution.class, userSession);
      if (backgroundExecutionHandler != null) {
        PortletRequestParameterProvider parameterProvider = new PortletRequestParameterProvider(request);
        parameterProvider.setParameter("solution", solutionName); //$NON-NLS-1$
        parameterProvider.setParameter("path", actionPath); //$NON-NLS-1$
        parameterProvider.setParameter("action", actionName); //$NON-NLS-1$
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
          intro = Messages.getString("UI.ERROR_0002_BAD_TEMPLATE_OBJECT"); //$NON-NLS-1$
        }
        
        PrintWriter writer = response.getWriter();
        writer.print(intro);
        
        String backgroundResponse = null;
        try  {
          backgroundResponse = backgroundExecutionHandler.backgroundExecuteAction(userSession, parameterProvider);  
        } catch(BackgroundExecutionException bex) {
          backgroundResponse = bex.getLocalizedMessage();
          writer.print(backgroundResponse);
          writer.print(footer);
          return;       
        }
        
        writer.print(backgroundResponse);
        writer.print(footer);
        return;
      }
    }

    ActionComponent component = new ActionComponent(solutionName, actionPath, actionName, null, outputPreference,
        urlFactory, null);
    component.setParameterProvider(HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters);
    component.setParameterProvider(IParameterProvider.SCOPE_SESSION, sessionParameters);
    component.setParameterProvider(PortletPreferencesParameterProvider.SCOPE_PORTLET_PREFERENCES,
        portletPrefsParameters);
    component.validate(userSession, null);

    String content = component.getContent("text/html"); //$NON-NLS-1$

    if (StringUtils.isEmpty(content)) {
      content = "&nbsp;"; //$NON-NLS-1$
    }
    response.setContentType("text/html"); //$NON-NLS-1$
    PrintWriter writer = response.getWriter();
    writer.print(content);

  }

}
