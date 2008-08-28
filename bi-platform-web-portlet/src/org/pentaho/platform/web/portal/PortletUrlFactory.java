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
 * @created Aug 3, 2005 
 * @author James Dixon
 */

package org.pentaho.platform.web.portal;

import javax.portlet.PortletMode;
import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoUrl;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.web.http.UIException;
import org.pentaho.platform.web.portal.messages.Messages;

/**
 * Factories up implementations of IPentahoUrl. In this case, the implementation
 * is org.pentaho.ui.portlet.PortletUrl.
 * 
 * Warning: do not hold on to instances of this class beyond the current request/response
 * cycle. This class maintains a reference to the RenderResponse, which is only valid
 * in the request/response cycle in which it was created.
 * 
 * Note: this class uses two similarly named classes: PortletURL and PortletUrl.
 * Don't let this confuse you as you read the code.
 * 
 * @author James Dixon, mods by Steven Barkdull
 *
 */
public class PortletUrlFactory implements IPentahoUrlFactory {
  private static final Log log = LogFactory.getLog(PortletUrlFactory.class);

  private RenderResponse portletResponse;

  private WindowState state;

  private PortletMode mode;

  public PortletUrlFactory(final RenderResponse portletResponse, final WindowState state, final PortletMode mode) {
    this.portletResponse = portletResponse;
    this.state = state;
    this.mode = mode;
  }

  /**
   * Get a URL builder that is appropriate for building action URLs.
   * 
   * @throws UIException when one of the underlying PortletURL's set methods
   * throw an exception. See docs for PortletURL:
   *  http://docs.jboss.org/jbportal/v2.0Final/javadoc/javax/portlet/PortletURL.html
   */
  public IPentahoUrl getActionUrlBuilder() {
    PortletURL portletUrl = portletResponse.createActionURL();
    try {
      portletUrl.setPortletMode(mode);
      portletUrl.setWindowState(state);
      portletUrl.setSecure(false);
    } catch (Exception e) {
      String msg = Messages.getString("PortletUrlFactory.ERROR_0000_GETACTIONURLBUILDER_FAILED") + mode; //$NON-NLS-1$
      PortletUrlFactory.log.error(msg);
      throw new UIException(msg, e);
    }
    return new PortletUrl(portletUrl);
  }

  /**
   * Get a URL builder that is appropriate for building render URLs.
   * 
   * @throws UIException when one of the underlying PortletURL's set methods
   * throw an exception. See docs for PortletURL:
   *  http://docs.jboss.org/jbportal/v2.0Final/javadoc/javax/portlet/PortletURL.html
   */
  public IPentahoUrl getDisplayUrlBuilder() {
    PortletURL portletUrl = portletResponse.createRenderURL();
    try {
      portletUrl.setPortletMode(mode);
      portletUrl.setWindowState(state);
      portletUrl.setSecure(false);
    } catch (Exception e) {
      String msg = Messages.getString("PortletUrlFactory.ERROR_0001_GETDISPLAYURLBUILDER_FAILED") + mode; //$NON-NLS-1$
      PortletUrlFactory.log.error(msg);
      throw new UIException(msg, e);
    }
    return new PortletUrl(portletUrl);
  }
}
