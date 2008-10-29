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
 * @created June 25, 2008
 * @author Steven Barkdull
 * 
 */
package org.pentaho.platform.engine.core.system.objfac;

import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;

/**
 * <span style="color:red; font-weight:bold">NOTICE: This class is deprecated and will not exist in platform version 2.1.</span>  <br>
 * Please use the new way of factory-ing dynamic PentahoObjects.  
 * See an explanation of how to switch from the old way to the new way here: {@link IObjectCreator}
 * @deprecated
 */
public class GlobalObjectCreator extends ObjectCreator {

  private Object instance = null;
  
  /**
   * Please use the new way of factory-ing dynamic PentahoObjects.  
   * See an explanation of how to switch from the old way to the new way here: {@link IObjectCreator}
   * @deprecated
   */
  public GlobalObjectCreator( String className ) {
    super( className );
  }
  
  /**
   * Please use the new way of factory-ing dynamic PentahoObjects.  
   * See an explanation of how to switch from the old way to the new way here: {@link IObjectCreator}
   * @deprecated
   */
  public Object getInstance( String key, IPentahoSession session ) throws ObjectFactoryException {
    if ( null == instance ) {
      instance = createObject();
    }
    if (instance instanceof IPentahoInitializer) {
      ((IPentahoInitializer) instance).init( session );
    }
    return instance;
  }
}