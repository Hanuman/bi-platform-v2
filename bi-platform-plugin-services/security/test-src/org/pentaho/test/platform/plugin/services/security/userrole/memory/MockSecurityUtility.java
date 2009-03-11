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
 * Copyright 2006 - 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created Apr 18, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.plugin.services.security.userrole.memory;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.TestingAuthenticationToken;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

public class MockSecurityUtility {

  private static String DefaultApplicationXML = "org/pentaho/test/platform/security/applicationContext-test-security.xml"; //$NON-NLS-1$

  public static void createPat(StandaloneSession session) {
    session.setAuthenticated("pat"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[2];
    auths[0] = new GrantedAuthorityImpl("ROLE_DEV"); //$NON-NLS-1$
    auths[1] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED"); //$NON-NLS-1$
    TestingAuthenticationToken auth = new TestingAuthenticationToken("pat", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
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
    TestingAuthenticationToken auth = new TestingAuthenticationToken("suzy", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
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
    TestingAuthenticationToken auth = new TestingAuthenticationToken("joe", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
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
    TestingAuthenticationToken auth = new TestingAuthenticationToken("tiffany", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static void createNoRolesGuy(StandaloneSession session) {
    session.setAuthenticated("fred"); //$NON-NLS-1$
    GrantedAuthority[] auths = new GrantedAuthority[] {};
    TestingAuthenticationToken auth = new TestingAuthenticationToken("fred", "none", auths //$NON-NLS-1$ //$NON-NLS-2$
    );
    auth.setAuthenticated(true);
    // We now have a credential. We need to bind it into the IPentahoSession
    SecurityHelper.setPrincipal(auth, session);
    // We should be good to go now...
  }

  public static GenericApplicationContext setupApplicationContext() {
    return setupApplicationContext(DefaultApplicationXML);
  }

  public static GenericApplicationContext setupApplicationContext(String applicationXML) {
    GenericApplicationContext applContext = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(applContext);
    // Read in a test bean-creation object.
    xmlReader.loadBeanDefinitions(new ClassPathResource(applicationXML));
    applContext.refresh();
    return applContext;
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
