package org.pentaho.platform.repository.pcr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.repository.IPentahoContentRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Initializes the Pentaho content repository.
 * 
 * @author wseyler
 * @author mlowery
 */
public class PentahoContentRepositorySystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog(PentahoContentRepositorySystemListener.class);

  public boolean startup(IPentahoSession session) {
    try {
      PentahoSystem.get(IPentahoContentRepository.class).getRepositoryEventHandler().onStartup();
      return true;
    } catch (Exception e) {
      logger.error("", e);
      return false;
    }
  }

  public void shutdown() {

  }

}
