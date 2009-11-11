package org.pentaho.platform.repository.pcr;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The result of running an executable. Stores the arguments that were passed to the executable.
 * 
 * @author mlowery
 */
public class RunResultRepositoryFileContent extends SimpleRepositoryFileContent {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RunResultRepositoryFileContent.class);
  
  public static final String CONTENT_TYPE = "runresult";

  // ~ Instance fields =================================================================================================

  /**
   * The arguments passed to the executable (e.g. xaction) that produced this content.
   */
  private Map<String, String> arguments;
  
  // ~ Constructors ====================================================================================================

  public RunResultRepositoryFileContent(final InputStream data, final String encoding, final String mimeType,
      final Map<String, String> arguments) {
    super(data, encoding, mimeType);
    this.arguments = arguments;
  }

  // ~ Methods =========================================================================================================

  public Map<String, String> getArguments() {
    return Collections.unmodifiableMap(arguments);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }
  
  
}
