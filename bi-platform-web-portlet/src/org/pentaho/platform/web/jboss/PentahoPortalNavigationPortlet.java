/*
 * Parts
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 * 
 * Parts
 * JBoss, Home of Professional Open Source
 * 
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.pentaho.platform.web.jboss;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: use PageCustomizerInterceptor instead in JBoss Portal 2.6 or greater
 */
public class PentahoPortalNavigationPortlet extends GenericPortlet {

  private static final Log logger = LogFactory.getLog(PentahoPortalNavigationPortlet.class);

  @Override
  public void render(final RenderRequest req, final RenderResponse resp) throws PortletException, IOException {
    resp.setContentType("text/html"); //$NON-NLS-1$
    resp.setTitle("Pentaho Portal Navigation"); //$NON-NLS-1$

    PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/catalog/index_p.jsp"); //$NON-NLS-1$
    try {
      prd.include(req, resp);
    } catch (PortletException e) {
      if (PentahoPortalNavigationPortlet.logger.isErrorEnabled()) {
        PentahoPortalNavigationPortlet.logger.error("", e); //$NON-NLS-1$
      }
    } catch (IOException e) {
      if (PentahoPortalNavigationPortlet.logger.isErrorEnabled()) {
        PentahoPortalNavigationPortlet.logger.error("", e); //$NON-NLS-1$
      }
    }

  }

}