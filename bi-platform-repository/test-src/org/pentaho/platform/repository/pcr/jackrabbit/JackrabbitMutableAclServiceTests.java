package org.pentaho.platform.repository.pcr.jackrabbit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.core.security.principal.EveryonePrincipal;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.PentahoJcrConstants;
import org.pentaho.platform.repository.pcr.SimpleJcrTestUtils;
import org.pentaho.platform.repository.pcr.jackrabbit.JackrabbitMutableAclService.JackrabbitSid;
import org.pentaho.platform.repository.pcr.springsecurity.RepositoryFilePermission;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.MutableAcl;
import org.springframework.security.acls.objectidentity.ObjectIdentity;
import org.springframework.security.acls.objectidentity.ObjectIdentityImpl;
import org.springframework.security.acls.sid.PrincipalSid;
import org.springframework.security.acls.sid.Sid;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:../bi-platform-sample-solution/system/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class JackrabbitMutableAclServiceTests implements ApplicationContextAware {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(JackrabbitMutableAclServiceTests.class);

  private static final Authentication AUTHENTICATION_JOE;

  private static final Authentication AUTHENTICATION_SUZY;

  private static final Authentication AUTHENTICATION_TIFFANY;

  private static final Authentication AUTHENTICATION_ADMIN;

  static {
    final String password = "password";
    final GrantedAuthority[] adminAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("Admin"),
        new GrantedAuthorityImpl("Authenticated") };
    final GrantedAuthority[] regularAuthorities = new GrantedAuthority[] { new GrantedAuthorityImpl("Authenticated") };

    UserDetails joe = new User("joe", password, true, true, true, true, adminAuthorities);
    UserDetails suzy = new User("suzy", password, true, true, true, true, regularAuthorities);
    UserDetails tiffany = new User("tiffany", password, true, true, true, true, regularAuthorities);
    UserDetails admin = new User("admin", password, true, true, true, true, adminAuthorities);

    AUTHENTICATION_JOE = new UsernamePasswordAuthenticationToken(joe, password, adminAuthorities);
    AUTHENTICATION_SUZY = new UsernamePasswordAuthenticationToken(suzy, password, regularAuthorities);
    AUTHENTICATION_TIFFANY = new UsernamePasswordAuthenticationToken(tiffany, password, regularAuthorities);
    AUTHENTICATION_ADMIN = new UsernamePasswordAuthenticationToken(admin, password, adminAuthorities);
  }

  // ~ Instance fields =================================================================================================

  private JackrabbitMutableAclService mutableAclService;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  //  private IPentahoContentRepository pentahoContentRepository;

  // ~ Constructors ====================================================================================================

  public JackrabbitMutableAclServiceTests() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Before
  public void setUp() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @After
  public void tearDown() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, "/pentaho");
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  @Test
  @Ignore
  public void testReadAclByIdObjectIdentity() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    Acl readAcl = mutableAclService.readAclById(oid);
    assertNotNull(readAcl);
    System.out.println(readAcl);
  }

  @Test
  @Ignore
  public void testCreateAcl() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    Acl createdAcl = mutableAclService.createAcl(oid);
    assertNotNull(createdAcl);
    System.out.println(createdAcl);
  }
  
  @Test
  @Ignore
  public void testDeleteAcl() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // get read access by default on these nodes
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "public", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "home", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home", "suzy", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    MutableAcl createdAcl = mutableAclService.createAcl(oid);
    // insert a deny ACE (all users with Authenticated role are denied READ access)
    createdAcl.insertAce(0, RepositoryFilePermission.READ, new PrincipalSid("Authenticated"), false);
    // insert an allow ACE (suzy can READ)
    createdAcl.insertAce(1, RepositoryFilePermission.READ, new PrincipalSid("suzy"), true);
    mutableAclService.updateAcl(createdAcl);
    mutableAclService.deleteAcl(oid, false);
    
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // suzy has read access
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    // tiffany has no read access (no access means PathNotFoundException--which is null in SimpleJcrTestUtils)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // admin has all access
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub1", PentahoJcrConstants.NT_FOLDER);
    // admin has all access (inherited ace)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    // tiffany has no read access (inherited ace)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // suzy has read access (inherited ace)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
  }
  
  @Test
  @Ignore
  public void testFindChildren() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "public", PentahoJcrConstants.NT_FOLDER);
    SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "home", PentahoJcrConstants.NT_FOLDER);
    String suzyId = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home", "suzy", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity[] children = mutableAclService.findChildren(new ObjectIdentityImpl(RepositoryFile.class, id));
    assertEquals(2, children.length);
    children = mutableAclService.findChildren(new ObjectIdentityImpl(RepositoryFile.class, suzyId));
    assertNull(children);
  }

  @Test
  @Ignore
  public void testUpdateAcl() {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    MutableAcl createdAcl = mutableAclService.createAcl(oid);
    Sid expectedSid = new JackrabbitSid(EveryonePrincipal.getInstance().getName());
    createdAcl.insertAce(0, RepositoryFilePermission.READ, expectedSid, true);
    MutableAcl updatedAcl = mutableAclService.updateAcl(createdAcl);
    assertNotNull(updatedAcl);
    Acl readAcl = mutableAclService.readAclById(oid);
    assertNotNull(readAcl);
    assertEquals(oid, readAcl.getObjectIdentity());
    assertNull(readAcl.getParentAcl());
    assertEquals(new PrincipalSid("ignored"), readAcl.getOwner()); // owner not supported yet
    assertTrue(readAcl.isEntriesInheriting()); // no option to turn off inheritance yet
    assertEquals(1, readAcl.getEntries().length);
    assertEquals(RepositoryFilePermission.READ, readAcl.getEntries()[0].getPermission());
    assertEquals(expectedSid, readAcl.getEntries()[0].getSid());
  }

  @Test
  @Ignore
  public void testReadAccess() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // get read access by default on these nodes
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "public", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "home", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home", "suzy", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    MutableAcl createdAcl = mutableAclService.createAcl(oid);
    // insert a deny ACE (all users with Authenticated role are denied READ access)
    createdAcl.insertAce(0, RepositoryFilePermission.READ, new PrincipalSid("Authenticated"), false);
    // insert an allow ACE (suzy can READ)
    createdAcl.insertAce(1, RepositoryFilePermission.READ, new PrincipalSid("suzy"), true);
    mutableAclService.updateAcl(createdAcl);
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // suzy has read access
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    // tiffany has no read access (no access means PathNotFoundException--which is null in SimpleJcrTestUtils)
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // admin has all access
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy"));
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub1", PentahoJcrConstants.NT_FOLDER);
    // admin has all access (inherited ace)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    // tiffany has no read access (inherited ace)
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // suzy has read access (inherited ace)
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/home/suzy/sub1"));
  }
  
  @Test
  @Ignore
  public void testWriteAccess() throws Exception {
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // get read access by default on these nodes
    String id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/", "pentaho", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "public", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho", "home", PentahoJcrConstants.NT_FOLDER);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home", "suzy", PentahoJcrConstants.NT_FOLDER);
    ObjectIdentity oid = new ObjectIdentityImpl(RepositoryFile.class, id);
    MutableAcl createdAcl = mutableAclService.createAcl(oid);
    // insert a deny ACE (all users with Authenticated role are denied WRITE access)
    createdAcl.insertAce(0, RepositoryFilePermission.WRITE, new PrincipalSid("Authenticated"), false);
    // insert an allow ACE (suzy can WRITE)
    createdAcl.insertAce(1, RepositoryFilePermission.WRITE, new PrincipalSid("suzy"), true);
    mutableAclService.updateAcl(createdAcl);
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub1", PentahoJcrConstants.NT_FOLDER);
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub2", PentahoJcrConstants.NT_FOLDER);
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    try {
      id = SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub3", PentahoJcrConstants.NT_FOLDER);
      fail("DataRetrievalFailureException expected");
    } catch (DataRetrievalFailureException e) {
    }
    
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_ADMIN);
    // admin has all access (inherited ace)
    SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub4", PentahoJcrConstants.NT_FOLDER);
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
    // tiffany has no write access (inherited ace)
    try {
      SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub5", PentahoJcrConstants.NT_FOLDER);
    fail("DataRetrievalFailureException expected");
    } catch (DataRetrievalFailureException e) {
    }
    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_SUZY);
    // suzy has write access (inherited ace)
    SimpleJcrTestUtils.addNode(testJcrTemplate, "/pentaho/home/suzy", "sub6", PentahoJcrConstants.NT_FOLDER);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    //    pentahoContentRepository = (IPentahoContentRepository) applicationContext.getBean("pentahoContentRepository");
    mutableAclService = (JackrabbitMutableAclService) applicationContext.getBean("aclService");
    //    DataSource aclDataSource = (DataSource) applicationContext.getBean("aclDataSource");
    //    testJdbcTemplate = new SimpleJdbcTemplate(aclDataSource);
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    //    mutableAclService = (MutableAclService) applicationContext.getBean("aclService");
    //    systemUsername = (String) applicationContext.getBean("systemUsername");
    //    systemUserSid = new PrincipalSid(systemUsername);
    //    regularUserAuthoritySid = new GrantedAuthoritySid((String) applicationContext.getBean("regularUserAuthorityName"));
  }

}
