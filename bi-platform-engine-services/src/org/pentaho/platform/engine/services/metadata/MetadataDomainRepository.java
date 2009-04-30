package org.pentaho.platform.engine.services.metadata;

import java.io.File;

import org.pentaho.metadata.repository.FileBasedMetadataDomainRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This is the platform implementation of the IMetadataDomainRepository
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class MetadataDomainRepository extends FileBasedMetadataDomainRepository {
  
  private static final String DOMAIN_FOLDER = "system/metadata/domains"; //$NON-NLS-1$
  
  protected File getDomainsFolder() {
    String domainsFolder = PentahoSystem.getApplicationContext().getSolutionPath(DOMAIN_FOLDER);

    File folder = new File(domainsFolder);
    return folder;
  }
}
