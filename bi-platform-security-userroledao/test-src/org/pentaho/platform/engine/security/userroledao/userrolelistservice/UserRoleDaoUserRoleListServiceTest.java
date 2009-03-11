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
package org.pentaho.platform.engine.security.userroledao.userrolelistservice;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;

/**
 * Unit test for {@link UserRoleDaoUserRoleListService}.
 * 
 * @author mlowery
 */
@RunWith(JMock.class)
public class UserRoleDaoUserRoleListServiceTest {
  private static final String PASSWORD = "password"; //$NON-NLS-1$

  private static final String USERNAME = "joe"; //$NON-NLS-1$
  
  private static final String USERNAME2 = "suzy"; //$NON-NLS-1$
  
  private static final String ROLE = "Admin"; //$NON-NLS-1$
  
  private static final String ROLE2 = "ceo"; //$NON-NLS-1$
  
  private Mockery context = new JUnit4Mockery();
  
  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetAllAuthorities() {
    final List<IPentahoRole> roles = new ArrayList<IPentahoRole>();
    roles.add(new PentahoRole(ROLE));
    roles.add(new PentahoRole(ROLE2));
    
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    final UserDetailsService userDetailsService = context.mock(UserDetailsService.class);
    context.checking(new Expectations() {
      {
        one(dao).getRoles();
        will(returnValue(roles));
      }
    });
    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    service.setUserDetailsService(userDetailsService);
    GrantedAuthority[] auths = service.getAllAuthorities();
    
    assertTrue(auths.length == 2);
    assertTrue(auths[0].getAuthority().equals(ROLE) || auths[0].getAuthority().equals(ROLE2));
    assertTrue(auths[1].getAuthority().equals(ROLE) || auths[1].getAuthority().equals(ROLE2));
  }

  @Test
  public void testGetAllUsernames() {
    final List<IPentahoUser> users = new ArrayList<IPentahoUser>();
    users.add(new PentahoUser(USERNAME));
    users.add(new PentahoUser(USERNAME));
    
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    final UserDetailsService userDetailsService = context.mock(UserDetailsService.class);
    context.checking(new Expectations() {
      {
        one(dao).getUsers();
        will(returnValue(users));
      }
    });
    
    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    service.setUserDetailsService(userDetailsService);
    String[] usernames = service.getAllUsernames();
    
    assertTrue(usernames.length == 2);
    assertTrue(usernames[0].equals(USERNAME) || usernames[0].equals(USERNAME2));
    assertTrue(usernames[1].equals(USERNAME) || usernames[1].equals(USERNAME2));
  }

  @Test
  public void testGetAuthoritiesForUser() {
    GrantedAuthority[] authorities = new GrantedAuthority[2];
    authorities[0] = new GrantedAuthorityImpl(ROLE);
    authorities[1] = new GrantedAuthorityImpl(ROLE2);

    final UserDetails userDetails = new User(USERNAME, PASSWORD, true, true, true, true, authorities); 
    
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    final UserDetailsService userDetailsService = context.mock(UserDetailsService.class);
    context.checking(new Expectations() {
      {
        one(userDetailsService).loadUserByUsername(USERNAME);
        will(returnValue(userDetails));
      }
    });

    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    service.setUserDetailsService(userDetailsService);
    GrantedAuthority[] auths = service.getAuthoritiesForUser(USERNAME);
    
    assertTrue(auths.length == 2);
    assertTrue(auths[0].getAuthority().equals(ROLE) || auths[0].getAuthority().equals(ROLE2));
    assertTrue(auths[1].getAuthority().equals(ROLE) || auths[1].getAuthority().equals(ROLE2));
  }

  @Test
  public void testGetUsernamesInRole() {
    final IPentahoRole role = new PentahoRole(ROLE);
    role.addUser(new PentahoUser(USERNAME));
    role.addUser(new PentahoUser(USERNAME2));
    
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    final UserDetailsService userDetailsService = context.mock(UserDetailsService.class);
    context.checking(new Expectations() {
      {
        one(dao).getRole(with(equal(ROLE)));
        will(returnValue(role));
      }
    });

    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    service.setUserDetailsService(userDetailsService);
    String[] usernames = service.getUsernamesInRole(new GrantedAuthorityImpl(ROLE));
    
    assertTrue(usernames.length == 2);
    assertTrue(usernames[0].equals(USERNAME) || usernames[0].equals(USERNAME2));
    assertTrue(usernames[1].equals(USERNAME) || usernames[1].equals(USERNAME2));
  }

}
