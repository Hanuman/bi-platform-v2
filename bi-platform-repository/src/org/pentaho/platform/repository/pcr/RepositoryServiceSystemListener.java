package org.pentaho.platform.repository.pcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.IRepositoryService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Initializes the repository service.
 * 
 * @author wseyler
 * @author mlowery
 */
public class RepositoryServiceSystemListener implements IPentahoSystemListener {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(RepositoryServiceSystemListener.class);

  // ~ Instance fields =================================================================================================

  // ~ Constructors ====================================================================================================

  public RepositoryServiceSystemListener() {
    super();
  }

  // ~ Methods =========================================================================================================

  public boolean startup(IPentahoSession session) {
    try {
      PentahoSystem.get(IRepositoryService.class).getRepositoryEventHandler().onStartup();
      return true;
    } catch (Exception e) {
      logger.error("", e); //$NON-NLS-1$
      return false;
    }
  }

  public void shutdown() {

  }

}
