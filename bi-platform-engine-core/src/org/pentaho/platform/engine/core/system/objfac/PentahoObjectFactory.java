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

import java.util.Map;

import org.pentaho.platform.api.engine.IObjectCreator;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.messages.Messages;
import org.pentaho.platform.util.VersionHelper;

@Deprecated  //FIXME: This class is not stable and should be removed ASAP.  
//The correct way to access Pentaho system objects is PentahoSystem.getObjectFactory().get(...)
public class PentahoObjectFactory implements IPentahoObjectFactory {

  private Map<String, IObjectCreator> objectCreators = null;
  
  public PentahoObjectFactory() {
    
  }
  
  public Object getObject( String key, final IPentahoSession session ) throws ObjectFactoryException {
    IObjectCreator creator = objectCreators.get( key );
    if ( null == creator ) {
      throw new ObjectFactoryException( Messages.getErrorString("PentahoSystem.ERROR_0421_OBJECT_NOT_CONFIGURED", key ) );   //$NON-NLS-1$
    }
    return creator.getInstance( key, session );
  }
  
  public boolean hasObject( String key ) {
    return objectCreators.containsKey( key );
  }
  
  // warning is suppressed because Spring may be used to inject his map, and it can't handle generic types
  @SuppressWarnings("unchecked")
  public void setObjectCreators( Map objectMap ) {
    this.objectCreators = objectMap;
    createRequiredImplementations();
  }
  
  private void createRequiredImplementations() {
    // force the existence of a IVersionHelper implementation
    String versionHelperKey = IVersionHelper.class.getSimpleName();
    if ( !objectCreators.containsKey( versionHelperKey ) ) {
      IObjectCreator c = new GlobalObjectCreator( VersionHelper.class.getName() );
      objectCreators.put( versionHelperKey, c );
    }
  }

  public <T> T get(Class<T> interfaceClass, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> T get(Class<T> interfaceClass, String key, IPentahoSession session) throws ObjectFactoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public void init(String configFile, Object context) {
    // TODO Auto-generated method stub
    
  }

  public boolean objectDefined(String key) {
    // TODO Auto-generated method stub
    return false;
  }

  public Class getImplementingClass(String key) {
    // TODO Auto-generated method stub
    return null;
  }
 
}
