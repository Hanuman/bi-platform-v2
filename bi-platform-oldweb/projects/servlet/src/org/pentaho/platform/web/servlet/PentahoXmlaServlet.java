package org.pentaho.platform.web.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import mondrian.olap.Util;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.DataSource;
import mondrian.xmla.impl.DefaultXmlaServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
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
    EntityResolver loader = new PentahoEntityResolver( );
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
   * This method overrides it's super and resolves any unbound JNDI sources
   */
  @Override
  protected DataSourcesConfig.DataSources parseDataSources(final String dataSourcesConfigString) {
    DataSourcesConfig.DataSources datasources = super.parseDataSources(dataSourcesConfigString);
    
    for (DataSource element : datasources.dataSources) {
      String datasourceInfo = element.dataSourceInfo;
      // determine if JNDI datasource needs binding
      Util.PropertyList list = Util.parseConnectString(datasourceInfo);
      if (list.get("DataSource") != null) { //$NON-NLS-1$
        String datasource = list.get("DataSource"); //$NON-NLS-1$
        if (!(datasource.indexOf(":") >= 0) && !(datasource.indexOf("/") >= 0)) { //$NON-NLS-1$ //$NON-NLS-2$
          try {
            if (PentahoXmlaServlet.logger.isDebugEnabled()) {
              PentahoXmlaServlet.logger.debug("resolving datasource: " + datasource);
            }
       	    IDatasourceService datasourceService =  (IDatasourceService) PentahoSystem.getObjectFactory().getObject(IDatasourceService.IDATASOURCE_SERVICE,null);            
            String resolvedDatasource = datasourceService.getDSBoundName(datasource);
            if (PentahoXmlaServlet.logger.isDebugEnabled()) {
              PentahoXmlaServlet.logger.debug("resolved datasource: " + resolvedDatasource);
            }
            list.put("DataSource", resolvedDatasource); //$NON-NLS-1$
            element.dataSourceInfo = list.toString();
          } catch (Exception e) {
            PentahoXmlaServlet.logger.error("failed to resolve datasource " + datasource, e); //$NON-NLS-1$
          }
        }
      }
    }
    return datasources;
  }
}