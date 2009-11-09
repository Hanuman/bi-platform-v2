package org.pentaho.platform.api.repository;

import java.io.InputStream;
import java.util.List;

public interface IPentahoContentDao {
  
  /**
   * Returns file at given absolute path.
   * 
   * @param absPath absolute path
   * @return file or {@code null} if file does not exist
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
}
