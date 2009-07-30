/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
*/
package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mondrian.olap.MondrianDef;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.agg.AggregationManager;
import mondrian.xmla.DataSourcesConfig;
import mondrian.xmla.DataSourcesConfig.Catalog;
import mondrian.xmla.DataSourcesConfig.Catalogs;
import mondrian.xmla.DataSourcesConfig.DataSource;
import mondrian.xmla.DataSourcesConfig.DataSources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.eigenbase.xom.DOMWrapper;
import org.eigenbase.xom.Parser;
import org.eigenbase.xom.XMLOutput;
import org.eigenbase.xom.XOMException;
import org.eigenbase.xom.XOMUtil;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPermissionMask;
import org.pentaho.platform.api.engine.ISolutionFile;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.PentahoEntityResolver;
import org.pentaho.platform.engine.services.solution.SolutionReposHelper;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException.Reason;
import org.pentaho.platform.repository.solution.filebased.FileSolutionFile;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;


/**
 * Reads in file containing Mondrian data sources and catalogs. (Contains code copied from <code>XmlaServlet</code>.)
 * 
 * @author mlowery
 */
public class MondrianCatalogHelper implements IMondrianCatalogService {

  // ~ Static fields/initializers ======================================================================================

  private static final Log logger = LogFactory.getLog(MondrianCatalogHelper.class);

  // ~ Instance fields =================================================================================================

  private String dataSourcesConfig;

  private List<MondrianCatalog> catalogs;

  private Map<String, MondrianCatalog> catalogMap;

  /**
   * true to use schema name from catalog definition (aka schema file) as catalog name.  
   */
  private boolean useSchemaNameAsCatalogName = true;

  /**
   * Maps cat.dataSourceInfo + cat.definition to catalog. This allows for quick searches for duplicate add requests.
   */
  private Map<String, MondrianCatalog> dsInfoAndCatDefMap;

  // ~ Constructors ====================================================================================================

  private static final MondrianCatalogHelper instance = new MondrianCatalogHelper();
  
  public static MondrianCatalogHelper getInstance() {
    return MondrianCatalogHelper.instance;
  }
  
  public MondrianCatalogHelper() {
    super();
    
    // LEGACY: This configures the dataSourcesConfig
    dataSourcesConfig = "file:" + //$NON-NLS-1$
      PentahoSystem.getApplicationContext().getSolutionPath("system/olap/datasources.xml"); //$NON-NLS-1$
    
  }

  private volatile boolean initialized = false;

  // ~ Methods =========================================================================================================

  protected synchronized void init(final IPentahoSession pentahoSession) {
    if (!initialized) {
      if (MondrianCatalogHelper.logger.isDebugEnabled()) {
        MondrianCatalogHelper.logger.debug("init"); //$NON-NLS-1$
      }
      catalogs = transformIntoCatalogList(makeDataSources(), pentahoSession);
      catalogMap = makeCatalogMap(catalogs);
      dsInfoAndCatDefMap = makeDataSourceInfoAndCatalogDefinitionMap(catalogs);
      initialized = true;
      AggregationManager.instance().getCacheControl(null).flushSchemaCache();
    }
  }

  /**
   * Same as implemented in <code>XmlaServlet</code> except takes advantage of Spring's Resource framework.
   */
  protected DataSourcesConfig.DataSources makeDataSources() {

    // Resource dataSourcesConfigResource = resourceLoader.getResource(dataSourcesConfig);

    URL dataSourcesConfigUrl = null;
    try {
      if (dataSourcesConfig.startsWith("file:")) { //$NON-NLS-1$
        dataSourcesConfigUrl = new URL(dataSourcesConfig);//dataSourcesConfigResource.getURL();
      } else if (dataSourcesConfig.startsWith("classpath:")) { //$NON-NLS-1$
        dataSourcesConfigUrl = getClass().getResource(dataSourcesConfig.substring(10));
      } else {
        throw new MondrianCatalogServiceException("dataSourcesConfig is not a valid URL or does not exist", //$NON-NLS-1$
            Reason.GENERAL);
      }
    } catch (IOException e) {
      throw new MondrianCatalogServiceException(
          Messages.getErrorString("MondrianCatalogHelper.ERROR_0001_INVALID_DATASOURCE_CONFIG", dataSourcesConfig),  //$NON-NLS-1$
          e, Reason.GENERAL);
    }

    // don't try to parse a null
    return (dataSourcesConfigUrl == null) ? null : parseDataSourcesUrl(dataSourcesConfigUrl);
  }

