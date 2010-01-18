package org.pentaho.platform.repository.pcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository.pcr.data.node.DataNode;
import org.pentaho.platform.repository.pcr.data.node.DataNodeRef;
import org.pentaho.platform.repository.pcr.data.node.DataProperty;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository.pcr.jcr.SimpleJcrTestUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/sample-repository.spring.xml",
    "classpath:/sample-repository-test-override.spring.xml" })
//@SuppressWarnings("nls")
public class DefaultRepositoryServiceTest implements ApplicationContextAware {
  // ~ Static fields/initializers ======================================================================================

  private static final String USERNAME_SUZY = "suzy";

  private static final String USERNAME_TIFFANY = "tiffany";

  private static final String USERNAME_PAT = "pat";

  private static final String USERNAME_JOE = "joe";

  private static final String TENANT_ID_ACME = "acme";

  private static final String TENANT_ID_DUFF = "duff";

  // ~ Instance fields =================================================================================================

  private IRepositoryService repo;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  private String repositoryAdminUsername;

  private String commonAuthenticatedAuthorityName;

  private String repositoryAdminAuthorityName;

  private String tenantAdminAuthorityNameSuffix;

  private String tenantAuthenticatedAuthorityNameSuffix;

  private boolean startupCalled;

  // ~ Constructors ==================================================================================================== 

