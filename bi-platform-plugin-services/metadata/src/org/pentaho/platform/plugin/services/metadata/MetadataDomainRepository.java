/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 */
package org.pentaho.platform.plugin.services.metadata;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.FileBasedMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * This is the platform implementation of the IMetadataDomainRepository.
 * 
 * TODO: Update this class to use CacheControl, getting created per session 
 * per Marc's input
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class MetadataDomainRepository extends FileBasedMetadataDomainRepository {
  
  private static final String LEGACY_LOCATION = "__LEGACY_LOCATION_"; //$NON-NLS-1$

  protected final Log logger = LogFactory.getLog(MetadataDomainRepository.class);
  
  public static String XMI_FILENAME = "metadata.xmi"; //$NON-NLS-1$
  
  public static final int[] ACCESS_TYPE_MAP = new int[] { IAclHolder.ACCESS_TYPE_READ, IAclHolder.ACCESS_TYPE_WRITE,
    IAclHolder.ACCESS_TYPE_UPDATE, IAclHolder.ACCESS_TYPE_DELETE, IAclHolder.ACCESS_TYPE_ADMIN,
    IAclHolder.ACCESS_TYPE_ADMIN };
  
  private static final String DOMAIN_FOLDER = "system/metadata/domains"; //$NON-NLS-1$

//  
// Use this approach for accessing the session object when
// updated to use the CacheManager
//
//  private IPentahoSession session;
//  
//  public void setSession(IPentahoSession session) {
//    this.session = session;
//  }
//  
//  public IPentahoSession getSession() {
//    return session;
//  }
  
  public IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }
  
  protected File getDomainsFolder() {
    String domainsFolder = PentahoSystem.getApplicationContext().getSolutionPath(DOMAIN_FOLDER);

    File folder = new File(domainsFolder);
    return folder;
  }
  
  public Set<String> getDomainIds() {
    // double check that all the legacy domains are loaded
    // this is necessary because the reloading of domains might have been done by 
    // a user who might not have permissions to all solutions
    reloadLegacyDomains(false);
    return super.getDomainIds();
  }
  
  public synchronized void reloadDomains() {
    // first reload new metadata domains
    super.reloadDomains();
    // then populate the legacy domains
    reloadLegacyDomains(true);
  }
  
  public synchronized void removeDomain(String domainId) {
    // determine if domain is legacy or not
    Domain domain = domains.get(domainId);
    
    if (domain.getProperty(LEGACY_LOCATION) != null) {
      // this is an xmi based domain, remove it
      removeLegacyDomain(domainId);
    } else {
      super.removeDomain(domainId);
    }
  }
  
  public Domain getDomain(String id) {
    Domain domain = super.getDomain(id);
    if (domain == null) {
      // reload legacy domains if not found
      reloadLegacyDomains(false);
      return super.getDomain(id);
    } else {
      return domain;
    }
  }
 
  public synchronized void removeLegacyDomain(String domainId) {
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    repo.removeSolutionFile(domainId + "/" + XMI_FILENAME);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized void reloadLegacyDomains(boolean overwrite) {
    // also load the XMI domains
    if (domains == null) {
      // we're too early for this call, it will be called again after domains are initialized.
      return;
    }
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    Document doc = repo.getSolutions(ISolutionRepository.ACTION_EXECUTE);
    List nodes = doc.selectNodes("/repository/file[@type='FILE.FOLDER']"); //$NON-NLS-1$
    int allSuccess = MetadataPublisher.NO_ERROR;
    for (Object node : nodes) {
      Node elem = ((Element) node).selectSingleNode("solution"); //$NON-NLS-1$
      if (elem != null) {
        String solution = elem.getText();
        if (overwrite || domains == null || !domains.containsKey(solution)) {
          allSuccess |= loadMetadata(solution);
        }
      }
    }
  }

  public int loadMetadata(final String solution) {
    int result = MetadataPublisher.NO_ERROR;
    String resourceName;
    InputStream xmiInputStream;
    resourceName = solution + "/" + XMI_FILENAME; //$NON-NLS-1$
    xmiInputStream = null;
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    if (repo.resourceExists(resourceName)) {
      try {
        xmiInputStream = repo.getResourceInputStream(resourceName, true);
        Domain domain = new XmiParser().parseXmi(xmiInputStream);
        domain.setProperty(LEGACY_LOCATION, resourceName); //$NON-NLS-1$
        domain.setId(solution);
        domains.put(solution, domain);
      } catch (Throwable t) {
        logger.error(Messages.getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD", resourceName), t); //$NON-NLS-1$
        result |= MetadataPublisher.UNABLE_TO_IMPORT;
      }
    } else {
      return result;
    }
    return result;
  }
  
  public synchronized void storeDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    if (domain.getId() == null) {
      throw new DomainIdNullException(Messages.getErrorString("IMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
    }

    if (!overwrite && domains != null && domains.get(domain.getId()) != null) {
      throw new DomainAlreadyExistsException(Messages.getErrorString("IMetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
    }

    if (domain.getProperty(LEGACY_LOCATION) != null) {
      storeLegacyDomain(domain, overwrite);
    } else {
      super.storeDomain(domain, overwrite);
    }
  }
  
  public synchronized void storeLegacyDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {

    if (domain.getId() == null) {
      throw new DomainIdNullException(Messages.getErrorString("IMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
    }
    
    // only allow editing vs. creation
    if (!overwrite) {
      throw new DomainAlreadyExistsException(Messages.getErrorString("IMetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
    }
    
    XmiParser parser = new XmiParser();
    String xmi = parser.generateXmi(domain);
    try {
      ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath("");
      repo.addSolutionFile(solutionPath, domain.getId(), XMI_FILENAME, xmi.getBytes(LocaleHelper.getSystemEncoding()), true); //$NON-NLS-1$
    } catch (Exception e) {
      throw new DomainStorageException("Failed to store legacy domain", e); //$NON-NLS-1$
    }
  }
}
