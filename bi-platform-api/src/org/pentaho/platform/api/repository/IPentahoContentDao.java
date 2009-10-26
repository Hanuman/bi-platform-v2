package org.pentaho.platform.api.repository;

public interface IPentahoContentDao {
  
  /**
   * Returns file at given absolute path.
   * 
   * @param absPath absolute path
   * @return file or <code>null</code> if file does not exist
   */
  RepositoryFile getFile(final String absPath);
  
//  void removeFile(final RepositoryFile file);
  
  /**
   * Returns <code>true</code> if the file at the given absolute path exists.
   * 
   * @param absPath absolute path
   * @return <code>true</code> if the file at the given absolute path exists
   */
  boolean exists(final String absPath);
  
  /**
   * Creates a file.
   * 
   * @param parentFolder parent folder (may be <code>null</code>)
   * @param file file to create
   * @return new file with non-null id
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file);
}
