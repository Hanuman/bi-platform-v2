package org.pentaho.platform.repository.pcr;

import java.util.List;

import org.pentaho.platform.api.repository.IRepositoryFileData;
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
   * Same as {@link #getFile(String)} except that if {@code loadMaps} is {@code true}, the maps for localized strings 
   * will be loaded as well. (Normally these are not loaded.) Use {@code true} in editing tools that can show the maps
   * for editing purposes.
   * 
   * @param absPath absolute path to file
   * @param loadMaps {@code true} to load localized string maps
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath, final boolean loadMaps);
  
  /**
   * Gets content for read.
   * 
   * @param file to read
   * @param contentClass class that implements {@link IRepositoryFileData}
   * @return content
   */
  <T extends IRepositoryFileData> T getContent(final RepositoryFile file, final Class<T> contentClass);

  //  void removeFile(final RepositoryFile file);

  /**
   * Creates a file.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param content file content
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return new file with non-null id
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file,
      final IRepositoryFileData content, final String... versionMessageAndLabel);

  /**
   * Creates a folder.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param versionMessageAndLabel optional version comment [0] and label [1] to be applied to parentFolder
   * @return new file with non-null id
   */
  RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file,
      final String... versionMessageAndLabel);

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
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   * @return updated file (possible with new version number)
   */
  RepositoryFile updateFile(final RepositoryFile file, final IRepositoryFileData content,
      final String... versionMessageAndLabel);

  /**
   * Deletes a file or folder.
   * 
   * @param file file to delete
   * @param versionMessageAndLabel optional version comment [0] and label [1]
   */
  void deleteFile(final RepositoryFile file, final String... versionMessageAndLabel);

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
   * Returns a list of version summary instances. The first version in the list is the root version. The last version
   * in the list is the base version. Branching and merging are not supported so this is a simple list.
   * 
   * @param file file whose versions to get
   * @return list of version summaries (never {@code null})
   */
  List<VersionSummary> getVersionSummaries(final RepositoryFile file);
  
  /**
   * Gets file as it was at the given version. Use this method to test for file existence too.
   * 
   * @param versionSummary version of file to retrieve
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final VersionSummary versionSummary);
}
