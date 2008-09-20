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
 *
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Sept 20, 2008
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

/**
 * This class should not exist.  It is here only satisfy the code-wise requirement for certain object factories
 * that are in development.
 * @author aphillips
 *
 */
public class NullObjectCreator extends ObjectCreator {

  public NullObjectCreator() {
    super(Object.class.getName());
  }

  public Object getInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
    return null;
  }
}