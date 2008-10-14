package org.pentaho.platform.engine.security.userroledao.hibernate.sample;

import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRoleAssignmentPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertRolePersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.assertUserPersisted;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.generateAndExecuteDdl;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getConnection;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.getSessionFactory;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.CREATE;
import static org.pentaho.platform.engine.security.userroledao.hibernate.TestUtil.DdlType.DROP;

import java.sql.Connection;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.userroledao.hibernate.HibernateUserRoleDao;

/**
 * Unit test for {@link SampleUsersAndRolesInitHandler}.
 * 
 * @author mlowery
 */
public class SampleUsersAndRolesInitHandlerTest {

  private static final String PASSWORD = "cGFzc3dvcmQ="; //$NON-NLS-1$

  private static final String JOE = "joe"; //$NON-NLS-1$

  private static final String ADMIN = "Admin"; //$NON-NLS-1$

  private HibernateUserRoleDao userRoleDao;

  private SessionFactory sessionFactory;

  private Connection connection;

  @Before
  public void setUp() throws Exception {
    // create and initialize class under test
    userRoleDao = new HibernateUserRoleDao();
    sessionFactory = getSessionFactory();
    userRoleDao.setSessionFactory(sessionFactory);

    // setup tables
    generateAndExecuteDdl(CREATE);

    // create connection for use in verification (don't want to use the class we're testing for verification purposes)
    connection = getConnection();
  }

  @After
  public void tearDown() throws Exception {
    // remove tables at the end of test
    generateAndExecuteDdl(DROP);
    connection.close();
    sessionFactory.close();
  }

  @Test
  public void testHandleInit() throws Exception {
    SampleUsersAndRolesInitHandler initHandler = new SampleUsersAndRolesInitHandler();
    initHandler.setSessionFactory(sessionFactory);
    initHandler.setUserRoleDao(userRoleDao);

    initHandler.handleInit();

    assertRolePersisted(connection, ADMIN);
    assertUserPersisted(connection, JOE, PASSWORD, true);
    assertRoleAssignmentPersisted(connection, JOE, ADMIN);
  }

}
