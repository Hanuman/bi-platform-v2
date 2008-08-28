package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * A service registering/enumerating registered Mondrian catalogs (schemas).
 * 
 * @author mlowery
 */
public interface IMondrianCatalogService {

  /**
   * Lists all catalogs (filtered according to access control rules).
   * @param jndiOnly return only JNDI-based catalogs
   */
  List<MondrianCatalog> listCatalogs(IPentahoSession pentahoSession, boolean jndiOnly);

  /**
   * Adds to the global catalog list and possibly persists this information.
   * @param overwrite true to overwrite existing catalog (based on match with definition and effectiveDataSourceInfo 
   */
  void addCatalog(MondrianCatalog catalog, boolean overwrite, IPentahoSession pentahoSession)
      throws MondrianCatalogServiceException;

  /**
   * Returns the catalog with the given name or <code>null</code> if name not recognized. 
   * @param name of the catalog to fetch
   */
  MondrianCatalog getCatalog(String name, final IPentahoSession pentahoSession);

  /**
   * this method loads a Mondrian schema
   * 
   * @param solutionLocation location of the schema
   * @param pentahoSession current session object
   * 
   * @return Mondrian Schema object
   */
  MondrianSchema loadMondrianSchema(String solutionLocation, IPentahoSession pentahoSession);
  

}