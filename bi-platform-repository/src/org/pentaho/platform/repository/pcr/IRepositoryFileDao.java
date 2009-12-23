package org.pentaho.platform.repository.pcr;

import java.util.List;

import org.pentaho.platform.api.repository.IRepositoryFileContent;
import org.pentaho.platform.api.repository.LockSummary;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.api.repository.VersionSummary;

/**
 * A data access object for reading and writing {@code RepositoryFile} instances.
 * 
 * @author mlowery
 */
public interface IRepositoryFileDao {

  /**
   * Returns file at given absolute path.
   * 
   * @param absPath absolute path
   * @return file or {@code null} if file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath);

  /**
   * Gets content for read.
   * 
   * @param file to read
   * @param contentClass class that implements {@link IRepositoryFileContent}
   * @return content
   */
  <T extends IRepositoryFileContent> T getContent(final RepositoryFile file, final Class<T> contentClass);

  //  void removeFile(final RepositoryFile file);

  /**
   * Creates a file.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param content file content
   * @param versionMessage optional version comment
   * @return new file with non-null id
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileContent content, final String versionMessage);

  /**
   * Creates a folder.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param versionMessage optional version comment
   * @return new file with non-null id
   */
  RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file, final String versionMessage);

  /**
   * Returns the children of this folder.
   * 
   * @param folder folder whose children to fetch
   * @return list of children (never {@code null})
   */
  List<RepositoryFile> getChildren(final RepositoryFile folder);

  /**
   * Updates a file and/or the content of a file.
   * 
   * @param file updated file
   * @param content updated content
   * @param versionMessage optional version comment
   * @return updated file (possible with new version number)
   */
  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileContent content, final String versionMessage);

  /**
   * Deletes a file or folder.
   * 
   * @param file file to delete
   * @param versionMessage optional version comment
   */
  void deleteFile(final RepositoryFile file, final String versionMessage);

  /**
   * Locks a file.
   * 
   * @param file file to lock
   * @param lock message
   */
  void lockFile(final RepositoryFile file, final String message);

  /**
   * Unlocks a file.
   * 
   * @param file file to unlock
   */
  void unlockFile(final RepositoryFile file);

  /**
   * Returns a non-null lock summary instance if this file is locked.
   * 
   * @param file file whose lock summary to get
   * @return lock summary or {@code null} if file is not locked
   */
  LockSummary getLockSummary(final RepositoryFile file);

  /**
   * Returns a list of version summary instances. The first version in the list is the root version. The last version
   * in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param file file whose versions to get
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries(final RepositoryFile file);
}
