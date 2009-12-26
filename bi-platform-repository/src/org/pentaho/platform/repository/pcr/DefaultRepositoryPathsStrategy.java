package org.pentaho.platform.repository.pcr;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository.RepositoryFile;
import org.pentaho.platform.repository.pcr.RepositoryPaths.IRepositoryPathsStrategy;

public class DefaultRepositoryPathsStrategy implements IRepositoryPathsStrategy {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(DefaultRepositoryPathsStrategy.class);

  // ~ Instance fields =================================================================================================

  private static final String FOLDER_HOME = "home"; //$NON-NLS-1$

  private static final String FOLDER_PUBLIC = "public"; //$NON-NLS-1$

  private static final String FOLDER_ROOT = "pentaho"; //$NON-NLS-1$

  private static final String PATH_ROOT = RepositoryFile.SEPARATOR + FOLDER_ROOT;

  private final String PATTERN_TENANT_ROOT_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}"; //$NON-NLS-1$

  private final String PATTERN_TENANT_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_HOME;

  private final String PATTERN_TENANT_PUBLIC_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_PUBLIC;

  private final String PATTERN_USER_HOME_PATH = PATH_ROOT + RepositoryFile.SEPARATOR + "{0}" //$NON-NLS-1$
      + RepositoryFile.SEPARATOR + FOLDER_HOME + RepositoryFile.SEPARATOR + "{1}"; //$NON-NLS-1$

  // ~ Constructors ====================================================================================================

  public DefaultRepositoryPathsStrategy() {
    super();
  }

  // ~ Methods =========================================================================================================

  public String getPentahoRootFolderPath() {
    return PATH_ROOT;
  }

  public String getTenantHomeFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_HOME_PATH, tenantId);
  }

  public String getTenantPublicFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_PUBLIC_PATH, tenantId);
  }

  public String getTenantRootFolderPath(final String tenantId) {
    return MessageFormat.format(PATTERN_TENANT_ROOT_PATH, tenantId);
  }

  public String getUserHomeFolderPath(final String tenantId, final String username) {
    return MessageFormat.format(PATTERN_USER_HOME_PATH, tenantId, username);
  }

  public String getPentahoRootFolderName() {
    return FOLDER_ROOT;
  }

  public String getTenantHomeFolderName() {
    return FOLDER_HOME;
  }

  public String getTenantPublicFolderName() {
    return FOLDER_PUBLIC;
  }

}
