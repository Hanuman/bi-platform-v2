package org.pentaho.platform.repository.pcr;

import java.io.Serializable;
import java.util.List;

import org.pentaho.platform.api.repository.IRepositoryFileData;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.VersionSummary;

/**
 * A data access object for reading and writing {@code RepositoryFile} instances. The methods in this interface closely
 * resemble those in {@link IRepositoryService}.
 * 
 * @author mlowery
 */
public interface IRepositoryFileDao {

  RepositoryFile getFile(final String absPath);

  RepositoryFile getFile(final String absPath, final boolean loadMaps);
  
  RepositoryFile getFileById(final Serializable fileId);

  RepositoryFile getFileById(final Serializable fileId, final boolean loadMaps);
  
  <T extends IRepositoryFileData> T getData(final Serializable fileId, final Serializable versionId, final Class<T> dataClass);

  RepositoryFile createFile(final Serializable parentFolderId, final RepositoryFile file,
      final IRepositoryFileData data, final String... versionMessageAndLabel);

  RepositoryFile createFolder(final Serializable parentFolderId, final RepositoryFile file,
      final String... versionMessageAndLabel);

  List<RepositoryFile> getChildren(final Serializable folderId);

  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData data,
      final String... versionMessageAndLabel);

  void deleteFile(final Serializable fileId, final String... versionMessageAndLabel);

  void undeleteFile(final Serializable fileId, final String... versionMessageAndLabel);
  
  void permanentlyDeleteFile(final Serializable fileId, final String... versionMessageAndLabel);
  
  List<RepositoryFile> getDeletedFiles(final Serializable folderId);
  
  List<RepositoryFile> getDeletedFiles();
  
  void lockFile(final Serializable fileId, final String message);

  void unlockFile(final Serializable fileId);

  List<VersionSummary> getVersionSummaries(final Serializable fileId);
  
  RepositoryFile getFile(final Serializable fileId, final Serializable versionId);
  
  void moveFile(final Serializable fileId, final String destAbsPath, final String... versionMessageAndLabel);
}
