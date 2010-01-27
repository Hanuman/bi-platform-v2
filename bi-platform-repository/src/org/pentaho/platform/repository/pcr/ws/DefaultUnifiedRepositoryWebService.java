package org.pentaho.platform.repository.pcr.ws;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.jws.WebService;

import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;

@WebService(endpointInterface = "org.pentaho.platform.repository.pcr.ws.IUnifiedRepositoryWebService", name = "UnifiedRepositoryWebService", portName = "UnifiedRepositoryWebServicePort", targetNamespace = "http://www.pentaho.org/ws/1.0")
public class DefaultUnifiedRepositoryWebService implements IUnifiedRepositoryWebService {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private IUnifiedRepository repo;

  // ~ Constructors ====================================================================================================

  /**
   * No-arg constructor for when in Pentaho BI Server.
   */
  public DefaultUnifiedRepositoryWebService() {
    super();
    repo = PentahoSystem.get(IUnifiedRepository.class);
    if (repo == null) {
      throw new IllegalStateException("no IUnifiedRepository implementation");
    }
  }

  public DefaultUnifiedRepositoryWebService(final IUnifiedRepository repo) {
    super();
    this.repo = repo;
  }

  // ~ Methods =========================================================================================================

  public void startup() {
    repo.getRepositoryLifecycleManager().startup();
  }

  public RepositoryFile createFile(String parentFolderId, RepositoryFile file, NodeRepositoryFileData data,
      String versionMessage) {
    return repo.createFile(parentFolderId, file, data, versionMessage);
  }

  public RepositoryFile createFolder(String parentFolderId, RepositoryFile file, String versionMessage) {
    return repo.createFolder(parentFolderId, file, versionMessage);
  }

  public void deleteFile(String fileId, String versionMessage) {
    repo.deleteFile(fileId, versionMessage);
  }
  
  public void deleteFileAtVersion(String fileId, String versionId) {
    repo.deleteFileAtVersion(fileId, versionId);
  }

  public void deleteFileWithPermanentFlag(String fileId, boolean permanent, String versionMessage) {
    repo.deleteFile(fileId, permanent, versionMessage);
  }

  public RepositoryFileAcl getAcl(String fileId) {
    return repo.getAcl(fileId);
  }

  public List<RepositoryFile> getChildren(String folderId) {
    return repo.getChildren(folderId);
  }

  public List<RepositoryFile> getChildrenWithFilter(String folderId, String filter) {
    return repo.getChildren(folderId, filter);
  }

  public NodeRepositoryFileData getDataAsNodeForRead(String fileId) {
    return repo.getDataForRead(fileId, NodeRepositoryFileData.class);
  }

  public NodeRepositoryFileData getDataAsNodeForReadAtVersion(String fileId, String versionId) {
    return repo.getDataForReadAtVersion(fileId, versionId, NodeRepositoryFileData.class);
  }

  public List<RepositoryFile> getDeletedFiles() {
    return repo.getDeletedFiles();
  }

  public List<RepositoryFile> getDeletedFilesInFolder(String folderId) {
    return repo.getDeletedFiles(folderId);
  }

  public List<RepositoryFile> getDeletedFilesInFolderWithFilter(String folderId, String filter) {
    return repo.getDeletedFiles(folderId, filter);
  }

  public List<RepositoryFileAce> getEffectiveAces(String fileId) {
    return repo.getEffectiveAces(fileId);
  }

  public RepositoryFile getFile(String absPath) {
    return repo.getFile(absPath);
  }

  public RepositoryFile getFileAtVersion(String fileId, String versionId) {
    return repo.getFileAtVersion(fileId, versionId);
  }

  public RepositoryFile getFileById(String fileId) {
    return repo.getFileById(fileId);
  }

  public List<VersionSummary> getVersionSummaries(String fileId) {
    List<VersionSummary> s = repo.getVersionSummaries(fileId);
    return s;
  }

  public VersionSummary getVersionSummary(String fileId, String versionId) {
    return repo.getVersionSummary(fileId, versionId);
  }

  public boolean hasAccess(String absPath, RepositoryFilePermission[] permissions) {
    return repo.hasAccess(absPath, EnumSet.copyOf(Arrays.asList(permissions)));
  }

  public void lockFile(String fileId, String message) {
    repo.lockFile(fileId, message);
  }

  public void moveFile(String fileId, String destAbsPath, String versionMessage) {
    repo.moveFile(fileId, destAbsPath, versionMessage);
  }

  public void undeleteFile(String fileId, String versionMessage) {
    repo.undeleteFile(fileId, versionMessage);
  }

  public void unlockFile(String fileId) {
    repo.unlockFile(fileId);
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl acl) {
    return repo.updateAcl(acl);
  }

  public RepositoryFile updateFile(RepositoryFile file, NodeRepositoryFileData data, String versionMessage) {
    return repo.updateFile(file, data, versionMessage);
  }

}
