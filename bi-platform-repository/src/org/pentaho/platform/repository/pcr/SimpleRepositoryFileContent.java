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
   * {@inheritDoc}
   */
  public InputStream getData() {
    return data;
  }

  /**
   * {@inheritDoc}
   */
  public String getEncoding() {
    return encoding;
  }

}
