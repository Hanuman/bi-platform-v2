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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Apr 18, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.web;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

public class MockUserRoleListService implements IUserRoleListService {

  public GrantedAuthority[] getAllAuthorities() {
    GrantedAuthority[] allAuths = new GrantedAuthority[7];
    allAuths[0] = new GrantedAuthorityImpl("ROLE_DEV"); //$NON-NLS-1$
    allAuths[1] = new GrantedAuthorityImpl("ROLE_ADMIN"); //$NON-NLS-1$
    allAuths[2] = new GrantedAuthorityImpl("ROLE_DEVMGR"); //$NON-NLS-1$
    allAuths[3] = new GrantedAuthorityImpl("ROLE_CEO"); //$NON-NLS-1$
    allAuths[4] = new GrantedAuthorityImpl("ROLE_CTO"); //$NON-NLS-1$
    allAuths[5] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    allAuths[6] = new GrantedAuthorityImpl("ROLE_IS"); //$NON-NLS-1$
    return allAuths;
  }

  public String[] getAllUsernames() {
    String[] allUsers = new String[4];
    allUsers[0] = "pat"; //$NON-NLS-1$
    allUsers[1] = "tiffany"; //$NON-NLS-1$
    allUsers[2] = "joe"; //$NON-NLS-1$
    allUsers[3] = "suzy"; //$NON-NLS-1$
    return allUsers;
  }

  public String[] getUsernamesInRole(GrantedAuthority authority) {
    if (authority.getAuthority().equals("ROLE_DEV")) { //$NON-NLS-1$
      return new String[] { "pat", "tiffany" }; //$NON-NLS-1$ //$NON-NLS-2$
    } else if (authority.getAuthority().equals("ROLE_ADMIN")) { //$NON-NLS-1$
      return new String[] { "joe" };//$NON-NLS-1$
    } else if (authority.getAuthority().equals("ROLE_DEVMGR")) { //$NON-NLS-1$
      return new String[] { "tiffany" };//$NON-NLS-1$
    } else if (authority.getAuthority().equals("ROLE_CEO")) { //$NON-NLS-1$
      return new String[] { "joe" };//$NON-NLS-1$
    } else if (authority.getAuthority().equals("ROLE_CTO")) { //$NON-NLS-1$
      return new String[] { "suzy" };//$NON-NLS-1$
    } else if (authority.getAuthority().equals("ROLE_IS")) { //$NON-NLS-1$
      return new String[] { "suzy" };//$NON-NLS-1$
    }
    return null;
  }

  public GrantedAuthority[] getAuthoritiesForUser(String userName) {
    if (userName.equals("pat")) { //$NON-NLS-1$
      return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_DEV") };//$NON-NLS-1$
    } else if (userName.equals("tiffany")) {//$NON-NLS-1$
      return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_DEV"), new GrantedAuthorityImpl("ROLE_DEVMGR") };//$NON-NLS-1$ //$NON-NLS-2$
    } else if (userName.equals("joe")) {//$NON-NLS-1$
      return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_ADMIN"), new GrantedAuthorityImpl("ROLE_CEO") };//$NON-NLS-1$ //$NON-NLS-2$
    } else if (userName.equals("suzy")) {//$NON-NLS-1$
      return new GrantedAuthority[] { new GrantedAuthorityImpl("ROLE_CTO"), new GrantedAuthorityImpl("ROLE_IS") };//$NON-NLS-1$ //$NON-NLS-2$
    }
    return new GrantedAuthority[] {};

  }

}
