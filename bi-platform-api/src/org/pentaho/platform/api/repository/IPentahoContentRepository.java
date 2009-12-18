package org.pentaho.platform.api.repository;

import java.util.List;

import org.springframework.security.acls.Acl;

/**
 * Entry point into the content repository.
 * 
 * <p>
 * Implementations should never filter results because of access restrictions. Furthermore, implementations should never
 * return {@code null} if a user does not have access to a file. The reason for this behavior is that you cannot know 
 * what the user wishes to do with a file when the user asks for it. Furthermore, this behavior is consistent with the
 * behavior of Linux. Of course, if a user has no read access to a folder, then you should not return file objects 
 * within that folder. Instead, throw an access exception.
 * </p>
 * 
 * <p>
 * With the exception of the {@code create} methods, all {@code RepositoryFile} instances should be retrieved 
 * from this service and not created explicitly. For example, to get a stream for execute, use
 * <pre>{@code
 * InputStream stream = getStreamForExecute(getFile("/myfile"));
 * }</pre>
 * </p>
 * 
 * @author mlowery
 */
public interface IPentahoContentRepository {

  /**
   * Starts up the repository.
   */
  void startup();
  
  /**
   * Shuts down the repository.
   */
  void shutdown();

  /**
   * Creates a home folder for the user if it does not already exist. Otherwise, returns existing home folder.
   * 
   * @return home folder
   */
  RepositoryFile createUserHomeFolderIfNecessary();

  /**
   * Gets file. Use this method to test for file existence too.
   * 
   * @param absPath absolute path to file
   * @return file or {@code null} if the file does not exist or access is denied
   */
  RepositoryFile getFile(final String absPath);
  
  /**
   * Gets content for read.
   * 
   * @param file to read
   * @param contentClass class that implements {@link IRepositoryFileContent}
   * @return content
   */
  <T extends IRepositoryFileContent> T getContentForRead(final RepositoryFile file, final Class<T> contentClass);

  /**
   * Gets content for execute.
   * 
   * @param file to execute
   * @param contentClass class that implements {@link IRepositoryFileContent}
   * @return content
   */
  <T extends IRepositoryFileContent> T getContentForExecute(final RepositoryFile file, final Class<T> contentClass);
  
  /**
   * Creates a file.
   * 
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param content file content
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file, final IRepositoryFileContent content);
  
  /**
   * Creates a folder.
   * 
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @return file that is equal to given file except with id populated
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
  
  // ~ Access query methods ============================================================================================

  Acl getAcl(final RepositoryFile file);

}
