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

import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.messages.Messages;

public class SessionObjectCreator extends ObjectCreator {

  public SessionObjectCreator( String className ) {
    super( className );
  }
  
  /**
   * 
   * @throws IllegalArgumentException
   */
  public Object getInstance( String key, IPentahoSession session )throws ObjectFactoryException  {

    if ( null == session ) {
      throw new IllegalArgumentException( Messages.getErrorString( "SessionObjectCreator.ERROR_0001_INVALID_SESSION" ) ); //$NON-NLS-1$
    }
    Object instance = session.getAttribute( key );

    if ((instance == null)) {
      instance = createObject();
      session.setAttribute( key, instance );
    }
    if (instance instanceof IPentahoInitializer) {
      ((IPentahoInitializer) instance).init( session );
    }
    return instance;
  }

}
