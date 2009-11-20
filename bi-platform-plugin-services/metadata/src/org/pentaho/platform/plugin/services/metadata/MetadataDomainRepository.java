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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.FileBasedMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ISolutionFilter;
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
  
  public static String LEGACY_XMI_FILENAME = "metadata.xmi"; //$NON-NLS-1$
  
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
    
    // the super call is made to avoid having it called twice, the logic in getDomainIds
    // calls reload if the map is empty.
    synchronized(domains) {
      super.reloadDomains();
      reloadLegacyDomains(false);
    }
    return super.getDomainIds();
  }
  
  public void reloadDomains() {
    synchronized(domains) {
      // first reload new metadata domains
      super.reloadDomains();
      // then populate the legacy domains
      reloadLegacyDomains(true);
    }
  }
  
  public void removeDomain(String domainId) {
    synchronized(domains) {
      // determine if domain is legacy or not
      Domain domain = domains.get(domainId);
      
      if (domain.getProperty(LEGACY_LOCATION) != null) {
        // this is an xmi based domain, remove it
        removeLegacyDomain(domainId);
      } else {
        super.removeDomain(domainId);
      }
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
 
  private void removeLegacyDomain(String domainId) {
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    repo.removeSolutionFile(domainId); 
    domains.remove(domainId);
  }
  
  @SuppressWarnings("unchecked")
  private void reloadLegacyDomains(boolean overwrite) {
    // also load the XMI domains
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    // filter the repository looking for XMI files
    Document doc = repo.getSolutionTree(ISolutionRepository.ACTION_EXECUTE, 
        new ISolutionFilter() {
      public boolean keepFile(ISolutionFile solutionFile, int actionOperation) {
        return solutionFile.isDirectory() || solutionFile.getExtension().equalsIgnoreCase("xmi"); //$NON-NLS-1$
      }
    });
    int allSuccess = MetadataPublisher.NO_ERROR;
    try {
      // first look for metadata.xmi leaves in solution roots
      // e.g. /tree/pentaho-solutions/somedir/metadata.xmi
      List nodes = doc.selectNodes("/tree/branch/branch/leaf[@isDir='false']"); //$NON-NLS-1$
      for (Object obj : nodes) {
        Element node = (Element) obj;
        Element pathNode = (Element)(node).selectSingleNode("path"); //$NON-NLS-1$
        String path = pathNode.getText();
        // remove the first node
        int idx = path.indexOf(ISolutionRepository.SEPARATOR, 1);
        path = path.substring( idx+1 ); 
        // make sure the filename is 'metadata.xmi'
        if( path.endsWith( LEGACY_XMI_FILENAME )) {
          if (overwrite || !domains.containsKey(path)) {
            allSuccess |= loadMetadata(path);
          }
        }
      }

      // now look for  */resources/metadata/*.xmi
      // e.g. /tree/pentaho-solutions/some/resources/metadata/*.xmi
      nodes = doc.selectNodes("/tree/branch/branch/branch/branch/leaf[@isDir='false']"); //$NON-NLS-1$
      for (Object obj : nodes) {
        // build up the directory path
        Element node = (Element) obj;
        String dir = node.getParent().attributeValue( "id" ); //$NON-NLS-1$
        // make sure the parent branch ends with /resources/metadata
        if( dir.endsWith( "/resources/metadata" ) ) { //$NON-NLS-1$
          Element pathNode = (Element)(node).selectSingleNode("path"); //$NON-NLS-1$        
          String path = pathNode.getText();
          // remove the first node
          int idx = path.indexOf(ISolutionRepository.SEPARATOR, 1);
          path = path.substring( idx+1 ); 
          if (overwrite || !domains.containsKey(path)) {
            allSuccess |= loadMetadata(path);
          }
        }
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private int loadMetadata(final String resourceName) {
    int result = MetadataPublisher.NO_ERROR;
    InputStream xmiInputStream;
    xmiInputStream = null;
    ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
    if (repo.resourceExists(resourceName, ISolutionRepository.ACTION_EXECUTE)) {
      try {
        xmiInputStream = repo.getResourceInputStream(resourceName, true, ISolutionRepository.ACTION_EXECUTE);
        Domain domain = new XmiParser().parseXmi(xmiInputStream);
        domain.setProperty(LEGACY_LOCATION, resourceName);
        // The domain key is the full path and name of the XMI file within the solution repository
        // e.g. mysolution/resources/metadata/mymodel.xmi or
        // mysolution/metadata.xmi
        domain.setId(resourceName);
        domains.put(resourceName, domain);
      } catch (Throwable t) {
        logger.error(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD", resourceName), t); //$NON-NLS-1$
        throw new RuntimeException(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD"), t); //$NON-NLS-1$
      } finally {
        if (xmiInputStream != null) {
          try {
            xmiInputStream.close();
          } catch (IOException ex) {
            logger.error(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD", resourceName), ex); //$NON-NLS-1$
            throw new RuntimeException(Messages.getInstance().getString("MetadataPublisher.ERROR_0001_COULD_NOT_LOAD"), ex); //$NON-NLS-1$
          }
        }
      }
    } else {
      return result;
    }
    return result;
  }
  
  public void storeDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {
    synchronized(domains) {
      if (domain.getId() == null) {
        throw new DomainIdNullException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
      }
  
      if (!overwrite && domains.get(domain.getId()) != null) {
        throw new DomainAlreadyExistsException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
      }
  
      if (domain.getProperty(LEGACY_LOCATION) != null) {
        storeLegacyDomain(domain, overwrite);
      } else {
        super.storeDomain(domain, overwrite);
      }
    }
  }
  
  private void storeLegacyDomain(Domain domain, boolean overwrite) throws DomainIdNullException, DomainAlreadyExistsException, DomainStorageException {

    if (domain.getId() == null) {
      throw new DomainIdNullException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0001_DOMAIN_ID_NULL")); //$NON-NLS-1$
    }
    
    // only allow editing vs. creation
    if (!overwrite) {
      throw new DomainAlreadyExistsException(org.pentaho.metadata.messages.Messages.getErrorString("IMetadataDomainRepository.ERROR_0002_DOMAIN_OBJECT_EXISTS", domain.getId())); //$NON-NLS-1$
    }
    
    XmiParser parser = new XmiParser();
    String xmi = parser.generateXmi(domain);
    try {
      String domainId = domain.getId();
      // parse the domain id into a path and filename
      // e.g. domainId = mysolution/resources/metadata/mymodel.xmi
      // path = mysolution/resources/metadata
      // fileName = mymodel.xmi
      int idx = domainId.lastIndexOf(ISolutionRepository.SEPARATOR);
      String path = domainId.substring(0, idx);
      String fileName = domainId.substring( idx+1);
      ISolutionRepository repo = PentahoSystem.get(ISolutionRepository.class, getSession());
      String solutionPath = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
      repo.addSolutionFile(solutionPath, path, fileName, xmi.getBytes(LocaleHelper.getSystemEncoding()), true);
      
      // adds the domain to the domains list
      domains.put(domain.getId(), domain);
      
    } catch (Exception e) {
      throw new DomainStorageException(Messages.getInstance().getErrorString("MetadataDomainRepository.ERROR_0006_FAILED_TO_STORE_LEGACY_DOMAIN", domain.getId()), e); //$NON-NLS-1$
    }
  }
}
