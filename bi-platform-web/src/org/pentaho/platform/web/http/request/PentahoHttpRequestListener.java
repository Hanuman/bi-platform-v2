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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.web.http.request;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;

/**
 * In a J2EE environment, sets the Pentaho session statically per request so the session
 * can be retrieved by other consumers within the same request without having it passed 
 * to them explicitly.
 * @author aphillips
 */
public class PentahoHttpRequestListener implements ServletRequestListener {
  
  protected final Log logger = LogFactory.getLog(PentahoHttpRequestListener.class);

  public void requestDestroyed(ServletRequestEvent sre) {
    if(logger.isTraceEnabled()) {
      logger.trace("unbinding session "+PentahoSessionHolder.getSession()+" from request: "+sre.getServletRequest());
    }
    PentahoSessionHolder.removeSession();
  }

  public void requestInitialized(ServletRequestEvent sre) {
    if (!(sre.getServletRequest() instanceof HttpServletRequest)) {
      return;
    }
    
    HttpServletRequest request = (HttpServletRequest)sre.getServletRequest();
    IPentahoSession session = PentahoHttpSessionHelper.getPentahoSession(request);
    
    if(logger.isTraceEnabled()) {
      logger.trace("binding session "+session+" to request "+sre.getServletRequest());
    }
    
    PentahoSessionHolder.setSession(PentahoHttpSessionHelper.getPentahoSession(request));
  }

}
