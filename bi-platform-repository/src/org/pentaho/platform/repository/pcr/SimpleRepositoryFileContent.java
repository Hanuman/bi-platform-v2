package org.pentaho.platform.repository.pcr;

import java.io.InputStream;

import org.pentaho.platform.api.repository.IRepositoryFileContent;

/**
 * A {@link IRepositoryFileContent} that has an input stream and encoding (the minimum required by 
 * {@link IRepositoryFileContent}).
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileContent implements IRepositoryFileContent {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private InputStream data;

  private String encoding;

  // ~ Constructors ====================================================================================================

  public SimpleRepositoryFileContent(final InputStream data, final String encoding) {
    super();
    this.data = data;
    this.encoding = encoding;
  }

  // ~ Methods =========================================================================================================

  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   */
  public InputStream getData() {
    return data;
  }

  /**
   * Returns the character encoding of the bytes in the data stream. May be {@code null} for non-character data.
   * 
   * @return character encoding
   */
  public String getEncoding() {
    return encoding;
  }

}
