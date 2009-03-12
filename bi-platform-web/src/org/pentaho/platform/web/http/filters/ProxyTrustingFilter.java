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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.http.filters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.HttpSessionContextIntegrationFilter;
import org.acegisecurity.context.SecurityContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserDetailsRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;


/**
 * 
 * This servlet is used to filter Servlet requests coming from another server
 * for processing and sets authentication for the user passed in by the
 * parameter <b>_TRUST_USER_</b>. It then passes the request down the servlet
 * chain to be serviced. Only requests coming from a trusted host will be
 * authenticated. Implement the filter and setup the trusted hosts by editing
 * the <b>web.xml</b> file as follows.
 * <p>
 * 
 * <pre>
 *  
 *  &lt;filter&gt;
 *    &lt;filter-name&gt;ProxyTrustingFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;org.pentaho.platform.web.http.filters.ProxyTrustingFilter&lt;/filter-class&gt;
 *      &lt;init-param&gt;
 *        &lt;param-name&gt;TrustedIpAddrs&lt;/param-name&gt;
 *        &lt;param-value&gt;192.168.10.60,192.168.10.61&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *  &lt;/filter&gt;
 * </pre>
 * 
 * In the above example, when a request coming from IP addresses 192.168.10.60
 * and 192.168.10.61 has the parameter _TRUST_USER_=<i>name</i> set, tha user
 * <i>name</i> will be authenticated.
 * 
 * <p>
 * NOTES:
 * <p>
 * 
 * It is easy to spoof the URL or IP address so this technique should only be
 * used if the server running the filter is not accessible to users. For example
 * if the BI Platform is hosted in a DMZ.
 * <p>
 * 
 * For this class to be useful, both Pentaho servers should be using the same
 * database repository.
 * <p>
 * The sending server should be using the ProxyServlet enabled to generate the
 * requests.
 * <p>
 * 
 * @see org.pentaho.platform.web.servlet.ProxyServlet
 * @author Doug Moran
 * 
 */

public class ProxyTrustingFilter implements Filter {
  FilterConfig filterConfig;

  String[] trustedIpAddrs = null;

  private static final Log logger = LogFactory.getLog(ProxyTrustingFilter.class);

  public Log getLogger() {
    return ProxyTrustingFilter.logger;
  }

  public void init(final FilterConfig filterConfiguration) throws ServletException {
    this.filterConfig = filterConfiguration;

    trustedIpAddrs = null;
    String hostStr = filterConfig.getInitParameter("TrustedIpAddrs"); //$NON-NLS-1$
    if (hostStr != null) {
      StringTokenizer st = new StringTokenizer(hostStr, ","); //$NON-NLS-1$
      List addrs = new ArrayList();
      while (st.hasMoreTokens()) {
        String tok = st.nextToken().trim();
        if (tok.length() > 0) {
          addrs.add(tok);
          // getLogger().info(
          // Messages.getString("ProxyTrustingFilter.DEBUG_0001_TRUSTING",
          // tok ) ); //$NON-NLS-1$
        }
      }
      if (addrs.size() > 0) { // Guarantee that its null or has at least 1
        // element
        trustedIpAddrs = (String[]) addrs.toArray(new String[0]);
      }
    }
  }

  boolean isTrusted(final String addr) {
    if (trustedIpAddrs != null) {
      for (String element : trustedIpAddrs) {
        if (element.equals(addr)) {
          return (true);
        }
      }
    }
    return (false);
  }
  
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException,
      ServletException {

    // long startTime = System.currentTimeMillis();

    if ((trustedIpAddrs != null) && (request instanceof HttpServletRequest)) {
      HttpServletRequest req = (HttpServletRequest) request;
      String remoteHost = req.getRemoteAddr();

      if (isTrusted(remoteHost)) {
        String name = request.getParameter("_TRUST_USER_"); //$NON-NLS-1$
        if ((name != null) && (name.length() > 0)) {
          PentahoSystem.systemEntryPoint();
          try {
            IPentahoSession userSession = null;
            IPentahoSession existingSession = (IPentahoSession) req.getSession().getAttribute(
                IPentahoSession.PENTAHO_SESSION_KEY);
            IUserDetailsRoleListService userDetailsRoleListService = PentahoSystem.getUserDetailsRoleListService();
            if ((existingSession == null) && (userDetailsRoleListService != null)) {
              HttpSession httpSession = req.getSession();
              userSession = userDetailsRoleListService.getEffectiveUserSession(name, null);
              Authentication auth = (Authentication) userSession.getAttribute(SecurityHelper.SESSION_PRINCIPAL);
              httpSession.setAttribute(IPentahoSession.PENTAHO_SESSION_KEY, userSession);

              /**
               * definition of anonymous inner class
               */
              SecurityContext authWrapper = new SecurityContext() {
                /**
                 * 
                 */
                private static final long serialVersionUID = 1L;
                private Authentication authentication;

                public Authentication getAuthentication() {
                  return authentication;
                };

                public void setAuthentication(Authentication authentication) {
                  this.authentication = authentication;
                };
              }; // end anonymous inner class
              
              authWrapper.setAuthentication(auth);
              httpSession.setAttribute(HttpSessionContextIntegrationFilter.ACEGI_SECURITY_CONTEXT_KEY,
                  authWrapper);
            }

          } finally {
            PentahoSystem.systemExitPoint();
          }
        }
      }
    }
    chain.doFilter(request, response);

    // long stopTime = System.currentTimeMillis();

    // getLogger().debug( Messages.getString(
    // Messages.getString("ProxyTrustingFilter.DEBUG_0004_REQUEST_TIME"),
    // String.valueOf( stopTime - startTime ) ) ); //$NON-NLS-1$
  }

  public void destroy() {

  }

  /**
   * @param args
   */
  public static void main(final String[] args) {

  }

}
