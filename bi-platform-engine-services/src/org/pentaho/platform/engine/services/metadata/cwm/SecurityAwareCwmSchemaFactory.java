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
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 */
package org.pentaho.platform.engine.services.metadata.cwm;

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.pms.factory.CwmSchemaFactoryInterface;
import org.pentaho.pms.schema.concept.ConceptUtilityInterface;

public class SecurityAwareCwmSchemaFactory extends PlatformCWMSchemaFactory implements IPentahoInitializer {

  IPentahoSession session;

  public SecurityAwareCwmSchemaFactory() {

  }

  public void init(final IPentahoSession inSession) {
    this.session = inSession;
  }

  public void setSession(final IPentahoSession value) {
    this.session = value;
  }

  public IPentahoSession getSession() {
    return this.session;
  }

  public static final int[] AccessTypeMap = new int[] { IAclHolder.ACCESS_TYPE_READ, IAclHolder.ACCESS_TYPE_WRITE,
      IAclHolder.ACCESS_TYPE_UPDATE, IAclHolder.ACCESS_TYPE_DELETE, IAclHolder.ACCESS_TYPE_ADMIN,
      IAclHolder.ACCESS_TYPE_ADMIN };

  @Override
  public boolean hasAccess(final int accessType, final ConceptUtilityInterface aclHolder) {
    if (aclHolder != null) {
      CWMAclHolder newHolder = new CWMAclHolder(aclHolder);
      int mappedActionOperation = SecurityAwareCwmSchemaFactory.AccessTypeMap[accessType];
      return SecurityHelper.hasAccess(newHolder, mappedActionOperation, session);
    } else {
      if (accessType == CwmSchemaFactoryInterface.ACCESS_TYPE_SCHEMA_ADMIN) {
        return SecurityHelper.isPentahoAdministrator(session);
      }
    }
    return true;
  }

}
