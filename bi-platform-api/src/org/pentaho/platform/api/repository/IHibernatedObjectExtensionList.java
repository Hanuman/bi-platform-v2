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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Nov 7, 2005 
 * @author mbatchel
 */
package org.pentaho.platform.api.repository;

import java.util.List;
import java.util.Map;

/**
 * Describes where to find hibernated object XML definitions (.hbm.xml) and the
 * class versions of the hibernated objects. This lets us discover changes to an
 * objects' definition which will trigger a schema update call.
 * @author mbatchel
 *
 */
public interface IHibernatedObjectExtensionList {
  /**
   * Returns a list of fully qualified class name hbm.xml files
   * for this extension.
   * 
   * @return list of .hbm.xml files
   * @see StdHibernateClassHandler
   */
  public List getHibernatedObjectResourceList();

  /**
   * Provides a map of an object class name (not fully qualified) to the
   * class version number. This allows the DefinitionVersionManager to
   * detect changes in a class version which should trigger a schema update call.
   * @return Map of classes to versions
   */
  public Map getHibernatedObjectVersionMap();
}
