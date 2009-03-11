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