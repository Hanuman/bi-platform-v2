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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.api.engine;

import java.util.Map;

public interface IPermissionMgr {
  public void setPermission(IPermissionRecipient permissionRecipient, IPermissionMask permission, Object domainInstance);

  /**
   * mlowery Note that this method takes a single recipient--either a role or user. It does not take an Authentication 
   * instance like Acegi Security's AccessDecisionVoter.  
   */
  public boolean hasPermission(IPermissionRecipient permissionRecipient, IPermissionMask permission,
      Object domainInstance);

  public Map<IPermissionRecipient, IPermissionMask> getPermissions(Object domainInstance);

  /**
   * TODO mlowery This is really addPermission. Perhaps a method name change?
   */
  public void setPermissions(Map<IPermissionRecipient, IPermissionMask> acl, Object domainInstance);
}
