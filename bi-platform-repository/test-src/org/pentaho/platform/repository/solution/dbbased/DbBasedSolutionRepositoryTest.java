package org.pentaho.platform.repository.solution.dbbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAclPublisher;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IActionSequence;
import org.pentaho.platform.api.engine.IActionSequenceResource;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISubscriptionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.security.acls.AclPublisher;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.pentaho.platform.engine.security.acls.voter.PentahoBasicAclVoter;
import org.pentaho.platform.engine.services.actionsequence.ActionSequenceResource;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.repository.hibernate.HibernateUtil;
import org.pentaho.platform.repository.hibernate.HibernateUtilTestHelper;
import org.pentaho.platform.repository.subscription.SubscriptionRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

/**
 * Unit test for {@link DbBasedSolutionRepository}.
 * 
 * @author mlowery
 */
@SuppressWarnings("nls")
public class DbBasedSolutionRepositoryTest {
  private static final String JDBC_PASSWORD = ""; //$NON-NLS-1$

  private static final String JDBC_USERNAME = "sa"; //$NON-NLS-1$

  private static final String JDBC_URL = "jdbc:hsqldb:mem:hibernate"; //$NON-NLS-1$

  private MicroPlatform microPlatform;

  private DbBasedSolutionRepository repo;

  private IPentahoSession pentahoSession;

  @Before
  public void setUp() throws Exception {
    microPlatform = new MicroPlatform("./test-res/DbBasedSolutionRepositoryTest/"); //$NON-NLS-1$
    microPlatform.define(ISolutionEngine.class, SolutionEngine.class);
    microPlatform.define(IAclPublisher.class, AclPublisher.class);
    microPlatform.define(IAclVoter.class, PentahoBasicAclVoter.class);
    microPlatform.define(ISubscriptionRepository.class, SubscriptionRepository.class);
    microPlatform.setSettingsProvider(new SystemSettings());
    microPlatform.start();

    pentahoSession = new StandaloneSession();
    repo = new DbBasedSolutionRepository();
  }

  @After
  public void tearDown() throws Exception {
    pentahoSession = null;
    repo = null;
    microPlatform = null;

    File f1 = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld3.xaction"); //$NON-NLS-1$
    FileUtils.deleteQuietly(f1);
    File f2 = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution2/HelloWorld3.xaction"); //$NON-NLS-1$
    FileUtils.deleteQuietly(f2);
    File f3 = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution2/myfolder1"); //$NON-NLS-1$
    FileUtils.deleteQuietly(f3);
    File f4 = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution2/HelloWorld3.mondrian.xml"); //$NON-NLS-1$
    FileUtils.deleteQuietly(f4);
    tearDownSolutionRepositoryTables();
  }

  protected void tearDownSolutionRepositoryTables() throws Exception {
    // this was necessary for some reason; not sure why
    HibernateUtil.commitTransaction();

    // delete all rows from certain tables used by DbBasedSolutionRepository
    Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
    Statement stmt = c.createStatement();
    stmt.executeUpdate("DROP TABLE PRO_ACLS_LIST IF EXISTS CASCADE"); //$NON-NLS-1$
    stmt.executeUpdate("DROP TABLE PRO_FILES IF EXISTS CASCADE"); //$NON-NLS-1$
    stmt.close();
    c.close();

    // this re-creates the dropped tables
    HibernateUtilTestHelper.initialize();
  }

  protected static String resultSetToString(final ResultSet rs) throws IOException, SQLException {
    StringBuilder buf = new StringBuilder();
    ResultSetMetaData md = rs.getMetaData();
    int count = md.getColumnCount();
    for (int i = 1; i <= count; i++) {
      if (i > 1) {
        buf.append(", "); //$NON-NLS-1$
      }
      buf.append(md.getColumnLabel(i));
    }
    buf.append("\n"); //$NON-NLS-1$
    while (rs.next()) {
      for (int i = 1; i <= count; i++) {
        if (i > 1) {
          buf.append(", "); //$NON-NLS-1$
        }

        buf.append(rs.getString(i));
      }
      buf.append("\n"); //$NON-NLS-1$
    }
    return buf.toString();
  }

