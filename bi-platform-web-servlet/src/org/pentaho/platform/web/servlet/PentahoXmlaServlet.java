package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Role;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.RolapConnection;
import mondrian.rolap.RolapConnectionProperties;
import mondrian.spi.CatalogLocator;
import mondrian.util.Pair;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.XmlaException;
import mondrian.xmla.XmlaHandler;
import mondrian.xmla.DataSourcesConfig.Catalog;
import mondrian.xmla.DataSourcesConfig.DataSources;
import mondrian.xmla.impl.DefaultXmlaServlet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.xml.sax.EntityResolver;

/**
 * Filters out <code>DataSource</code> elements that are not XMLA-related.
 * <p />
 * Background: Pentaho re-used datasources.xml for non-XMLA purposes. But since <code>DefaultXmlaServlet</code> requires 
 * actual XMLA datasources, this servlet extends <code>DefaultXmlaServlet</code> and removes the non-XMLA datasources 
 * before continuing normal <code>DefaultXmlaServlet</code> behavior.
 * <p />
 * The convention here is that any <code>DataSource</code> elements with  
 * <code>&lt;ProviderType&gt;None&lt;/ProviderType&gt;</code> are considered non-XMLA and are filtered out.
 * 
 * @author mlowery
 */
public class PentahoXmlaServlet extends DefaultXmlaServlet {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(PentahoXmlaServlet.class);

  //  private static final Logger LOGGER = Logger.getLogger(PentahoXmlaServlet.class);

  private static final long serialVersionUID = -5873069189408153768L;

  // - Constructors ================================

  public PentahoXmlaServlet() {
    super();
  }

  // ~ Methods =========================================================================================================

  @Override
  protected String readDataSourcesContent(final URL dataSourcesConfigUrl) throws IOException {
    String original = Util.readURL(dataSourcesConfigUrl, Util.toMap(System.getProperties()));
    EntityResolver loader = new PentahoEntityResolver();
    Document originalDocument = XmlDom4JHelper.getDocFromString(original, loader);
    if (PentahoXmlaServlet.logger.isDebugEnabled()) {
      PentahoXmlaServlet.logger.debug("original document: " + originalDocument.asXML());
    }
    Document modifiedDocument = (Document) originalDocument.clone();
    List<Node> nodesToRemove = modifiedDocument.selectNodes("/DataSources/DataSource[ProviderType='None']");
    if (PentahoXmlaServlet.logger.isDebugEnabled()) {
      PentahoXmlaServlet.logger.debug("nodesToRemove: " + nodesToRemove.size());
    }
    for (Node node : nodesToRemove) {
      node.detach();
    }
    if (PentahoXmlaServlet.logger.isDebugEnabled()) {
      PentahoXmlaServlet.logger.debug("modified document: " + modifiedDocument.asXML());
    }
    return modifiedDocument.asXML();
  }

  /**
   * override default doPost and configure SolutionRepositoryVFS, before
   * calling parent's doPost
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // configure solution repository VFS service 
    SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem
        .getSolutionRepository(PentahoHttpSessionHelper.getPentahoSession(request)));
    super.doPost(request, response);
  }

  @Override
  protected XmlaHandler getXmlaHandler() {
    if (this.xmlaHandler == null) {
      this.xmlaHandler = new PentahoXmlaHandler(this.dataSources, this.catalogLocator, "cxmla");
    }
    return this.xmlaHandler;
  }

  /**
   * Overrides XmlaHandler to supply a DataSource from Pentaho DataSource Manager.
   * 
   * @author mlowery
   */
  private class PentahoXmlaHandler extends XmlaHandler {

    public PentahoXmlaHandler(DataSources dataSources, CatalogLocator catalogLocator, String prefix) {
      super(dataSources, catalogLocator, prefix);
    }

