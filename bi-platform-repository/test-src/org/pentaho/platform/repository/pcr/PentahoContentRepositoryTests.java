package org.pentaho.platform.repository.pcr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
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
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.security.SecurityHelper;
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
@SuppressWarnings("nls")
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

  private IPentahoContentRepository pentahoContentRepository;

  /**
   * Used for state verification and test cleanup.
   */
  private JcrTemplate testJcrTemplate;

  private String repositoryAdminUsername;

  private String commonAuthenticatedAuthorityName;

  private Sid commonAuthenticatedAuthoritySid;

  private String repositoryAdminAuthorityName;

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
    SimpleJcrTestUtils.deleteItem(testJcrTemplate, "/pentaho");
    logout();
  }

  @Test(expected = IllegalStateException.class)
  public void testNotStartedUp() throws Exception {
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
  }

  @Test
  public void testStartup() throws Exception {
    pentahoContentRepository.startup();
    final String rootFolderPath = "/pentaho";
    loginAsRepositoryAdmin();
    // make sure pentaho root folder exists
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, rootFolderPath));
    // make sure ACEs exist
    assertLocalAceExists(pentahoContentRepository.getFile(rootFolderPath), commonAuthenticatedAuthoritySid,
        RepositoryFilePermission.READ);
    assertLocalAceExists(pentahoContentRepository.getFile(rootFolderPath), commonAuthenticatedAuthoritySid,
        RepositoryFilePermission.READ_ACL);
    // assertOwner(pentahoContentRepository.getFile(rootFolderPath), repositoryAdminSid);
  }

  @Test
  public void testGetOrCreateUserHomeFolder() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile suzyHomeFolder = pentahoContentRepository.getOrCreateUserHomeFolder();
    assertNotNull(suzyHomeFolder);
    final String tenantRootFolderPath = "/pentaho/acme";
    final String publicFolderPath = tenantRootFolderPath + "/public";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, tenantRootFolderPath));
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, publicFolderPath));
    assertLocalAclEmpty(pentahoContentRepository.getFile(publicFolderPath));
    //    assertOwner(pentahoContentRepository.getFile(publicFolderPath), repositoryAdminSid);
    final String homeFolderPath = tenantRootFolderPath + "/home";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, homeFolderPath));
    assertLocalAclEmpty(pentahoContentRepository.getFile(homeFolderPath));
    //    assertOwner(pentahoContentRepository.getFile(homeFolderPath), repositoryAdminSid);
    final String suzyFolderPath = homeFolderPath + "/suzy";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, suzyFolderPath));
    Sid suzySid = new PrincipalSid("suzy");
    assertLocalAceExists(pentahoContentRepository.getFile(suzyFolderPath), suzySid, RepositoryFilePermission.WRITE);
    assertLocalAceExists(pentahoContentRepository.getFile(suzyFolderPath), suzySid, RepositoryFilePermission.READ);
    // make sure Jackrabbit agrees
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, suzyFolderPath, Privilege.JCR_WRITE));
    assertTrue(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, suzyFolderPath, Privilege.JCR_READ));
    // assertOwner(pentahoContentRepository.getFile(suzyFolderPath), suzySid);
  }

  @Test
  public void testGetFileAccessDenied() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile tiffanyHomeFolder = pentahoContentRepository.getOrCreateUserHomeFolder();
    assertNotNull(tiffanyHomeFolder);
    assertNotNull(pentahoContentRepository.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(
        true).build()));
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String acmeTenantRootFolderPath = "/pentaho/acme";
    final String homeFolderPath = acmeTenantRootFolderPath + "/home";
    final String tiffanyFolderPath = homeFolderPath + "/tiffany";
    // read access for suzy on home
    assertNotNull(pentahoContentRepository.getFile(homeFolderPath));
    // no read access for suzy on tiffany's folder
    assertNull(pentahoContentRepository.getFile(tiffanyFolderPath));
    // no read access for suzy on subfolder of tiffany's folder
    final String tiffanySubFolderPath = tiffanyFolderPath + "/test";
    assertNull(pentahoContentRepository.getFile(tiffanySubFolderPath));
    // make sure Pat can't see acme folder (pat is in the duff tenant)
    login(USERNAME_PAT, TENANT_ID_DUFF);
    assertNull(pentahoContentRepository.getFile(acmeTenantRootFolderPath));
    assertFalse(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, acmeTenantRootFolderPath, Privilege.JCR_READ));
    assertFalse(SimpleJcrTestUtils.hasPrivileges(testJcrTemplate, acmeTenantRootFolderPath,
        Privilege.JCR_READ_ACCESS_CONTROL));
  }

  @Test
  public void testGetFileAdmin() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile tiffanyHomeFolder = pentahoContentRepository.getOrCreateUserHomeFolder();
    pentahoContentRepository.createFolder(tiffanyHomeFolder, new RepositoryFile.Builder("test").folder(true).build());
    login(USERNAME_JOE, TENANT_ID_ACME, true);
    pentahoContentRepository.getFile("/pentaho/acme/home/tiffany/test");
  }

  @Test
  public void testGetFileNotExist() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    RepositoryFile file2 = pentahoContentRepository.getFile("/doesnotexist");
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
    pentahoContentRepository.startup();
    pentahoContentRepository.startup();
  }

  @Test
  public void testGetOrCreateUserHomeFolderTwice() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    assertNotNull(pentahoContentRepository.getOrCreateUserHomeFolder());
    assertNotNull(pentahoContentRepository.getOrCreateUserHomeFolder());
  }

  @Test
  public void testCreateFolder() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/acme/home/suzy");
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    Date beginTime = Calendar.getInstance().getTime();
    newFolder = pentahoContentRepository.createFolder(parentFolder, newFolder);
    Date endTime = Calendar.getInstance().getTime();
    assertTrue(beginTime.before(newFolder.getCreatedDate()));
    assertTrue(endTime.after(newFolder.getCreatedDate()));
    assertNotNull(newFolder);
    assertNotNull(newFolder.getId());
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/acme/home/suzy/test"));
  }

  @Test(expected = DataRetrievalFailureException.class)
  public void testCreateFolderAccessDenied() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho");
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    pentahoContentRepository.createFolder(parentFolder, newFolder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFolderAtRootIllegal() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).build();
    pentahoContentRepository.createFolder(null, newFolder);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileAtRootIllegal() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String fileName = "helloworld.xaction";
    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, "text/plain");
    pentahoContentRepository.createFile(null, new RepositoryFile.Builder(fileName).build(), content);
  }

  @Test
  public void testCreateSimpleFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/acme/home/suzy");
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    byte[] data = expectedDataString.getBytes(expectedEncoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String expectedMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final String expectedAbsolutePath = "/pentaho/acme/home/suzy/helloworld.xaction";

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, expectedEncoding,
        expectedMimeType);
    Date beginTime = Calendar.getInstance().getTime();
    RepositoryFile newFile = pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(expectedName)
        .build(), content);
    Date endTime = Calendar.getInstance().getTime();
    assertTrue(beginTime.before(newFile.getLastModifiedDate()));
    assertTrue(endTime.after(newFile.getLastModifiedDate()));
    assertNotNull(newFile.getId());
    RepositoryFile foundFile = pentahoContentRepository.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
    assertNotNull(foundFile.getCreatedDate());
    assertNotNull(foundFile.getLastModifiedDate());

    SimpleRepositoryFileContent contentFromRepo = pentahoContentRepository.getContentForRead(foundFile,
        SimpleRepositoryFileContent.class);
    assertEquals(expectedEncoding, contentFromRepo.getEncoding());
    assertEquals(expectedMimeType, contentFromRepo.getMimeType());
    assertEquals(expectedDataString, IOUtils.toString(contentFromRepo.getData(), expectedEncoding));
  }

  @Test
  public void testCreateRunResultFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    final String expectedDataString = "Hello World!";
    final String expectedEncoding = "UTF-8";
    final String expectedRunResultMimeType = "text/plain";
    final String expectedName = "helloworld.xaction";
    final String parentFolderPath = "/pentaho/acme/home/suzy";
    final String expectedAbsolutePath = parentFolderPath + RepositoryFile.SEPARATOR + expectedName;
    final Map<String, String> expectedRunArguments = new HashMap<String, String>();
    expectedRunArguments.put("testKey", "testValue");
    RepositoryFile newFile = createRunResultFile(parentFolderPath, expectedName, expectedDataString, expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments);

    assertNotNull(newFile.getId());
    RepositoryFile foundFile = pentahoContentRepository.getFile(expectedAbsolutePath);
    assertNotNull(foundFile);
    assertEquals(expectedName, foundFile.getName());
    assertEquals(expectedAbsolutePath, foundFile.getAbsolutePath());
    assertNotNull(foundFile.getCreatedDate());
    assertNotNull(foundFile.getLastModifiedDate());

    RunResultRepositoryFileContent contentFromRepo = pentahoContentRepository.getContentForRead(foundFile,
        RunResultRepositoryFileContent.class);
    assertEquals(expectedEncoding, contentFromRepo.getEncoding());

    assertEquals(expectedDataString, IOUtils.toString(contentFromRepo.getData(), expectedEncoding));
    assertEquals(expectedRunArguments, contentFromRepo.getArguments());
    assertEquals(expectedRunResultMimeType, contentFromRepo.getMimeType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateFileUnrecognizedContentType() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/acme/home/suzy");
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

    pentahoContentRepository
        .createFile(parentFolder, new RepositoryFile.Builder("helloworld.xaction").build(), content);
  }

  @Test
  public void testGetChildren() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    List<RepositoryFile> children = pentahoContentRepository.getChildren(pentahoContentRepository.getFile("/pentaho"));
    assertEquals(0, children.size());
    pentahoContentRepository.getOrCreateUserHomeFolder();
    children = pentahoContentRepository.getChildren(pentahoContentRepository.getFile("/pentaho"));
    assertEquals(1, children.size());
    RepositoryFile f0 = children.get(0);
    assertEquals("acme", f0.getName());
    children = pentahoContentRepository.getChildren(pentahoContentRepository.getFile("/pentaho/acme"));
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
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    List<RepositoryFile> children = pentahoContentRepository.getChildren(pentahoContentRepository
        .getFile("/pentaho/acme/home"));
    assertEquals(1, children.size());
  }

  @Test
  public void testUpdateFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();

    final String parentFolderPath = "/pentaho/acme/home/suzy";
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

    pentahoContentRepository.updateFile(newFile, modContent);

    RunResultRepositoryFileContent modContentFromRepo = pentahoContentRepository.getContentForRead(
        pentahoContentRepository.getFile("/pentaho/acme/home/suzy/helloworld.xaction"),
        RunResultRepositoryFileContent.class);

    assertEquals(expectedModDataString, IOUtils.toString(modContentFromRepo.getData(), expectedEncoding));

    assertEquals(modExpectedRunArguments, modContentFromRepo.getArguments());
  }

  @Test
  public void testDeleteVersionedFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    final String parentOfFolderToDeletePath = "/pentaho/acme/home/suzy";
    RepositoryFile parentFolder = pentahoContentRepository.getFile(parentOfFolderToDeletePath);
    RepositoryFile newFolder = pentahoContentRepository.createFolder(parentFolder, new RepositoryFile.Builder("test")
        .folder(true).build());
    assertNotNull(newFolder);
    final String folderToDeletePath = parentOfFolderToDeletePath + "/test";
    assertNotNull(SimpleJcrTestUtils.getItem(testJcrTemplate, folderToDeletePath));
    int versionCount = SimpleJcrTestUtils.getVersionCount(testJcrTemplate, parentOfFolderToDeletePath);
    assertTrue(versionCount > 0);
    pentahoContentRepository.deleteFile(newFolder);
    assertNull(SimpleJcrTestUtils.getItem(testJcrTemplate, "/pentaho/acme/home/suzy/test"));
    assertTrue(SimpleJcrTestUtils.getVersionCount(testJcrTemplate, parentOfFolderToDeletePath) > versionCount);
  }

  @Test
  public void testWriteToPublic() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    final String parentFolderPath = "/pentaho/acme/public";
    final String encoding = "UTF-8";
    final Map<String, String> runArguments = new HashMap<String, String>();
    runArguments.put("testKey", "testValue");
    assertNotNull(createRunResultFile(parentFolderPath, "helloworld.xaction", "Hello World!", encoding, "text/plain",
        runArguments));
  }

  @Test
  public void testCreateVersionedFolder() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    RepositoryFile parentFolder = pentahoContentRepository.getFile("/pentaho/acme/home/suzy");
    RepositoryFile newFolder = new RepositoryFile.Builder("test").folder(true).versioned(true).build();
    newFolder = pentahoContentRepository.createFolder(parentFolder, newFolder);
  }

  @Test
  public void testCreateVersionedFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    final String parentFolderPath = "/pentaho/acme/home/suzy";
    RepositoryFile parentFolder = pentahoContentRepository.getFile(parentFolderPath);

    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(fileName)
        .versioned(true).build(), content);
    assertTrue(newFile.isVersioned());
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    int versionCount = SimpleJcrTestUtils.getVersionCount(testJcrTemplate, filePath);
    assertTrue(versionCount > 0);
    pentahoContentRepository.updateFile(newFile, content);
    assertTrue(SimpleJcrTestUtils.getVersionCount(testJcrTemplate, filePath) > versionCount);
  }

  @Test
  public void testLockFile() throws Exception {
    pentahoContentRepository.startup();
    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.getOrCreateUserHomeFolder();
    final String parentFolderPath = "/pentaho/acme/public";
    RepositoryFile parentFolder = pentahoContentRepository.getFile(parentFolderPath);
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes(encoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileContent content = new SimpleRepositoryFileContent(dataStream, encoding, mimeType);
    RepositoryFile newFile = pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(fileName)
        .build(), content);
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;
    assertNull(pentahoContentRepository.getLockSummary(newFile));
    final String lockMessage = "test by Mat";
    pentahoContentRepository.lockFile(newFile, lockMessage);

    assertTrue(SimpleJcrTestUtils.isLocked(testJcrTemplate, filePath));
    assertTrue(SimpleJcrTestUtils.getString(testJcrTemplate, filePath + "/pho:lockMessage").equals(lockMessage));
    assertNotNull(SimpleJcrTestUtils.getDate(testJcrTemplate, filePath + "/pho:lockDate"));

    login(USERNAME_TIFFANY, TENANT_ID_ACME);
    assertNotNull(pentahoContentRepository.getLockSummary(pentahoContentRepository.getFile(filePath)));

    login(USERNAME_SUZY, TENANT_ID_ACME);
    pentahoContentRepository.unlockFile(newFile);

    assertFalse(SimpleJcrTestUtils.isLocked(testJcrTemplate, filePath));
    assertNull(pentahoContentRepository.getLockSummary(newFile));
  }

  private RepositoryFile createRunResultFile(final String parentFolderPath, final String expectedName,
      final String expectedDataString, final String expectedEncoding, final String expectedRunResultMimeType,
      Map<String, String> expectedRunArguments) throws Exception {
    RepositoryFile parentFolder = pentahoContentRepository.getFile(parentFolderPath);
    byte[] data = expectedDataString.getBytes(expectedEncoding);
    ByteArrayInputStream dataStream = new ByteArrayInputStream(data);
    final RunResultRepositoryFileContent content = new RunResultRepositoryFileContent(dataStream, expectedEncoding,
        expectedRunResultMimeType, expectedRunArguments);
    return pentahoContentRepository.createFile(parentFolder, new RepositoryFile.Builder(expectedName).build(), content);
  }

  private void assertLocalAceExists(final RepositoryFile file, final Sid sid, final Permission permission) {
    Acl acl = pentahoContentRepository.getAcl(file);
    assertTrue(acl.isGranted(new Permission[] { permission }, new Sid[] { sid }, true));
  }

  private void assertLocalAclEmpty(final RepositoryFile file) {
    Acl acl = pentahoContentRepository.getAcl(file);
    assertTrue(acl.getEntries().length == 0);
  }

  private void assertOwner(final RepositoryFile file, final Sid sid) {
    Acl acl = pentahoContentRepository.getAcl(file);
    assertTrue(sid.equals(acl.getOwner()));
  }

  private Serializable getNodeId(final String absPath) throws Exception {
    return SimpleJcrTestUtils.getNodeId(testJcrTemplate, absPath);
  }

  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    pentahoContentRepository = (IPentahoContentRepository) applicationContext.getBean("pentahoContentRepository");
    SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
    testJcrTemplate = new JcrTemplate(jcrSessionFactory);
    testJcrTemplate.setAllowCreate(true);
    testJcrTemplate.setExposeNativeSession(true);
    //    testMutableAclService = (MutableAclService) applicationContext.getBean("aclService");
    repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
    repositoryAdminAuthorityName = (String) applicationContext.getBean("repositoryAdminAuthorityName");
    commonAuthenticatedAuthorityName = (String) applicationContext.getBean("commonAuthenticatedAuthorityName");
    commonAuthenticatedAuthoritySid = new GrantedAuthoritySid(commonAuthenticatedAuthorityName);
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
    final String tenantAuthenticatedAuthorityNameSuffix = "_Authenticated";
    final String tenantAdminAuthorityNameSuffix = "_Admin";

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
