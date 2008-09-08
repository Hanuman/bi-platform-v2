/*
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mondrian.xmla.DataSourcesConfig.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.pentaho.platform.web.servlet.messages.Messages;

public class MondrianCatalogPublisher extends RepositoryFilePublisher {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(MondrianCatalogPublisher.class);

  private static final long serialVersionUID = -6052692173173633694L;

  // ~ Instance fields =================================================================================================

  private IMondrianCatalogService mondrianCatalogService = MondrianCatalogHelper.getInstance();

  private String baseUrl;

  // ~ Constructors ====================================================================================================

  public MondrianCatalogPublisher() {
    super();
    baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
  }

  // ~ Methods =========================================================================================================

  @Override
  public Log getLogger() {
    return MondrianCatalogPublisher.logger;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    resp.setCharacterEncoding(LocaleHelper.getSystemEncoding());

    IPentahoSession pentahoSession = PentahoHttpSessionHelper.getPentahoSession(req);
    String publishPath = req.getParameter("publishPath"); //$NON-NLS-1$
    String publishKey = req.getParameter("publishKey");//$NON-NLS-1$
    String jndiName = req.getParameter("jndiName");//$NON-NLS-1$
    String jdbcDriver = req.getParameter("jdbcDriver");//$NON-NLS-1$
    String jdbcUrl = req.getParameter("jdbcUrl");//$NON-NLS-1$
    String jdbcUserId = req.getParameter("jdbcUserId");//$NON-NLS-1$
    String jdbcPassword = req.getParameter("jdbcPassword");//$NON-NLS-1$
    boolean overwrite = Boolean.valueOf(req.getParameter("overwrite")).booleanValue(); //$NON-NLS-1$
    boolean enableXmla = Boolean.valueOf(req.getParameter("enableXmla")).booleanValue(); //$NON-NLS-1$

    List<FileItem> fileItems = Collections.emptyList();
    try {
      fileItems = getFileItems(req);
    } catch (FileUploadException e) {
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED"), e); //$NON-NLS-1$
      }
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }
    int status = ISolutionRepository.FILE_ADD_FAILED;
    try {
      status = doPublish(fileItems, publishPath, publishKey, null, null, null, null, null,
        overwrite, pentahoSession);
    } catch (Exception e) {
      MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0005_PUBLISH_EXCEPTION"), e); //$NON-NLS-1$
    }

    if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
      resp.getWriter().println(status);
      return;
    }

    if (MondrianCatalogPublisher.logger.isDebugEnabled()) {
      MondrianCatalogPublisher.logger.debug("publishPath=" + publishPath); //$NON-NLS-1$
    }
    if ((publishPath != null) && (publishPath.endsWith("/") || publishPath.endsWith("\\"))) { //$NON-NLS-1$ //$NON-NLS-2$
      publishPath = publishPath.substring(0, publishPath.length() - 1);
    }

    if (MondrianCatalogPublisher.logger.isDebugEnabled()) {
      MondrianCatalogPublisher.logger.debug("jndiName=" + jndiName); //$NON-NLS-1$
    }
    if (StringUtils.isBlank(jndiName)) {
      throw new ServletException(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0003_JNDINAME_REQUIRED")); //$NON-NLS-1$
    }

    // expecting exactly one file
    if (fileItems.size() != 1) {
      // when this is appended, FILE_ADD_SUCCESSFUL has already been appended from super
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0004_FILE_COUNT", "" + fileItems.size())); //$NON-NLS-1$ //$NON-NLS-2$
      }
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }

    FileItem fi = fileItems.iterator().next();

    String dsUrl = baseUrl;
    if (!dsUrl.endsWith("/")) { //$NON-NLS-1$
      dsUrl += "/"; //$NON-NLS-1$
    }
    dsUrl += "Xmla"; //$NON-NLS-1$
    String dsAuthMode = DataSource.AUTH_MODE_UNAUTHENTICATED;
    String dsProviderName = "Pentaho"; //$NON-NLS-1$

    // DataSources where ProviderType=None are filtered by PentahoXmlaServlet
    String dsProviderType = enableXmla ? DataSource.PROVIDER_TYPE_MDP : "None"; //$NON-NLS-1$

    String catDef = "solution:" + publishPath + "/" + fi.getName(); //$NON-NLS-1$//$NON-NLS-2$

    MondrianSchema mondrianSchema = mondrianCatalogService.loadMondrianSchema(catDef, pentahoSession);
    
    String catName = mondrianSchema.getName();
    String dsName = "Provider=Mondrian;DataSource=" + mondrianSchema.getName(); //$NON-NLS-1$
    String dsDesc = "Published Mondrian Schema " + mondrianSchema.getName() + " using jndi datasource " + jndiName; //$NON-NLS-1$ //$NON-NLS-2$
    
    // verify JNDI
    try {
 	  IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);    	
 	  datasourceService.getDSBoundName(jndiName);
    } catch (ObjectFactoryException objface) {
      	MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0006_UNABLE_TO_FACTORY_OBJECT", jndiName), objface); //$NON-NLS-1$      	
    } catch (DatasourceServiceException dse) {
      MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0001_JNDI_NAMING_ERROR", jndiName), dse); //$NON-NLS-1$
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }

    // used in both the catalog and the catalog datasource
    // Note: we use the unbound JNDI name here, the PentahoXmlaServlet and PivotViewComponent resolve the JNDI name

    String catConnectStr = "Provider=mondrian;DataSource=" + jndiName; //$NON-NLS-1$

    MondrianDataSource ds = new MondrianDataSource(dsName, dsDesc, dsUrl, catConnectStr, dsProviderName,
        dsProviderType, dsAuthMode, null);

    MondrianCatalog cat = new MondrianCatalog(catName, catConnectStr, catDef, ds, new MondrianSchema(catName,
        new ArrayList<MondrianCube>()));

    try {
      mondrianCatalogService.addCatalog(cat, overwrite, pentahoSession);
    } catch (MondrianCatalogServiceException e) {
      if (MondrianCatalogPublisher.logger.isErrorEnabled()) {
        MondrianCatalogPublisher.logger.error(Messages.getErrorString("MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED"), e); //$NON-NLS-1$
      }
      resp.getWriter().println(ISolutionRepository.FILE_ADD_FAILED);
      return;
    }

    resp.getWriter().println(ISolutionRepository.FILE_ADD_SUCCESSFUL);

  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  public IMondrianCatalogService getMondrianCatalogService() {
    return mondrianCatalogService;
  }

  public void setMondrianCatalogService(final IMondrianCatalogService mondrianCatalogService) {
    this.mondrianCatalogService = mondrianCatalogService;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(final String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
