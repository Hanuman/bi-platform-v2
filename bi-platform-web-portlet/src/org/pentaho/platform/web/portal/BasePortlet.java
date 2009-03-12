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
 * @created Aug 2, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import java.io.IOException;
import java.util.MissingResourceException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.portal.messages.Messages;

public abstract class BasePortlet extends GenericPortlet implements ILogger {

  protected final static boolean debug = PentahoSystem.debug;

  protected String logId;

  protected static Log logger;

  private int logLevel = ILogger.DEBUG;

  public BasePortlet() {

  }

  public abstract Log getLogger();

  public String getLogId() {
    return logId;
  }

  public void setLogId(final String lId) {
    logId = lId;
  }

  @Override
  public void init() throws PortletException {

    try {
      if (logId == null) {
        logId = getClass().getName() + ":" + ILogger.SESSION_LOG + ":portlet: "; //$NON-NLS-1$//$NON-NLS-2$
      }

      // now call the init of the subclass
      initPortlet();
      BasePortlet.logger = getLogger();
    } catch (Throwable error) {
      // fixes for JIRA case 'PLATFORM-152'
      try {
        if (BasePortlet.logger == null) {
          BasePortlet.logger = getLogger();
        }
        if (BasePortlet.logger != null) {
          BasePortlet.logger.error(Messages.getErrorString("BasePortlet.ERROR_0002_COULD_NOT_INIT"), error); //$NON-NLS-1$
        } else {
          Logger.error(getClass().getName(), Messages.getErrorString("BasePortlet.ERROR_0002_COULD_NOT_INIT"), error); //$NON-NLS-1$
        }
      } catch (Throwable logError) {
        Logger.error(getClass().getName(), Messages.getErrorString("BasePortlet.ERROR_0002_COULD_NOT_INIT"), error); //$NON-NLS-1$
      }
      throw new PortletException(Messages.getErrorString("BasePortlet.ERROR_0002_COULD_NOT_INIT"), error); //$NON-NLS-1$
    }

  }

  public void initPortlet() {

  }

  public abstract void processPortletAction(ActionRequest request, ActionResponse response,
      PentahoPortletSession userSession) throws PortletException, java.io.IOException;

  public abstract void doPortletView(RenderRequest request, RenderResponse response, PentahoPortletSession userSession)
      throws PortletException, java.io.IOException;

  public abstract void doPortletHelp(RenderRequest request, RenderResponse response, PentahoPortletSession userSession)
      throws PortletException, java.io.IOException;

  public abstract void doPortletEdit(RenderRequest request, RenderResponse response, PentahoPortletSession userSession)
      throws PortletException, java.io.IOException;

  @Override
  public final void processAction(final ActionRequest request, final ActionResponse response) throws PortletException,
      java.io.IOException {

    // JIRA case #PLATFORM 150
    PentahoSystem.systemEntryPoint();

    try {
      PentahoPortletSession userSession = getPentahoSession(request);
      // PortletPreferences preferences = request.getPreferences();
      String user = request.getRemoteUser();

      if ((user != null) && !userSession.isAuthenticated()) {
        // the user was not logged in before but is now....
        userSession.setAuthenticated(user);
      }

      // TODO: perform validation and auditing of this action

      // now call the action of the subclass
      processPortletAction(request, response, userSession);
    } finally {
      // JIRA case #PLATFORM 150
      PentahoSystem.systemExitPoint();
    }

  }

  protected void setupSession(final PentahoPortletSession userSession) {

    PentahoSystem.sessionStartup(userSession);

  }

