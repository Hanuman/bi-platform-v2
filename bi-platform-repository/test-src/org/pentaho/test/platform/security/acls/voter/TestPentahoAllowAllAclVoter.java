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
package org.pentaho.test.platform.security.acls.voter;

import org.acegisecurity.GrantedAuthorityImpl;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoAllowAllAclVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.security.MockSecurityUtility;

public class TestPentahoAllowAllAclVoter extends BaseTest {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestPentahoAllowAllAclVoter.class);
    System.exit(0);
  }

  public void testVoter() {
    StandaloneSession session = new StandaloneSession("suzy"); //$NON-NLS-1$
    MockSecurityUtility.createSuzy(session);
    RepositoryFile testFile = new RepositoryFile("Test Folder", null, null);//$NON-NLS-1$
    // RepositoryFile has no acls on it. Nobody should be able to access it.
    // But, we're using an allowAll voter.
    PentahoAllowAllAclVoter voter = new PentahoAllowAllAclVoter();
    assertTrue(voter.hasAccess(session, testFile, PentahoAclEntry.PERM_EXECUTE));
    IPentahoAclEntry entry = voter.getEffectiveAcl(session, testFile);
    assertEquals(((PentahoAclEntry) entry).getMask(), PentahoAclEntry.PERM_FULL_CONTROL);
    assertTrue(voter.isPentahoAdministrator(session));
    assertTrue(voter.isGranted(session, new GrantedAuthorityImpl("ROLE_ANYTHING")));//$NON-NLS-1$
  }
}