    /**
     * Override to see if the datasource is in Pentaho DataSource Manager. Use it if so. Otherwise, this method is
     * exactly the same as the superclass implementation.
     */
    @Override
    protected Connection getConnection(Catalog catalog, Role role, String roleName) throws XmlaException {
      DataSourcesConfig.DataSource ds = catalog.getDataSource();

      Util.PropertyList connectProperties = Util.parseConnectString(catalog.getDataSourceInfo());

      String catalogUrl = catalogLocator.locate(catalog.definition);

      if (logger.isDebugEnabled()) {
        if (catalogUrl == null) {
          logger.debug("PentahoXmlaHandler.getConnection: catalogUrl is null");
        } else {
          logger.debug("PentahoXmlaHandler.getConnection: catalogUrl=" + catalogUrl);
        }
      }

      connectProperties.put(RolapConnectionProperties.Catalog.name(), catalogUrl);

      // Checking access
      if (!DataSourcesConfig.DataSource.AUTH_MODE_UNAUTHENTICATED.equalsIgnoreCase(ds.getAuthenticationMode())
          && (role == null) && (roleName == null)) {
        throw new XmlaException(CLIENT_FAULT_FC, HSB_ACCESS_DENIED_CODE, HSB_ACCESS_DENIED_FAULT_FS,
            new SecurityException("Access denied for data source needing authentication"));
      }

      // Role in request overrides role in connect string, if present.
      if (roleName != null) {
        connectProperties.put(RolapConnectionProperties.Role.name(), roleName);
      }

      String dsName = connectProperties.get("DataSource"); //$NON-NLS-1$

      logger.debug("connect string datasource: " + dsName);

      javax.sql.DataSource datasource = getDataSource(dsName);

      logger.debug("got javax.sql.DataSource from Pentaho DS Mgr: " + datasource);

      RolapConnection conn = null;
      if (StringUtils.isNotBlank(dsName) && datasource != null) {
        conn = (RolapConnection) DriverManager.getConnection(connectProperties, null, datasource);
        logger.debug("created rolap connection: " + conn);
      } else {
        // either using jdbc or pentaho ds mgr does not know about dsName; fall back on "normal" behavior
        conn = (RolapConnection) DriverManager.getConnection(resolveUboundJndi(connectProperties), null);
      }

      if (role != null) {
        conn.setRole(role);
      }

      if (logger.isDebugEnabled()) {
        if (conn == null) {
          logger.debug("PentahoXmlaHandler.getConnection: returning connection null");
        } else {
          logger.debug("PentahoXmlaHandler.getConnection: returning connection not null");
        }
      }
      return conn;
    }

    /**
     * Uses Pentaho DataSource Manager to get datasource.
     * @param dsName datasource name
     * @return datasource or <code>null</code> if not found
     */
    private javax.sql.DataSource getDataSource(String dsName) {
      try {

        IDatasourceService datasourceSvc = (IDatasourceService) PentahoSystem.getObjectFactory().getObject(
            "IDatasourceService", null);

        javax.sql.DataSource datasource = datasourceSvc.getDataSource(dsName);

        return datasource;
      } catch (ObjectFactoryException e) {
        logger.error("IDatasourceService.UNABLE_TO_INSTANTIATE_OBJECT", e);
        return null;
      } catch (DatasourceServiceException e) {
        logger.error("IDatasourceService.UNABLE_TO_INSTANTIATE_OBJECT", e);
        return null;
      }
    }

  }

  private PropertyList resolveUboundJndi(PropertyList orig) {
    // make a copy of the orig prop list
    PropertyList newPropList = new Util.PropertyList();
    Iterator iter = orig.iterator();
    while (iter.hasNext()) {
      Pair<String, String> pair = (Pair<String, String>) iter.next();
      newPropList.put(pair.left, pair.right);
    }

    String datasource = orig.get("DataSource"); //$NON-NLS-1$
    String resolvedDatasource = datasource;
    IDatasourceService datasourceService;
    try {
      datasourceService = (IDatasourceService) PentahoSystem.getObjectFactory().getObject(
          IDatasourceService.IDATASOURCE_SERVICE, null);
      resolvedDatasource = datasourceService.getDSBoundName(datasource);
    } catch (ObjectFactoryException e) {
      // this should be a runtime exception anyway
      throw new RuntimeException(e);
    } catch (DatasourceServiceException e) {
      logger.error("an exception occurred", e);
      
    }
    newPropList.put("DataSource", resolvedDatasource);
    return newPropList;
  }

}