  @Override
  public final void doView(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {
    // JIRA case #PLATFORM 150
    PentahoSystem.systemEntryPoint();

    try {
      PortletSession session = request.getPortletSession(true);
      PentahoPortletSession userSession = getPentahoSession(request);

      // PortletPreferences preferences = request.getPreferences();
      String user = request.getRemoteUser();
      if ((user != null) && !userSession.isAuthenticated()) {
        // the user was not logged in before but is now....
        // Principal p = request.getUserPrincipal();
        // System .out.println("***** Portlet Principal: " + p);

        userSession.setAuthenticated(user);
        setupSession(userSession);
      } else if ((user == null) && userSession.isAuthenticated()) {
        // the user has logged out...
        userSession.setNotAuthenticated();
        removeUserSession(userSession);
      } else if ((user != null) && !userSession.getName().equals(user)) {
        // this is a different user
        removeUserSession(userSession);
        userSession = getPentahoSession(request);
      }

      // TODO: perform validation and auditing of this action

      // handle any message from the action

      String message = (String) session.getAttribute("action-message", PortletSession.PORTLET_SCOPE); //$NON-NLS-1$
      if (message != null) {
        /*
         * String messageHtml = PortletUtil.getMessageHtml( message );
         * session.removeAttribute( "action-message",
         * PortletSession.PORTLET_SCOPE ); //$NON-NLS-1$
         * response.setContentType( "text/html" ); //$NON-NLS-1$
         * response.getWriter().write( messageHtml );
         */
      }

      // now call the action of the subclass
      try {
        doPortletView(request, response, userSession);
      } catch (Throwable t) {
        error(Messages.getErrorString("BasePortlet.ERROR_0003_PORTLET_ERROR"), t); //$NON-NLS-1$
      }

    } finally {
      // JIRA case #PLATFORM 150
      PentahoSystem.systemExitPoint();
    }
  }

  @Override
  public final void doHelp(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {

    PentahoPortletSession userSession = getPentahoSession(request);

    // TODO: perform validation and auditing of this action

    // now call the action of the subclass
    doPortletHelp(request, response, userSession);
  }

  @Override
  public final void doEdit(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {

    PentahoPortletSession userSession = getPentahoSession(request);

    // TODO: perform validation and auditing of this action

    // now call the action of the subclass
    doPortletEdit(request, response, userSession);
  }

  protected void removeUserSession(final PentahoPortletSession userSession) {
    userSession.destroy();
  }

  // TODO sbarkdull BasePortlet.getPentahoSession and PentahoHttpSessionHelper.getPentahoSession
  // should likely be merged or have the common parts pulled out into
  // a common method so there is only one place to get a pentaho session
  protected PentahoPortletSession getPentahoSession(final PortletRequest request) {

    PortletSession session = request.getPortletSession(true);
    IPentahoSession existingSession = (IPentahoSession) session.getAttribute(IPentahoSession.PENTAHO_SESSION_KEY,
        PortletSession.APPLICATION_SCOPE);

    if (existingSession != null) {
      if (!(existingSession instanceof PentahoPortletSession)) {
        // there is a session object of another type, we need to replace
        // it
        existingSession = null;
      }
    }

    LocaleHelper.setLocale(request.getLocale());
    PentahoPortletSession userSession;
    if (existingSession == null) {
      if (BasePortlet.debug) {
        debug(Messages.getString("BasePortlet.DEBUG_CREATING_SESSION")); //$NON-NLS-1$
      }
      userSession = new PentahoPortletSession(request.getRemoteUser(), session, request.getLocale());
      session.removeAttribute(IPentahoSession.PENTAHO_SESSION_KEY);
      session.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession, PortletSession.APPLICATION_SCOPE);
    } else {
      userSession = (PentahoPortletSession) existingSession;
    }

    if (userSession != null) {
      logId = Messages.getString("BasePortlet.CODE_LOG_ID", session.getId()); //$NON-NLS-1$
    }

    return userSession;

  }

  protected void sendMessage(final String message, final PortletSession session) {
    session.setAttribute("action-message", message, PortletSession.PORTLET_SCOPE); //$NON-NLS-1$
  }

  public int getLoggingLevel() {
    return logLevel;
  }

  public void setLoggingLevel(final int logLevel) {
    this.logLevel = logLevel;
    if (BasePortlet.debug) {
      debug(Messages.getString("BasePortlet.DEBUG_SETTING_LOGGING_LEVEL") + logLevel); //$NON-NLS-1$
    }
  }

  public void trace(final String message) {
    if (logLevel <= ILogger.TRACE) {
      BasePortlet.logger.trace(logId + message);
    }
  }

  public void debug(final String message) {
    if (logLevel <= ILogger.DEBUG) {
      BasePortlet.logger.debug(logId + message);
    }
  }

  public void info(final String message) {
    if (logLevel <= ILogger.INFO) {
      BasePortlet.logger.info(logId + message);
    }
  }

  public void warn(final String message) {
    if (logLevel <= ILogger.WARN) {
      BasePortlet.logger.warn(logId + message);
    }
  }

  public void error(final String message) {
    if (logLevel <= ILogger.ERROR) {
      BasePortlet.logger.error(logId + message);
    }
  }

  public void fatal(final String message) {
    if (logLevel <= ILogger.FATAL) {
      BasePortlet.logger.fatal(logId + message);
    }
  }

  public void trace(final String message, final Throwable error) {
    if (logLevel <= ILogger.TRACE) {
      BasePortlet.logger.trace(logId + message, error);
      Logger.addException( error );
    }
  }

  public void debug(final String message, final Throwable error) {
    if (logLevel <= ILogger.DEBUG) {
      BasePortlet.logger.debug(logId + message, error);
      Logger.addException( error );
    }
  }

  public void info(final String message, final Throwable error) {
    if (logLevel <= ILogger.INFO) {
      BasePortlet.logger.info(logId + message, error);
      Logger.addException( error );
    }
  }

  public void warn(final String message, final Throwable error) {
    if (logLevel <= ILogger.WARN) {
      BasePortlet.logger.warn(logId + message, error);
      Logger.addException( error );
    }
  }

  public void error(final String message, final Throwable error) {
    if (logLevel <= ILogger.ERROR) {
      BasePortlet.logger.error(logId + message, error);
      Logger.addException( error );
    }
  }

  public void fatal(final String message, final Throwable error) {
    if (logLevel <= ILogger.FATAL) {
      BasePortlet.logger.fatal(logId + message, error);
      Logger.addException( error );
    }
  }

  /**
   * Override
   */
  @Override
  protected String getTitle(final RenderRequest request) {
    String title = null;
    try {
      title = getPortletConfig().getResourceBundle(request.getLocale()).getString(
          getPortletConfig().getPortletName() + ".javax.portlet.title"); //$NON-NLS-1$
    } catch (MissingResourceException e) {
      return super.getTitle(request);
    }
    return title;
  }

}