  protected DataSourcesConfig.DataSources parseDataSourcesUrl(final URL dataSourcesConfigUrl) {

    try {
      String dataSourcesConfigString = readDataSourcesContent(dataSourcesConfigUrl);
      return parseDataSources(dataSourcesConfigString);

    } catch (Exception e) {
      throw Util.newError(e, Messages.getErrorString("MondrianCatalogHelper.ERROR_0002_FAILED_TO_PARSE_DATASOURCE_CONFIG", dataSourcesConfigUrl.toExternalForm())); //$NON-NLS-1$
    }
  }

  protected String readDataSourcesContent(final URL dataSourcesConfigUrl) throws IOException {
    return Util.readURL(dataSourcesConfigUrl, Util.toMap(System.getProperties()));
  }

  protected DataSourcesConfig.DataSources parseDataSources(final String dataSourcesConfigString) {

    try {
      if (dataSourcesConfigString == null) {
        MondrianCatalogHelper.logger.warn(Messages.getString("MondrianCatalogHelper.WARN_PARSE_NULL_INPUT")); //$NON-NLS-1$
        return null;
      }
      String replacedConfigString = Util.replaceProperties(dataSourcesConfigString, Util.toMap(System.getProperties()));

      if (MondrianCatalogHelper.logger.isDebugEnabled()) {
        String msg = "parseDataSources: dataSources=" + replacedConfigString; //$NON-NLS-1$
        MondrianCatalogHelper.logger.debug(msg);
      }
      final Parser parser = XOMUtil.createDefaultParser();
      final DOMWrapper doc = parser.parse(replacedConfigString);
      return new DataSourcesConfig.DataSources(doc);

    } catch (XOMException e) {
      throw Util.newError(e, Messages.getErrorString("MondrianCatalogHelper.ERROR_0002_FAILED_TO_PARSE_DATASOURCE_CONFIG", dataSourcesConfigString)); //$NON-NLS-1$
    }
  }

  protected Map<String, MondrianCatalog> makeCatalogMap(final List<MondrianCatalog> cats) {
    Map<String, MondrianCatalog> map = new HashMap<String, MondrianCatalog>();
    for (MondrianCatalog catalog : cats) {
      map.put(catalog.getName(), catalog);
    }
    return map;
  }

  protected Map<String, MondrianCatalog> makeDataSourceInfoAndCatalogDefinitionMap(final List<MondrianCatalog> cats) {
    Map<String, MondrianCatalog> map = new HashMap<String, MondrianCatalog>();
    for (MondrianCatalog catalog : cats) {
      map.put(makeKey(catalog), catalog);
    }
    return map;
  }

  protected String cleanseDataSourceInfo(String dataSourceInfo) {
    if (dataSourceInfo == null) {
      return null;
    }
    // remove EnableXmla if necessary before building the key  
    PropertyList propertyList = Util.parseConnectString(dataSourceInfo);
    if (propertyList.get("EnableXmla") != null) {
      propertyList.remove("EnableXmla");
    }
    return propertyList.toString();
  }
  
  protected String makeKey(final MondrianCatalog catalog) {
    String dataSourceInfo = cleanseDataSourceInfo(catalog.getEffectiveDataSource().getDataSourceInfo());
    return dataSourceInfo + "+" + catalog.getDefinition(); //$NON-NLS-1$
  }

  public String getDataSourcesConfig() {
    return dataSourcesConfig;
  }

