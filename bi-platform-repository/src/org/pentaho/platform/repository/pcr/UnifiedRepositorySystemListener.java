package org.pentaho.platform.repository.pcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Initializes the repository service.
 * 
 * @author wseyler
 * @author mlowery
 */
public class UnifiedRepositorySystemListener implements IPentahoSystemListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(UnifiedRepositorySystemListener.class);

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public UnifiedRepositorySystemListener() {
    super();
  }

  // ~ Methods =========================================================================================================

  public boolean startup(IPentahoSession session) {
    try {
      PentahoSystem.get(IUnifiedRepository.class).getRepositoryLifecycleManager().startup();
      return true;
    } catch (Exception e) {
      logger.error("", e); //$NON-NLS-1$
      return false;
    }
  }

  public void shutdown() {

  }

}
