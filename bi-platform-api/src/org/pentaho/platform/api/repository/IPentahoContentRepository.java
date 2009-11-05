package org.pentaho.platform.api.repository;

import java.io.InputStream;
import java.util.List;

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
   * @return file or {@code null} if the file does not exist
   */
  RepositoryFile getFile(final String absPath);
  
  /**
   * Gets stream for read.
   * 
   * @param file to read
   * @return stream
   */
  InputStream getStreamForRead(final RepositoryFile file);

  /**
   * Gets stream for execute.
   * 
   * @param file to execute
   * @return stream
   */
  InputStream getStreamForExecute(final RepositoryFile file);
  
  /**
   * Creates a file.
   * 
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param data stream with file data
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file, final InputStream data);
  
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
}
