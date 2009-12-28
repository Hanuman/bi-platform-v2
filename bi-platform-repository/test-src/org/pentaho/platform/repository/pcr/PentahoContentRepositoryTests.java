package org.pentaho.platform.repository.pcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.api.jsr283.security.Privilege;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
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
import org.springframework.security.acls.AccessControlEntry;
import org.springframework.security.acls.Acl;
import org.springframework.security.acls.Permission;
import org.springframework.security.acls.sid.GrantedAuthoritySid;
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
//@SuppressWarnings("nls")
public class PentahoContentRepositoryTests implements ApplicationContextAware {
  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoContentRepositoryTests.class);

  private static final String USERNAME_SUZY = "suzy";

  private static final String USERNAME_TIFFANY = "tiffany";

  private static final String USERNAME_PAT = "pat";

  private static final String USERNAME_JOE = "joe";

  private static final String TENANT_ID_ACME = "acme";

  private static final String TENANT_ID_DUFF = "duff";

  // ~ Instance fields =================================================================================================

  private IPentahoContentRepository repo;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  private String repositoryAdminUsername;

  private String commonAuthenticatedAuthorityName;

  private Sid commonAuthenticatedAuthoritySid;

  private String repositoryAdminAuthorityName;

  private String tenantAdminAuthorityNameSuffix;

  private String tenantAuthenticatedAuthorityNameSuffix;

  // ~ Constructors ==================================================================================================== 

  public PentahoContentRepositoryTests() throws Exception {
    super();
  }

  // ~ Methods =========================================================================================================

  @BeforeClass
  public static void setUpClass() throws Exception {
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
  }

  @Before
  public void setUp() throws Exception {
    logout();
  }

  @After
  public void tearDown() throws Exception {
    loginAsRepositoryAdmin();
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath());
    logout();

    // null out fields to get back memory
    repo = null;
    testJcrTemplate = null;
    repositoryAdminUsername = null;
    commonAuthenticatedAuthorityName = null;
    commonAuthenticatedAuthoritySid = null;
    repositoryAdminAuthorityName = null;
    tenantAdminAuthorityNameSuffix = null;
    tenantAuthenticatedAuthorityNameSuffix = null;
  }

  @Test(expected = IllegalStateException.class)
  public void testNotStartedUp() throws Exception {
    login(USERNAME_SUZY, TENANT_ID_ACME);
  }

  @Test
  public void testStartup() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    loginAsRepositoryAdmin();
    // make sure pentaho root folder exists
    final String rootFolderPath = RepositoryPaths.getPentahoRootFolderPath();
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, rootFolderPath));
    // make sure ACEs exist
    assertLocalAceExists(repo.getFile(rootFolderPath), commonAuthenticatedAuthoritySid, RepositoryFilePermission.READ);
    assertLocalAceExists(repo.getFile(rootFolderPath), commonAuthenticatedAuthoritySid,
        RepositoryFilePermission.READ_ACL);
    // assertOwner(pentahoContentRepository.getFile(rootFolderPath), repositoryAdminSid);
  }

  @Test
  public void testGetOrCreateUserHomeFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile suzyHomeFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    assertNotNull(suzyHomeFolder);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantRootFolderPath()));
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantPublicFolderPath()));
    assertLocalAclEmpty(repo.getFile(RepositoryPaths.getTenantPublicFolderPath()));
    //    assertOwner(pentahoContentRepository.getFile(publicFolderPath), repositoryAdminSid);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getTenantHomeFolderPath()));
    assertLocalAclEmpty(repo.getFile(RepositoryPaths.getTenantHomeFolderPath()));
    //    assertOwner(pentahoContentRepository.getFile(homeFolderPath), repositoryAdminSid);
    final String suzyFolderPath = RepositoryPaths.getUserHomeFolderPath();
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, suzyFolderPath));
    Sid suzySid = new PrincipalSid(USERNAME_SUZY);
    assertLocalAceExists(repo.getFile(suzyFolderPath), suzySid, RepositoryFilePermission.WRITE);
    assertLocalAceExists(repo.getFile(suzyFolderPath), suzySid, RepositoryFilePermission.READ);
    // make sure Jackrabbit agrees
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, suzyFolderPath, Privilege.JCR_WRITE));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, suzyFolderPath, Privilege.JCR_READ));
    // assertOwner(pentahoContentRepository.getFile(suzyFolderPath), suzySid);
  }

  @Test
  public void testGetFileAccessDenied() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile tiffanyHomeFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    assertNotNull(tiffanyHomeFolder);
    assertNotNull(repo.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(true).build()));
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
    repo.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(true).build());
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

  //  @Test
  //  public void testGetStreamForExecute() throws Exception {
  //    pentahoContentRepository.startup();
  //    SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION_TIFFANY);
  //    RepositoryFile file = pentahoContentRepository.getStreamForExecute("/pentaho/acme/home");
  //    assertNotNull(file);
  //  }

  @Test
  public void testStartupTwice() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath() + "[1]"));
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getPentahoRootFolderPath() + "[2]"));
  }

  @Test
  public void testGetOrCreateUserHomeFolderTwice() throws Exception {
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
    newFolder = repo.createFolder(parentFolder, newFolder);
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
    repo.createFolder(parentFolder, newFolder);
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
    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, "text/plain");
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

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, expectedEncoding,
        expectedMimeType);
    Date beginTime = Calendar.getInstance().getTime();
    RepositoryFile newFile = repo.createFile(parentFolder, new RepositoryFile.Builder(expectedName).build(), content);
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

    SimpleRepositoryFileContent contentFromRepo = repo.getContentForRead(foundFile, SimpleRepositoryFileContent.class);
    assertEquals(expectedEncoding, contentFromRepo.getEncoding());
    assertEquals(expectedMimeType, contentFromRepo.getMimeType());
    assertEquals(expectedDataString, IOUtils.toString(contentFromRepo.getData(), expectedEncoding));
  }

  @Test
  public void testCreateRunResultFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    final String expectedRunResultMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    final Map<String, String> expectedRunArguments = new HashMap<String, String>();
    expectedRunArguments.put("testKey", "testValue");
    RepositoryFile newFile = createRunResultFile(parentFolderPath, expectedName, expectedDataString, expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments);

    assertNotNull(newFile.getId());
    RepositoryFile foundFile = repo.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
    assertNotNull(foundFile.getCreatedDate());
    assertNotNull(foundFile.getLastModifiedDate());

    RunResultRepositoryFileContent contentFromRepo = repo.getContentForRead(foundFile,
        RunResultRepositoryFileContent.class);
    assertEquals(expectedEncoding, contentFromRepo.getEncoding());

    assertEquals(expectedDataString, IOUtils.toString(contentFromRepo.getData(), expectedEncoding));
    assertEquals(expectedRunArguments, contentFromRepo.getArguments());
    assertEquals(expectedRunResultMimeType, contentFromRepo.getMimeType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileUnrecognizedContentType() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    //    final String expectedDataString = "Hello World!";
    //    final String expectedEncoding = "UTF-8";
    //    byte[] data = expectedDataString.getBytes(expectedEncoding);
    //    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    //    final String expectedContentType = "notsupported";
    //    final String expectedMimeType = "text/plain";
    //    final String expectedName = "helloworld.xaction";
    //    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, expectedEncoding,
    //        expectedMimeType);
    IRepositoryFileContent content = new IRepositoryFileContent() {
      public String getContentType() {
        return "notsupported";
      }
    };

    repo.createFile(parentFolder, new RepositoryFile.Builder("helloworld.xaction").build(), content);
  }

  @Test
  public void testGetChildren() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    List<RepositoryFile> children = repo.getChildren(repo.getFile(RepositoryPaths.getPentahoRootFolderPath()));
    assertEquals(1, children.size());
    RepositoryFile f0 = children.get(0);
    assertEquals("acme", f0.getName());
    children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantRootFolderPath()));
    assertEquals(2, children.size());
    RepositoryFile f1 = children.get(0);
    assertEquals("home", f1.getName());
    RepositoryFile f2 = children.get(1);
    assertEquals("public", f2.getName());
  }

  /**
   * A user should only be able to see his home folder (unless your the admin).
   */
  @Test
  public void testListHomeFolders() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    List<RepositoryFile> children = repo.getChildren(repo.getFile(RepositoryPaths.getTenantHomeFolderPath()));
    assertEquals(1, children.size());
  }

  @Test
  public void testUpdateFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String expectedEncoding = "UTF-8";
    final String expectedRunResultMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final Map<String, String> expectedRunArguments = new HashMap<String, String>();
    expectedRunArguments.put("testKey", "testValue");

    RepositoryFile newFile = createRunResultFile(parentFolderPath, expectedName, "Hello World!", expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments);

    final String expectedModDataString = "Ciao World!";
    ByteArrayInputStream modDataStream = new ByteArrayInputStream(expectedModDataString.getBytes(expectedEncoding));
    final Map<String, String> modExpectedRunArguments = new HashMap<String, String>();
    modExpectedRunArguments.put("testKey2", "testValue2");

    final RunResultRepositoryFileContent modContent = new RunResultRepositoryFileContent(modDataStream,
        expectedEncoding, expectedRunResultMimeType, modExpectedRunArguments);

    repo.updateFile(newFile, modContent);

    RunResultRepositoryFileContent modContentFromRepo = repo.getContentForRead(repo.getFile(RepositoryPaths
        .getUserHomeFolderPath()
        + "/helloworld.xaction"), RunResultRepositoryFileContent.class);

    assertEquals(expectedModDataString, IOUtils.toString(modContentFromRepo.getData(), expectedEncoding));

    assertEquals(modExpectedRunArguments, modContentFromRepo.getArguments());
  }

  /**
   * Create the same folder twice inside a versioned parent folder. Second time through, we should fail and 
   */
  @Test
  public void testTransactionRollback() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    assertTrue(parentFolder.isVersioned());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = repo.createFolder(parentFolder, newFolder);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/test"));
    RepositoryFile anotherFolder = new RepositoryFile.Builder("test").folder(true).build();
    try {
      repo.createFolder(parentFolder, anotherFolder);
      fail("expected DataIntegrityViolationException");
    } catch (DataIntegrityViolationException e) {
    }
    assertFalse(SimpleJcrTestUtils.isCheckedOut(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath()));
  }

  @Test
  public void testDeleteVersionedFile() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentOfFolderToDeletePath = RepositoryPaths.getUserHomeFolderPath();
    RepositoryFile parentFolder = repo.getFile(parentOfFolderToDeletePath);
    RepositoryFile newFolder = repo.createFolder(parentFolder, new RepositoryFile.Builder("test").folder(true).build());
    assertNotNull(newFolder);
    final String folderToDeletePath = parentOfFolderToDeletePath + "/test";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, folderToDeletePath));
    int versionCount = SimpleJcrTestUtils.getVersionCount(testJcrTemplate, parentOfFolderToDeletePath);
    assertTrue(versionCount > 0);
    repo.deleteFile(newFolder);
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/test"));
    assertTrue(SimpleJcrTestUtils.getVersionCount(testJcrTemplate, parentOfFolderToDeletePath) > versionCount);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void testCreateDuplicateFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = repo.createFolder(parentFolder, newFolder);
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, RepositoryPaths.getUserHomeFolderPath() + "/test"));
    RepositoryFile anotherFolder = new RepositoryFile.Builder("test").folder(true).build();
    newFolder = repo.createFolder(parentFolder, anotherFolder);
  }

  @Test
  public void testWriteToPublic() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String parentFolderPath = RepositoryPaths.getTenantPublicFolderPath();
    final String encoding = "UTF-8";
    final Map<String, String> runArguments = new HashMap<String, String>();
    runArguments.put("testKey", "testValue");
    assertNotNull(createRunResultFile(parentFolderPath, "helloworld.xaction", "Hello World!", encoding, "text/plain",
        runArguments));
  }

  @Test
  public void testCreateVersionedFolder() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder, newFolder);
    assertTrue(newFolder.isVersioned());
    assertNotNull(newFolder.getVersionId());
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

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder,
        new RepositoryFile.Builder(fileName).versioned(true).build(), content);
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

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder, new RepositoryFile.Builder(fileName).build(), content);
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertFalse(newFile.isLocked());
    assertNull(newFile.getLockDate());
    assertNull(newFile.getLockMessage());
    assertNull(newFile.getLockOwner());
    final String lockMessage = "test by Mat";
    repo.lockFile(newFile, lockMessage);

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
    repo.unlockFile(newFile);

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

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder, new RepositoryFile.Builder(fileName).build(), content);
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertFalse(repo.getFile(filePath).isLocked());
    final String lockMessage = "test by Mat";
    repo.lockFile(newFile, lockMessage);

    repo.deleteFile(newFile);

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
    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = repo.createFile(parentFolder,
        new RepositoryFile.Builder(fileName).versioned(true).build(), content, "created helloworld.xaction",
        "new version", "label 0");
    repo.updateFile(newFile, content, "update 1", "label1");
    repo.updateFile(newFile, content, "update 2", "label2");
    RepositoryFile updatedFile = repo.updateFile(newFile, content, "update 3", "label3");
    List<VersionSummary> versionSummaries = repo.getVersionSummaries(updatedFile);
    assertNotNull(versionSummaries);
    assertTrue(versionSummaries.size() >= 3);
    assertEquals("update 3", versionSummaries.get(versionSummaries.size() - 1).getMessage());
    assertEquals(Arrays.asList(new String[] { "label3" }), versionSummaries.get(versionSummaries.size() - 1)
        .getLabels());
    System.out.println(versionSummaries);
    System.out.println(versionSummaries.size());
  }

  @Test
  public void testCircumventApiToGetVersionHistoryNodeAccessDenied() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getUserHomeFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder, newFolder);
    String versionHistoryAbsPath = SimpleJcrTestUtils.getVersionHistoryNodePath(testJcrTemplate, newFolder
        .getAbsolutePath());
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, versionHistoryAbsPath));
  }

  @Test
  public void testGetFileByVersionSummary() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);

    final String parentFolderPath = RepositoryPaths.getUserHomeFolderPath();
    final String encoding = "UTF-8";
    final String runResultMimeType = "text/plain";
    final String fileName = "helloworld.xaction";
    final Map<String, String> runArguments = new HashMap<String, String>();
    runArguments.put("testKey", "testValue");

    final String origDataString = "Hello World!";
    RepositoryFile newFile = createRunResultFile(parentFolderPath, fileName, origDataString, encoding,
        runResultMimeType, runArguments, true);
    final Serializable fileId = newFile.getId();
    final Serializable parentId = newFile.getParentId();
    final String absolutePath = newFile.getAbsolutePath();

    final String modDataString = "Ciao World!";
    ByteArrayInputStream modDataStream = new ByteArrayInputStream(modDataString.getBytes(encoding));

    final RunResultRepositoryFileContent modContent = new RunResultRepositoryFileContent(modDataStream, encoding,
        runResultMimeType, runArguments);

    repo.updateFile(newFile, modContent);

    List<VersionSummary> versionSummaries = repo.getVersionSummaries(newFile);
    RepositoryFile v1 = repo.getFile(versionSummaries.get(0));
    RepositoryFile v2 = repo.getFile(versionSummaries.get(versionSummaries.size() - 1));
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
    System.out.println("or: " + newFile);
    System.out.println("v1: " + v1);
    System.out.println("v2: " + v2);
    RunResultRepositoryFileContent c1 = repo.getContentForRead(v1, RunResultRepositoryFileContent.class);
    RunResultRepositoryFileContent c2 = repo.getContentForRead(v2, RunResultRepositoryFileContent.class);
    assertEquals(origDataString, IOUtils.toString(c1.getData(), c1.getEncoding()));
    assertEquals(modDataString, IOUtils.toString(c2.getData(), c2.getEncoding()));
  }

  @Test
  public void testOwnership() throws Exception {
    repo.getRepositoryEventHandler().onStartup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = repo.getFile(RepositoryPaths.getTenantPublicFolderPath());
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = repo.createFolder(parentFolder, newFolder);
//    assertEquals(USERNAME_SUZY, newFolder.getOwner());
    // TODO mlowery finish once setAcl is done
  }

  private RepositoryFile createRunResultFile(final String parentFolderPath, final String expectedName,
      final String expectedDataString, final String expectedEncoding, final String expectedRunResultMimeType,
      Map<String, String> expectedRunArguments, boolean versioned) throws Exception {
    RepositoryFile parentFolder = repo.getFile(parentFolderPath);
    byte[] data = expectedDataString.getBytes(expectedEncoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final RunResultRepositoryFileContent content = new RunResultRepositoryFileContent(dataStream, expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments);
    return repo
        .createFile(parentFolder, new RepositoryFile.Builder(expectedName).versioned(versioned).build(), content);
  }

  private RepositoryFile createRunResultFile(final String parentFolderPath, final String expectedName,
      final String expectedDataString, final String expectedEncoding, final String expectedRunResultMimeType,
      Map<String, String> expectedRunArguments) throws Exception {
    return createRunResultFile(parentFolderPath, expectedName, expectedDataString, expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments, false);
  }

  private void assertLocalAceExists(final RepositoryFile file, final Sid sid, final Permission permission) {
    Acl acl = repo.getAcl(file);
    AccessControlEntry[] aces = acl.getEntries();
    for (int i = 0; i < aces.length; i++) {
      AccessControlEntry ace = aces[i];
      if (sid.equals(ace.getSid()) && permission.equals(ace.getPermission())) {
        return;
      }
    }
    fail();
  }

  private void assertLocalAclEmpty(final RepositoryFile file) {
    Acl acl = repo.getAcl(file);
    assertTrue(acl.getEntries().length == 0);
  }

  private void assertOwner(final RepositoryFile file, final Sid sid) {
    Acl acl = repo.getAcl(file);
    assertTrue(sid.equals(acl.getOwner()));
  }

  private Serializable getNodeId(final String absPath) throws Exception {
    return SimpleJcrTestUtils.getNodeId(testJcrTemplate, absPath);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    repo = (IPentahoContentRepository) applicationContext.getBean("pentahoContentRepository");
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    testJcrTemplate.setExposeNativeSession(true);
    //    testMutableAclService = (MutableAclService) applicationContext.getBean("aclService");
    repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
    repositoryAdminAuthorityName = (String) applicationContext.getBean("repositoryAdminAuthorityName");
    commonAuthenticatedAuthorityName = (String) applicationContext.getBean("commonAuthenticatedAuthorityName");
    commonAuthenticatedAuthoritySid = new GrantedAuthoritySid(commonAuthenticatedAuthorityName);
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
    // necessary for unit test (Spring Security requires Authenticated role on all methods of PentahoContentRepository)
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
