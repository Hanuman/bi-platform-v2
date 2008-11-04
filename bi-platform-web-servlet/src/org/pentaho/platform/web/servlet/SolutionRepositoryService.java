/*
 * Copyright 2006 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Jul 12, 2005 
 * @author James Dixon, Angelo Rodriguez, Steven Barkdull
 * 
 */

package org.pentaho.platform.web.servlet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAclSolutionFile;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentGeneratorInfo;
import org.pentaho.platform.api.engine.IFileInfo;
import org.pentaho.platform.api.engine.IFileInfoGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.IPermissionRecipient;
import org.pentaho.platform.api.engine.IPluginSettings;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.SimplePermissionMask;
import org.pentaho.platform.engine.security.SimpleRole;
import org.pentaho.platform.engine.security.SimpleUser;
import org.pentaho.platform.engine.services.WebServiceUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SolutionRepositoryService extends ServletBase {

  /**
   * 
   */
  private static final long serialVersionUID = -5870073658756939643L;

  private static final Log logger = LogFactory.getLog(SolutionRepositoryService.class);

  /**
   * contains instance of a sax parser factory. Use getSAXParserFactory() method to get a copy of the factory.
   */
  private static final ThreadLocal<SAXParserFactory> SAX_FACTORY = new ThreadLocal<SAXParserFactory>();
  private static final String RESPONSE_DOCUMENT_ENCODING = "UTF-8";
  private static final String RESPONSE_DOCUMENT_VERSION_NUM = "1.0";

  @Override
  public Log getLogger() {
    return SolutionRepositoryService.logger;
  }

  public SolutionRepositoryService() {
    super();
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    PentahoSystem.systemEntryPoint();
    OutputStream outputStream = response.getOutputStream();
    try {
      boolean wrapWithSoap = "false".equals(request.getParameter("ajax")); //$NON-NLS-1$ //$NON-NLS-2$
      String component = request.getParameter("component"); //$NON-NLS-1$
      response.setContentType("text/xml"); //$NON-NLS-1$
      response.setCharacterEncoding(LocaleHelper.getSystemEncoding());

      IPentahoSession userSession = getPentahoSession(request);
      // send the header of the message to prevent time-outs while we are working
      response.setHeader("expires", "0"); //$NON-NLS-1$ //$NON-NLS-2$

      dispatch(request, response, component, outputStream, userSession, wrapWithSoap);

      /**
       * NOTE: PLEASE DO NOT CATCH Exception, since this is the super class of RuntimeException. We do NOT want to catch RuntimeException, only CHECKED
       * exceptions!
       */
    } catch (SolutionRepositoryServiceException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (PentahoAccessControlException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerConfigurationException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (ParserConfigurationException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerException ex) {
      commonErrorHandler(outputStream, ex);
    } catch (TransformerFactoryConfigurationError ex) {
      commonErrorHandler(outputStream, ex.getException());
    } catch (IOException ex) {
      // Use debugErrorHandler for ioException
      debugErrorHandler(outputStream, ex);
    } finally {
      PentahoSystem.systemExitPoint();
    }
    if (ServletBase.debug) {
      debug(Messages.getString("HttpWebService.DEBUG_WEB_SERVICE_END")); //$NON-NLS-1$
    }
  }

  /**
   * Used for logging exceptions that happen that aren't necessarily exceptional. It's common that IOExceptions will happen as people begin their transactions
   * and then abandon their web page by closing their browser or current tab. Logging each one fills up the server log needlessly. Setting to debug level allows
   * us visibility without compromising a production deployment.
   * 
   * @param outputStream
   * @param ex
   * @throws IOException
   */
  private void debugErrorHandler(final OutputStream outputStream, final Exception ex) throws IOException {
    String msg = Messages.getErrorString("SolutionRepositoryService.ERROR_0001_ERROR_DURING_SERVICE_REQUEST"); //$NON-NLS-1$;
    debug(msg, ex);
    WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
  }

  private void commonErrorHandler(final OutputStream outputStream, final Exception ex) throws IOException {
    String msg = Messages.getErrorString("SolutionRepositoryService.ERROR_0001_ERROR_DURING_SERVICE_REQUEST"); //$NON-NLS-1$;
    error(msg, ex);
    WebServiceUtil.writeString(outputStream, WebServiceUtil.getErrorXml(msg), false);
  }

  private static String[] getFilters(final HttpServletRequest request) {
    String filter = request.getParameter("filter"); //$NON-NLS-1$
    List<String> filters = new ArrayList<String>();
    if (!StringUtils.isEmpty(filter)) {
      StringTokenizer st = new StringTokenizer(filter, "*.,");
      while (st.hasMoreTokens()) {
        filters.add(st.nextToken());
      }
    }

    return filters.toArray(new String[] {});
  }

  protected void dispatch(final HttpServletRequest request, final HttpServletResponse response, final String component, final OutputStream outputStream,
      final IPentahoSession userSession, final boolean wrapWithSOAP) throws IOException, SolutionRepositoryServiceException, PentahoAccessControlException,
      ParserConfigurationException, TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {

    IParameterProvider parameterProvider = new HttpRequestParameterProvider(request);
    if ("getSolutionRepositoryDoc".equals(component)) { //$NON-NLS-1$
      String[] filters = SolutionRepositoryService.getFilters(request);
      Document doc = getSolutionRepositoryDoc(userSession, filters);
      WebServiceUtil.writeDocument(outputStream, doc, wrapWithSOAP);
    } else if ("createNewFolder".equals(component)) { //$NON-NLS-1$
      String solution = request.getParameter("solution"); //$NON-NLS-1$
      String path = request.getParameter("path"); //$NON-NLS-1$
      String name = request.getParameter("name"); //$NON-NLS-1$
      String desc = request.getParameter("desc"); //$NON-NLS-1$
      boolean result = createFolder(userSession, solution, path, name, desc);
      WebServiceUtil.writeString(outputStream, "<result>" + result + "</result>", wrapWithSOAP); //$NON-NLS-1$
    } else if ("delete".equals(component)) { //$NON-NLS-1$
      String solution = request.getParameter("solution"); //$NON-NLS-1$
      String path = request.getParameter("path"); //$NON-NLS-1$
      String name = request.getParameter("name"); //$NON-NLS-1$
      boolean result = delete(userSession, solution, path, name);
      WebServiceUtil.writeString(outputStream, "<result>" + result + "</result>", wrapWithSOAP); //$NON-NLS-1$
    } else if ("setAcl".equals(component)) { //$NON-NLS-1$
      setAcl(parameterProvider, outputStream, userSession, wrapWithSOAP);
    } else if ("getAcl".equals(component)) { //$NON-NLS-1$
      getAcl(parameterProvider, outputStream, userSession, wrapWithSOAP);

    } else {
      throw new RuntimeException(Messages.getErrorString("HttpWebService.UNRECOGNIZED_COMPONENT_REQUEST", component)); //$NON-NLS-1$
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
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
  public static synchronized boolean delete(final IPentahoSession userSession, final String solution, final String path, final String name)
      throws IOException {
    /*
    * This method is static/synchronized because we want to be absolutely sure
    * we prevent cases where multiple delete calls could occur at the same time, not because we feel that something bad could happen,
    * but because we absolutely want to make sure that nothing bad does happen.  By removing the static from this method we would effectively
    * open ourselves up for multiple instances of this servlet being allowed to enter the method (most containers will created a pool of
    * servlets).
    */
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, name);
    ISolutionFile solutionFile = repository.getFileByPath(fullPath);
    if (solutionFile != null && repository.hasAccess(solutionFile, IPentahoAclEntry.PERM_DELETE)) {
      repository.removeSolutionFile(fullPath);
      return true;
    }
    return false;
  }

  /**
   * This method creates a folder along with it's index.xml file.  The method 
   * is static/synchronized because we want to be absolutely sure we prevent 
   * cases where multiple createFolder calls could occur at the same time.
   * 
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
  public static synchronized boolean createFolder(IPentahoSession userSession, String solution, String path, String name, String desc) throws IOException {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    if (solution == null) {
      solution = "";
    }
    
    // verify that the name does not contain a path separator before creating the folder
    if (name == null || name.indexOf("/") >= 0 || name.indexOf("\\") >= 0 || //$NON-NLS-1$ //$NON-NLS-2$
        name.indexOf(ISolutionRepository.SEPARATOR) >= 0) {
      return false;
    }
    
    String parentFolderPath = ActionInfo.buildSolutionPath(solution, path, "" + ISolutionRepository.SEPARATOR);
    ISolutionFile parentSolutionFile = repository.getFileByPath(parentFolderPath);
    if (parentSolutionFile != null && parentSolutionFile.isDirectory() && 
        repository.hasAccess(parentSolutionFile, IPentahoAclEntry.PERM_CREATE)) {
      File parent = new File(PentahoSystem.getApplicationContext().getSolutionPath(parentFolderPath));
      File newFolder = new File(parent, name);
      if (newFolder.exists()) {
        // if the new folder already exists, we need to get out
        return false;
      }
      repository.createFolder(newFolder);
      
      // create the index file content
      String defaultIndex = "<index><name>" + name + "</name><description>" + (desc!=null?desc:name) //$NON-NLS-1$
          + "</description><icon>reporting.png</icon><visible>true</visible><display-type>list</display-type></index>"; //$NON-NLS-1$

      // add the index file to the repository
      String indexPath = ActionInfo.buildSolutionPath(solution, path, name);
      String baseURL = PentahoSystem.getApplicationContext().getSolutionPath("");
      repository.addSolutionFile(baseURL, indexPath, ISolutionRepository.INDEX_FILENAME, defaultIndex.getBytes(), false);
      return true;
    }
    return false;
  }
  
  
  private boolean acceptFilter(String name, String[] filters) {
    if (filters == null || filters.length == 0) {
      return true;
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

  private void processRepositoryFile(IPentahoSession session, boolean isAdministrator, ISolutionRepository repository, Element parentElement,
      ISolutionFile parentFile, String[] filters) {
    ISolutionFile children[] = parentFile.listFiles();
    for (ISolutionFile childSolutionFile : children) {
      if (!accept(isAdministrator, repository, childSolutionFile)) {
        // we don't want this file, skip to the next one
        continue;
      }

      String name = childSolutionFile.getFileName();
      if (name.startsWith(".")) {
        // these are hidden files of some type that are never shown
        // we don't want this file, skip to the next one
        continue;
      }
      if (childSolutionFile.isDirectory()) {
        // we always process directories

        // MDD 10/16/2008 Not always.. what about 'system'
        if (childSolutionFile.getFileName().startsWith("system")) {
          // skip the system dir, we DO NOT ever want this to hit the client
          continue;
        }

        Element child = parentElement.getOwnerDocument().createElement("file");
        parentElement.appendChild(child);
        try {
          String localizedName = repository.getLocalizedFileProperty(childSolutionFile, "name");
          child.setAttribute("localized-name", localizedName == null || "".equals(localizedName) ? name : localizedName);
        } catch (Exception e) {
          child.setAttribute("localized-name", name); //$NON-NLS-1$
        }
        try {
          String visible = repository.getLocalizedFileProperty(childSolutionFile, "visible");
          child.setAttribute("visible", visible == null || "".equals(visible) ? "false" : visible);
        } catch (Exception e) {
          e.printStackTrace();
          child.setAttribute("visible", "false"); //$NON-NLS-1$
        }
        String description = repository.getLocalizedFileProperty(childSolutionFile, "description");
        child.setAttribute("description", description == null || "".equals(description) ? name : description);
        child.setAttribute("name", name); //$NON-NLS-1$
        child.setAttribute("isDirectory", "true"); //$NON-NLS-1$
        child.setAttribute("lastModifiedDate", "" + childSolutionFile.getLastModified()); //$NON-NLS-1$
        processRepositoryFile(session, isAdministrator, repository, child, childSolutionFile, filters);
        // we have finished processing this so skip to the next one
        continue;
      }

      if (acceptFilter(name, filters)) { //$NON-NLS-1$
        int lastPoint = name.lastIndexOf('.');
        String extension = ""; //$NON-NLS-1$
        if (lastPoint != -1) {
          // ignore anything with no extension
          extension = name.substring(lastPoint + 1).toLowerCase();
        }

        // xaction and URL support are built in
        boolean addFile = "xaction".equals(extension) || "url".equals(extension); //$NON-NLS-1$ //$NON-NLS-2$
        boolean isPlugin = false;
        // see if there is a plugin for this file type
        IPluginSettings pluginSettings = PentahoSystem.get(IPluginSettings.class, session); //$NON-NLS-1$
        if (pluginSettings != null) {
          Set<String> types = pluginSettings.getContentTypes();
          isPlugin = types != null && types.contains(extension);
          addFile |= isPlugin;
        }

        Element child = parentElement.getOwnerDocument().createElement("file");
        parentElement.appendChild(child);
        IFileInfo fileInfo = null;

        if (addFile) {
          try {
            // the visibility flag for action-sequences is controlled by /action-sequence/documentation/result-type
            // and we should no longer be looking at 'visible' because it was never actually used!
            String visible = "none".equals(repository.getLocalizedFileProperty(childSolutionFile, "documentation/result-type")) ? "false" : "true";
            child.setAttribute("visible", visible == null || "".equals(visible) ? "true" : visible);
          } catch (Exception e) {
            child.setAttribute("visible", "true"); //$NON-NLS-1$
          }
          if (name.endsWith(".xaction")) {
            // add special props?
            // localization..
          } else if (name.endsWith(".url")) {

            // add special props
            String props = new String(childSolutionFile.getData());
            StringTokenizer tokenizer = new StringTokenizer(props, "\n");
            while (tokenizer.hasMoreTokens()) {
              String line = tokenizer.nextToken();
              int pos = line.indexOf('=');
              if (pos > 0) {
                String propname = line.substring(0, pos);
                String value = line.substring(pos + 1);
                if ((value != null) && (value.length() > 0) && (value.charAt(value.length() - 1) == '\r')) {
                  value = value.substring(0, value.length() - 1);
                }
                if ("URL".equalsIgnoreCase(propname)) {
                  child.setAttribute("url", value);
                }
              }
            }
          } else if (isPlugin) {
            // must be a plugin - make it look like a URL
            IContentGeneratorInfo info = pluginSettings.getDefaultContentGeneratorInfoForType(extension, session);
            if (info != null) {
              IFileInfoGenerator fig = info.getFileInfoGenerator();
              if (fig != null) {
                fig.setLogger(this);
                // get the file info object for this file
                fileInfo = fig.getFileInfo(childSolutionFile.getSolution(), childSolutionFile.getSolutionPath(), name, childSolutionFile.getData());
                String handlerId = pluginSettings.getContentGeneratorIdForType(extension, session);
                String fileUrl = pluginSettings.getContentGeneratorUrlForType(extension, session);
                String solution = childSolutionFile.getSolutionPath();
                String path = ""; //$NON-NLS-1$
                int pos = solution.indexOf(ISolutionRepository.SEPARATOR);
                if (pos != -1) {
                  path = solution.substring(pos + 1);
                  solution = solution.substring(0, pos);
                }
                String url;
                if (!fileUrl.equals("")) { //$NON-NLS-1$
                  url = PentahoSystem.getApplicationContext().getBaseUrl() + fileUrl + "?solution=" + solution + "&path=" + path + "&action=" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                } else {
                  url = PentahoSystem.getApplicationContext().getBaseUrl()
                      + "content/" + handlerId + "?solution=" + solution + "&path=" + path + "&action=" + name; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                }
                child.setAttribute("url", url); //$NON-NLS-1$
              }
            }
          }
        }

        // localization
        try {
          String localizedName = null;
          if (name.endsWith(".url")) {
            localizedName = repository.getLocalizedFileProperty(childSolutionFile, "url_name");
          } else if (fileInfo != null) {
            localizedName = fileInfo.getTitle();
          } else {
            localizedName = repository.getLocalizedFileProperty(childSolutionFile, "title");
          }
          child.setAttribute("localized-name", localizedName == null || "".equals(localizedName) ? name : localizedName);
        } catch (Exception e) {
          child.setAttribute("localized-name", name); //$NON-NLS-1$
        }
        try {
          // only folders, urls and xactions have descriptions
          if (name.endsWith(".url")) {
            String url_description = repository.getLocalizedFileProperty(childSolutionFile, "url_description");
            String description = repository.getLocalizedFileProperty(childSolutionFile, "description");
            if (url_description == null && description == null) {
              child.setAttribute("description", name);
            } else {
              child.setAttribute("description", url_description == null || "".equals(url_description) ? description : url_description);
            }
          } else if (name.endsWith(".xaction")) {
            String description = repository.getLocalizedFileProperty(childSolutionFile, "description");
            child.setAttribute("description", description == null || "".equals(description) ? name : description);
          } else if (fileInfo != null) {
            child.setAttribute("description", fileInfo.getDescription()); //$NON-NLS-1$
          } else {
            child.setAttribute("description", name);
          }
        } catch (Exception e) {
          child.setAttribute("description", "xxxxxxx"); //$NON-NLS-1$
        }

        // add permissions for each file/folder
        child.setAttribute("name", name); //$NON-NLS-1$
        child.setAttribute("isDirectory", "" + childSolutionFile.isDirectory()); //$NON-NLS-1$
        child.setAttribute("lastModifiedDate", "" + childSolutionFile.getLastModified()); //$NON-NLS-1$
      }
    }
  }

  public org.w3c.dom.Document getSolutionRepositoryDoc(IPentahoSession session, String[] filters) throws ParserConfigurationException {
    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);
    ISolutionFile rootFile = repository.getRootFolder();
    org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    org.w3c.dom.Element root = document.createElement("repository");
    document.appendChild(root);
    root.setAttribute("path", rootFile.getFullPath());
    boolean isAdministrator = SecurityHelper.isPentahoAdministrator(session);
    processRepositoryFile(session, isAdministrator, repository, root, rootFile, filters);
    return document;
  }

  private void getAcl(final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession, final boolean wrapWithSOAP)
      throws SolutionRepositoryServiceException, IOException {
    String solution = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$
    String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$
    String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$

    if (StringUtil.doesPathContainParentPathSegment(solution) || StringUtil.doesPathContainParentPathSegment(path)) {
      String msg = Messages.getString("AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME"); //$NON-NLS-1$
      throw new SolutionRepositoryServiceException(msg);
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, filename);
    ISolutionFile solutionFile = repository.getFileByPath(fullPath);

    String strXml = null;
    // ouch, i hate instanceof
    if (solutionFile instanceof IAclSolutionFile) {
      Map<IPermissionRecipient, IPermissionMask> filePermissions = repository.getPermissions((solutionFile));
      String processingInstruction = XmlHelper.createXmlProcessingInstruction(SolutionRepositoryService.RESPONSE_DOCUMENT_VERSION_NUM,
          SolutionRepositoryService.RESPONSE_DOCUMENT_ENCODING);
      strXml = processingInstruction + getAclAsXml(filePermissions);
    } else {
      strXml = "<acl notsupported='true'/>"; //$NON-NLS-1$
    }
    WebServiceUtil.writeString(outputStream, strXml, false);
  }

  // TODO sbarkdull, this method belongs in an AclUtils class?
  // turn acl into an XML representation, and return the document.
  // probably belongs in the SecurityHelper class, but does this class still exist?
  String getAclAsXml(final Map<IPermissionRecipient, IPermissionMask> filePermissions) {
    StringBuffer sb = new StringBuffer(XmlHelper.createXmlProcessingInstruction(SolutionRepositoryService.RESPONSE_DOCUMENT_VERSION_NUM,
        SolutionRepositoryService.RESPONSE_DOCUMENT_ENCODING));

    sb.append("<acl>");
    for (Map.Entry<IPermissionRecipient, IPermissionMask> filePerm : filePermissions.entrySet()) {
      IPermissionRecipient permRecipient = filePerm.getKey();
      if (permRecipient instanceof SimpleRole) {
        sb.append("<entry role='" + permRecipient.getName() + "' permissions='" + filePerm.getValue().getMask() + "'/>");
      } else {
        // entry belongs to a user
        sb.append("<entry user='" + permRecipient.getName() + "' permissions='" + filePerm.getValue().getMask() + "'/>");
      }
    }
    sb.append("</acl>");
    return sb.toString();
  }

  Map<IPermissionRecipient, IPermissionMask> createAclFromXml(final String strXml) throws ParserConfigurationException, SAXException, IOException {
    SAXParser parser = SolutionRepositoryService.getSAXParserFactory().newSAXParser();
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
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
      if (qName.equalsIgnoreCase("entry")) {
        String permissions = attributes.getValue("", "permissions");
        IPermissionRecipient permRecipient = null;
        String user = attributes.getValue("", "user");
        if (null != user) {
          permRecipient = new SimpleUser(user);
        } else {
          permRecipient = new SimpleRole(attributes.getValue("", "role"));
        }
        this.acl.put(permRecipient, new SimplePermissionMask(Integer.parseInt(permissions)));
      }
    }
  }

  private void setAcl(final IParameterProvider parameterProvider, final OutputStream outputStream, final IPentahoSession userSession, final boolean wrapWithSOAP)
      throws SolutionRepositoryServiceException, IOException, PentahoAccessControlException {
    String solution = parameterProvider.getStringParameter("solution", null); //$NON-NLS-1$ 
    String path = parameterProvider.getStringParameter("path", null); //$NON-NLS-1$ 
    String filename = parameterProvider.getStringParameter("filename", null); //$NON-NLS-1$
    String strAclXml = parameterProvider.getStringParameter("aclXml", null); //$NON-NLS-1$

    if (StringUtil.doesPathContainParentPathSegment(solution) || StringUtil.doesPathContainParentPathSegment(path)) {
      String msg = Messages.getString("AdhocWebService.ERROR_0008_MISSING_OR_INVALID_REPORT_NAME"); //$NON-NLS-1$
      throw new SolutionRepositoryServiceException(msg);
    }

    ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, userSession);
    String fullPath = ActionInfo.buildSolutionPath(solution, path, filename);
    ISolutionFile solutionFile = repository.getFileByPath(fullPath);

    // ouch, i hate instanceof
    if (solutionFile instanceof IAclSolutionFile) {
      Map<IPermissionRecipient, IPermissionMask> acl;
      try {
        acl = createAclFromXml(strAclXml);

        // TODO sbarkdull, fix these really really lame exception msgs
      } catch (ParserConfigurationException e) {
        throw new SolutionRepositoryServiceException("ParserConfigurationException", e);
      } catch (SAXException e) {
        throw new SolutionRepositoryServiceException("SAXException", e);
      } catch (IOException e) {
        throw new SolutionRepositoryServiceException("IOException", e);
      }
      repository.setPermissions(solutionFile, acl);
    }
    // TODO sbarkdull, what if its not instanceof

    String msg = WebServiceUtil.getStatusXml(Messages.getString("AdhocWebService.ACL_UPDATE_SUCCESSFUL")); //$NON-NLS-1$
    WebServiceUtil.writeString(outputStream, msg, false);
  }

  /**
   * Get a SAX Parser Factory
   * 
   * NOTE: Need sax parser factory per thread for thread safety. See: http://java.sun.com/j2se/1.5.0/docs/api/javax/xml/parsers/SAXParserFactory.html
   * 
   * @return
   */
  public static SAXParserFactory getSAXParserFactory() {
    SAXParserFactory threadLocalSAXParserFactory = SolutionRepositoryService.SAX_FACTORY.get();
    if (null == threadLocalSAXParserFactory) {
      threadLocalSAXParserFactory = SAXParserFactory.newInstance();
      SolutionRepositoryService.SAX_FACTORY.set(threadLocalSAXParserFactory);
    }
    return threadLocalSAXParserFactory;
  }
}
