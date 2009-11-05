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
 * @created Jul 12, 2005 
 * @author James Dixon, Angelo Rodriguez, Steven Barkdull
 */
package org.pentaho.platform.repository.solution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginOperation;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.repository.ISolutionRepositoryService;
import org.pentaho.platform.api.repository.SolutionRepositoryServiceException;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SolutionRepositoryServiceImpl implements ISolutionRepositoryService {

  private static final long serialVersionUID = -5870073658756939643L;
  private static final Log logger = LogFactory.getLog(SolutionRepositoryServiceImpl.class);
  private static final String RESPONSE_DOCUMENT_ENCODING = "UTF-8"; //$NON-NLS-1$
  private static final String RESPONSE_DOCUMENT_VERSION_NUM = "1.0"; //$NON-NLS-1$

  /**
   * contains instance of a sax parser factory. Use getSAXParserFactory() method to get a copy of the factory.
   */
  private static final ThreadLocal<SAXParserFactory> SAX_FACTORY = new ThreadLocal<SAXParserFactory>();

  static {
    // The SolutionRepositoryService creates/uses the ICacheManager from PentahoSystem to create a new
    // cache region specifically for the caching of the solution repository document. This is not put
    // into a session cache intentionally. Client tools like PRD do not maintain a session and would
    // thus never have any benefit from this. Since we are using a cache manager, if the cache is
    // unused long enough entries will age out.

    // We are caching the solution repository document on a per-user basis, as required, because the
    // document is that user's view of the repository, with respect to ACLs.

    // Upon publish, reload, or reset repository calls this cache is cleared in the reset method
    // of SolutionRepositoryBase.
    ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
    if (!cacheManager.cacheEnabled(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION)) {
      cacheManager.addCacheRegion(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION);
    }    
  }
  
  public SolutionRepositoryServiceImpl() {
    super();
  }

  /**
   * This method will delete a file from the ISolutionRepository and respects IPentahoAclEntry.PERM_DELETE.
   * 
   * @param userSession
   *          An IPentahoSession for the user requesting the delete operation
   * @param solution
   *          The name of the solution, such as 'steel-wheels'
   * @param path
   *          The path within the solution to the file/folder to be deleted (does not include the file/folder itself)
   * @param name
   *          The name of the file or folder which will be deleted in the given solution/path
   * @return Success of the delete operation is returned
   * @throws IOException
   */
  public boolean delete(final IPentahoSession userSession, final String solution, final String path,
      final String name) throws IOException {

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, name);
    return repository.removeSolutionFile(fullPath);
  }

  /**
   * This method creates a folder along with it's index.xml file.  
   * This method also verifies that the user has PERM_CREATE permissions before
   * creating the folder.
   * 
   * @param userSession the current user 
   * @param solution the solution path
   * @param path the folder path
   * @param name the name of the new folder
   * @param desc the description of the new folder
   * @return true if success
   * @throws IOException
   */
  public boolean createFolder(IPentahoSession userSession, String solution, String path, String name, String desc)
      throws IOException {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    if (solution == null) {
      solution = ""; //$NON-NLS-1$
    }

    // verify that the name does not contain a path separator before creating the folder
    if (name == null || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0 || //$NON-NLS-1$ //$NON-NLS-2$
        name.indexOf(ISolutionRepository.SEPARATOR) >= 0) {
      return false;
    }

    String parentFolderPath = ActionInfo.buildSolutionPath(solution, path, "" + ISolutionRepository.SEPARATOR); //$NON-NLS-1$
    ISolutionFile parentSolutionFile = repository.getSolutionFile(parentFolderPath, ISolutionRepository.ACTION_CREATE);
    if (parentSolutionFile != null && parentSolutionFile.isDirectory()) {
      File parent = new File(PentahoSystem.getApplicationContext().getSolutionPath(parentFolderPath));
      File newFolder = new File(parent, name);
      if (newFolder.exists()) {
        // if the new folder already exists, we need to get out
        return false;
      }
      repository.createFolder(newFolder);

      // create the index file content
      String defaultIndex = "<index><name>" + name + "</name><description>" + (desc != null ? desc : name) //$NON-NLS-1$ //$NON-NLS-2$
          + "</description><icon>reporting.png</icon><visible>true</visible><display-type>list</display-type></index>"; //$NON-NLS-1$

      // add the index file to the repository
      String indexPath = ActionInfo.buildSolutionPath(solution, path, name);
      String baseURL = PentahoSystem.getApplicationContext().getSolutionPath(""); //$NON-NLS-1$
      repository
          .addSolutionFile(baseURL, indexPath, ISolutionRepository.INDEX_FILENAME, defaultIndex.getBytes(), false);
      return true;
    }
    return false;
  }

  private boolean acceptFilter(String name, String[] filters) {
    if (filters == null || filters.length == 0) {
      return false;
    }
    for (int i = 0; i < filters.length; i++) {
      if (name.endsWith(filters[i])) {
        return true;
      }
    }
    return false;
  }

  private boolean accept(boolean isAdministrator, ISolutionRepository repository, ISolutionFile file) {
    return isAdministrator || repository.hasAccess(file, IPentahoAclEntry.PERM_EXECUTE);
  }

  private void processRepositoryFile(IPentahoSession session, boolean isAdministrator, ISolutionRepository repository,
      Node parentNode, ISolutionFile file, String[] filters) {
    if (!accept(isAdministrator, repository, file)) {
      // we don't want this file, skip to the next one
      return;
    }

    String name = file.getFileName();
    if (name.startsWith(".")) { //$NON-NLS-1$
      // these are hidden files of some type that are never shown
      // we don't want this file, skip to the next one
      return;
    }
    
    if (file.isDirectory()) {
      // we always process directories
        
      // MDD 10/16/2008 Not always.. what about 'system'
      if (file.getFileName().startsWith("system")) { //$NON-NLS-1$
        // skip the system dir, we DO NOT ever want this to hit the client
        return;
      }
      
      // maintain legacy behavior
      if (repository.getRootFolder(ISolutionRepository.ACTION_EXECUTE).getFullPath().equals(file.getFullPath())) {
        // never output the root folder as part of the repo doc; skip root and process its children
        ISolutionFile[] children = file.listFiles();
        for (ISolutionFile childSolutionFile : children) {
          processRepositoryFile(session, isAdministrator, repository, parentNode, childSolutionFile, filters);
        }
        return;
      }
      
      Element child = parentNode instanceof Document ? ((Document) parentNode).createElement("file") : parentNode.getOwnerDocument().createElement("file");  //$NON-NLS-1$//$NON-NLS-2$
      parentNode.appendChild(child);
      try {
        String localizedName = repository.getLocalizedFileProperty(file, "name", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
        child.setAttribute("localized-name", localizedName == null || "".equals(localizedName) ? name : localizedName); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (Exception e) {
        child.setAttribute("localized-name", name); //$NON-NLS-1$
      }
      try {
        String visible = repository.getLocalizedFileProperty(file, "visible", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
        child.setAttribute("visible", visible == null || "".equals(visible) ? "false" : visible); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      } catch (Exception e) {
        e.printStackTrace();
        child.setAttribute("visible", "false"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      String description = repository.getLocalizedFileProperty(file, "description", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
      child.setAttribute("description", description == null || "".equals(description) ? name : description); //$NON-NLS-1$ //$NON-NLS-2$
      child.setAttribute("name", name); //$NON-NLS-1$
      child.setAttribute("isDirectory", "true"); //$NON-NLS-1$ //$NON-NLS-2$
      child.setAttribute("lastModifiedDate", "" + file.getLastModified()); //$NON-NLS-1$ //$NON-NLS-2$

        
      ISolutionFile[] children = file.listFiles();
      for (ISolutionFile childSolutionFile : children) {
        processRepositoryFile(session, isAdministrator, repository, child, childSolutionFile, filters);
      }
    } else {     
      int lastPoint = name.lastIndexOf('.');
      String extension = ""; //$NON-NLS-1$
      if (lastPoint != -1) {
        // ignore anything with no extension
        extension = name.substring(lastPoint + 1).toLowerCase();
      }

      // xaction and URL support are built in
      boolean addFile = acceptFilter(name, filters) || "xaction".equals(extension) || "url".equals(extension); //$NON-NLS-1$ //$NON-NLS-2$
      boolean isPlugin = false;
      // see if there is a plugin for this file type
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, session);
      if (pluginManager != null) {
        Set<String> types = pluginManager.getContentTypes();
        isPlugin = types != null && types.contains(extension);
        addFile |= isPlugin;
      }

      if (addFile) {
        Element child = parentNode instanceof Document ? ((Document) parentNode).createElement("file") : parentNode.getOwnerDocument().createElement("file"); //$NON-NLS-1$ //$NON-NLS-2$
        parentNode.appendChild(child);
        IFileInfo fileInfo = null;
        try {
            // the visibility flag for action-sequences is controlled by
            // /action-sequence/documentation/result-type
            // and we should no longer be looking at 'visible' because it was
            // never actually used!
            String visible = "none".equals(repository.getLocalizedFileProperty(file, "documentation/result-type", ISolutionRepository.ACTION_EXECUTE)) ? "false" : "true"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            child.setAttribute("visible", (visible == null || "".equals(visible) || "true".equals(visible)) ? "true" : "false");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        } catch (Exception e) {
          child.setAttribute("visible", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (name.endsWith(".xaction")) { //$NON-NLS-1$
          // add special props?
          // localization..
        } else if (name.endsWith(".url")) { //$NON-NLS-1$

          // add special props
          String props = new String(file.getData());
          StringTokenizer tokenizer = new StringTokenizer(props, "\n"); //$NON-NLS-1$
          while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            int pos = line.indexOf('=');
            if (pos > 0) {
              String propname = line.substring(0, pos);
              String value = line.substring(pos + 1);
              if ((value != null) && (value.length() > 0) && (value.charAt(value.length() - 1) == '\r')) {
                value = value.substring(0, value.length() - 1);
              }
              if ("URL".equalsIgnoreCase(propname)) { //$NON-NLS-1$
                child.setAttribute("url", value); //$NON-NLS-1$
              }
            }
          }
        } else if (isPlugin) {
          // must be a plugin - make it look like a URL
          try {
            // get the file info object for this file
            // not all plugins are going to actually use the inputStream, so we have a special
            // wrapper inputstream so that we can pay that price when we need to (2X speed boost)
            PluginFileInputStream inputStream = new PluginFileInputStream(repository, file);
            fileInfo = pluginManager.getFileInfo(extension, session, file, inputStream);
            String handlerId = pluginManager.getContentGeneratorIdForType(extension, session);
            String fileUrl = pluginManager.getContentGeneratorUrlForType(extension, session);
            String solution = file.getSolutionPath();
            String path = ""; //$NON-NLS-1$
            if (solution.startsWith(ISolutionRepository.SEPARATOR + "")) { //$NON-NLS-1$
              solution = solution.substring(1);
            }
            int pos = solution.indexOf(ISolutionRepository.SEPARATOR);
            if (pos != -1) {
              path = solution.substring(pos + 1);
              solution = solution.substring(0, pos);
            }
            String url = null;
            if (!"".equals(fileUrl)) { //$NON-NLS-1$
              url = PentahoSystem.getApplicationContext().getBaseUrl() + fileUrl
                  + "?solution=" + solution + "&path=" + path + "&action=" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
              IContentInfo info = pluginManager.getContentInfoFromExtension(extension, session);
              for (IPluginOperation operation : info.getOperations()) {
                if (operation.getId().equalsIgnoreCase("RUN")) { //$NON-NLS-1$
              	  String command = operation.getCommand();
                  command = command.replaceAll("\\{solution\\}", solution); //$NON-NLS-1$
                  command = command.replaceAll("\\{path\\}", path); //$NON-NLS-1$
                  command = command.replaceAll("\\{name\\}", name); //$NON-NLS-1$
                  url = PentahoSystem.getApplicationContext().getBaseUrl() + command;
                  break;
                }
              }
              if (url == null) {
                url = PentahoSystem.getApplicationContext().getBaseUrl()
                  + "content/" + handlerId + "?solution=" + solution + "&path=" + path + "&action=" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
              }
            }
            child.setAttribute("url", url); //$NON-NLS-1$
            
            String paramServiceUrl = PentahoSystem.getApplicationContext().getBaseUrl() 
              + "content/" + handlerId + "?solution=" + solution + "&path=" + path + "&action=" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            
            child.setAttribute("param-service-url", paramServiceUrl); //$NON-NLS-1$
            
          } catch (Throwable t) {
            t.printStackTrace();
          }

        }

        // localization
        try {
          String localizedName = null;
          if (name.endsWith(".url")) { //$NON-NLS-1$
            localizedName = repository.getLocalizedFileProperty(file, "url_name", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
          } else if (fileInfo != null) {
            localizedName = fileInfo.getTitle();
          } else {
            localizedName = repository.getLocalizedFileProperty(file, "title", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
          }
          child
              .setAttribute("localized-name", localizedName == null || "".equals(localizedName) ? name : localizedName); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
          child.setAttribute("localized-name", name); //$NON-NLS-1$
        }
        try {
          // only folders, urls and xactions have descriptions
          if (name.endsWith(".url")) { //$NON-NLS-1$
            String url_description = repository.getLocalizedFileProperty(file, "url_description", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
            String description = repository.getLocalizedFileProperty(file, "description", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
            if (url_description == null && description == null) {
              child.setAttribute("description", name); //$NON-NLS-1$
            } else {
              child.setAttribute("description", url_description == null || "".equals(url_description) ? description //$NON-NLS-1$ //$NON-NLS-2$
                  : url_description);
            }
          } else if (name.endsWith(".xaction")) { //$NON-NLS-1$
            String description = repository.getLocalizedFileProperty(file, "description", ISolutionRepository.ACTION_EXECUTE); //$NON-NLS-1$
            child.setAttribute("description", description == null || "".equals(description) ? name : description); //$NON-NLS-1$ //$NON-NLS-2$
          } else if (fileInfo != null) {
            child.setAttribute("description", fileInfo.getDescription()); //$NON-NLS-1$
          } else {
            child.setAttribute("description", name); //$NON-NLS-1$
          }
        } catch (Exception e) {
          child.setAttribute("description", "xxxxxxx"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // add permissions for each file/folder
        child.setAttribute("name", name); //$NON-NLS-1$
        child.setAttribute("isDirectory", "" + file.isDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
        child.setAttribute("lastModifiedDate", "" + file.getLastModified()); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } // else isfile
  }

  public org.w3c.dom.Document getSolutionRepositoryDoc(IPentahoSession session, String[] filters) throws ParserConfigurationException {
    // The SolutionRepositoryService creates/uses the ICacheManager from PentahoSystem to create a new
    // cache region specifically for the caching of the solution repository document. This is not put
    // into a session cache intentionally. Client tools like PRD do not maintain a session and would
    // thus never have any benefit from this. Since we are using a cache manager, if the cache is
    // unused long enough entries will age out.

    // We are caching the solution repository document on a per-user basis, as required, because the
    // document is that user's view of the repository, with respect to ACLs.

    // Upon publish, reload, or reset repository calls this cache is cleared in the reset method
    // of SolutionRepositoryBase.

    ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
    org.w3c.dom.Document document = null;
    if (cacheManager != null && cacheManager.cacheEnabled(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION)) {
      document = (org.w3c.dom.Document) cacheManager.getFromRegionCache(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION, session.getName() + LocaleHelper.getLocale());
    }
      
    if (document == null) {
      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
      ISolutionFile rootFile = repository.getRootFolder(ISolutionRepository.ACTION_EXECUTE);
      document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      org.w3c.dom.Element root = document.createElement("repository"); //$NON-NLS-1$
      document.appendChild(root);
      root.setAttribute("path", rootFile.getFullPath()); //$NON-NLS-1$
      boolean isAdministrator = SecurityHelper.isPentahoAdministrator(session);
      processRepositoryFile(session, isAdministrator, repository, root, rootFile, filters);
      // only attempt to add to the cache if by this point it exists, it's possible that
      // the implementation of the ICacheManager might not allow the creation of new
      // or custom caches like this.
      if (cacheManager != null && cacheManager.cacheEnabled(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION)) {
        cacheManager.putInRegionCache(ISolutionRepository.REPOSITORY_SERVICE_CACHE_REGION, session.getName() + LocaleHelper.getLocale(), document);
      }

    }

    return document;
  }

  
  /**
   * Returns an XML snippet consisting of a single <code>file</code> element. The <code>file</code> element is the same 
   * as would have been returned by <code>getSolutionRepositoryDoc</code>.
   * @param session current session
   * @return doc
   * @throws ParserConfigurationException
   */
  public org.w3c.dom.Document getSolutionRepositoryFileDetails(IPentahoSession session, String fullPath)
      throws ParserConfigurationException {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
    ISolutionFile rootFile = repository.getSolutionFile(fullPath, ISolutionRepository.ACTION_EXECUTE);
    org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    boolean isAdministrator = SecurityHelper.isPentahoAdministrator(session);
    processRepositoryFile(session, isAdministrator, repository, document, rootFile, new String[0]);
    return document;
  }

  private Map<IPermissionRecipient, IPermissionMask> createAclFromXml(final String strXml) throws ParserConfigurationException,
      SAXException, IOException {
    SAXParser parser = getSAXParserFactory().newSAXParser();
    Map<IPermissionRecipient, IPermissionMask> m = new HashMap<IPermissionRecipient, IPermissionMask>();

    DefaultHandler h = new AclParserHandler(m);
    String encoding = XmlHelper.getEncoding(strXml);
    InputStream is = new ByteArrayInputStream(strXml.getBytes(encoding));

    parser.parse(is, h);

    return m;
  }

  private class AclParserHandler extends DefaultHandler {

    Map<IPermissionRecipient, IPermissionMask> acl;

    public AclParserHandler(final Map<IPermissionRecipient, IPermissionMask> acl) {
      this.acl = acl;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
        throws SAXException {
      if (qName.equalsIgnoreCase("entry")) { //$NON-NLS-1$
        String permissions = attributes.getValue("", "permissions"); //$NON-NLS-1$ //$NON-NLS-2$
        IPermissionRecipient permRecipient = null;
        String user = attributes.getValue("", "user"); //$NON-NLS-1$ //$NON-NLS-2$
        if (null != user) {
          permRecipient = new SimpleUser(user);
        } else {
          permRecipient = new SimpleRole(attributes.getValue("", "role")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.acl.put(permRecipient, new SimplePermissionMask(Integer.parseInt(permissions)));
      }
    }
  }

  /**
   * Sets the ACL Xml for a particular file in the solution repository
   * 
   * @param solution
   * @param path
   * @param filename
   * @param strAclXml
   * @param userSession
   */
  public void setAcl(final String solution, final String path, final String filename, final String strAclXml,
      final IPentahoSession userSession) throws SolutionRepositoryServiceException,
      IOException, PentahoAccessControlException {

    if (StringUtil.doesPathContainParentPathSegment(solution) || StringUtil.doesPathContainParentPathSegment(path)) {
      String msg = Messages.getInstance().getString("AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME"); //$NON-NLS-1$
      throw new SolutionRepositoryServiceException(msg);
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, filename);
    ISolutionFile solutionFile = repository.getSolutionFile(fullPath, ISolutionRepository.ACTION_SHARE);

    // ouch, i hate instanceof
    if (solutionFile instanceof IAclSolutionFile) {
      Map<IPermissionRecipient, IPermissionMask> acl;
      try {
        acl = createAclFromXml(strAclXml);

        // TODO sbarkdull, fix these really really lame exception msgs
      } catch (ParserConfigurationException e) {
        throw new SolutionRepositoryServiceException("ParserConfigurationException", e); //$NON-NLS-1$
      } catch (SAXException e) {
        throw new SolutionRepositoryServiceException("SAXException", e); //$NON-NLS-1$
      } catch (IOException e) {
        throw new SolutionRepositoryServiceException("IOException", e); //$NON-NLS-1$
      }
      repository.setPermissions(solutionFile, acl);
    }
    // TODO sbarkdull, what if its not instanceof
  }
  
  /**
   * Gets ACLs based on a solution repository file.
   * 
   * @param parameterProvider
   * @param outputStream
   * @param userSession
   * 
   * @return acl xml
   */
  public String getAclXml(final String solution, final String path, final String filename, final IPentahoSession userSession) throws SolutionRepositoryServiceException,
      IOException {
    if (StringUtil.doesPathContainParentPathSegment(solution) || StringUtil.doesPathContainParentPathSegment(path)) {
      String msg = Messages.getInstance().getString("AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME"); //$NON-NLS-1$
      throw new SolutionRepositoryServiceException(msg);
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, filename);
    ISolutionFile solutionFile = repository.getSolutionFile(fullPath, ISolutionRepository.ACTION_EXECUTE);

    String strXml = null;
    // ouch, i hate instanceof
    if (solutionFile instanceof IAclSolutionFile) {
      Map<IPermissionRecipient, IPermissionMask> filePermissions = repository.getPermissions((solutionFile));
      String processingInstruction = XmlHelper
          .createXmlProcessingInstruction(RESPONSE_DOCUMENT_VERSION_NUM,
              RESPONSE_DOCUMENT_ENCODING);
      strXml = processingInstruction + getAclAsXml(filePermissions);
    } else {
      strXml = "<acl notsupported='true'/>"; //$NON-NLS-1$
    }
    return strXml;
  }

  // TODO sbarkdull, this method belongs in an AclUtils class?
  // turn acl into an XML representation, and return the document.
  // probably belongs in the SecurityHelper class, but does this class still exist?
  private String getAclAsXml(final Map<IPermissionRecipient, IPermissionMask> filePermissions) {
    StringBuffer sb = new StringBuffer(XmlHelper.createXmlProcessingInstruction(
        RESPONSE_DOCUMENT_VERSION_NUM, RESPONSE_DOCUMENT_ENCODING));

    sb.append("<acl>"); //$NON-NLS-1$
    for (Map.Entry<IPermissionRecipient, IPermissionMask> filePerm : filePermissions.entrySet()) {
      IPermissionRecipient permRecipient = filePerm.getKey();
      if (permRecipient instanceof SimpleRole) {
        sb
            .append("<entry role='" + permRecipient.getName() + "' permissions='" + filePerm.getValue().getMask() //$NON-NLS-1$ //$NON-NLS-2$
                + "'/>"); //$NON-NLS-1$
      } else {
        // entry belongs to a user
        sb
            .append("<entry user='" + permRecipient.getName() + "' permissions='" + filePerm.getValue().getMask() //$NON-NLS-1$ //$NON-NLS-2$
                + "'/>"); //$NON-NLS-1$
      }
    }
    sb.append("</acl>"); //$NON-NLS-1$
    return sb.toString();
  }
  
  /**
   * Get a SAX Parser Factory
   * 
   * NOTE: Need sax parser factory per thread for thread safety. See: http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/parsers/SAXParserFactory.html
   * 
   * @return
   */
  private static SAXParserFactory getSAXParserFactory() {
    SAXParserFactory threadLocalSAXParserFactory = SAX_FACTORY.get();
    if (null == threadLocalSAXParserFactory) {
      threadLocalSAXParserFactory = SAXParserFactory.newInstance();
      SAX_FACTORY.set(threadLocalSAXParserFactory);
    }
    return threadLocalSAXParserFactory;
  }
  
  

  /**
   * This class is basically a wrapper for the solution file inputstream, but it will only
   * pay the price *IF* we need to actually open the inputstream.  This has a huge performance
   * benefit (9s down to 3s).
   */
  private class PluginFileInputStream extends InputStream {
    
    private ISolutionRepository repository;
    private ISolutionFile file;
    private InputStream inputStream;
    
    public PluginFileInputStream(ISolutionRepository repository, ISolutionFile file)
    {
      this.repository = repository;
      this.file = file;
    }
    
    public int read() throws IOException
    {
      if (inputStream == null) {
        inputStream = repository.getResourceInputStream(file.getFullPath(), true, ISolutionRepository.ACTION_EXECUTE);
      }
      return inputStream.read();
    }
    
    public int read(byte[] b) throws IOException
    {
      if (inputStream == null) {
        inputStream = repository.getResourceInputStream(file.getFullPath(), true, ISolutionRepository.ACTION_EXECUTE);
      }
      return inputStream.read(b);
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
      if (inputStream == null) {
        inputStream = repository.getResourceInputStream(file.getFullPath(), true, ISolutionRepository.ACTION_EXECUTE);
      }
      return inputStream.read(b, off, len);
    }

    public synchronized void mark(int readlimit)
    {
      if (inputStream != null) {
        inputStream.mark(readlimit);
      }
    }

    public boolean markSupported()
    {
      if (inputStream != null) {
        return inputStream.markSupported();
      }
      return super.markSupported();
    }

    public synchronized void reset() throws IOException
    {
      if (inputStream != null) {
        inputStream.reset();
      }
      super.reset();
    }
    
    public long skip(long n) throws IOException
    {
      if (inputStream != null) {
        inputStream.skip(n);
      }
      return super.skip(n);
    }
    
    public void close() throws IOException
    {
      if (inputStream != null) {
        inputStream.close();
      }
    }
    
    public int available() throws IOException
    {
      if (inputStream != null) {
        return inputStream.available();
      }
      return 0;
    }
    
  }
  
}
