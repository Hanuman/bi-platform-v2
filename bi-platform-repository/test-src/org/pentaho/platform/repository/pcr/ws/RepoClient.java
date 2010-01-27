package org.pentaho.platform.repository.pcr.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.RepositoryFileSid;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.repository.pcr.data.node.DataNode;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;

public class RepoClient {
  private IUnifiedRepository repo;

  @Before
  public void setUp() throws Exception {
//    System.setProperty("com.sun.xml.ws.monitoring.endpoint", "true");
//    System.setProperty("com.sun.xml.ws.monitoring.client", "true");
//    System.setProperty("com.sun.xml.ws.monitoring.registrationDebug", "FINE");
//    System.setProperty("com.sun.xml.ws.monitoring.runtimeDebug", "true");
    
    Service service = Service.create(new URL("http://localhost:8080/pentaho/webservices/repo?wsdl"), new QName(
        "http://www.pentaho.org/ws/1.0", "DefaultUnifiedRepositoryWebServiceService"));

    IUnifiedRepositoryWebService repoWebService = service.getPort(IUnifiedRepositoryWebService.class);

    // TODO mlowery uncomment this in the real client
    // repoWebService.startup();
    // basic auth
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "suzy");
    ((BindingProvider) repoWebService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "password");
    repo = new UnifiedRepositoryToWebServiceAdapter(repoWebService);
    cleanup();
  }

  protected void cleanup() throws Exception {
    RepositoryFile folder1 = repo.getFile("/pentaho/tenant0/home/suzy/folder1");
    if (folder1 != null) {
      repo.deleteFile(folder1.getId(), true, null);
    }
  }

  @Test
  public void testEverything() {
    RepositoryFile f0 = repo.getFile("/pentaho");
    assertNotNull(f0);
    assertEquals("pentaho", f0.getName());
    RepositoryFile f = repo.getFile("/pentaho/tenant0/home/suzy");
    assertNotNull(f.getId());
    assertEquals("/pentaho/tenant0/home/suzy", f.getAbsolutePath());
    assertNotNull(f.getCreatedDate());
    assertEquals("suzy", f.getName());
    assertTrue(f.isFolder());
    assertNotNull(repo.getFileById(f.getId()));
    RepositoryFile folder1 = repo.createFolder(f.getId(), new RepositoryFile.Builder("folder1").folder(true).build(),
        null);
    assertNotNull(folder1);
    assertEquals("folder1", folder1.getName());
    assertNotNull(folder1.getId());
    DataNode node = new DataNode("testNode");
    node.setProperty("prop1", "hello world");
    NodeRepositoryFileData data = new NodeRepositoryFileData(node);
    RepositoryFile file1 = repo.createFile(folder1.getId(), new RepositoryFile.Builder("file1.whatever")
        .versioned(true).build(), data, null);
    assertNotNull(file1);
    assertNotNull(file1.getId());
    NodeRepositoryFileData file1Data = repo.getDataForRead(file1.getId(), NodeRepositoryFileData.class);
    assertNotNull(file1Data);
    assertEquals("testNode", file1Data.getNode().getName());
    assertEquals("hello world", file1Data.getNode().getProperty("prop1").getString());
    List<RepositoryFile> folder1Children = repo.getChildren(folder1.getId());
    assertNotNull(folder1Children);
    assertEquals(1, folder1Children.size());
    List<RepositoryFile> folder1ChildrenFiltered = repo.getChildren(folder1.getId(), "*.sample");
    assertNotNull(folder1ChildrenFiltered);
    assertEquals(0, folder1ChildrenFiltered.size());
    List<RepositoryFile> folder1ChildrenFiltered2 = repo.getChildren(folder1.getId(), "*.whatever");
    assertNotNull(folder1ChildrenFiltered2);
    assertEquals(1, folder1ChildrenFiltered2.size());
    assertEquals(0, repo.getDeletedFiles().size());
    repo.deleteFile(file1.getId(), null);
    assertEquals(1, repo.getDeletedFiles().size());
    assertEquals(1, repo.getDeletedFiles(folder1.getId()).size());
    assertEquals(0, repo.getDeletedFiles(folder1.getId(), "*.sample").size());
    assertEquals(1, repo.getDeletedFiles(folder1.getId(), "*.whatever").size());
    repo.undeleteFile(file1.getId(), null);
    assertEquals(0, repo.getDeletedFiles().size());
    assertEquals(0, repo.getDeletedFiles(folder1.getId()).size());
    assertEquals(0, repo.getDeletedFiles(folder1.getId(), "*.whatever").size());
    assertFalse(repo.hasAccess("/pentaho", EnumSet.of(RepositoryFilePermission.WRITE)));
    List<RepositoryFileAce> folder1EffectiveAces = repo.getEffectiveAces(folder1.getId());
    assertEquals(1, folder1EffectiveAces.size());
    RepositoryFileAcl folder1Acl = repo.getAcl(folder1.getId());
    assertEquals("suzy", folder1Acl.getOwner().getName());
    RepositoryFileAcl updatedFolder1Acl = repo.updateAcl(new RepositoryFileAcl.Builder(folder1Acl).entriesInheriting(
        false).ace("suzy", RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL).build());
    assertNotNull(updatedFolder1Acl);
    assertEquals(1, updatedFolder1Acl.getAces().size());
    updatedFolder1Acl = repo.updateAcl(new RepositoryFileAcl.Builder(updatedFolder1Acl).ace("tiffany",
        RepositoryFileSid.Type.USER, RepositoryFilePermission.READ).build());
    List<RepositoryFileAce> file1EffectiveAces = repo.getEffectiveAces(file1.getId());
    assertEquals(2, file1EffectiveAces.size());
    assertFalse(updatedFolder1Acl.isEntriesInheriting());
    DataNode updatedNode = new DataNode("testNode");
    updatedNode.setProperty("prop1", "ciao world");
    NodeRepositoryFileData updatedData = new NodeRepositoryFileData(updatedNode);
    Date beforeUpdate = new Date();
    RepositoryFile updatedFile1 = repo.updateFile(file1, updatedData, null);
    assertNotNull(updatedFile1);
    assertTrue(updatedFile1.getLastModifiedDate().after(beforeUpdate));
    assertFalse(file1.isLocked());
    repo.lockFile(file1.getId(), "I locked this file");
    file1 = repo.getFile("/pentaho/tenant0/home/suzy/folder1/file1.whatever");
    assertTrue(file1.isLocked());
    repo.unlockFile(file1.getId());
    file1 = repo.getFile("/pentaho/tenant0/home/suzy/folder1/file1.whatever");
    assertFalse(file1.isLocked());
    repo.moveFile(file1.getId(), "/pentaho/tenant0/home/suzy", null);
    assertNull(repo.getFile("/pentaho/tenant0/home/suzy/folder1/file1.whatever"));
    assertNotNull(repo.getFile("/pentaho/tenant0/home/suzy/file1.whatever"));
    repo.moveFile(file1.getId(), "/pentaho/tenant0/home/suzy/folder1", null);
    assertNotNull(repo.getFile("/pentaho/tenant0/home/suzy/folder1/file1.whatever"));
    assertNull(repo.getFile("/pentaho/tenant0/home/suzy/file1.whatever"));
    List<VersionSummary> versionSummaries = repo.getVersionSummaries(file1.getId());
    assertNotNull(versionSummaries);
    assertTrue(versionSummaries.size() >= 2);
    assertEquals("suzy", versionSummaries.get(0).getAuthor());
    VersionSummary versionSummary = repo.getVersionSummary(file1.getId(), null);
    assertNotNull(versionSummary);
    assertNotNull(versionSummary.getId());
    versionSummary = repo.getVersionSummary(file1.getId(), versionSummaries.get(versionSummaries.size() - 1).getId());
    assertNotNull(versionSummary);
    assertNotNull(versionSummary.getId());
    RepositoryFile file1AtVersion = repo.getFileAtVersion(file1.getId(), versionSummary.getId());
    assertNotNull(file1AtVersion);
    assertEquals(versionSummary.getId(), file1AtVersion.getVersionId());
    NodeRepositoryFileData file1DataAtVersion = repo.getDataForReadAtVersion(file1.getId(), versionSummary.getId(),
        NodeRepositoryFileData.class);
    assertNotNull(file1DataAtVersion);
    assertEquals("ciao world", file1DataAtVersion.getNode().getProperty("prop1").getString());
  }
}
