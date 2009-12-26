package org.pentaho.platform.repository.pcr;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.springframework.util.Assert;

public class RepositoryPaths {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryPaths.class);

  public static final String DEFAULT = "DEFAULT"; //$NON-NLS-1$

  public static final String SYSTEM_PROPERTY = "pentaho.repositoryPaths.strategy"; //$NON-NLS-1$

  private static String strategyName = System.getProperty(SYSTEM_PROPERTY);

  private static IRepositoryPathsStrategy strategy;

  static {
    initialize();
  }

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public RepositoryPaths() {
    super();
  }

  // ~ Methods =========================================================================================================

  public static String getPentahoRootFolderPath() {
    return strategy.getPentahoRootFolderPath();
  }

  public static String getTenantHomeFolderPath(final String tenantId) {
    return strategy.getTenantHomeFolderPath(tenantId);
  }

  public static String getTenantPublicFolderPath(final String tenantId) {
    return strategy.getTenantPublicFolderPath(tenantId);
  }

  public static String getTenantRootFolderPath(final String tenantId) {
    return strategy.getTenantRootFolderPath(tenantId);
  }

  public static String getUserHomeFolderPath(final String tenantId, final String username) {
    return strategy.getUserHomeFolderPath(tenantId, username);
  }

  public static String getTenantHomeFolderPath() {
    return getTenantHomeFolderPath(internalGetTenantId());
  }

  public static String getTenantPublicFolderPath() {
    return getTenantPublicFolderPath(internalGetTenantId());
  }

  public static String getTenantRootFolderPath() {
    return getTenantRootFolderPath(internalGetTenantId());
  }

  public static String getUserHomeFolderPath() {
    return getUserHomeFolderPath(internalGetTenantId(), internalGetUsername());
  }

  public static String getTenantHomeFolderName() {
    return strategy.getTenantHomeFolderName();
  }

  public static String getTenantPublicFolderName() {
    return strategy.getTenantPublicFolderName();
  }

  public static String getPentahoRootFolderName() {
    return strategy.getPentahoRootFolderName();
  }

  private static void initialize() {
    if ((strategyName == null) || "".equals(strategyName)) { //$NON-NLS-1$
      strategyName = DEFAULT;
    }

    if (strategyName.equals(DEFAULT)) {
      strategy = new DefaultRepositoryPathsStrategy();
    } else {
      // Try to load a custom strategy
      try {
        Class clazz = Class.forName(strategyName);
        Constructor customStrategy = clazz.getConstructor(new Class[] {});
        strategy = (IRepositoryPathsStrategy) customStrategy.newInstance(new Object[] {});
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    logger.debug("RepositoryPaths initialized: strategy=" + strategyName);
  }

  public static void setStrategyName(final String strategyName) {
    RepositoryPaths.strategyName = strategyName;
    initialize();
  }

  public static interface IRepositoryPathsStrategy {
    String getPentahoRootFolderPath();

    String getTenantHomeFolderPath(final String tenantId);

    String getTenantPublicFolderPath(final String tenantId);

    String getTenantRootFolderPath(final String tenantId);

    String getUserHomeFolderPath(final String tenantId, final String username);

    String getTenantHomeFolderName();

    String getTenantPublicFolderName();

    String getPentahoRootFolderName();
  }

  /**
   * Returns the username of the current user.
   */
  private static String internalGetUsername() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return pentahoSession.getName();
  }

  /**
   * Returns the tenant ID of the current user.
   */
  private static String internalGetTenantId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state(pentahoSession != null);
    return (String) pentahoSession.getAttribute(IPentahoSession.TENANT_ID_KEY);
  }

}
