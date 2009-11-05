package org.pentaho.platform.api.repository;

import java.io.InputStream;

/**
 * The content or payload of the file. Access to this object should be subject to access control.
 * 
 * @author mlowery
 */
public interface IRepositoryFileContent {

  /**
   * Returns the character encoding of the bytes in the data stream. May be {@code null} for non-character data.
   * 
   * @return character encoding
   */
  String getEncoding();
  
  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   */
  InputStream getData();
  
}