  protected void printTestHeader(final String testName) {
    System.out.println("********** [" + testName + " BEGIN] **********"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  protected void login(final String username, final String... roleNames) {
    pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    GrantedAuthority[] auths = new GrantedAuthority[roleNames.length];
    for (int i = 0; i < roleNames.length; i++) {
      auths[i] = new GrantedAuthorityImpl(roleNames[i]);
    }
    Authentication auth = new UsernamePasswordAuthenticationToken(username, null, auths);
    pentahoSession.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
  }

  protected IPentahoSession newSessionWithlogin(final String username, final String... roleNames) {
    IPentahoSession pSession = new StandaloneSession(username);
    pSession.setAuthenticated(username);
    GrantedAuthority[] auths = new GrantedAuthority[roleNames.length];
    for (int i = 0; i < roleNames.length; i++) {
      auths[i] = new GrantedAuthorityImpl(roleNames[i]);
    }
    Authentication auth = new UsernamePasswordAuthenticationToken(username, null, auths);
    pSession.setAttribute(SecurityHelper.SESSION_PRINCIPAL, auth);
    return pSession;
  }

  protected void prettyPrint(final Document doc) throws Exception {
    OutputFormat format = OutputFormat.createPrettyPrint();
    XMLWriter writer = new XMLWriter(System.out, format);
    writer.write(doc);
  }

  protected boolean accessControlEntryExists(final IPermissionRecipient recipient, final ISolutionFile file,
      int actionOperation) {
    Map<IPermissionRecipient, IPermissionMask> acl = repo.getPermissions(file);
    IPermissionMask mask = acl.get(recipient);
    if (mask != null) {
      if (mask.getMask() == actionOperation) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  
  protected boolean accessControlEntryMaskBitSet(final IPermissionRecipient recipient, final ISolutionFile file,
      int expectedMask) {
    Map<IPermissionRecipient, IPermissionMask> acl = repo.getPermissions(file);
    IPermissionMask mask = acl.get(recipient);
    if (mask != null) {
      if ((mask.getMask() & expectedMask) == expectedMask) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Test
  public void testInit() throws Exception {
    printTestHeader("testInit"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
    Statement stmt = c.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRO_FILES"); //$NON-NLS-1$
    rs.next();
    assertTrue(rs.getLong(1) > 0);
    rs.close();
    rs = stmt.executeQuery("SELECT * FROM PRO_FILES"); //$NON-NLS-1$
    System.out.println(resultSetToString(rs));
    rs.close();
    stmt.close();
    c.close();
  }

  @Test
  public void testGetActionSequence() throws Exception {
    printTestHeader("testGetActionSequence"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    IActionSequence seq = repo.getActionSequence("mysolution1", "", "HelloWorld.xaction", ILogger.TRACE, //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        ISolutionRepository.ACTION_EXECUTE);
    assertNotNull(seq);
  }

  /**
   * Expect null from getActionSequence when access is denied.
   */
  @Test
  public void testGetActionSequenceAccessDenied() throws Exception {
    printTestHeader("testGetActionSequenceAccessDenied"); //$NON-NLS-1$
    repo.init(pentahoSession);
    IActionSequence seq = repo.getActionSequence("mysolution1", "", "HelloWorld.xaction", ILogger.TRACE, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        ISolutionRepository.ACTION_EXECUTE);
    assertNull(seq);
  }

  @Test
  public void testGetSolutions() throws Exception {
    printTestHeader("testGetSolutions"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getSolutions(ISolutionRepository.ACTION_EXECUTE);
    prettyPrint(doc);
    assertTrue(Boolean.valueOf(doc.selectObject(
        "/repository/file[@type='FILE.FOLDER']/file[@type='FILE.ACTIVITY']/filename/text()='HelloWorld.xaction'") //$NON-NLS-1$
        .toString()));
  }

  @Test
  public void testGetSolutionsNoLogin() throws Exception {
    printTestHeader("testGetSolutionsNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getSolutions(ISolutionRepository.ACTION_EXECUTE);
    assertNull(doc);
  }

  /**
   * visibleOnly parameter is not used (4th parameter to getSolutions).
   */
  @Test
  public void testGetSolutionsWithPath() throws Exception {
    printTestHeader("testGetSolutionsWithPath"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getSolutions("mysolution1", "", ISolutionRepository.ACTION_EXECUTE, false); //$NON-NLS-1$//$NON-NLS-2$
    prettyPrint(doc);
    assertTrue(Boolean.valueOf(doc.selectObject(
        "/files/file[@type='FILE.FOLDER']/file[@type='FILE.ACTIVITY']/filename/text()='HelloWorld.xaction'") //$NON-NLS-1$
        .toString()));
  }

  @Test
  public void testGetSolutionStructure() throws Exception {
    printTestHeader("testGetSolutionStructure"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getSolutionStructure(ISolutionRepository.ACTION_EXECUTE);
    prettyPrint(doc);
    assertNotNull(doc
        .selectSingleNode("/repository/entry[@name='DbBasedSolutionRepositoryTest']/entry[@name='mysolution1']/entry[@name='HelloWorld.xaction']")); //$NON-NLS-1$
  }

  @Test
  public void testGetSolutionStructureNoLogin() throws Exception {
    printTestHeader("testGetSolutionStructureNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getSolutionStructure(ISolutionRepository.ACTION_EXECUTE);
    assertNull(doc);
  }

  /**
   * reloadSolutionRepository does not need a login.
   */
  @Test
  public void testReloadSolutionRepository() throws Exception {
    printTestHeader("testReloadSolutionRepository"); //$NON-NLS-1$

    File newFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld3.xaction"); //$NON-NLS-1$

    repo.init(pentahoSession);

    assertFalse(newFile.exists()); // file should not exist before we copy it
    // make a copy of an existing xaction
    FileUtils.copyFile(new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"), newFile); //$NON-NLS-1$

    repo.reloadSolutionRepository(pentahoSession, ILogger.TRACE);
  }

  @Test
  public void testGetRepositoryName() throws Exception {
    printTestHeader("testGetRepositoryName"); //$NON-NLS-1$
    repo.init(pentahoSession);
    assertEquals("DbBasedSolutionRepositoryTest", repo.getRepositoryName()); //$NON-NLS-1$
  }

  @Test
  public void testRemoveSolutionFile() throws Exception {
    printTestHeader("testRemoveSolutionFile"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    // file should not exist before we copy it
    assertFalse(repo.resourceExists("mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
    int res = repo
        .addSolutionFile(
            PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", "HelloWorld3.xaction", FileUtils.readFileToByteArray(new File( //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    "./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction")), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    boolean removed = repo.removeSolutionFile("mysolution2", "", "HelloWorld3.xaction"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    assertTrue(removed);
  }

  @Test
  public void testRemoveSolutionFileSystem() throws Exception {
    printTestHeader("testRemoveSolutionFileSystem"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    boolean removed = repo.removeSolutionFile("system", "", "pentaho.xml"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    assertFalse(removed);
  }

  @Test
  public void testRemoveSolutionFileAccessDenied() throws Exception {
    printTestHeader("testRemoveSolutionFileAccessDenied"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    // file should not exist before we copy it
    assertFalse(repo.resourceExists("mysolution1/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
    int res = repo
        .addSolutionFile(
            PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution1", "HelloWorld3.xaction", FileUtils.readFileToByteArray(new File( //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                    "./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction")), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    boolean removed = repo.removeSolutionFile("mysolution1", "", "HelloWorld3.xaction"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    assertFalse(removed);
  }

  @Test
  public void testAddSolutionFile() throws Exception {
    printTestHeader("testAddSolutionFile"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    int res = repo.addSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
  }

  @Test
  public void testAddSolutionFileExistsNoOverwrite() throws Exception {
    printTestHeader("testAddSolutionFileExistsNoOverwrite"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    assertFalse(repo.resourceExists("mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
    int res = repo.addSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    res = repo.addSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), false); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_EXISTS, res);
  }

  @Test
  public void testAddSolutionFileAccessDenied() throws Exception {
    printTestHeader("testAddSolutionFileAccessDenied"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    int res = repo.addSolutionFile(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution1", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_FAILED, res);
  }

  @Test
  public void testGetSolutionFileLastModified() throws Exception {
    printTestHeader("testGetSolutionFileLastModified"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    File f = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld2.xaction"); //$NON-NLS-1$
    long lastMod = repo.getSolutionFileLastModified(
        "mysolution1/HelloWorld2.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertEquals(f.lastModified(), lastMod);
  }

  @Test
  public void testGetSolutionFileLastModifiedNoLogin() throws Exception {
    printTestHeader("testGetSolutionFileLastModifiedNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    long lastMod = repo.getSolutionFileLastModified(
        "mysolution1/HelloWorld2.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertEquals(-1, lastMod);
  }

  @Test
  public void testGetClassLoader() throws Exception {
    printTestHeader("testGetClassLoader"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    byte[] expectedBytes = FileUtils.readFileToByteArray(new File(
        "./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction")); //$NON-NLS-1$
    ClassLoader classLoader = repo.getClassLoader("mysolution1"); //$NON-NLS-1$
    InputStream is = classLoader.getResourceAsStream("HelloWorld.xaction"); //$NON-NLS-1$
    byte[] actualBytes = IOUtils.toByteArray(is);
    IOUtils.closeQuietly(is);
    assertTrue(Arrays.equals(expectedBytes, actualBytes));
  }

  /**
   * The filter parameter (getFullSolutionTree's second parameter) is never used.
   */
  @Test
  public void testGetFullSolutionTree() throws Exception {
    printTestHeader("testGetFullSolutionTree"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, null);
    prettyPrint(doc);
    // getFullSolutionTree no longer returns the system folder or anything in it.
//    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/system/hibernate']")); //$NON-NLS-1$
    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/mysolution2']")); //$NON-NLS-1$

  }

  /**
   * getFullSolutionTree returns EVERYTHING regardless of who you are.
   */
  @Test
  public void testGetFullSolutionTreeNoLogin() throws Exception {
    printTestHeader("testGetFullSolutionTreeNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getFullSolutionTree(ISolutionRepository.ACTION_EXECUTE, null);
    prettyPrint(doc);
    // getFullSolutionTree no longer returns the system folder or anything in it.
//    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/system/hibernate']")); //$NON-NLS-1$
    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/mysolution2']")); //$NON-NLS-1$
  }

  @Test
  public void testGetSolutionTree() throws Exception {
    printTestHeader("testGetSolutionTree"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getSolutionTree(ISolutionRepository.ACTION_EXECUTE);
    prettyPrint(doc);
    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/mysolution2']")); //$NON-NLS-1$
  }

  @Test
  public void testGetSolutionTreeNoLogin() throws Exception {
    printTestHeader("testGetSolutionTreeNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getSolutionTree(ISolutionRepository.ACTION_EXECUTE);
    prettyPrint(doc);
    assertNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/mysolution2']")); //$NON-NLS-1$
  }

  /**
   * By passing in a filter, we can circumvent any access checks (assuming the user has EXECUTE access on the root
   * solution folder--no other access necessary).
   */
  @Test
  public void testGetSolutionTreeCircumventDefaultFilter() throws Exception {
    printTestHeader("testGetSolutionTreeCircumventDefaultFilter"); //$NON-NLS-1$
    login("tiffany"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getSolutionTree(ISolutionRepository.ACTION_EXECUTE, new ISolutionFilter() {
      public boolean keepFile(ISolutionFile solutionFile, int actionOperation) {
        return true;
      }
    });
    prettyPrint(doc);
    assertNotNull(doc.selectSingleNode("//branch[@id='/DbBasedSolutionRepositoryTest/mysolution2']")); //$NON-NLS-1$
  }

  @Test
  public void testResourceExists() throws Exception {
    printTestHeader("testResourceExists"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    boolean exists = repo.resourceExists("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertTrue(exists);
  }

  @Test
  public void testResourceExistsNoLogin() throws Exception {
    printTestHeader("testResourceExistsNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    boolean exists = repo.resourceExists("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertFalse(exists);
  }

  @Test
  public void testResourceSize() throws Exception {
    printTestHeader("testResourceSize"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    long expectedSize = FileUtils.readFileToByteArray(new File(
        "./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction")).length; //$NON-NLS-1$
    long actualSize = repo.resourceSize("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertEquals(expectedSize, actualSize);
  }

  @Test
  public void testResourceSizeNoLogin() throws Exception {
    printTestHeader("testResourceSizeNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    long actualSize = repo.resourceSize("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertEquals(0, actualSize);
  }

  /**
   * The following getResource* methods go through getResourceInputStream, so we take a shortcut and only test that
   * method:
   * <ul>
   * <li>getResourceAsBytes</li>
   * <li>getResourceAsDocument</li>
   * <li>getResourceAsString</li>
   * <li>getResourceReader</li>
   * <li>getResourceDataSource</li>
   * </ul>
   */
  @Test
  public void testGetResourceInputStream() throws Exception {
    printTestHeader("testGetResourceInputStream"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    byte[] expectedBytes = FileUtils.readFileToByteArray(new File(
        "./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction")); //$NON-NLS-1$
    InputStream is = repo.getResourceInputStream(
        "mysolution1/HelloWorld.xaction", true, ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    byte[] actualBytes = IOUtils.toByteArray(is);
    assertTrue(Arrays.equals(expectedBytes, actualBytes));
    IOUtils.closeQuietly(is);
  }

  @Test(expected = FileNotFoundException.class)
  public void testGetResourceInputStreamNoLogin() throws Exception {
    printTestHeader("testGetResourceInputStreamNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    InputStream is = repo.getResourceInputStream(
        "mysolution1/HelloWorld.xaction", true, ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    IOUtils.closeQuietly(is);
  }

  @Test
  public void testGetAllActionSequences() throws Exception {
    printTestHeader("testGetAllActionSequences"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    String[] files = repo.getAllActionSequences(ISolutionRepository.ACTION_EXECUTE);
    Arrays.sort(files);
    System.out.println(Arrays.toString(files));
    assertTrue(Arrays.binarySearch(files, "mysolution1/HelloWorld.xaction") >= 0); //$NON-NLS-1$
  }

  @Test
  public void testGetAllActionSequencesNoLogin() throws Exception {
    printTestHeader("testGetAllActionSequencesNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    String[] files = repo.getAllActionSequences(ISolutionRepository.ACTION_EXECUTE);
    Arrays.sort(files);
    System.out.println(Arrays.toString(files));
    assertFalse(Arrays.binarySearch(files, "mysolution1/HelloWorld.xaction") >= 0); //$NON-NLS-1$
  }

  @Test
  public void testGetNavigationUIDocument() throws Exception {
    printTestHeader("testGetNavigationUIDocument"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    Document doc = repo.getNavigationUIDocument("", "", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$ //$NON-NLS-2$
    prettyPrint(doc);
    assertNotNull(doc.selectSingleNode("/repository[@name='DbBasedSolutionRepositoryTest']")); //$NON-NLS-1$
    assertNotNull(doc.selectSingleNode("//file[@name='mysolution1']")); //$NON-NLS-1$
  }

  @Test
  public void testGetNavigationUIDocumentNoLogin() throws Exception {
    printTestHeader("testGetNavigationUIDocumentNoLogin"); //$NON-NLS-1$
    repo.init(pentahoSession);
    Document doc = repo.getNavigationUIDocument("", "", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$ //$NON-NLS-2$
    assertNull(doc);
  }

  @Test
  @Ignore
  public void testGetXSLName() throws Exception {
    printTestHeader("testGetXSLName"); //$NON-NLS-1$
  }

  @Test
  @Ignore
  public void testResetRepository() throws Exception {
    printTestHeader("testResetRepository"); //$NON-NLS-1$
  }

  @Test
  public void testGetFileByPath() throws Exception {
    printTestHeader("testGetFileByPath"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertNotNull(file);
    file = repo.getFileByPath("mysolution1/HelloWorld5.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertNull(file);
  }

  @Test
  public void testGetFileByPathNoLogin() throws Exception {
    printTestHeader("testGetFileByPath"); //$NON-NLS-1$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertNull(file);
  }

  @Test
  @Ignore
  public void testLocalizeDoc() throws Exception {
    printTestHeader("testLocalizeDoc"); //$NON-NLS-1$
  }

  @Test
  public void testSupportsAccessControls() throws Exception {
    printTestHeader("testSupportsAccessControls"); //$NON-NLS-1$
    repo.init(pentahoSession);
    assertTrue(repo.supportsAccessControls());
  }

  @Test
  public void testGetSolutionFile() throws Exception {
    printTestHeader("testGetSolutionFile"); //$NON-NLS-1$
    IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
        "mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getSolutionFile(resource, ISolutionRepository.ACTION_EXECUTE);
    assertNotNull(file);
  }

  @Test
  public void testGetSolutionFileNoLogin() throws Exception {
    printTestHeader("testGetSolutionFileNoLogin"); //$NON-NLS-1$
    IActionSequenceResource resource = new ActionSequenceResource(
        "", IActionSequenceResource.SOLUTION_FILE_RESOURCE, "text/xml", //$NON-NLS-1$ //$NON-NLS-2$
        "mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getSolutionFile(resource, ISolutionRepository.ACTION_EXECUTE);
    assertNull(file);
  }

  @Test
  public void testCreateFolder() throws Exception {
    printTestHeader("testCreateFolder"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    File newFolder = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution2/myfolder1"); //$NON-NLS-1$
    ISolutionFile solutionFolder = repo.createFolder(newFolder);
    assertNotNull(solutionFolder);
    assertTrue(newFolder.exists());
    assertTrue(repo.resourceExists("mysolution2/myfolder1", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
  }

  @Test
  public void testCreateFolderAccessDenied() throws Exception {
    printTestHeader("testCreateFolderAccessDenied"); //$NON-NLS-1$
    repo.init(pentahoSession);
    File newFolder = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution2/myfolder1"); //$NON-NLS-1$
    ISolutionFile solutionFolder = repo.createFolder(newFolder);
    assertNull(solutionFolder);
    assertFalse(newFolder.exists());
    assertFalse(repo.resourceExists("mysolution2/myfolder1", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
  }

  @Test
  public void testHasAccess() throws Exception {
    printTestHeader("testHasAccess"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile f1 = repo.getFileByPath("mysolution1", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    ISolutionFile f2 = repo.getFileByPath("mysolution2", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    @SuppressWarnings("unused")
    ISolutionFile f3 = repo.getFileByPath("", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    ISolutionFile f4 = repo.getFileByPath("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertTrue(repo.hasAccess(f2, ISolutionRepository.ACTION_CREATE));
    assertFalse(repo.hasAccess(f1, ISolutionRepository.ACTION_CREATE));
    assertFalse(repo.hasAccess(f4, ISolutionRepository.ACTION_UPDATE));
  }

  @Test
  public void testHasAccessAdminNeverDenied() throws Exception {
    printTestHeader("testHasAccessAdminNeverDenied"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile f2 = repo.getFileByPath("mysolution2", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    ISolutionFile f3 = repo.getFileByPath("", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    // admins can do anything
    assertTrue(repo.hasAccess(f2, ISolutionRepository.ACTION_CREATE)); 
    assertTrue(repo.hasAccess(f3, ISolutionRepository.ACTION_DELETE)); 
  }

  /**
   * @see #testgetPermissionsOnNonACLedFile()
   * @see #testSetPermissionsOnNonACLedFile()
   */
  @Test
  public void testHasAccessOnNonACLedFile() throws Exception {
    printTestHeader("testHasAccessOnNonACLedFile"); //$NON-NLS-1$
    // we don't login because it shouldn't matter if you're logged in or not
    repo.init(pentahoSession);
    ISolutionFile f1 = repo.getFileByPath("mysolution1/HelloWorld2.properties", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertNotNull(f1);
  }

  /**
   * @see #testHasAccessOnNonACLedFile()
   * @see #testGetPermissionsOnNonACLedFile()
   */
  @Test(expected=PentahoAccessControlException.class)
  public void testSetPermissionsOnNonACLedFile() throws Exception {
    printTestHeader("testSetPermissionsOnNonACLedFile"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    IPermissionRecipient recipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask mask = new SimplePermissionMask(ISolutionRepository.ACTION_EXECUTE);
    Map<IPermissionRecipient, IPermissionMask> acl = new HashMap<IPermissionRecipient, IPermissionMask>();
    acl.put(recipient, mask);
    ISolutionFile f1 = repo.getFileByPath("mysolution1/HelloWorld2.properties", ISolutionRepository.ACTION_SHARE); //$NON-NLS-1$
    assertNotNull(f1);
    repo.setPermissions(f1, acl);
  }

  /**
   * @see #testHasAccessOnNonACLedFile()
   * @see #testSetPermissionsOnNonACLedFile()
   */
  @Test
  public void testGetPermissionsOnNonACLedFile() throws Exception {
    printTestHeader("testGetPermissionsOnNonACLedFile"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile f1 = repo.getFileByPath("mysolution1/HelloWorld2.properties", ISolutionRepository.ACTION_SHARE); //$NON-NLS-1$
    assertTrue(repo.getPermissions(f1).isEmpty());
  }
  
  @Test
  public void testGetEffectivePermissionsOnNonACLedFile() throws Exception {
    printTestHeader("testGetEffectivePermissionsOnNonACLedFile"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile f1 = repo.getFileByPath("mysolution1/HelloWorld2.properties", ISolutionRepository.ACTION_SHARE); //$NON-NLS-1$
    assertTrue(repo.getEffectivePermissions(f1).isEmpty());
  }

  @Test
  public void testShare() throws Exception {
    printTestHeader("testShare"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile f1 = repo.getFileByPath("mysolution3/HelloWorld4.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    SimpleUser shareRecipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    List<IPermissionRecipient> shareRecipients = new ArrayList<IPermissionRecipient>();
    shareRecipients.add(shareRecipient);
    assertFalse(accessControlEntryExists(shareRecipient, f1, ISolutionRepository.ACTION_EXECUTE
        | ISolutionRepository.ACTION_SUBSCRIBE));
    repo.share(f1, shareRecipients);
    assertTrue(accessControlEntryExists(shareRecipient, f1, ISolutionRepository.ACTION_EXECUTE
        | ISolutionRepository.ACTION_SUBSCRIBE));
  }

  @Test
  public void testPublish() throws Exception {
    printTestHeader("testPublish"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    int res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    ISolutionFile publishedFile = repo.getFileByPath(
        "mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_CREATE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_DELETE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_EXECUTE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_SHARE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_SUBSCRIBE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_UPDATE));
  }
  
  @Test
  public void testPublishNonACLedFile() throws Exception {
    printTestHeader("testPublishNonACLedFile"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    int res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.mondrian.xml", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    @SuppressWarnings("unused")
    ISolutionFile publishedFile = repo.getFileByPath(
        "mysolution2/HelloWorld3.mondrian.xml", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
  }

  @Test
  public void testPublishExistsNoOverwrite() throws Exception {
    printTestHeader("testPublishExistsNoOverwrite"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    assertFalse(repo.resourceExists("mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
    int res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    ISolutionFile publishedFile = repo.getFileByPath(
        "mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_CREATE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_DELETE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_EXECUTE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_SHARE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_SUBSCRIBE));
    assertTrue(repo.hasAccess(publishedFile, ISolutionRepository.ACTION_UPDATE));
    res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), false); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_EXISTS, res);
  }

  @Test
  public void testPublishAsAdminThenPublishSameAsAuthenticated() throws Exception {
    printTestHeader("testPublishAsAdminThenPublishSameAsAuthenticated"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    assertFalse(repo.resourceExists("mysolution2/HelloWorld3.xaction", ISolutionRepository.ACTION_EXECUTE)); //$NON-NLS-1$
    int res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_SUCCESSFUL, res);
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution2", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_FAILED, res);
  }

  @Test
  public void testPublishAccessDenied() throws Exception {
    printTestHeader("testPublishAccessDenied"); //$NON-NLS-1$
    File srcFile = new File("./test-res/DbBasedSolutionRepositoryTest/mysolution1/HelloWorld.xaction"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    int res = repo.publish(PentahoSystem.getApplicationContext().getSolutionPath(""), "mysolution1", //$NON-NLS-1$ //$NON-NLS-2$
        "HelloWorld3.xaction", FileUtils.readFileToByteArray(srcFile), true); //$NON-NLS-1$
    assertEquals(ISolutionRepository.FILE_ADD_FAILED, res);
  }

  @Test
  public void testAddPermission() throws Exception {
    printTestHeader("testAddPermission"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution3/HelloWorld4.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    IPermissionRecipient recipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask mask = new SimplePermissionMask(ISolutionRepository.ACTION_UPDATE);
    assertFalse(accessControlEntryMaskBitSet(recipient, file, ISolutionRepository.ACTION_UPDATE));
    repo.addPermission(file, recipient, mask);
    assertTrue(accessControlEntryMaskBitSet(recipient, file, ISolutionRepository.ACTION_UPDATE));
  }

  @Test
  public void testAddPermissionAccessDenied() throws Exception {
    printTestHeader("testAddPermissionAccessDenied"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    IPermissionRecipient recipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask mask = new SimplePermissionMask(ISolutionRepository.ACTION_UPDATE);
    assertFalse(accessControlEntryMaskBitSet(recipient, file, ISolutionRepository.ACTION_UPDATE));
    repo.addPermission(file, recipient, mask);
    assertFalse(accessControlEntryMaskBitSet(recipient, file, ISolutionRepository.ACTION_UPDATE));
  }

  @Test
  public void testSetPermissions() throws Exception {
    printTestHeader("testSetPermissions"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution3/HelloWorld4.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    IPermissionRecipient recipientTiffany = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask maskTiffany = new SimplePermissionMask(ISolutionRepository.ACTION_UPDATE);
    Map<IPermissionRecipient, IPermissionMask> acl = new HashMap<IPermissionRecipient, IPermissionMask>();
    acl.put(recipientTiffany, maskTiffany);
    // next three lines necessary so that suzy doesn't get locked out of file
    IPermissionRecipient recipientSuzy = new SimpleUser("suzy"); //$NON-NLS-1$
    IPermissionMask maskSuzy = new SimplePermissionMask(ISolutionRepository.ACTION_EXECUTE);
    acl.put(recipientSuzy, maskSuzy);
    Map<IPermissionRecipient, IPermissionMask> origAcl = repo.getPermissions(file);
    assertFalse(accessControlEntryMaskBitSet(recipientTiffany, file, ISolutionRepository.ACTION_UPDATE));
    repo.setPermissions(file, acl);
    assertTrue(accessControlEntryExists(recipientTiffany, file, ISolutionRepository.ACTION_UPDATE));
    assertFalse(acl.equals(origAcl));
  }

  @Test(expected = PentahoAccessControlException.class)
  public void testSetPermissionsAccessDenied() throws Exception {
    printTestHeader("testSetPermissionsAccessDenied"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution1/HelloWorld.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    IPermissionRecipient recipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask mask = new SimplePermissionMask(ISolutionRepository.ACTION_UPDATE);
    Map<IPermissionRecipient, IPermissionMask> acl = new HashMap<IPermissionRecipient, IPermissionMask>();
    acl.put(recipient, mask);
    repo.setPermissions(file, acl);
  }
  
  @Test
  public void testGetPermissions() throws Exception {
    printTestHeader("testGetPermissions"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$ //$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution1", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    Map<IPermissionRecipient, IPermissionMask> acl = repo.getPermissions(file);
    assertTrue(acl.size() == 2);
    assertEquals(new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL), acl.get(new SimpleRole("Admin"))); //$NON-NLS-1$
    assertEquals(new SimplePermissionMask(PentahoAclEntry.PERM_EXECUTE), acl.get(new SimpleRole("Authenticated"))); //$NON-NLS-1$
  }

  @Test
  public void testGetEffectivePermissions() throws Exception {
    printTestHeader("testGetEffectivePermissions"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getFileByPath("mysolution3/HelloWorld4.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    Map<IPermissionRecipient, IPermissionMask> acl = repo.getEffectivePermissions(file);
    assertTrue(acl.size() == 1);
    assertEquals(new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL), acl.get(new SimpleUser("suzy"))); //$NON-NLS-1$
    IPermissionRecipient recipient = new SimpleUser("tiffany"); //$NON-NLS-1$
    IPermissionMask mask = new SimplePermissionMask(ISolutionRepository.ACTION_UPDATE);
    Map<IPermissionRecipient, IPermissionMask> newAcl = new HashMap<IPermissionRecipient, IPermissionMask>();
    newAcl.put(recipient, mask);
    // next ACE is necessary so I don't lock myself out of the subsequent getEffectivePermissions call
    newAcl.put(new SimpleRole("Authenticated"), new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL)); //$NON-NLS-1$
    repo.setPermissions(file, newAcl);
    acl = repo.getEffectivePermissions(file);
    assertTrue(acl.size() == 2);
    assertEquals(new SimplePermissionMask(PentahoAclEntry.PERM_UPDATE), acl.get(new SimpleUser("tiffany"))); //$NON-NLS-1$
    assertEquals(new SimplePermissionMask(PentahoAclEntry.PERM_FULL_CONTROL), acl.get(new SimpleRole("Authenticated"))); //$NON-NLS-1$
  }

  @Test
  @Ignore
  public void testSynchronizeSolutionWithSolutionSource() throws Exception {
    printTestHeader("testSynchronizeSolutionWithSolutionSource"); //$NON-NLS-1$
  }

  @Test
  @Ignore
  public void testSolutionSynchronizationSupported() throws Exception {
    printTestHeader("testSolutionSynchronizationSupported"); //$NON-NLS-1$
  }

  @Test
  public void testGetLocalizedFileProperty() throws Exception {
    printTestHeader("testGetLocalizedFileProperty"); //$NON-NLS-1$
    login("suzy", "Authenticated"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getSolutionFile("mysolution1/HelloWorld2.xaction", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    String title = repo.getLocalizedFileProperty(file, "title", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
    assertEquals("Hello World 2", title); //$NON-NLS-1$
  }

  @Test
  public void showInputPage() throws Exception {
    String TYPE_PARAM = "type"; //$NON-NLS-1$
//    String ACTION_PARAM = "action"; //$NON-NLS-1$
//    String ADD_NAME_PARAM = "add_name"; //$NON-NLS-1$
//    String PATH_PARAM = "path"; //$NON-NLS-1$
//    String LIST_ACTION = "list"; //$NON-NLS-1$
//    String ADD_BTN_PARAM = "addBtn"; //$NON-NLS-1$
//    String UPDATE_BTN_PARAM = "updateBtn"; //$NON-NLS-1$
    String ROLE_TYPE = "role"; //$NON-NLS-1$
//    String PERM_TYPE = "perm"; //$NON-NLS-1$
//    String ROLE_PREFIX = ROLE_TYPE + "_"; //$NON-NLS-1$
//    String PERMISSION_PREFIX = PERM_TYPE + "_"; //$NON-NLS-1$
    String USER_TYPE = "user"; //$NON-NLS-1$
//    String USER_PREFIX = USER_TYPE + "_"; //$NON-NLS-1$
//    String PERMISSION_SEPERATOR = "#"; //$NON-NLS-1$
//    String DELETE_PREFIX = "delete_"; //$NON-NLS-1$
//    String NO_FILE_PATH_NODE_NAME = "no-file-path"; //$NON-NLS-1$
//    String SET_PERMISSIONS_DENIED_NAME = "set-permissions-denied"; //$NON-NLS-1$
//    String NO_ACLS_NODE_NAME = "no-acls"; //$NON-NLS-1$
    String INPUT_PAGE_NODE_NAME = "input-page"; //$NON-NLS-1$
    String FILE_PATH_NODE_NAME = "file-path"; //$NON-NLS-1$
    String IS_DIR_NODE_NAME = "is-directory"; //$NON-NLS-1$
    String RECIPIENTS_NODE_NAME = "recipients"; //$NON-NLS-1$
    String ROLE_NODE_NAME = "role"; //$NON-NLS-1$
    String USER_NODE_NAME = "user"; //$NON-NLS-1$
    String PERMISSION_NAMES_NODE_NAME = "permission-names"; //$NON-NLS-1$
    String NAME_NODE_NAME = "name"; //$NON-NLS-1$
    String ACCESS_CONTROL_LIST_NODE_NAME = "ac-list"; //$NON-NLS-1$
    String ACCESS_CONTROL_NODE_NAME = "access-control"; //$NON-NLS-1$
    String RECIPIENT_NODE_NAME = "recipient"; //$NON-NLS-1$
    String PERMISSION_NODE_NAME = "permission"; //$NON-NLS-1$
    String PERMITTED_NODE_NAME = "permitted"; //$NON-NLS-1$
    String EMPTY_STRING = ""; //$NON-NLS-1$
    String TRUE = "true"; //$NON-NLS-1$
    String FALSE = "false"; //$NON-NLS-1$
//    String ON = "on"; //$NON-NLS-1$
    String DISPLAY_PATH_NODE_NAME = "display-path"; //$NON-NLS-1$
    printTestHeader("testTemporaryTest"); //$NON-NLS-1$
    login("joe", "Admin"); //$NON-NLS-1$//$NON-NLS-2$
    repo.init(pentahoSession);
    ISolutionFile file = repo.getSolutionFile("mysolution1", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$

    Document document = DocumentHelper.createDocument();
    Element root = document.addElement(INPUT_PAGE_NODE_NAME).addText(file.getFullPath());

    // Add the info for the file we're working on
    root.addElement(FILE_PATH_NODE_NAME).addText(file.getFullPath());
    root.addElement(DISPLAY_PATH_NODE_NAME).addText(
        file.getFullPath().replaceFirst(repo.getRepositoryName(), EMPTY_STRING).replaceFirst("//", "/")); //$NON-NLS-1$//$NON-NLS-2$
    root.addElement(IS_DIR_NODE_NAME).addText(file.isDirectory() ? TRUE : FALSE);
    Element recipients = root.addElement(RECIPIENTS_NODE_NAME);

    Iterator iter = null;
    if (true) {
      // Add all the possible roles
      List rList = new ArrayList();
      rList.add("suzy");
      rList.add("joe");
      if (rList != null) {
        iter = rList.iterator();
        while (iter.hasNext()) {
          recipients.addElement(ROLE_NODE_NAME).addText(iter.next().toString());
        }
      }
    }
    if (true) {
      // Add all the possible users
      List uList = new ArrayList();
      uList.add("Authenticated");
      uList.add("Admin");
      if (uList != null) {
        iter = uList.iterator();
        while (iter.hasNext()) {
          recipients.addElement(USER_NODE_NAME).addText(iter.next().toString());
        }
      }
    }
    // Add the names of all the permissions
    Map permissionsMap = PentahoAclEntry.getValidPermissionsNameMap();
    // permissionsMap.remove(Messages.getString("PentahoAclEntry.USER_SUBSCRIBE")); //$NON-NLS-1$
    Iterator keyIter = permissionsMap.keySet().iterator();
    Element permNames = root.addElement(PERMISSION_NAMES_NODE_NAME);
    while (keyIter.hasNext()) {
      permNames.addElement(NAME_NODE_NAME).addText(keyIter.next().toString());
    }

    Element acListNode = root.addElement(ACCESS_CONTROL_LIST_NODE_NAME);
    TreeMap<IPermissionRecipient, IPermissionMask> sortedMap = new TreeMap<IPermissionRecipient, IPermissionMask>(
        new Comparator<IPermissionRecipient>() {
          public int compare(IPermissionRecipient arg0, IPermissionRecipient arg1) {
            return arg0.getName().compareTo(arg1.getName());
          }
        });
    sortedMap.putAll(repo.getPermissions(file));
    for (Map.Entry<IPermissionRecipient, IPermissionMask> mapEntry : sortedMap.entrySet()) {
      IPermissionRecipient permissionRecipient = mapEntry.getKey();
      Element acNode = acListNode.addElement(ACCESS_CONTROL_NODE_NAME);
      Element recipientNode = acNode.addElement(RECIPIENT_NODE_NAME);
      recipientNode.setText(permissionRecipient.getName());
      recipientNode.addAttribute(TYPE_PARAM, (permissionRecipient instanceof SimpleRole) ? ROLE_TYPE : USER_TYPE);
      // Add individual permissions for this group
      for (Iterator keyIterator = permissionsMap.keySet().iterator(); keyIterator.hasNext();) {
        Element aPermission = acNode.addElement(PERMISSION_NODE_NAME);
        String permName = keyIterator.next().toString();
        aPermission.addElement(NAME_NODE_NAME).setText(permName);

        int permMask = ((Integer) permissionsMap.get(permName)).intValue();
        // TODO: Fix this test
        boolean isPermitted = true;//repo.hasAccess(permissionRecipient, file, permMask);
        aPermission.addElement(PERMITTED_NODE_NAME).addText(isPermitted ? TRUE : FALSE);
      }
    }
    prettyPrint(document);
  }

}
