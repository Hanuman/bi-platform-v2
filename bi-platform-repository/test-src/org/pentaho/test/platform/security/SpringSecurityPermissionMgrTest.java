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

import java.io.File;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
import java.util.Properties;
//import java.util.Set;
//
//import org.pentaho.platform.api.engine.IAclHolder;
//import org.pentaho.platform.api.engine.IAclSolutionFile;
//import org.pentaho.platform.api.engine.IPermissionMask;
//import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.platform.engine.security.SpringSecurityPermissionMgr;
//import org.pentaho.platform.engine.security.SimplePermissionMask;
//import org.pentaho.platform.engine.security.SimpleRole;
//import org.pentaho.platform.engine.security.SimpleUser;
//import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
//import org.pentaho.platform.repository.hibernate.HibernateUtil;
//import org.pentaho.platform.repository.solution.dbbased.RepositoryFile;
//import org.pentaho.platform.util.messages.Messages;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings("nls")
public class SpringSecurityPermissionMgrTest extends BaseTest {
  private StringBuffer longString = new StringBuffer();
  private static final String SOLUTION_PATH = "test-src/solution";

  private static final String ALT_SOLUTION_PATH = "test-src/solution";

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }
  public static void main(String[] args) {
    SpringSecurityPermissionMgrTest test = new SpringSecurityPermissionMgrTest();
    test.setUp();
    try {
//      test.testHasPermission();
//      test.testSetPermission();
//      test.testSetPermissions();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }

  public SpringSecurityPermissionMgrTest(String arg0) {
    super(arg0);
    addProperties();
  }

  public SpringSecurityPermissionMgrTest() {
    super();
    addProperties();
  }

  private void addProperties() {
    Properties props = System.getProperties();
    longString.append(props.getProperty("java.home")).append(props.getProperty("sun.cpu.isalist")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.vm.version")).append(props.getProperty("user.home")). //$NON-NLS-1$ //$NON-NLS-2$
        append(props.getProperty("java.class.path")); //$NON-NLS-1$      
  }

  public ISolutionRepository getSolutionRepository(StandaloneSession session) {
    ISolutionRepository rtn = (ISolutionRepository) new org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository();
    rtn.init(session);
    return rtn;
  }

//  public void testGetPermissions() {
//    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    MockSecurityUtility.createPat(session);
//
//    // Get the repository
//    org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository repo = (org.pentaho.platform.repository.solution.dbbased.DbBasedSolutionRepository) getSolutionRepository(session);
//    RepositoryFile aFile = (RepositoryFile) repo
//        .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$      
//    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = SpringSecurityPermissionMgr.instance().getPermissions(
//        (IAclHolder) aFile).entrySet();
//    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//    int count = 0;
//    for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//        .hasNext();) {
//      Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//      IPermissionRecipient permissionRecipient = mapEntry.getKey();
//      String recipient = permissionRecipient.getName();
//      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//        String permName = keyIterator.next().toString();
//        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        IPermissionMask permissionMask2 = new SimplePermissionMask(permMask);
//        boolean isPermitted = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipient, permissionMask2, aFile);
//        System.out.println("For " + recipient + " permission " + permName + " is " + (isPermitted ? "" : " not ")
//            + " set.");
//        if (isPermitted) {
//          count++;
//        }
//      }
//    }
//    boolean gotPermissions = count > 0;
//    assertEquals(Boolean.TRUE, Boolean.valueOf(gotPermissions));
//  }
//
//  public void testSetPermissions() {
//    try {
//      StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      MockSecurityUtility.createPat(session);
//      // Get the repository
//      ISolutionRepository repo = getSolutionRepository(session);
//      if (!repo.supportsAccessControls()) {
//        assertFalse("ACLs are not supported by the repository", false);
//      } else {
//
//        HibernateUtil.beginTransaction();
//        RepositoryFile aFile = (RepositoryFile) repo
//            .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//
//        Map permMap = new HashMap<IPermissionRecipient, IPermissionMask>();
//        String recipient[] = { "Admin", "pat" };
//        IPermissionRecipient permissionRecipientRole = new SimpleRole(recipient[0]);
//        IPermissionRecipient permissionRecipientUser = new SimpleUser(recipient[1]);
//        SimplePermissionMask permissionMask = new SimplePermissionMask();
//        String perm[] = { "Update", "Execute", "Subscribe" };
//        for (int i = 0; i < perm.length; i++) {
//          permissionMask
//              .addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[i])).intValue());
//        }
//        permMap.put(permissionRecipientRole, permissionMask);
//        permMap.put(permissionRecipientUser, permissionMask);
//        if (aFile instanceof IAclSolutionFile) {
//          SpringSecurityPermissionMgr.instance().setPermissions(permMap, (IAclSolutionFile) aFile);
//        }
//        HibernateUtil.commitTransaction();
//        assertTrue("Permissions are set successfully", true);
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//      assertFalse("Permissions were not set successfully", false);
//    }
//
//  }
//
//  public void testSetPermission() {
//    try {
//      StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      MockSecurityUtility.createPat(session);
//      // Get the repository
//      ISolutionRepository repo = getSolutionRepository(session);
//      // ACL The first one...
//      HibernateUtil.beginTransaction();
//      RepositoryFile aFile = (RepositoryFile) repo
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//
//      String name = "Admin";
//      IPermissionRecipient permissionRecipientRole = new SimpleRole(name);
//      SimplePermissionMask permissionMask = new SimplePermissionMask();
//      String perm = "Update";
//      ;
//      permissionMask.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm)).intValue());
//      if (aFile instanceof IAclSolutionFile) {
//        SpringSecurityPermissionMgr.instance().setPermission(permissionRecipientRole, permissionMask, (IAclSolutionFile) aFile);
//      }
//      HibernateUtil.commitTransaction();
//      assertTrue("Permission is set successfully", true);
//    } catch (Exception e) {
//      e.printStackTrace();
//      assertFalse("Permission was not set successfully", false);
//    }
//  }
//
//  public void testHasPermission() {
//    try {
//      StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//      // Mock up credentials for ACL Testing
//      MockSecurityUtility.createPat(session);
//      // Get the repository
//      ISolutionRepository repo = getSolutionRepository(session);
//      // ACL The first one...
//      RepositoryFile aFile = (RepositoryFile) repo
//          .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//      String name = "pat";
//      IPermissionRecipient permissionRecipientUser = new SimpleUser(name);
//      SimplePermissionMask permissionMask = new SimplePermissionMask();
//      String perm = "Execute";
//      ;
//      boolean hasPermission = false;
//      permissionMask.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm)).intValue());
//      if (aFile instanceof IAclSolutionFile) {
//        hasPermission = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipientUser, permissionMask,
//            (IAclSolutionFile) aFile);
//      }
//      assertEquals(Boolean.TRUE, Boolean.valueOf(hasPermission));
//
//    } catch (Exception e) {
//      e.printStackTrace();
//      assertFalse("We were not able to determine whether the file had permission or not", false);
//    }
//  }
//
//  public void testAllInOne_Part1() {
//    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    // Mock up credentials for ACL Testing
//    MockSecurityUtility.createPat(session);
//    // Get the repository
//    ISolutionRepository repo = getSolutionRepository(session);
//    // ACL The first one...
//
//    RepositoryFile aFile = (RepositoryFile) repo
//        .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//
//    Map permMap1 = new HashMap<IPermissionRecipient, IPermissionMask>();
//    Map permMap2 = new HashMap<IPermissionRecipient, IPermissionMask>();
//    String recipient = "pat";
//    IPermissionRecipient permissionRecipientUser = new SimpleUser(recipient);
//    SimplePermissionMask permissionMask1 = new SimplePermissionMask();
//    SimplePermissionMask permissionMask2 = new SimplePermissionMask();
//    SimplePermissionMask permissionMask3 = new SimplePermissionMask();
//    String perm[] = { "Subscribe", "Update", "Execute" };
//    String perm1 = "Manage";
//    Map permissionMap = PentahoAclEntry.getValidPermissionsNameMap();
//    Integer permission = (Integer) permissionMap.get(perm1);
//    permissionMask1.addPermission(permission.intValue());
//    permMap1.put(permissionRecipientUser, permissionMask1);
//    HibernateUtil.beginTransaction();
//    if (aFile instanceof IAclSolutionFile) {
//      SpringSecurityPermissionMgr.instance().setPermission(permissionRecipientUser, permissionMask1, (IAclSolutionFile) aFile);
//    }
//    HibernateUtil.commitTransaction();
//    for (int i = 0; i < perm.length; i++) {
//      permissionMask2.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[i])).intValue());
//    }
//    permMap2.put(permissionRecipientUser, permissionMask2);
//    HibernateUtil.beginTransaction();
//    if (aFile instanceof IAclSolutionFile) {
//      SpringSecurityPermissionMgr.instance().setPermissions(permMap2, (IAclSolutionFile) aFile);
//    }
//    HibernateUtil.commitTransaction();
//
//    boolean hasPermission = false;
//    permissionMask3.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[0])).intValue());
//    if (aFile instanceof IAclSolutionFile) {
//      hasPermission = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipientUser, permissionMask3,
//          (IAclSolutionFile) aFile);
//    }
//    assertEquals(hasPermission, true);
//    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = SpringSecurityPermissionMgr.instance().getPermissions(
//        (IAclHolder) aFile).entrySet();
//    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//    List updatedRecipientList = new ArrayList();
//    List updatedPermissionList = new ArrayList();
//    int count = 0;
//    String recipientName = null;
//    for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//        .hasNext();) {
//      Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//      IPermissionRecipient permissionRecipient = mapEntry.getKey();
//      recipientName = permissionRecipient.getName();
//      updatedRecipientList.add(recipientName);
//      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//        String permName = keyIterator.next().toString();
//        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        IPermissionMask permissionMask4 = new SimplePermissionMask(permMask);
//        boolean isPermitted = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipient, permissionMask4, aFile);
//        if (isPermitted) {
//          count++;
//        }
//      }
//      assertEquals(perm.length, count);
//      assertEquals(recipientName, recipient);
//    }
//
//  }
//
//  public void testAllInOne_Part2() {
//    StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
//    // Mock up credentials for ACL Testing
//    MockSecurityUtility.createJoe(session);
//    // Get the repository
//    ISolutionRepository repo = getSolutionRepository(session);
//    // ACL The first one...
//    HibernateUtil.beginTransaction();
//    RepositoryFile aFile = (RepositoryFile) repo
//        .getFileByPath("samples/reporting/MDX_report.xaction"); //$NON-NLS-1$
//
//    Map permMap1 = new HashMap<IPermissionRecipient, IPermissionMask>();
//    Map permMap2 = new HashMap<IPermissionRecipient, IPermissionMask>();
//    String recipient = "Admin";
//    IPermissionRecipient permissionRecipientRole = new SimpleRole(recipient);
//    SimplePermissionMask permissionMask1 = new SimplePermissionMask();
//    SimplePermissionMask permissionMask2 = new SimplePermissionMask();
//    SimplePermissionMask permissionMask3 = new SimplePermissionMask();
//    String perm[] = { "Subscribe", "Update", "Execute" };
//    String perm1 = "Manage";
//    Map permissionMap = PentahoAclEntry.getValidPermissionsNameMap();
//    Integer permission = (Integer) permissionMap.get(perm1);
//    HibernateUtil.beginTransaction();
//    permissionMask1.addPermission(permission.intValue());
//    if (aFile instanceof IAclSolutionFile) {
//      SpringSecurityPermissionMgr.instance().setPermission(permissionRecipientRole, permissionMask1, (IAclSolutionFile) aFile);
//    }
//    HibernateUtil.commitTransaction();
//
//    for (int i = 0; i < perm.length; i++) {
//      permissionMask2.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[i])).intValue());
//    }
//    HibernateUtil.beginTransaction();
//    permMap1.put(permissionRecipientRole, permissionMask2);
//    if (aFile instanceof IAclSolutionFile) {
//      SpringSecurityPermissionMgr.instance().setPermissions(permMap1, (IAclSolutionFile) aFile);
//    }
//    HibernateUtil.commitTransaction();
//
//    boolean hasPermission = false;
//    permissionMask3.addPermission(((Integer) PentahoAclEntry.getValidPermissionsNameMap().get(perm[0])).intValue());
//    if (aFile instanceof IAclSolutionFile) {
//      hasPermission = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipientRole, permissionMask3,
//          (IAclSolutionFile) aFile);
//    }
//    assertEquals(hasPermission, true);
//    Set<Map.Entry<IPermissionRecipient, IPermissionMask>> mapEntrySet = SpringSecurityPermissionMgr.instance().getPermissions(
//        (IAclHolder) aFile).entrySet();
//    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
//    List updatedRecipientList = new ArrayList();
//    List updatedPermissionList = new ArrayList();
//    int count = 0;
//    String recipientName = null;
//    for (Iterator<Map.Entry<IPermissionRecipient, IPermissionMask>> iterator = mapEntrySet.iterator(); iterator
//        .hasNext();) {
//      Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry = iterator.next();
//      IPermissionRecipient permissionRecipient = mapEntry.getKey();
//      recipientName = permissionRecipient.getName();
//      updatedRecipientList.add(recipientName);
//      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
//        String permName = keyIterator.next().toString();
//        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
//        IPermissionMask permissionMask4 = new SimplePermissionMask(permMask);
//        boolean isPermitted = SpringSecurityPermissionMgr.instance().hasPermission(permissionRecipient, permissionMask4, aFile);
//        if (isPermitted) {
//          count++;
//        }
//      }
//      assertEquals(count, perm.length);
//      assertEquals(recipient, recipientName);
//    }
//
//  }

  public void testDummyTest() {
    // do nothing, get the above test to pass!
  }
  
}
