package org.pentaho.platform.repository.pcr.ws;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.RepositoryFileAce;
import org.pentaho.platform.api.repository.RepositoryFileAcl;
import org.pentaho.platform.api.repository.RepositoryFilePermission;
import org.pentaho.platform.api.repository.VersionSummary;
import org.pentaho.platform.repository.pcr.data.node.NodeRepositoryFileData;

public class UnifiedRepositoryToWebServiceAdapter implements IUnifiedRepository {

  private IUnifiedRepositoryWebService repoWebService;

  public UnifiedRepositoryToWebServiceAdapter(IUnifiedRepositoryWebService repoWebService) {
    super();
    this.repoWebService = repoWebService;
  }

  public RepositoryFile createFile(Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
      String versionMessage) {
    if (!(data instanceof NodeRepositoryFileData)) {
      throw new IllegalArgumentException();
    }
    return repoWebService.createFile(parentFolderId.toString(), file, (NodeRepositoryFileData) data, versionMessage);
  }

  public RepositoryFile createFolder(Serializable parentFolderId, RepositoryFile file, String versionMessage) {
    return repoWebService.createFolder(parentFolderId.toString(), file, versionMessage);
  }

  public void deleteFile(Serializable fileId, boolean permanent, String versionMessage) {
    repoWebService.deleteFileWithPermanentFlag(fileId.toString(), permanent, versionMessage);
  }

  public void deleteFile(Serializable fileId, String versionMessage) {
    repoWebService.deleteFile(fileId.toString(), versionMessage);
  }
  
  public void deleteFileAtVersion(Serializable fileId, Serializable versionId) {
    repoWebService.deleteFileAtVersion(fileId.toString(), versionId.toString());
  }

  public RepositoryFileAcl getAcl(Serializable fileId) {
    return repoWebService.getAcl(fileId.toString());
  }

  public List<RepositoryFile> getChildren(Serializable folderId) {
    return repoWebService.getChildren(folderId.toString());
  }

  public List<RepositoryFile> getChildren(Serializable folderId, String filter) {
    return repoWebService.getChildrenWithFilter(folderId.toString(), filter);
  }

  public <T extends IRepositoryFileData> T getDataForExecute(Serializable fileId, Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> T getDataForExecuteAtVersion(Serializable fileId, Serializable versionId,
      Class<T> dataClass) {
    throw new UnsupportedOperationException();
  }

  public <T extends IRepositoryFileData> T getDataForRead(Serializable fileId, Class<T> dataClass) {
    return (T) repoWebService.getDataAsNodeForRead(fileId.toString());
  }

  public <T extends IRepositoryFileData> T getDataForReadAtVersion(Serializable fileId, Serializable versionId,
      Class<T> dataClass) {
    return (T) repoWebService.getDataAsNodeForReadAtVersion(fileId.toString(), versionId != null ? versionId.toString()
        : null);
  }

  public List<RepositoryFile> getDeletedFiles(Serializable folderId) {
    return repoWebService.getDeletedFilesInFolder(folderId.toString());
  }

  public List<RepositoryFile> getDeletedFiles(Serializable folderId, String filter) {
    return repoWebService.getDeletedFilesInFolderWithFilter(folderId.toString(), filter);
  }

  public List<RepositoryFile> getDeletedFiles() {
    return repoWebService.getDeletedFiles();
  }

  public List<RepositoryFileAce> getEffectiveAces(Serializable fileId) {
    return repoWebService.getEffectiveAces(fileId.toString());
  }

  public RepositoryFile getFile(String absPath) {
    return repoWebService.getFile(absPath);
  }

  public RepositoryFile getFile(String absPath, boolean loadLocaleMaps) {
    throw new UnsupportedOperationException();
  }

  public RepositoryFile getFileAtVersion(Serializable fileId, Serializable versionId) {
    return repoWebService.getFileAtVersion(fileId.toString(), versionId != null ? versionId.toString() : null);
  }

  public RepositoryFile getFileById(Serializable fileId) {
    return repoWebService.getFileById(fileId.toString());
  }

  public RepositoryFile getFileById(Serializable fileId, boolean loadLocaleMaps) {
    throw new UnsupportedOperationException();
  }

  public IRepositoryLifecycleManager getRepositoryLifecycleManager() {
    throw new UnsupportedOperationException();
  }

  public List<VersionSummary> getVersionSummaries(Serializable fileId) {
    List<VersionSummary> s = repoWebService.getVersionSummaries(fileId.toString());
    return s;
  }

  public VersionSummary getVersionSummary(Serializable fileId, Serializable versionId) {
    return repoWebService.getVersionSummary(fileId.toString(), versionId != null ? versionId.toString() : null);
  }

  public boolean hasAccess(String absPath, EnumSet<RepositoryFilePermission> permissions) {
    return repoWebService.hasAccess(absPath, permissions.toArray(new RepositoryFilePermission[permissions.size()]));
  }

  public void lockFile(Serializable fileId, String message) {
    repoWebService.lockFile(fileId.toString(), message);
  }

  public void moveFile(Serializable fileId, String destAbsPath, String versionMessage) {
    repoWebService.moveFile(fileId.toString(), destAbsPath, versionMessage);
  }

  public void undeleteFile(Serializable fileId, String versionMessage) {
    repoWebService.undeleteFile(fileId.toString(), versionMessage);
  }

  public void unlockFile(Serializable fileId) {
    repoWebService.unlockFile(fileId.toString());
  }

  public RepositoryFileAcl updateAcl(RepositoryFileAcl acl) {
    return repoWebService.updateAcl(acl);
  }

  public RepositoryFile updateFile(RepositoryFile file, IRepositoryFileData data, String versionMessage) {
    return repoWebService.updateFile(file, (NodeRepositoryFileData) data, versionMessage);
  }

}
