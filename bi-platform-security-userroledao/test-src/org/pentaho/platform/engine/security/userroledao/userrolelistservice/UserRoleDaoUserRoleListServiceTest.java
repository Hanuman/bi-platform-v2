package org.pentaho.platform.engine.security.userroledao.userrolelistservice;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
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
    context.checking(new Expectations() {
      {
        one(dao).getRoles();
        will(returnValue(roles));
      }
    });
    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
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
    context.checking(new Expectations() {
      {
        one(dao).getUsers();
        will(returnValue(users));
      }
    });
    
    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    String[] usernames = service.getAllUsernames();
    
    assertTrue(usernames.length == 2);
    assertTrue(usernames[0].equals(USERNAME) || usernames[0].equals(USERNAME2));
    assertTrue(usernames[1].equals(USERNAME) || usernames[1].equals(USERNAME2));
  }

  @Test
  public void testGetAuthoritiesForUser() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.addRole(new PentahoRole(ROLE));
    user.addRole(new PentahoRole(ROLE2));
    
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
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
    context.checking(new Expectations() {
      {
        one(dao).getRole(with(equal(ROLE)));
        will(returnValue(role));
      }
    });

    UserRoleDaoUserRoleListService service = new UserRoleDaoUserRoleListService();
    service.setUserRoleDao(dao);
    String[] usernames = service.getUsernamesInRole(new GrantedAuthorityImpl(ROLE));
    
    assertTrue(usernames.length == 2);
    assertTrue(usernames[0].equals(USERNAME) || usernames[0].equals(USERNAME2));
    assertTrue(usernames[1].equals(USERNAME) || usernames[1].equals(USERNAME2));
  }

}
