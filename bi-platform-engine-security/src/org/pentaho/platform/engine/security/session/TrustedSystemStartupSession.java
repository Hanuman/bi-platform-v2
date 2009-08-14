/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.engine.security.session;

import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemStartupSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * A "bootstrap" session used to initialize the platform.
 * 
 * <p>This is a specialization of <code>SystemStartupSession</code> 
 * that is implicitly trusted to be authenticated. In other words, this session does not get populated with an 
 * <code>Authentication</code> by <code>SecurityStartupFilter</code> (since that filter only runs during a request). 
 * This session is populated with an <code>Authentication</code> during its construction. The 
 * <code>Authentication</code> put in this session contains the Pentaho administrator role so that it has permission to 
 * execute any global action sequences.</p>
 * 
 * <p>While this class is appropriate for a db-based solution repository (one that enforces security), it should also 
 * work with a file-based solution repository. You would only need to use the superclass with file-based solution 
 * repository if you did not want to create a dependency on this project.</p>
 * 
 * @author mlowery
 */
public class TrustedSystemStartupSession extends SystemStartupSession {

  private static final long serialVersionUID = 6609958707270830980L;

  public TrustedSystemStartupSession() {
    super();
    setAuthenticated(getName());
    // create authentication
    GrantedAuthority[] roles;
    IAclVoter aclVoter = PentahoSystem.get(IAclVoter.class, null);
    if (aclVoter != null) {
      roles = new GrantedAuthority[1];
      roles[0] = aclVoter.getAdminRole();
    } else {
      // silently ignore a missing IAclVoter (access will be denied for lack of roles)
      roles = new GrantedAuthority[0];
    }
    Authentication auth = new UsernamePasswordAuthenticationToken(getName(), "", roles); //$NON-NLS-1$
    SecurityHelper.setPrincipal(auth, this);
  }

}
