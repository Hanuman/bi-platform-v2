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
   * Returns a stream for the file.
   * 
   * @param file file to open
   * @return stream
   */
  InputStream getStream(final RepositoryFile file);
  
//  void removeFile(final RepositoryFile file);
  
  /**
   * Creates a file.
   * 
   * @param parentFolder parent folder (may be {@code null})
   * @param file file to create
   * @param data stream with file data
   * @return new file with non-null id
   */
  RepositoryFile createFile(final RepositoryFile parentFolder, final RepositoryFile file, final InputStream data);
  
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
}
