package org.pentaho.platform.api.repository;

import java.util.List;

public interface IPentahoContentDao {
  
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
   * @return new file with non-null id
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file, final IRepositoryFileContent content);
  
  /**
   * Creates a folder.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @return new file with non-null id
   */
  RepositoryFile createFolder(final RepositoryFile parentFolder, final RepositoryFile file);
  
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
   */
  void updateFile(final RepositoryFile file, final IRepositoryFileContent content);
  
  /**
   * Deletes a file or folder.
   * 
   * @param file file to delete
   */
  void deleteFile(final RepositoryFile file);
  
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
}
