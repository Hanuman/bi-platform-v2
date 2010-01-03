package org.pentaho.platform.repository.pcr;

import java.io.InputStream;

import org.pentaho.platform.api.repository.IRepositoryFileData;

/**
 * A {@link IRepositoryFileData} that has an input stream, encoding, and optional MIME type.
 * 
 * @author mlowery
 */
public class SimpleRepositoryFileData implements IRepositoryFileData {

  // ~ Static fields/initializers ======================================================================================

  private static final long serialVersionUID = -1571991472814251230L;

  // ~ Instance fields =================================================================================================

  private InputStream stream;

  private String encoding;

  private String mimeType;

  // ~ Constructors ====================================================================================================

  public SimpleRepositoryFileData(final InputStream stream, final String encoding, final String mimeType) {
    super();
    this.stream = stream;
    this.encoding = encoding;
    this.mimeType = mimeType;
  }

  // ~ Methods =========================================================================================================

  /**
   * Returns a stream for reading the data in this file.
   * 
   * @return stream (may be {@code null})
   */
  public InputStream getStream() {
    return stream;
  }

  /**
   * Returns the character encoding of the bytes in the data stream. May be {@code null} for non-character data.
   * 
   * @return character encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Returns the MIME type of the data in this file.
   * 
   * @return MIME type
   */
  public String getMimeType() {
    return mimeType;
  }

}
