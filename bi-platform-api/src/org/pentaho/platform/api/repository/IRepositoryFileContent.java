package org.pentaho.platform.api.repository;

/**
 * The content or payload of the file. Access to this instances of this type should be subject to 
 * access control.
 * 
 * @author mlowery
 */
public interface IRepositoryFileContent {

  /**
   * String representing the content type. Available via {@link RepositoryFile#getContentType()}.
   * 
   * Can be used by client code to determine the exact IRepositoryFileContent implementation class that will be 
   * returned.
   * 
   * @return content type
   * 
   * @see RepositoryFile#getContentType()
   */
  String getContentType();
}
