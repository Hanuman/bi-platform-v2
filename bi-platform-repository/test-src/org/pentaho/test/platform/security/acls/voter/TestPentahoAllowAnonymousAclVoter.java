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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved. 
 * Created Apr 19, 2006
 *
 * @author mbatchel
 */
package org.pentaho.test.platform.security.acls.voter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.AcegiPermissionMgr;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoAllowAnonymousAclVoter;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;

public class TestPentahoAllowAnonymousAclVoter extends BaseTest {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestPentahoAllowAnonymousAclVoter.class);
    System.exit(0);
  }

  public void testVoter() {
    StandaloneSession session = new StandaloneSession(null);
    // We should now have an anonymous user
    RepositoryFile testFile = new RepositoryFile("Test Folder", null, null);//$NON-NLS-1$
    Map<IPermissionRecipient, IPermissionMask> perms = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();
    perms.put(new SimpleRole("suzy"), new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL));
    perms.put(new SimpleRole("Anonymous"), new SimplePermissionMask(PentahoAclEntry.PERM_EXECUTE));
    perms.put(new SimpleRole("ROLE_CTO"), new SimplePermissionMask(PentahoAclEntry.PERM_EXECUTE_SUBSCRIBE));
    AcegiPermissionMgr.instance().setPermissions(perms, testFile);

    PentahoAllowAnonymousAclVoter voter = new PentahoAllowAnonymousAclVoter();
    assertTrue(voter.hasAccess(session, testFile, PentahoAclEntry.PERM_EXECUTE));

  }

}
