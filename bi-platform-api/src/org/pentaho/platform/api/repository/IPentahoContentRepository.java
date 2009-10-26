package org.pentaho.platform.api.repository;

/**
 * Entry point into the content repository.
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
   * Gets file for read.
   * 
   * @param absPath absolute path to file
   * @return file
   */
  RepositoryFile getFile(final String absPath);

  /**
   * Gets file for execute.
   * 
   * @param absPath absolute path to file
   * @return file
   */
  RepositoryFile getFileForExecute(final String absPath);
  
  /**
   * Returns <code>true</code> if file exists.
   * 
   * @param absPath absolute path to file
   * @return <code>true</code> if file exists
   */
  boolean exists(final String absPath);
  
  /**
   * Creates a file.
   * 
   * 
   * @param parentFolder parent folder (may be <code>null</code>)
   * @param file file to create
   * @return file that is equal to given file except with id populated
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file);
}
