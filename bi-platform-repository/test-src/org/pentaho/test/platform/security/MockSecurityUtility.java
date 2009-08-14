/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 3 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * Created Apr 18, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.security;

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

public class MockSecurityUtility {

  public static void createPat(StandaloneSession session) {
    session.setAuthenticated("pat"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[2];
    auths[0] = new GrantedAuthorityImpl("ROLE_DEV"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("pat", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createSuzy(StandaloneSession session) {
    session.setAuthenticated("suzy"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[3];
    auths[0] = new GrantedAuthorityImpl("ROLE_CTO"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("ROLE_IS"); //$NON-NLS-1$
    auths[2] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("suzy", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createJoe(StandaloneSession session) {
    session.setAuthenticated("joe"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[3];
    auths[0] = new GrantedAuthorityImpl("ceo"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("Admin"); //$NON-NLS-1$
    auths[2] = new GrantedAuthorityImpl("Authenticated"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("joe", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createTiffany(StandaloneSession session) {
    session.setAuthenticated("tiffany"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[3];
    auths[0] = new GrantedAuthorityImpl("ROLE_DEV"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("ROLE_DEVMGR"); //$NON-NLS-1$
    auths[2] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("tiffany", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createNoRolesGuy(StandaloneSession session) {
    session.setAuthenticated("fred"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[] {};
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("fred", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static RepositoryFile getPopulatedSolution() {
    RepositoryFile root = new RepositoryFile("root", null, null);//$NON-NLS-1$
    final int topFolderCount = 3;
    final int subFolderCount = 3;
    final int filesPerFolder = 4;
    final byte[] fileData = "This is file data".getBytes();//$NON-NLS-1$
    for (int i = 0; i < topFolderCount; i++) {
      RepositoryFile topFolder = new RepositoryFile("topFolder" + i, root, null); //$NON-NLS-1$
      for (int j = 0; j < subFolderCount; j++) {
        RepositoryFile subFolder = new RepositoryFile("subFolder" + j, topFolder, null); //$NON-NLS-1$
        for (int k = 0; k < filesPerFolder; k++) {
          RepositoryFile aFile = new RepositoryFile("aFile" + k, subFolder, fileData); //$NON-NLS-1$
          if (aFile == null) {
            // Ignored - it won't be.
          }
        }
      }
    }
    return root;
  }

}
