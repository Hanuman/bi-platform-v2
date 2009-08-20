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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.test.platform.plugin.services.security.userrole.memory;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.DefaultGrantedAuthorityComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.plugin.services.security.userrole.memory.InMemoryUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMapEditor;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.memory.InMemoryDaoImpl;
import org.springframework.security.userdetails.memory.UserMap;
import org.springframework.security.userdetails.memory.UserMapEditor;

@SuppressWarnings("nls")
public class InMemoryUserRoleListServiceTests {

  private static final Log logger = LogFactory.getLog(InMemoryUserRoleListServiceTests.class);

  InMemoryUserRoleListService dao;

  public InMemoryUserRoleListServiceTests() {
    super();
  }

  @Before
  public void setUp() throws Exception {
    dao = new InMemoryUserRoleListService();
    dao.setUserRoleListEnhancedUserMap(makeUserRoleListEnhancedUserMap());
    dao.setAllAuthorities(makeAllAuthorities());
    InMemoryDaoImpl wrapped = new InMemoryDaoImpl();
    wrapped.setUserMap(makeUserMap());
    wrapped.afterPropertiesSet();
    dao.setUserDetailsService(wrapped);
    dao.afterPropertiesSet();
  }

  protected GrantedAuthority[] makeAllAuthorities() {
    GrantedAuthority one = new GrantedAuthorityImpl("ROLE_ONE"); //$NON-NLS-1$
    GrantedAuthority two = new GrantedAuthorityImpl("ROLE_TWO"); //$NON-NLS-1$
    GrantedAuthority three = new GrantedAuthorityImpl("ROLE_THREE"); //$NON-NLS-1$
    return new GrantedAuthority[] { one, two, three };
  }

  @Test
  public void testGetAllUserNames() throws Exception {
    String[] allUserNames = dao.getAllUsernames();
    assertTrue("User list should not be empty", allUserNames.length > 0); //$NON-NLS-1$
    for (int i = 0; i < allUserNames.length; i++) {
      if (logger.isDebugEnabled()) {
        logger.debug("testGetAllUserNames(): User name: " + allUserNames[i]); //$NON-NLS-1$
      }
      assertTrue(
          "User name must be marissa or scott", (allUserNames[i].equals("marissa") || allUserNames[i].equals("scott"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  @Test
  public void testGetAllUserNamesSorted() throws Exception {
    dao.setUsernameComparator(new DefaultUsernameComparator());
    List<String> usernames = Arrays.asList(dao.getAllUsernames());
    if (logger.isDebugEnabled()) {
      logger.debug("testGetAllUserNamesSorted(): Usernames: " + usernames); //$NON-NLS-1$
    }
    assertTrue(usernames.indexOf("marissa") < usernames.indexOf("scott"));
  }

  @Test
  public void testGetAllAuthorities() throws Exception {
    GrantedAuthority[] allAuthorities = dao.getAllAuthorities();
    assertTrue("Authority list should contain three roles", allAuthorities.length == 3); //$NON-NLS-1$
    for (int i = 0; i < allAuthorities.length; i++) {
      if (logger.isDebugEnabled()) {
        logger.debug("testGetAllAuthorities(): Authority: " + allAuthorities[i].getAuthority()); //$NON-NLS-1$
      }
      assertTrue("Authority name must be ROLE_ONE, ROLE_TWO or ROLE_THREE", ( //$NON-NLS-1$
          allAuthorities[i].getAuthority().equals("ROLE_ONE") //$NON-NLS-1$
              || allAuthorities[i].getAuthority().equals("ROLE_TWO") //$NON-NLS-1$
          || allAuthorities[i].getAuthority().equals("ROLE_THREE") //$NON-NLS-1$
          ));
    }
  }

  @Test
  public void testGetAllAuthoritiesSorted() throws Exception {
    dao.setGrantedAuthorityComparator(new DefaultGrantedAuthorityComparator());
    List<GrantedAuthority> authorities = Arrays.asList(dao.getAllAuthorities());
    if (logger.isDebugEnabled()) {
      logger.debug("testGetAllAuthoritiesSorted(): Authorities: " + authorities); //$NON-NLS-1$
    }
    assertTrue(authorities.indexOf(new GrantedAuthorityImpl("ROLE_THREE")) < authorities
        .indexOf(new GrantedAuthorityImpl("ROLE_TWO")));
  }

  @Test
  public void testGetAllUserNamesInRole() throws Exception {
    String[] allUserNames = dao.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_ONE")); //$NON-NLS-1$
    assertTrue("Two users should be in the role ROLE_ONE", allUserNames.length == 2); //$NON-NLS-1$
    for (int i = 0; i < allUserNames.length; i++) {
      if (logger.isDebugEnabled()) {
        logger.debug("testGetAllUserNamesInRole(): User name: " + allUserNames[i]); //$NON-NLS-1$
      }
      assertTrue(
          "User name must be marissa or scott", (allUserNames[i].equals("marissa") || allUserNames[i].equals("scott"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  @Test
  public void testGetAllUserNamesInRoleSorted() throws Exception {
    dao.setUsernameComparator(new DefaultUsernameComparator());
    List<String> usernames = Arrays.asList(dao.getUsernamesInRole(new GrantedAuthorityImpl("ROLE_ONE")));
    if (logger.isDebugEnabled()) {
      logger.debug("testGetAllUserNamesInRoleSorted(): Usernames: " + usernames); //$NON-NLS-1$
    }
    assertTrue(usernames.indexOf("marissa") < usernames.indexOf("scott"));
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    GrantedAuthority[] userAuths = dao.getAuthoritiesForUser("marissa"); //$NON-NLS-1$
    if (logger.isDebugEnabled()) {
      logger.debug("testGetRolesForUser(): Roles: " + Arrays.toString(userAuths)); //$NON-NLS-1$
    }
    assertNotNull(userAuths);
    assertTrue(userAuths.length == 2);
    assertEquals(userAuths[0].getAuthority(), "ROLE_ONE"); //$NON-NLS-1$
    assertEquals(userAuths[1].getAuthority(), "ROLE_TWO"); //$NON-NLS-1$
  }

  @Test
  public void testGetRolesForUserSorted() throws Exception {
    dao.setGrantedAuthorityComparator(new DefaultGrantedAuthorityComparator());
    List<GrantedAuthority> authorities = Arrays.asList(dao.getAuthoritiesForUser("scott")); //$NON-NLS-1$
    if (logger.isDebugEnabled()) {
      logger.debug("testGetRolesForUser(): Roles: " + authorities); //$NON-NLS-1$
    }

    assertTrue(authorities.indexOf(new GrantedAuthorityImpl("ROLE_ONE")) < authorities
        .indexOf(new GrantedAuthorityImpl("ROLE_THREE")));
  }

  private UserRoleListEnhancedUserMap makeUserRoleListEnhancedUserMap() {
    UserRoleListEnhancedUserMapEditor editor = new UserRoleListEnhancedUserMapEditor();
    editor.setAsText("marissa=koala,ROLE_ONE,ROLE_TWO,enabled\r\nscott=wombat,ROLE_ONE,ROLE_THREE,enabled"); //$NON-NLS-1$
    return (UserRoleListEnhancedUserMap) editor.getValue();
  }

  private UserMap makeUserMap() {
    UserMapEditor editor = new UserMapEditor();
    editor.setAsText("scott=wombat,ROLE_THREE,ROLE_ONE,enabled\r\nmarissa=koala,ROLE_ONE,ROLE_TWO,enabled"); //$NON-NLS-1$
    return (UserMap) editor.getValue();
  }

}
