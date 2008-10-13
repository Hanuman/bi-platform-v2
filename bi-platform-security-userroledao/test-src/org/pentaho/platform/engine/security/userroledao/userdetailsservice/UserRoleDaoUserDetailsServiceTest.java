package org.pentaho.platform.engine.security.userroledao.userdetailsservice;

import static org.junit.Assert.assertTrue;

import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.engine.security.userroledao.PentahoRole;
import org.pentaho.platform.engine.security.userroledao.PentahoUser;

/**
 * Unit test for {@link UserRoleDaoUserDetailsService}.
 * 
 * @author mlowery
 */
@RunWith(JMock.class)
public class UserRoleDaoUserDetailsServiceTest {

  private static final String ROLE_PREFIX = "ROLE_"; //$NON-NLS-1$

  private static final String ROLE = "Admin"; //$NON-NLS-1$

  private static final String PASSWORD = "password"; //$NON-NLS-1$

  private static final String USERNAME = "joe"; //$NON-NLS-1$

  private Mockery context = new JUnit4Mockery();

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testLoadUserByUsernameUsernameNotFound() {
    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(null));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.loadUserByUsername(USERNAME);
  }

  @Test
  public void testLoadUserByUsername() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);
    user.addRole(new PentahoRole(ROLE));

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    UserDetails userFromService = userDetailsService.loadUserByUsername(USERNAME);

    assertTrue(userFromService.getUsername().equals(USERNAME));
    assertTrue(userFromService.getPassword().equals(PASSWORD));
    assertTrue(userFromService.isEnabled() == true);
    assertTrue(userFromService.getAuthorities().length == 1);
    assertTrue(userFromService.getAuthorities()[0].getAuthority().equals(ROLE));
  }

  @Test(expected = UsernameNotFoundException.class)
  public void testLoadUserByUsernameNoRoles() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.loadUserByUsername(USERNAME);
  }

  @Test
  public void testLoadUserByUsernameWithRolePrefix() {
    final IPentahoUser user = new PentahoUser(USERNAME);
    user.setEnabled(true);
    user.setPassword(PASSWORD);
    user.addRole(new PentahoRole(ROLE));

    final IUserRoleDao dao = context.mock(IUserRoleDao.class);
    context.checking(new Expectations() {
      {
        one(dao).getUser(with(equal(USERNAME)));
        will(returnValue(user));
      }
    });

    UserRoleDaoUserDetailsService userDetailsService = new UserRoleDaoUserDetailsService();
    userDetailsService.setUserRoleDao(dao);
    userDetailsService.setRolePrefix(ROLE_PREFIX);
    UserDetails userFromService = userDetailsService.loadUserByUsername(USERNAME);

    assertTrue(userFromService.getAuthorities()[0].getAuthority().equals(ROLE_PREFIX + ROLE));
  }
}
