package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.List;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.VersionSummary;


/**
 * A data access object for reading and writing {@code RepositoryFile} instances. The methods in this interface might 
 * closely resemble those in {@link IUnifiedRepository} but this interface is not part of the public Pentaho API and
 * can evolve independently.
 * 
 * @author mlowery
 */
public interface IRepositoryFileDao {

  RepositoryFile getFile(final String absPath);

  RepositoryFile getFile(final String absPath, final boolean loadLocaleMaps);
  
  RepositoryFile getFileById(final Serializable fileId);

  RepositoryFile getFileById(final Serializable fileId, final boolean loadLocaleMaps);
  
  <T extends IRepositoryFileData> T getData(final Serializable fileId, final Serializable versionId, final Class<T> dataClass);

  RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String versionMessage);

  RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String versionMessage);

  List<RepositoryFile> getChildren(final Serializable folderId, final String filter);

  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String versionMessage);

  void deleteFile(final Serializable fileId, final String versionMessage);

  void undeleteFile(final Serializable fileId, final String versionMessage);
  
  void permanentlyDeleteFile(final Serializable fileId, final String versionMessage);
  
  List<RepositoryFile> getDeletedFiles(final Serializable folderId, final String filter);
  
  List<RepositoryFile> getDeletedFiles();
  
  void lockFile(final Serializable fileId, final String message);

  void unlockFile(final Serializable fileId);

  List<VersionSummary> getVersionSummaries(final Serializable fileId);
  
  VersionSummary getVersionSummary(final Serializable fileId, final Serializable versionId);
  
  RepositoryFile getFile(final Serializable fileId, final Serializable versionId);
  
  void moveFile(final Serializable fileId, final String destAbsPath, final String versionMessage);
}
