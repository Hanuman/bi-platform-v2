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
import org.pentaho.platform.web.servlet.messages.Messages;
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

  private static final String PREFIX = "cxmla"; //$NON-NLS-1$

  private static final String KEY_DATASOURCE = "DataSource"; //$NON-NLS-1$
  
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
      PentahoXmlaServlet.logger
          .debug(Messages.getString("PentahoXmlaServlet.DEBUG_ORIG_DOC", originalDocument.asXML())); //$NON-NLS-1$
    }
    Document modifiedDocument = (Document) originalDocument.clone();
    List<Node> nodesToRemove = modifiedDocument.selectNodes("/DataSources/DataSource[ProviderType='None']"); //$NON-NLS-1$
    if (PentahoXmlaServlet.logger.isDebugEnabled()) {
      PentahoXmlaServlet.logger.debug(Messages.getString(
          "PentahoXmlaServlet.DEBUG_NODES_TO_REMOVE", String.valueOf(nodesToRemove.size()))); //$NON-NLS-1$
    }
    for (Node node : nodesToRemove) {
      node.detach();
    }
    if (PentahoXmlaServlet.logger.isDebugEnabled()) {
      PentahoXmlaServlet.logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_MOD_DOC", modifiedDocument.asXML())); //$NON-NLS-1$
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
      this.xmlaHandler = new PentahoXmlaHandler(this.dataSources, this.catalogLocator, PREFIX);
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
          logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_CATALOGURL_NULL")); //$NON-NLS-1$
        } else {
          logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_CATALOGURL", catalogUrl)); //$NON-NLS-1$
        }
      }

      connectProperties.put(RolapConnectionProperties.Catalog.name(), catalogUrl);

      // Checking access
      if (!DataSourcesConfig.DataSource.AUTH_MODE_UNAUTHENTICATED.equalsIgnoreCase(ds.getAuthenticationMode())
          && (role == null) && (roleName == null)) {
        throw new XmlaException(CLIENT_FAULT_FC, HSB_ACCESS_DENIED_CODE, HSB_ACCESS_DENIED_FAULT_FS,
            new SecurityException(Messages.getString("PentahoXmlaServlet.ERROR_0001_ACCESS_DENIED_DATASOURCE"))); //$NON-NLS-1$
      }

      // Role in request overrides role in connect string, if present.
      if (roleName != null) {
        connectProperties.put(RolapConnectionProperties.Role.name(), roleName);
      }

      String dsName = connectProperties.get(KEY_DATASOURCE);

      logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_CONNECT_STRING_DATASOURCE", dsName)); //$NON-NLS-1$

      javax.sql.DataSource datasource = getDataSource(dsName);

      logger.debug(Messages.getString(
          "PentahoXmlaServlet.DEBUG_DATASOURCE_FROM_PENTAHO", datasource != null ? datasource.toString() : null)); //$NON-NLS-1$

      RolapConnection conn = null;
      if (StringUtils.isNotBlank(dsName) && datasource != null) {
        conn = (RolapConnection) DriverManager.getConnection(connectProperties, null, datasource);
        logger.debug(Messages.getString(
            "PentahoXmlaServlet.DEBUG_CREATED_ROLAP_CONN", conn != null ? conn.toString() : null)); //$NON-NLS-1$
      } else {
        // either using jdbc or pentaho ds mgr does not know about dsName; fall back on "normal" behavior
        conn = (RolapConnection) DriverManager.getConnection(resolveUboundJndi(connectProperties), null);
      }

      if (role != null) {
        conn.setRole(role);
      }

      if (logger.isDebugEnabled()) {
        if (conn == null) {
          logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_CONN_NULL")); //$NON-NLS-1$
        } else {
          logger.debug(Messages.getString("PentahoXmlaServlet.DEBUG_CONN_NOT_NULL")); //$NON-NLS-1$
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
            "IDatasourceService", null); //$NON-NLS-1$

        javax.sql.DataSource datasource = datasourceSvc.getDataSource(dsName);

        return datasource;
      } catch (ObjectFactoryException e) {
        logger.error(Messages.getErrorString("PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE"), e); //$NON-NLS-1$
        return null;
      } catch (DatasourceServiceException e) {
        logger.error(Messages.getErrorString("PentahoXmlaServlet.ERROR_0002_UNABLE_TO_INSTANTIATE"), e); //$NON-NLS-1$
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

    String datasource = orig.get(KEY_DATASOURCE);
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
      logger.error(Messages.getString("PentahoXmlaServlet.ERROR_0003_GETDSBOUNDNAME_FAILED"), e); //$NON-NLS-1$

    }
    newPropList.put(KEY_DATASOURCE, resolvedDatasource);
    return newPropList;
  }

}