  public DefaultRepositoryServiceTest() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test"));
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
  }

  @AfterClass
  public static void tearDownClass() throws Exception {

  }

  @Before
  public void setUp() throws Exception {
    logout();
    startupCalled = true;
  }

  @After
  public void tearDown() throws Exception {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath());
    logout();

    if (startupCalled) {
      repo.getRepositoryEventHandler().onShutdown();
    }

    // null out fields to get back memory
    repo = null;
    testJcrTemplate = null;
    repositoryAdminUsername = null;
    commonAuthenticatedAuthorityName = null;
    repositoryAdminAuthorityName = null;
    tenantAdminAuthorityNameSuffix = null;
    tenantAuthenticatedAuthorityNameSuffix = null;
  }

  @Test(expected = IllegalStateException.class)
  public void testNotStartedUp() throws Exception {
    startupCalled = false;
    login(USERNAME_SUZY, TENANT_ID_ACME);
  }

  @Test
  public void testOnStartup() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    loginAsRepositoryAdmin();
    // make sure pentaho root folder exists
    final String rootFolderPath = RepositoryPaths.getPentahoRootFolderPath();
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, rootFolderPath));
  }

  @Test
  public void testGetFileWithLoadedMaps() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String fileName = "helloworld.sample";
    RepositoryFile newFile = createSampleFile(RepositoryPaths.getUserHomeFolderPath(), fileName, "blah", false, 123);
    assertEquals(fileName, newFile.getTitle());
    RepositoryFile.Builder builder = new RepositoryFile.Builder(newFile);
    final String EN_US_VALUE = "Hello World Sample";
    builder.title(Locale.getDefault().toString(), EN_US_VALUE);
    final String ROOT_LOCALE_VALUE = "Hello World";
    builder.title(RepositoryFile.ROOT_LOCALE, ROOT_LOCALE_VALUE);
    final SampleRepositoryFileData modContent = new SampleRepositoryFileData("blah", false, 123);
    repo.updateFile(builder.build(), modContent);
    RepositoryFile updatedFileWithMaps = repo.getFile(RepositoryPaths.getUserHomeFolderPath()
        + RepositoryFile.SEPARATOR + "helloworld.sample", true);

    assertEquals(EN_US_VALUE, updatedFileWithMaps.getTitleMap().get(Locale.getDefault().toString()));
    assertEquals(ROOT_LOCALE_VALUE, updatedFileWithMaps.getTitleMap().get(RepositoryFile.ROOT_LOCALE));
  }

  /**
   * This test method depends on {@code DefaultRepositoryEventHandler} behavior.
   */
  @Test
  public void testOnNewUser() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile suzyHomeFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    assertNotNull(suzyHomeFolder);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantRootFolderPath()));
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantPublicFolderPath()));
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantHomeFolderPath()));
    final String suzyFolderPath = RepositoryPaths.getUserHomeFolderPath();
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, suzyFolderPath));
  }

  /**
   * This test method depends on {@code DefaultRepositoryEventHandler} behavior.
   */
  @Test
  public void testAclsOnDefaultFolders() throws Exception {
    final RepositoryFileSid acmeAdminSid = new RepositoryFileSid(TENANT_ID_ACME + tenantAdminAuthorityNameSuffix,
        RepositoryFileSid.Type.ROLE);
    final RepositoryFileSid suzySid = new RepositoryFileSid(USERNAME_SUZY, RepositoryFileSid.Type.USER);
    final RepositoryFileSid acmeAuthenticatedAuthoritySid = new RepositoryFileSid(TENANT_ID_ACME
        + tenantAuthenticatedAuthorityNameSuffix, RepositoryFileSid.Type.ROLE);
    final RepositoryFileSid repositoryAdminSid = new RepositoryFileSid(repositoryAdminUsername,
        RepositoryFileSid.Type.USER);
    final RepositoryFileSid commonAuthenticatedAuthoritySid = new RepositoryFileSid(commonAuthenticatedAuthorityName,
        RepositoryFileSid.Type.ROLE);

    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    // pentaho root folder
    // admin has implicit all access
    //    assertLocalAceExists(repo.getFile(RepositoryPaths.getPentahoRootFolderPath()), repositoryAdminSid, EnumSet
    //        .of(Permission.ALL));
    assertLocalAceExists(repo.getFile(RepositoryPaths.getPentahoRootFolderPath()), commonAuthenticatedAuthoritySid,
        EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL));
    assertEquals(repositoryAdminSid, repo.getFile(RepositoryPaths.getPentahoRootFolderPath()).getOwner());
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath(),
        Privilege.JCR_READ));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL));

    // tenant root folder
    assertLocalAceExists(repo.getFile(RepositoryPaths.getTenantRootFolderPath()), acmeAdminSid, EnumSet
        .of(RepositoryFilePermission.ALL));
    assertLocalAceExists(repo.getFile(RepositoryPaths.getTenantRootFolderPath()), acmeAuthenticatedAuthoritySid,
        EnumSet.of(RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL));
    assertEquals(acmeAdminSid, repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getOwner());
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL));

    // tenant public folder
    assertLocalAceExists(repo.getFile(RepositoryPaths.getTenantPublicFolderPath()), acmeAuthenticatedAuthoritySid,
        EnumSet.of(RepositoryFilePermission.APPEND, RepositoryFilePermission.WRITE, RepositoryFilePermission.WRITE_ACL,
            RepositoryFilePermission.READ, RepositoryFilePermission.READ_ACL, RepositoryFilePermission.DELETE_CHILD));
    assertEquals(acmeAdminSid, repo.getFile(RepositoryPaths.getTenantPublicFolderPath()).getOwner());
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL));

    // tenant home folder
    assertLocalAclEmpty(repo.getFile(RepositoryPaths.getTenantHomeFolderPath()));
    assertEquals(acmeAdminSid, repo.getFile(RepositoryPaths.getTenantHomeFolderPath()).getOwner());
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL));

    // suzy home folder
    assertLocalAceExists(repo.getFile(RepositoryPaths.getUserHomeFolderPath()), suzySid, EnumSet
        .of(RepositoryFilePermission.ALL));
    assertEquals(suzySid, repo.getFile(RepositoryPaths.getUserHomeFolderPath()).getOwner());
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath(),
        Privilege.JCR_ALL));
  }

  @Test
  public void testGetFileAccessDenied() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile tiffanyHomeFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    assertNotNull(tiffanyHomeFolder);
    assertNotNull(repo.createFolder(tiffanyHomeFolder.getId(), new RepositoryFile.Builder("test").folder(true).build()));
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String acmeTenantRootFolderPath = RepositoryPaths.getTenantRootFolderPath();
    final String homeFolderPath = RepositoryPaths.getTenantHomeFolderPath();
    final String tiffanyFolderPath = homeFolderPath + "/tiffany";
    // read access for suzy on home
    assertNotNull(repo.getFile(homeFolderPath));
    // no read access for suzy on tiffany's folder
    assertNull(repo.getFile(tiffanyFolderPath));
    // no read access for suzy on subfolder of tiffany's folder
    final String tiffanySubFolderPath = tiffanyFolderPath + "/test";
    assertNull(repo.getFile(tiffanySubFolderPath));
    // make sure Pat can't see acme folder (pat is in the duff tenant)
    login(USERNAME_PAT, TENANT_ID_DUFF);
    assertNull(repo.getFile(acmeTenantRootFolderPath));
    assertFalse(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, acmeTenantRootFolderPath, Privilege.JCR_READ));
    assertFalse(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, acmeTenantRootFolderPath,
        Privilege.JCR_READ_ACCESS_CONTROL));
  }

  @Test
  public void testGetFileAdmin() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile tiffanyHomeFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    repo.createFolder(tiffanyHomeFolder.getId(), new RepositoryFile.Builder("test").folder(true).build());
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    repo.getFile(RepositoryPaths.getTenantHomeFolderPath() + "/tiffany/test");
  }

  @Test
  public void testGetFileNotExist() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile file2 = repo.getFile("/doesnotexist");
    assertNull(file2);
  }

  @Test
  public void testStartupTwice() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath() + "[1]"));
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath() + "[2]"));
  }

  @Test
  public void testOnNewUserTwice() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    login(USERNAME_SUZY, TENANT_ID_ACME);
  }

  @Test
  public void testCreateFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    Date beginTime = Calendar.getInstance().getTime();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    Date endTime = Calendar.getInstance().getTime();
    assertTrue(beginTime.before(newFolder.getCreatedDate()));
    assertTrue(endTime.after(newFolder.getCreatedDate()));
    assertNotNull(newFolder);
    assertNotNull(newFolder.getId());
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/test"));
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void testCreateFolderAccessDenied() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getPentahoRootFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    repo.createFolder(parentFolder.getId(), newFolder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFolderAtRootIllegal() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    repo.createFolder(null, newFolder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileAtRootIllegal() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String fileName = "helloworld.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, encoding, "text/plain");
    repo.createFile(null, new RepositoryFile.Builder(fileName).build(), content);
  }

  @Test
  public void testCreateSimpleFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes(expectedEncoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath = RepositoryPaths.getUserHomeFolderPath() + "/helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, expectedEncoding,
        expectedMimeType);
    Date beginTime = Calendar.getInstance().getTime();
    Thread.sleep(1000); // when the test runs too fast, begin and lastModifiedDate are the same; manual pause
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(expectedName).build(),
        content);
    Date endTime = Calendar.getInstance().getTime();
    assertTrue(beginTime.before(newFile.getLastModifiedDate()));
    assertTrue(endTime.after(newFile.getLastModifiedDate()));
    assertNotNull(newFile.getId());
    RepositoryFile foundFile = repo.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
    assertNotNull(foundFile.getCreatedDate());
    assertNotNull(foundFile.getLastModifiedDate());

    SimpleRepositoryFileData contentFromRepo = repo.getDataForRead(foundFile.getId(), SimpleRepositoryFileData.class);
    assertEquals(expectedEncoding, contentFromRepo.getEncoding());
    assertEquals(expectedMimeType, contentFromRepo.getMimeType());
    assertEquals(expectedDataString, IOUtils.toString(contentFromRepo.getStream(), expectedEncoding));
  }

  @Test
  public void testCreateSampleFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String expectedName = "helloworld.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    RepositoryFile newFile = createSampleFile(parentFolderPath, expectedName, sampleString, sampleBoolean,
        sampleInteger);

    assertNotNull(newFile.getId());
    RepositoryFile foundFile = repo.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
    assertNotNull(foundFile.getCreatedDate());
    assertNotNull(foundFile.getLastModifiedDate());

    SampleRepositoryFileData data = repo.getDataForRead(foundFile.getId(), SampleRepositoryFileData.class);

    assertEquals(sampleString, data.getSampleString());
    assertEquals(sampleBoolean, data.getSampleBoolean());
    assertEquals(sampleInteger, data.getSampleInteger());
  }
  
  @Test
  public void testCreateNodeFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String expectedName = "helloworld.doesnotmatter";
    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;

    RepositoryFile sampleFile = createSampleFile(parentFolderPath, "helloworld2.sample", "dfdd", true, 83);
    
    final Date EXP_DATE = new Date();
    
    DataNode node = new DataNode("kdjd");
    node.setProperty("ddf", "ljsdfkjsdkf");
    DataNode newChild1 = node.addNode("herfkmdx");
    newChild1.setProperty("sdfs", true);
    newChild1.setProperty("ks3", EXP_DATE);
    newChild1.setProperty("ids32", 7.32D);
    newChild1.setProperty("erere3", 9856684583L);
    newChild1.setProperty("tttss4", "843skdfj33ksaljdfj");
    newChild1.setProperty("urei2", new DataNodeRef(sampleFile.getId()));
    DataNode newChild2 = node.addNode("pppqqqs2");
    
    NodeRepositoryFileData data = new NodeRepositoryFileData(node);
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(expectedName).build(), data);
    
    assertNotNull(newFile.getId());
    RepositoryFile foundFile = repo.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());

    DataNode foundNode = repo.getDataForRead(foundFile.getId(), NodeRepositoryFileData.class).getNode();

    assertEquals(node.getName(), foundNode.getName());
    assertNotNull(foundNode.getId());
    assertEquals(node.getProperty("ddf"), foundNode.getProperty("ddf"));
    int actualPropCount = 0;
    for (DataProperty prop : foundNode.getProperties()) {
      actualPropCount++;
    }
    assertEquals(1, actualPropCount);
    assertTrue(foundNode.hasNode("herfkmdx"));
    DataNode foundChild1 = foundNode.getNode("herfkmdx");
    assertNotNull(foundChild1.getId());
    assertEquals(newChild1.getName(), foundChild1.getName());
    assertEquals(newChild1.getProperty("sdfs"), foundChild1.getProperty("sdfs"));
    assertEquals(newChild1.getProperty("ks3"), foundChild1.getProperty("ks3"));
    assertEquals(newChild1.getProperty("ids32"), foundChild1.getProperty("ids32"));
    assertEquals(newChild1.getProperty("erere3"), foundChild1.getProperty("erere3"));
    assertEquals(newChild1.getProperty("tttss4"), foundChild1.getProperty("tttss4"));
    assertEquals(newChild1.getProperty("urei2"), foundChild1.getProperty("urei2"));
    
    try {
     SimpleJcrTestUtils.deleteItem(testJcrTemplate, sampleFile.getAbsolutePath());
     fail();
    } catch (Exception e) {
      // should fail due to referential integrity (newFile payload has reference to sampleFile)
    }
    
    
    actualPropCount = 0;
    for (DataProperty prop : newChild1.getProperties()) {
      actualPropCount++;
    }
    assertEquals(6, actualPropCount);
    DataNode foundChild2 = foundNode.getNode("pppqqqs2");
    assertNotNull(foundChild2.getId());
    assertEquals(newChild2.getName(), foundChild2.getName());
    actualPropCount = 0;
    for (DataProperty prop : foundChild2.getProperties()) {
      actualPropCount++;
    }
    assertEquals(0, actualPropCount);
    
    // ordering
    int i = 0;
    for (DataNode currentNode : foundNode.getNodes()) {
      if (i++ == 0) {
        assertEquals(newChild1.getName(), currentNode.getName());
      } else {
        assertEquals(newChild2.getName(), currentNode.getName());
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileUnrecognizedContentType() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    IRepositoryFileData content = new IRepositoryFileData() {
    };
    repo.createFile(parentFolder.getId(), new RepositoryFile.Builder("helloworld.xaction").build(), content);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileNoExtension() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    createSampleFile(RepositoryPaths.getUserHomeFolderPath(), "helloworld", "blah", false, 123);
  }

  @Test
  public void testGetChildren() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME); // creates acme tenant folder
    List<RepositoryFile> children = repo.getChildren(repo.getFile(RepositoryPaths.getPentahoRootFolderPath()).getId());
    assertEquals(1, children.size());
    RepositoryFile f0 = children.get(0);
    assertEquals("acme", f0.getName());
    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getId());
    assertEquals(2, children.size());
    RepositoryFile f1 = children.get(0);
    assertEquals("home", f1.getName());
    RepositoryFile f2 = children.get(1);
    assertEquals("public", f2.getName());

    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getId(), null);
    assertEquals(2, children.size());
    
    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getId(), "*");
    assertEquals(2, children.size());

    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getId(), "*me");
    assertEquals(1, children.size());

    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()).getId(), "*Z*");
    assertEquals(0, children.size());

  }

  /**
   * A user should only be able to see his home folder (unless your the admin).
   */
  @Test
  public void testListHomeFolders() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    List<RepositoryFile> children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantHomeFolderPath()).getId());
    assertEquals(1, children.size());
  }

  @Test
  public void testUpdateFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String fileName = "helloworld.sample";

    RepositoryFile newFile = createSampleFile(parentFolderPath, fileName, "Hello World!", false, 222);

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 99;

    final SampleRepositoryFileData modContent = new SampleRepositoryFileData(modSampleString, modSampleBoolean,
        modSampleInteger);

    repo.updateFile(newFile, modContent);

    SampleRepositoryFileData modData = repo.getDataForRead(repo.getFile(
        RepositoryPaths.getUserHomeFolderPath() + RepositoryFile.SEPARATOR + fileName).getId(),
        SampleRepositoryFileData.class);

    assertEquals(modSampleString, modData.getSampleString());
    assertEquals(modSampleBoolean, modData.getSampleBoolean());
    assertEquals(modSampleInteger, modData.getSampleInteger());
  }

  /**
   * Create a versioned file then update it with invalid data and the checkout that we did before setting the data 
   * should be rolled back.
   */
  @Test
  public void testTransactionRollback() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String expectedName = "helloworld.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    RepositoryFile newFile = createSampleFile(parentFolderPath, expectedName, sampleString, sampleBoolean,
        sampleInteger, true);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, expectedAbsolutePath));

    try {
      repo.updateFile(newFile, new IRepositoryFileData() {
      });
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }
    assertFalse(SimpleJcrTestUtils.isCheckedOut(testJcrTemplate, expectedAbsolutePath));
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void testCreateDuplicateFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/test"));
    RepositoryFile anotherFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), anotherFolder);
  }

  @Test
  public void testWriteToPublic() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    assertNotNull(createSampleFile(parentFolderPath, "helloworld.sample", "Hello World!", false, 500));
  }

  @Test
  public void testCreateVersionedFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    assertTrue(newFolder.isVersioned());
    assertNotNull(newFolder.getVersionId());
    RepositoryFile newFolder2 = repo.createFolder(newFolder.getId(), new RepositoryFile.Builder("test2").folder(true)
        .build());
    RepositoryFile newFile = createSampleFile(newFolder2.getAbsolutePath(), "helloworld.sample", "sdfdf", false, 5);
    repo.lockFile(newFile.getId(), "lock within versioned folder");
    repo.unlockFile(newFile.getId());
  }

  @Test
  public void testCreateVersionedFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);

    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(fileName).versioned(true)
        .build(), content);
    assertTrue(newFile.isVersioned());
    assertNotNull(newFile.getVersionId());
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    int versionCount = SimpleJcrTestUtils.getVersionCount(testJcrTemplate, filePath);
    assertTrue(versionCount > 0);
    repo.updateFile(newFile, content);
    assertTrue(SimpleJcrTestUtils.getVersionCount(testJcrTemplate, filePath) > versionCount);
  }

  @Test
  public void testLockFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(fileName).build(),
        content);
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertFalse(newFile.isLocked());
    assertNull(newFile.getLockDate());
    assertNull(newFile.getLockMessage());
    assertNull(newFile.getLockOwner());
    final String lockMessage = "test by Mat";
    repo.lockFile(newFile.getId(), lockMessage);

    assertTrue(SimpleJcrTestUtils.isLocked(testJcrTemplate, filePath));
    assertEquals(lockMessage, SimpleJcrTestUtils.getString(testJcrTemplate, filePath + "/pho:lockMessage"));
    assertNotNull(SimpleJcrTestUtils.getDate(testJcrTemplate, filePath + "/pho:lockDate"));

    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile lockedFile = repo.getFile(filePath);
    assertTrue(lockedFile.isLocked());
    assertNotNull(lockedFile.getLockDate());
    assertEquals(lockMessage, lockedFile.getLockMessage());
    assertEquals(USERNAME_SUZY, lockedFile.getLockOwner());

    login(USERNAME_SUZY, TENANT_ID_ACME);
    repo.unlockFile(newFile.getId());

    assertFalse(SimpleJcrTestUtils.isLocked(testJcrTemplate, filePath));
    RepositoryFile unlockedFile = repo.getFile(filePath);
    assertFalse(unlockedFile.isLocked());
    assertNull(unlockedFile.getLockDate());
    assertNull(unlockedFile.getLockMessage());
    assertNull(unlockedFile.getLockOwner());

    // make sure lock token node has been removed
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/.lockTokens/"
        + newFile.getId()));

  }

  @Test
  public void testUndeleteFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final String fileName = "helloworld.sample";
    RepositoryFile newFile = createSampleFile(parentFolderPath, fileName, "dfdfd", true, 3);

    assertEquals(0, repo.getDeletedFiles().size());
    repo.deleteFile(newFile.getId());
    assertEquals(1, repo.getDeletedFiles(parentFolder.getId()).size());
    assertEquals(newFile.getId(), repo.getDeletedFiles(parentFolder.getId()).get(0).getId());
    assertEquals(1, repo.getDeletedFiles(parentFolder.getId(), "*.sample").size());
    assertEquals(0, repo.getDeletedFiles(parentFolder.getId(), "*.doesnotexist").size());
    assertEquals(1, repo.getDeletedFiles().size());
    assertEquals(newFile, repo.getDeletedFiles().get(0));

    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    // tiffany shouldn't see suzy's deleted file
    assertEquals(0, repo.getDeletedFiles(parentFolder.getId()).size());
    assertEquals(0, repo.getDeletedFiles().size());

    login(USERNAME_SUZY, TENANT_ID_ACME);
    repo.undeleteFile(newFile.getId());
    assertEquals(0, repo.getDeletedFiles(parentFolder.getId()).size());
    assertEquals(0, repo.getDeletedFiles().size());

    repo.deleteFile(newFile.getId());
    repo.permanentlyDeleteFile(newFile.getId());
    try {
      repo.undeleteFile(newFile.getId());
      fail();
    } catch (Exception e) {

    }
  }

  @Test
  public void testDeleteLockedFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(fileName).build(),
        content);
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertFalse(repo.getFile(filePath).isLocked());
    final String lockMessage = "test by Mat";
    repo.lockFile(newFile.getId(), lockMessage);

    repo.deleteFile(newFile.getId());
    // lock only removed when file is permanently deleted
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/.lockTokens/"
        + newFile.getId()));
    repo.undeleteFile(newFile.getId());
    repo.deleteFile(newFile.getId());
    repo.permanentlyDeleteFile(newFile.getId());

    // make sure lock token node has been removed
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/.lockTokens/"
        + newFile.getId()));
  }

  @Test
  public void testGetVersionSummaries() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";
    final SimpleRepositoryFileData content = new SimpleRepositoryFileData(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(fileName).versioned(true)
        .build(), content, "created helloworld.xaction", "new version", "label 0");
    repo.updateFile(newFile, content, "update 1", "label1");
    repo.updateFile(newFile, content, "update 2", "label2");
    RepositoryFile updatedFile = repo.updateFile(newFile, content, "update 3", "label3");
    List<VersionSummary> versionSummaries = repo.getVersionSummaries(updatedFile.getId());
    assertNotNull(versionSummaries);
    assertTrue(versionSummaries.size() >= 3);
    assertEquals("update 3", versionSummaries.get(versionSummaries.size() - 1).getMessage());
    assertEquals(Arrays.asList(new String[] { "label3" }), versionSummaries.get(versionSummaries.size() - 1)
        .getLabels());
    assertEquals(USERNAME_SUZY, versionSummaries.get(0).getAuthor());
    System.out.println(versionSummaries);
    System.out.println(versionSummaries.size());
  }

  @Test
  public void testCircumventApiToGetVersionHistoryNodeAccessDenied() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    String versionHistoryAbsPath = SimpleJcrTestUtils.getVersionHistoryNodePath(testJcrTemplate, newFolder
        .getAbsolutePath());
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, versionHistoryAbsPath));
  }

  @Test
  public void testGetVersionSummary() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String fileName = "helloworld.sample";

    final String origSampleString = "Hello World!";
    final boolean origSampleBoolean = false;
    final int origSampleInteger = 1024;

    RepositoryFile newFile = createSampleFile(parentFolderPath, fileName, origSampleString, origSampleBoolean,
        origSampleInteger, true);
    SampleRepositoryFileData newContent = repo.getDataForRead(newFile.getId(), SampleRepositoryFileData.class);

    VersionSummary v1 = repo.getVersionSummary(newFile.getId(), newFile.getVersionId());
    assertNotNull(v1);
    assertEquals(USERNAME_SUZY, v1.getAuthor());
    assertEquals(new Date().getDate(), v1.getDate().getDate());

    repo.updateFile(newFile, newContent);

    // gets last version summary
    VersionSummary v2 = repo.getVersionSummary(newFile.getId(), null);

    assertNotNull(v2);
    assertEquals(USERNAME_SUZY, v2.getAuthor());
    assertEquals(new Date().getDate(), v2.getDate().getDate());
    assertFalse(v1.equals(v2));
    List<VersionSummary> sums = repo.getVersionSummaries(newFile.getId());
    assertEquals(sums.get(0), v1);
    assertEquals(sums.get(sums.size() - 1), v2);
  }

  @Test
  public void testGetFileByVersionSummary() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String fileName = "helloworld.sample";

    final String origSampleString = "Hello World!";
    final boolean origSampleBoolean = false;
    final int origSampleInteger = 1024;

    RepositoryFile newFile = createSampleFile(parentFolderPath, fileName, origSampleString, origSampleBoolean,
        origSampleInteger, true);
    final Serializable fileId = newFile.getId();
    final Serializable parentId = newFile.getParentId();
    final String absolutePath = newFile.getAbsolutePath();

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 2048;

    final SampleRepositoryFileData modData = new SampleRepositoryFileData(modSampleString, modSampleBoolean,
        modSampleInteger);

    RepositoryFile.Builder builder = new RepositoryFile.Builder(newFile);
    final String desc = "Hello World description";
    builder.description(RepositoryFile.ROOT_LOCALE, desc);
    repo.updateFile(builder.build(), modData);

    List<VersionSummary> versionSummaries = repo.getVersionSummaries(newFile.getId());
    RepositoryFile v1 = repo.getFile(newFile.getId(), versionSummaries.get(0).getId());
    RepositoryFile v2 = repo.getFile(newFile.getId(), versionSummaries.get(versionSummaries.size() - 1).getId());
    assertEquals(fileName, v1.getName());
    assertEquals(fileName, v2.getName());
    assertEquals(fileId, v1.getId());
    assertEquals(fileId, v2.getId());
    assertEquals(parentId, v1.getParentId());
    assertEquals(parentId, v2.getParentId());
    assertEquals("1.0", v1.getVersionId());
    assertEquals("1.3", v2.getVersionId());
    assertEquals(absolutePath, v1.getAbsolutePath());
    assertEquals(absolutePath, v2.getAbsolutePath());
    assertNull(v1.getDescription());
    assertEquals(desc, v2.getDescription());

    System.out.println("or: " + newFile);
    System.out.println("v1: " + v1);
    System.out.println("v2: " + v2);
    SampleRepositoryFileData c1 = repo.getDataForRead(v1.getId(), v1.getVersionId(), SampleRepositoryFileData.class);
    SampleRepositoryFileData c2 = repo.getDataForRead(v2.getId(), v2.getVersionId(), SampleRepositoryFileData.class);
    assertEquals(origSampleString, c1.getSampleString());
    assertEquals(origSampleBoolean, c1.getSampleBoolean());
    assertEquals(origSampleInteger, c1.getSampleInteger());
    assertEquals(modSampleString, c2.getSampleString());
    assertEquals(modSampleBoolean, c2.getSampleBoolean());
    assertEquals(modSampleInteger, c2.getSampleInteger());
  }

  @Test
  public void testOwnership() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    final String testFolderPath = RepositoryPaths.getTenantPublicFolderPath() + RepositoryFile.SEPARATOR + "test";
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    // new folders/files don't have an owner yet at the time they're read; unfortunate aspect of impl
    assertNull(newFolder.getOwner());
    // to get a non-null owner, use getFile
    RepositoryFile fetchedFolder = repo.getFile(testFolderPath);
    assertEquals(new RepositoryFileSid(USERNAME_SUZY), fetchedFolder.getOwner());

    // set acl removing suzy's rights to this folder
    loginAsRepositoryAdmin();
    RepositoryFileAcl testFolderAcl = repo.getAcl(repo.getFile(testFolderPath).getId());
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder(testFolderAcl).entriesInheriting(false).clearAces()
        .build();
    repo.updateAcl(newAcl);
    // but suzy is still the owner--she should be able to "acl" herself back into the folder
    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertNotNull(repo.getFile(testFolderPath));
  }

  @Test
  public void testGetAcl() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    RepositoryFileAcl acl = repo.getAcl(newFolder.getId());
    assertEquals(true, acl.isEntriesInheriting());
    assertEquals(new RepositoryFileSid(USERNAME_SUZY), acl.getOwner());
    assertEquals(newFolder.getId(), acl.getId());
    assertEquals(newFolder.getParentId(), acl.getParentId());
    assertTrue(acl.getAces().isEmpty());
  }

  @Test
  public void testGetAcl2() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    RepositoryFileAcl acl = repo.getAcl(newFolder.getId());
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder(acl).entriesInheriting(false).ace(
        new RepositoryFileSid(USERNAME_SUZY), RepositoryFilePermission.ALL).build();
    repo.updateAcl(newAcl);
    RepositoryFileAcl fetchedAcl = repo.getAcl(newFolder.getId());
    assertEquals(1, fetchedAcl.getAces().size());
  }

  @Test
  public void testHasAccess() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertTrue(repo.hasAccess(RepositoryPaths.getTenantPublicFolderPath(), EnumSet.of(RepositoryFilePermission.READ)));
    login(USERNAME_PAT, TENANT_ID_DUFF);
    assertFalse(repo.hasAccess(RepositoryPaths.getTenantPublicFolderPath(TENANT_ID_ACME), EnumSet
        .of(RepositoryFilePermission.READ)));
    // false is returned if path does not exist
    assertFalse(repo.hasAccess(RepositoryPaths.getPentahoRootFolderPath() + RepositoryFile.SEPARATOR + "doesnotexist",
        EnumSet.of(RepositoryFilePermission.READ)));
  }

  @Test
  public void testGetEffectiveAces() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile acmePublicFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    List<RepositoryFileAcl.Ace> effectiveAces1 = repo.getEffectiveAces(acmePublicFolder.getId());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(acmePublicFolder.getId(), newFolder);
    List<RepositoryFileAcl.Ace> effectiveAces2 = repo.getEffectiveAces(newFolder.getId());
    assertEquals(effectiveAces1, effectiveAces2);
  }

  @Test
  public void testUpdateAcl() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder.getId(), newFolder);
    RepositoryFileAcl acl = repo.getAcl(newFolder.getId());

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder(acl);
    RepositoryFileSid tiffanySid = new RepositoryFileSid(USERNAME_TIFFANY);
    newAclBuilder.owner(tiffanySid);
    repo.updateAcl(newAclBuilder.build());
    RepositoryFileAcl fetchedAcl = repo.getAcl(newFolder.getId());
    assertEquals(new RepositoryFileSid(USERNAME_TIFFANY), fetchedAcl.getOwner());
  }

  @Test
  public void testMoveFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile moveTest1Folder = new RepositoryFile.Builder("moveTest1").folder(true).versioned(true).build();
    moveTest1Folder = repo.createFolder(parentFolder.getId(), moveTest1Folder);
    RepositoryFile moveTest2Folder = new RepositoryFile.Builder("moveTest2").folder(true).versioned(true).build();
    moveTest2Folder = repo.createFolder(parentFolder.getId(), moveTest2Folder);
    RepositoryFile testFolder = new RepositoryFile.Builder("test").folder(true).build();
    testFolder = repo.createFolder(moveTest1Folder.getId(), testFolder);
    // move folder into new folder
    repo.moveFile(testFolder.getId(), moveTest2Folder.getAbsolutePath());
    assertNull(repo.getFile(RepositoryPaths.getTenantPublicFolderPath() + RepositoryFile.SEPARATOR + "moveTest1"
        + RepositoryFile.SEPARATOR + "test"));
    assertNotNull(repo.getFile(RepositoryPaths.getTenantPublicFolderPath() + RepositoryFile.SEPARATOR + "moveTest2"
        + RepositoryFile.SEPARATOR + "test"));
    // rename within same folder
    repo.moveFile(testFolder.getId(), moveTest2Folder.getAbsolutePath() + RepositoryFile.SEPARATOR + "newTest");
    assertNull(repo.getFile(RepositoryPaths.getTenantPublicFolderPath() + RepositoryFile.SEPARATOR + "moveTest2"
        + RepositoryFile.SEPARATOR + "test"));
    assertNotNull(repo.getFile(RepositoryPaths.getTenantPublicFolderPath() + RepositoryFile.SEPARATOR + "moveTest2"
        + RepositoryFile.SEPARATOR + "newTest"));

    RepositoryFile newFile = createSampleFile(moveTest2Folder.getAbsolutePath(), "helloworld.sample", "ddfdf", false,
        83);
    try {
      repo.moveFile(testFolder.getId(), moveTest2Folder.getAbsolutePath() + RepositoryFile.SEPARATOR + "doesnotexist"
          + RepositoryFile.SEPARATOR + "newTest2");
      fail();
    } catch (IllegalArgumentException e) {
      // moving a folder to a path with a non-existent parent folder is illegal
    }

    try {
      repo.moveFile(testFolder.getId(), newFile.getAbsolutePath());
      fail();
    } catch (IllegalArgumentException e) {
      // moving a folder to a file is illegal
    }

  }

  private RepositoryFile createSampleFile(final String parentFolderPath, final String fileName,
      final String sampleString, final boolean sampleBoolean, final int sampleInteger, boolean versioned)
      throws Exception {
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    final SampleRepositoryFileData content = new SampleRepositoryFileData(sampleString, sampleBoolean, sampleInteger);
    return repo.createFile(parentFolder.getId(), new RepositoryFile.Builder(fileName).versioned(versioned).build(),
        content);
  }

  private RepositoryFile createSampleFile(final String parentFolderPath, final String fileName,
      final String sampleString, final boolean sampleBoolean, final int sampleInteger) throws Exception {
    return createSampleFile(parentFolderPath, fileName, sampleString, sampleBoolean, sampleInteger, false);
  }

  private void assertLocalAceExists(final RepositoryFile file,
      final org.pentaho.platform.api.repository.RepositoryFileSid sid,
      final EnumSet<RepositoryFilePermission> permissions) {
    RepositoryFileAcl acl = repo.getAcl(file.getId());

    List<RepositoryFileAcl.Ace> aces = acl.getAces();
    for (int i = 0; i < aces.size(); i++) {
      RepositoryFileAcl.Ace ace = aces.get(i);
      if (sid.equals(ace.getSid()) && permissions.equals(ace.getPermissions())) {
        return;
      }
    }
    fail();
  }

  private void assertLocalAclEmpty(final RepositoryFile file) {
    RepositoryFileAcl acl = repo.getAcl(file.getId());
    assertTrue(acl.getAces().size() == 0);
  }

  private Serializable getNodeId(final String absPath) throws Exception {
    return SimpleJcrTestUtils.getNodeId(testJcrTemplate, absPath);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    repo = (IRepositoryService) applicationContext.getBean("repositoryService");
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    testJcrTemplate.setExposeNativeSession(true);
    repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
    repositoryAdminAuthorityName = (String) applicationContext.getBean("repositoryAdminAuthorityName");
    commonAuthenticatedAuthorityName = (String) applicationContext.getBean("commonAuthenticatedAuthorityName");
    tenantAuthenticatedAuthorityNameSuffix = (String) applicationContext
        .getBean("tenantAuthenticatedAuthorityNameSuffix");
    tenantAdminAuthorityNameSuffix = (String) applicationContext.getBean("tenantAdminAuthorityNameSuffix");
  }

  /**
   * Logs in with given username.
   * 
   * @param username username of user
   * @param tenantId tenant to which this user belongs
   * @tenantAdmin true to add the tenant admin authority to the user's roles
   */
  private void login(final String username, final String tenantId, final boolean tenantAdmin) {
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenantId);
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add(new GrantedAuthorityImpl(commonAuthenticatedAuthorityName));
    authList.add(new GrantedAuthorityImpl(tenantId + tenantAuthenticatedAuthorityNameSuffix));
    if (tenantAdmin) {
      authList.add(new GrantedAuthorityImpl(tenantId + tenantAdminAuthorityNameSuffix));
    }
    GrantedAuthority[] authorities = authList.toArray(new GrantedAuthority[0]);
    UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
    Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
    SecurityHelper.setPrincipal(auth, pentahoSession);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(auth);

    repo.getRepositoryEventHandler().onNewTenant();
    repo.getRepositoryEventHandler().onNewUser();
  }

  private void loginAsRepositoryAdmin() {
    StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
    pentahoSession.setAuthenticated(repositoryAdminUsername);
    final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[2];
    // necessary for AclAuthorizationStrategyImpl
    repositoryAdminAuthorities[0] = new GrantedAuthorityImpl(repositoryAdminAuthorityName);
    // necessary for unit test (Spring Security requires Authenticated role on all methods of DefaultRepositoryService)
    repositoryAdminAuthorities[1] = new GrantedAuthorityImpl(commonAuthenticatedAuthorityName);
    final String password = "ignored";
    UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true,
        repositoryAdminAuthorities);
    Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails,
        password, repositoryAdminAuthorities);
    SecurityHelper.setPrincipal(repositoryAdminAuthentication, pentahoSession);
    PentahoSessionHolder.setSession(pentahoSession);
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication(repositoryAdminAuthentication);
  }

  private void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication(null);
  }

  private void login(final String username, final String tenantId) {
    login(username, tenantId, false);
  }

}
