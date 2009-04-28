package org.pentaho.platform.engine.services.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

/**
 * This is an initial implementation of the IMetadataDomainRepository
 * 
 * @author Will Gorman(wgorman@pentaho.com)
 */
public class MetadataDomainRepository implements IMetadataDomainRepository {
  
  private static final Log logger = LogFactory.getLog(MetadataDomainRepository.class);
  
  private static final String DOMAIN_SUFFIX = ".domain.xml"; //$NON-NLS-1$
  private static final String DOMAIN_FOLDER = "system/metadata/domains"; //$NON-NLS-1$
  
  Map<String, Domain> domains = null;
  
  public synchronized void storeDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    // stores a domain to system/metadata/DOMAIN_ID.domain.xml
    //ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, session);
    //repo.addSolutionFile(baseUrl, path, fileName, data, overwrite)

    if (domain.getId() == null) {
      // todo: replace with exception
      throw new DomainIdNullException(Messages.getErrorString("MetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
    }
    
    if (!overwrite && domains != null && domains.get(domain.getId()) != null) {
      throw new DomainAlreadyExistsException(Messages.getErrorString("MetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
    }
    
    File folder = getDomainsFolder();
    if (!folder.exists()) {
      folder.mkdirs();
    }
    
    File domainFile = new File(folder, getDomainFilename(domain.getId()));
    
    if (!overwrite && domainFile.exists()) {
      throw new DomainAlreadyExistsException(Messages.getErrorString("MetadataDomainRepository.ERROR_0003_DOMAIN_FILE_EXISTS", domain.getId())); //$NON-NLS-1$
    }
    
    SerializationService service = new SerializationService();
    try {
      FileOutputStream output = new FileOutputStream(domainFile);
      service.serializeDomain(domain, output);
    } catch (FileNotFoundException e) {
      throw new DomainStorageException(Messages.getErrorString("MetadataDomainRepository.ERROR_0004_DOMAIN_STORAGE_EXCEPTION"), e); //$NON-NLS-1$
    }
    
    // adds the domain to the domains list
    if (domains == null) {
      domains = new HashMap<String, Domain>();
    }
    domains.put(domain.getId(), domain);
  }
  
  private String getDomainFilename(String id) {
    String cleansedName = id.replaceAll("[^a-zA-Z0-9_]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
    return cleansedName + DOMAIN_SUFFIX;
  }
  
  public Domain getDomain(String id) {
    // for now, lazy load all the domains at once.  We could be smarter,
    // loading the files as requested.
    
    if (domains == null) {
      reloadDomains();
    }
    return domains.get(id);
  }
  
  public Set<String> getDomainIds() {
    if (domains == null) {
      reloadDomains();
    }
    return domains.keySet();
  }
  
  private static class DomainFileNameFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
      return name.endsWith(DOMAIN_SUFFIX);
    }
  }
  
  private File getDomainsFolder() {
    String domainsFolder = PentahoSystem.getApplicationContext().getSolutionPath(DOMAIN_FOLDER);

    File folder = new File(domainsFolder);
    return folder;
  }
  
  public synchronized void flushDomains() {
    domains = null;
  }
  
  public synchronized void reloadDomains() {
    // load the domains from the file system
    // for each file in the system/metadata/domains folder that ends with .domain.xml, load
    Map<String, Domain> localDomains = new HashMap<String, Domain>();
    SerializationService service = new SerializationService();
    File folder = getDomainsFolder();
    if (folder.exists()) {
      for (File file : folder.listFiles(new DomainFileNameFilter())) {
        // load domain
        try {
          Domain domain = service.deserializeDomain(new FileInputStream(file));
          localDomains.put(domain.getId(), domain);
        } catch (FileNotFoundException e) {
          logger.error(Messages.getErrorString("MetadataDomainRepository.ERROR_0005_FAILED_TO_LOAD_DOMAIN", file.getName()) , e); //$NON-NLS-1$
        }
      }
    }
    
    domains = localDomains;
  }
  
  public synchronized void removeDomain(String domainId) {
    File folder = getDomainsFolder();
    File domainFile = new File(folder, getDomainFilename(domainId));
    domains.remove(domainId);
    domainFile.delete();
  }
}