  public void setDataSourcesConfig(final String dataSourcesConfig) {
    this.dataSourcesConfig = dataSourcesConfig;
  }

  public List<MondrianCatalog> listCatalogs(final IPentahoSession pentahoSession, final boolean jndiOnly) {
    if (MondrianCatalogHelper.logger.isDebugEnabled()) {
      MondrianCatalogHelper.logger.debug("listCatalogs"); //$NON-NLS-1$
    }
    if (!initialized) {
      init(pentahoSession);
    }
    // defensive copy
    return Collections.unmodifiableList(filter(catalogs, pentahoSession, jndiOnly));
  }

  public synchronized void addCatalog(final MondrianCatalog catalog, final boolean overwrite,
      final IPentahoSession pentahoSession) throws MondrianCatalogServiceException {
    if (MondrianCatalogHelper.logger.isDebugEnabled()) {
      MondrianCatalogHelper.logger.debug("addCatalog"); //$NON-NLS-1$
    }

    if (!initialized) {
      init(pentahoSession);
    }

    // do an access check first
    if (!hasAccess(catalog, CatalogPermission.WRITE, pentahoSession)) {
      if (MondrianCatalogHelper.logger.isDebugEnabled()) {
        MondrianCatalogHelper.logger.debug("user does not have access; throwing exception"); //$NON-NLS-1$
      }
      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0003_INSUFFICIENT_PERMISSION"), Reason.ACCESS_DENIED); //$NON-NLS-1$
    }
    
    // check for existing dataSourceInfo+catalog
    boolean alreadyExists = dsInfoAndCatDefMap.containsKey(makeKey(catalog));

    if (alreadyExists && !overwrite) {
      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0004_ALREADY_EXISTS"), Reason.ALREADY_EXISTS); //$NON-NLS-1$
    }
    
    if( alreadyExists ) {
    	MondrianCatalog existing = dsInfoAndCatDefMap.get( makeKey(catalog) );
    	if( !existing.getDefinition().equals( catalog.getDefinition() ) ) {
    	      // this scenario occurs if the file is in a different location but has the same schema name
    	      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0004_ALREADY_EXISTS"), Reason.ALREADY_EXISTS); //$NON-NLS-1$
    	}
    }
    
    DataSources dataSources = makeDataSources();

    MondrianDataSource mDataSource = catalog.getDataSource();
    DataSource ds = null;

  // see if the ds already exists
    for(int i = 0; i < dataSources.dataSources.length && ds == null; i++) {
      DataSource currentDs = dataSources.dataSources[i];
      if (mDataSource.getName().equals(currentDs.name)) {
        ds = currentDs;
      }
    }
    
    if (ds == null) {
      ds = new DataSource();
      ds.authenticationMode = mDataSource.getAuthenticationMode();
      ds.dataSourceInfo = mDataSource.getDataSourceInfo();
      ds.description = mDataSource.getDescription();
      ds.name = mDataSource.getName();
      ds.providerName = mDataSource.getProviderName();
      ds.providerType = mDataSource.getProviderType();
      ds.url = mDataSource.getUrl();
      dataSources.dataSources = (DataSource[])ArrayUtils.add(dataSources.dataSources, ds);
    }
  
    Catalog cat = null;
    
    if (alreadyExists) {
      // find the catalog and overwrite
      for (Catalog currentCat : ds.catalogs.catalogs) {
        if (cleanseDataSourceInfo(catalog.getEffectiveDataSource().getDataSourceInfo()).equals(
            cleanseDataSourceInfo(currentCat.dataSourceInfo)) 
            && catalog.getDefinition().equals(currentCat.definition)) {
          cat = currentCat;
        }
      }
    } else {
      cat = new Catalog();
      if (ds.catalogs == null) {
        ds.catalogs = new Catalogs();
      }
      if (ds.catalogs.catalogs == null) {
        ds.catalogs.catalogs = new Catalog[0];
      }
      ds.catalogs.catalogs = (Catalog[])ArrayUtils.add(ds.catalogs.catalogs, cat);
      cat.setDataSource(ds);
    }
    
    cat.dataSourceInfo = catalog.getDataSourceInfo();
    cat.definition = catalog.getDefinition();
    cat.name = catalog.getName();

    writeDataSources( dataSources );    
    // if we got here then assume file write was successful; refresh from file

    if (MondrianCatalogHelper.logger.isDebugEnabled()) {
      MondrianCatalogHelper.logger.debug("refreshing from dataSourcesConfig (" + dataSourcesConfig + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    this.initialized = false;
    init(pentahoSession);
  }

  protected void writeDataSources( DataSources dataSources ) {
  
	    File dataSourcesFile;
	    try {
	      dataSourcesFile = new File(new URL(dataSourcesConfig).getFile()); // dataSourcesConfigResource.getFile();
	    } catch (IOException e) {
	      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0005_RESOURCE_NOT_AVAILABLE"), e, Reason.GENERAL); //$NON-NLS-1$
	    }

	    Writer sxml;
	    try {
	      sxml = new FileWriter(dataSourcesFile);
	    } catch (IOException e) {
	      throw new MondrianCatalogServiceException(e);
	    }
	    StringWriter sw = new StringWriter();
	    XMLOutput pxml = new XMLOutput(sw);
	    pxml.print("<?xml version=\"1.0\"?>\n"); //$NON-NLS-1$
	    dataSources.displayXML(pxml, 0);
	    Document doc = null;
	    try {
	      doc = XmlDom4JHelper.getDocFromString(sw.toString(), new PentahoEntityResolver() );  
	    } catch(XmlParseException e) {
	      throw new MondrianCatalogServiceException(e);
	    }
	    
	    
	    // pretty print
	    try {
	      OutputFormat format = OutputFormat.createPrettyPrint();
	      format.setEncoding(doc.getXMLEncoding());
	      XMLWriter writer = new XMLWriter( sxml, format );
	      writer.write(doc);
	      writer.close();
	      // CleanXmlHelper.saveDomToWriter(doc, sxml);
	    } catch (IOException e) {
	      throw new MondrianCatalogServiceException(e);
	    }

	    IOUtils.closeQuietly(sxml);
  }
  
  public MondrianCatalog getCatalog(final String name, final IPentahoSession pentahoSession) {
    if (MondrianCatalogHelper.logger.isDebugEnabled()) {
      MondrianCatalogHelper.logger.debug("getCatalog"); //$NON-NLS-1$
    }
    if (!initialized) {
      init(pentahoSession);
    }
    MondrianCatalog cat = catalogMap.get(name);
    if (null != cat) {
      if (hasAccess(cat, CatalogPermission.READ, pentahoSession)) {
        return cat;
      } else {
        if (MondrianCatalogHelper.logger.isDebugEnabled()) {
          MondrianCatalogHelper.logger.debug("user requested catalog with name \"" + name //$NON-NLS-1$
              + "\", but had insufficient privileges; returning null"); //$NON-NLS-1$
        }
        return null;
      }
    } else {
      if (MondrianCatalogHelper.logger.isDebugEnabled()) {
        MondrianCatalogHelper.logger.debug("user requested catalog with name\"" + name + "\", but catalog doesn't exist"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      return null;
    }

  }

  protected List<MondrianCatalog> transformIntoCatalogList(final DataSourcesConfig.DataSources dataSources,
      final IPentahoSession pentahoSession) {
    List<MondrianCatalog> localCatalogs = new ArrayList<MondrianCatalog>();
    for (DataSourcesConfig.DataSource dataSource : dataSources.dataSources) {
      List<String> catalogNames = new ArrayList<String>();
      for (DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs) {
        catalogNames.add(catalog.name);
      }
      // create dataSource
      MondrianDataSource mondrianDataSource = new MondrianDataSource(dataSource.name, dataSource.description,
          dataSource.url, dataSource.dataSourceInfo, dataSource.providerName, dataSource.providerType,
          dataSource.authenticationMode, catalogNames);
      for (DataSourcesConfig.Catalog catalog : dataSource.catalogs.catalogs) {
        if (catalog.definition.startsWith("solution:")) { //$NON-NLS-1$
          // try catch here so the whole thing doesn't blow up if one datasource is configured incorrectly.
          try {
            MondrianSchema schema = makeSchema(docAtUrlToString(catalog.definition, pentahoSession));
  
            MondrianCatalog mondrianCatalog = new MondrianCatalog(useSchemaNameAsCatalogName ? schema.getName()
                : catalog.name, catalog.dataSourceInfo, catalog.definition, mondrianDataSource, schema);
            localCatalogs.add(mondrianCatalog);
          } catch (Exception e) {
            MondrianCatalogHelper.logger.error(Messages.getErrorString("MondrianCatalogHelper.ERROR_0013_FAILED_TO_LOAD_SCHEMA", catalog.definition), e); //$NON-NLS-1$
            
          }
        } else {
          MondrianCatalogHelper.logger.warn(Messages.getString("MondrianCatalogHelper.WARN_SKIPPING_DATASOURCE_DEF", catalog.definition)); //$NON-NLS-1$
        }
      }
    }
    return localCatalogs;
  }
  
  /**
   * this method loads a mondrian schema
   * 
   * @param solutionLocation location of the schema
   * @param pentahoSession current session object
   * 
   * @return Mondrian Schema object.
   */
  public MondrianSchema loadMondrianSchema(final String solutionLocation, final IPentahoSession pentahoSession) {
    return makeSchema(docAtUrlToString(solutionLocation, pentahoSession));
  }

  protected String docAtUrlToString(final String urlStr, final IPentahoSession pentahoSession) {
    String relPath = getSolutionRepositoryRelativePath(urlStr, pentahoSession);

    String res = null;
    InputStream in = null;
    try {
      in = PentahoSystem.get(ISolutionRepository.class, pentahoSession).getResourceInputStream(relPath, true, ISolutionRepository.ACTION_EXECUTE);
      in.mark(Integer.MAX_VALUE);
      try {
        // Read the encoding from the XML file - see BISERVER-895
        // get as string with default encoding then re-get as string with encoding specified in xml
        String encoding = XmlHelper.getEncoding(IOUtils.toString(in), null);
        in.reset();
        res = IOUtils.toString(in, encoding);
      } catch (IOException ioe) {
        throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0006_IO_PROBLEM"), ioe); //$NON-NLS-1$
      }
    } catch (FileNotFoundException fnfe) {
      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0007_FILE_NOT_FOUND"), fnfe); //$NON-NLS-1$
    } finally {
      IOUtils.closeQuietly(in);
    }
    return res;
  }

  protected MondrianSchema getSchema(final String catalogName) {
    MondrianCatalog catalog = catalogMap.get(catalogName);
    if (null == catalog) {
      return null;
    } else {
      return catalog.getSchema();
    }
  }

  protected MondrianSchema makeSchema(final String catalogStr) {
    if (MondrianCatalogHelper.logger.isDebugEnabled()) {
      MondrianCatalogHelper.logger.debug("makeSchema (catalogStr=" + catalogStr.substring(0, Math.min(40, catalogStr.length())) + "...)"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    MondrianSchema schema = null;
    try {
      final Parser xmlParser = XOMUtil.createDefaultParser();
      final DOMWrapper def = xmlParser.parse(catalogStr);
      MondrianDef.Schema schemaFromXml = new MondrianDef.Schema(def);
      String schemaName = schemaFromXml.name;
      List<MondrianCube> mondrianCubes = new ArrayList<MondrianCube>();
      for (MondrianDef.Cube cube : schemaFromXml.cubes) {
        if (cube.enabled == null || cube.enabled.booleanValue()) {
          mondrianCubes.add(new MondrianCube(cube.name));
        }
      }
      for (MondrianDef.VirtualCube cube : schemaFromXml.virtualCubes) {
        if (cube.enabled == null || cube.enabled.booleanValue()) {
          mondrianCubes.add(new MondrianCube(cube.name));
        }
      }
      schema = new MondrianSchema(schemaName, mondrianCubes);

    } catch (XOMException e) {
      if (MondrianCatalogHelper.logger.isErrorEnabled()) {
        MondrianCatalogHelper.logger.error(Messages.getErrorString("MondrianCatalogHelper.ERROR_0008_ERROR_OCCURRED"), e); //$NON-NLS-1$
      }
      throw Util.newError(e, Messages.getErrorString("MondrianCatalogHelper.ERROR_0009_WHILE_PARSING_CATALOG", catalogStr)); //$NON-NLS-1$
    }
    return schema;
  }

  protected static enum CatalogPermission {
    READ, WRITE
  }

  protected List<MondrianCatalog> filter(final List<MondrianCatalog> origList, final IPentahoSession pentahoSession,
      final boolean jndiOnly) {
    List<MondrianCatalog> filtered = new ArrayList<MondrianCatalog>();
    for (MondrianCatalog orig : origList) {
      if (hasAccess(orig, CatalogPermission.READ, pentahoSession)
          && (!jndiOnly || orig.getEffectiveDataSource().isJndi())) {
        filtered.add(orig);
      }
    }
    return filtered;
  }

  /**
   * This (hacky) implementation bases its decision on whether or not the user has the permission (indicated by
   * <code>CatalogPermission</code>) based on whether the user has permission on the file in the solution repository 
   * indicated by <code>catalog.getDefinition()</code>.
   * <p />
   * Why is this class even enforcing security anyway!?
   */
  protected boolean hasAccess(final MondrianCatalog cat, final CatalogPermission perm,
      final IPentahoSession pentahoSession) {

    if (!PentahoSystem.get(ISolutionRepository.class, pentahoSession).supportsAccessControls()) {
      return true;
    }

    IPermissionMask mappedPerm = new IPermissionMask() {

      public int getMask() {
        return perm == CatalogPermission.READ ? ISolutionRepository.ACTION_EXECUTE : ISolutionRepository.ACTION_CREATE
            | ISolutionRepository.ACTION_UPDATE;
      }

    };

    String relPath = getSolutionRepositoryRelativePath(cat.getDefinition(), pentahoSession);

    ISolutionFile solutionFile = PentahoSystem.get(ISolutionRepository.class, pentahoSession).getSolutionFile(relPath, ISolutionRepository.ACTION_EXECUTE);

    if (null == solutionFile) {

      // try to get parent folder
      relPath = relPath.substring(0, relPath.lastIndexOf("/")); //$NON-NLS-1$
      solutionFile = PentahoSystem.get(ISolutionRepository.class, pentahoSession).getSolutionFile(relPath, ISolutionRepository.ACTION_EXECUTE);
      if (null == solutionFile) {
        // file not found
        throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0010_PATH_NOT_FOUND", cat.getDefinition())); //$NON-NLS-1$
      }
    }

    // mlowery I believe there's a bug in db-based solution repository where if the file is not yet in the PRO_FILES
    // table yet in the filesystem, getFileByPath happily delegates to its parent which just checks the file on disk
    if (solutionFile instanceof FileSolutionFile) {
      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0011_REPOSITORY_ERROR", cat.getDefinition())); //$NON-NLS-1$
    }

    return PentahoSystem.get(ISolutionRepository.class, pentahoSession).hasAccess(solutionFile, mappedPerm.getMask());
  }

  protected String getSolutionRepositoryRelativePath(final String path, final IPentahoSession pentahoSession) {
    SolutionReposHelper.setSolutionRepositoryThreadVariable(PentahoSystem.get(ISolutionRepository.class, pentahoSession));

    try {
      FileSystemManager fsManager = VFS.getManager();
      return fsManager.resolveFile(path).getName().getPath();
    } catch (FileSystemException e) {
      throw new MondrianCatalogServiceException(Messages.getErrorString("MondrianCatalogHelper.ERROR_0012_FILESYSTEM_PROBLEM"), e); //$NON-NLS-1$
    }

  }

  public boolean isUseSchemaNameAsCatalogName() {
    return useSchemaNameAsCatalogName;
  }

  public void setUseSchemaNameAsCatalogName(final boolean useSchemaNameAsCatalogName) {
    this.useSchemaNameAsCatalogName = useSchemaNameAsCatalogName;
  }
  
	public static int addToCatalog( String baseUrl, boolean enableXmla, String schemaSolutionPath, IPentahoSession session, String jndiName, boolean overwrite ) {

		IMondrianCatalogService mondrianCatalogService = MondrianCatalogHelper.getInstance();
		
	    String dsUrl = baseUrl;
	    if (!dsUrl.endsWith("/")) { //$NON-NLS-1$
	      dsUrl += "/"; //$NON-NLS-1$
	    }
	    dsUrl += "Xmla"; //$NON-NLS-1$
	    String dsAuthMode = DataSource.AUTH_MODE_UNAUTHENTICATED;
	    String dsProviderName = "Pentaho"; //$NON-NLS-1$

	    // DataSources where ProviderType=None are filtered by PentahoXmlaServlet
	    String dsProviderType = enableXmla ? DataSource.PROVIDER_TYPE_MDP : "None"; //$NON-NLS-1$

	    String catDef = "solution:" + schemaSolutionPath; //$NON-NLS-1$//$NON-NLS-2$

	    MondrianSchema mondrianSchema = mondrianCatalogService.loadMondrianSchema(catDef, session);
	    
	    String catName = mondrianSchema.getName();
	    String dsName = "Provider=Mondrian;DataSource=" + mondrianSchema.getName(); //$NON-NLS-1$
	    String dsDesc = "Published Mondrian Schema " + mondrianSchema.getName() + " using jndi datasource " + jndiName; //$NON-NLS-1$ //$NON-NLS-2$
	    
	    // verify JNDI
	    try {
     	  IDatasourceService datasourceService =  PentahoSystem.getObjectFactory().get(IDatasourceService.class ,null);	    	
     	  datasourceService.getDSBoundName(jndiName);
	    } catch (ObjectFactoryException objface) {
	      Logger.error("MondrianCatalogHelper",Messages.getErrorString("MondrianCatalogPublisher.ERROR_0006_UNABLE_TO_FACTORY_OBJECT", jndiName), objface); //$NON-NLS-1$
	    } catch (DatasourceServiceException dse) {
	      Logger.error( "MondrianCatalogHelper", Messages.getErrorString("MondrianCatalogPublisher.ERROR_0001_JNDI_NAMING_ERROR", jndiName), dse); //$NON-NLS-1$
	      return -1;
	    }

	    // used in both the catalog and the catalog datasource
	    // Note: we use the unbound JNDI name here, the PentahoXmlaServlet and PivotViewComponent resolve the JNDI name

	    String catConnectStr = "Provider=mondrian;DataSource=" + jndiName; //$NON-NLS-1$

	    MondrianDataSource ds = new MondrianDataSource(dsName, dsDesc, dsUrl, catConnectStr, dsProviderName,
	        dsProviderType, dsAuthMode, null);

	    MondrianCatalog cat = new MondrianCatalog(catName, catConnectStr, catDef, ds, new MondrianSchema(catName,
	        new ArrayList<MondrianCube>()));

	    try {
	      mondrianCatalogService.addCatalog(cat, overwrite, session);
	    } catch (MondrianCatalogServiceException e) {
	        Logger.error( "MondrianCatalogHelper", Messages.getErrorString("MondrianCatalogPublisher.ERROR_0002_EXCEPTION_OCCURRED"), e); //$NON-NLS-1$
	      return -1;
	    }

	    return 0;
	}

}