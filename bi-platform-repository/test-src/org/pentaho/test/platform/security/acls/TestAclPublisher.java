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
package org.pentaho.test.platform.security.acls;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.engine.security.AcegiPermissionMgr;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.acls.AclPublisher;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.security.MockSecurityUtility;

public class TestAclPublisher extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/solution";
  private static final String ALT_SOLUTION_PATH = "test-src/solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if(file.exists()) {
      return SOLUTION_PATH;  
    } else {      
      return ALT_SOLUTION_PATH;
    }
    
  }
  public static void main(String[] args) {
    junit.textui.TestRunner.run(TestAclPublisher.class);
    System.exit(0);
  }

  private Map<IPermissionRecipient, IPermissionMask> defaultAcls = new LinkedHashMap<IPermissionRecipient, IPermissionMask>();

  public void setup() {
    super.setUp();
    defaultAcls.put(new SimpleRole("ROLE_ADMIN"), new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-1$
    defaultAcls.put(new SimpleRole("ROLE_CTO"), new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-1$
    defaultAcls.put(new SimpleRole("ROLE_DEV"), new SimplePermissionMask(PentahoAclEntry.PERM_EXECUTE_SUBSCRIBE)); //$NON-NLS-1$
    defaultAcls.put(new SimpleRole("ROLE_AUTHENTICATED"), new SimplePermissionMask(PentahoAclEntry.PERM_EXECUTE)); //$NON-NLS-1$
  }

  public void testPublisher() {
    AclPublisher publisher = new AclPublisher(defaultAcls);
    assertNotNull(publisher);

    RepositoryFile rootFile = MockSecurityUtility.getPopulatedSolution();
    publisher.publishDefaultAcls(rootFile);
    checkAcls(rootFile);
  }

  public void checkAcls(IAclSolutionFile solnFile) {
    if (solnFile.isDirectory()) {
      Map<IPermissionRecipient, IPermissionMask> perms = AcegiPermissionMgr.instance().getPermissions(solnFile);
      assertEquals(perms.size(), defaultAcls.size());
      assertTrue(perms.entrySet().containsAll(defaultAcls.entrySet()));
      Set kidsSet = solnFile.getChildrenFiles();
      Iterator it = kidsSet.iterator();
      while (it.hasNext()) {
        IAclSolutionFile kidFile = (IAclSolutionFile) it.next();
        if (kidFile.isDirectory()) {
          checkAcls(kidFile);
        }
      }
    }
  }

}
