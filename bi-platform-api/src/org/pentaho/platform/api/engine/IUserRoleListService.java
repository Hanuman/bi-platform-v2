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
 *
 * @created Mar 9, 2006 
 * @author Marc Batchelor
 */

package org.pentaho.platform.api.engine;

import org.springframework.security.GrantedAuthority;

public interface IUserRoleListService {

  /**
   * Returns all authorities known to the provider. Cannot return
   * <code>null</code>
   * @return the authorities (never <code>null</code>)
   */
  public GrantedAuthority[] getAllAuthorities();

  /**
   * Returns all user names known to the provider. Cannot return
   * <code>null</code>
   * @return the users (never <code>null</code>)
   */
  public String[] getAllUsernames();

  /**
   * Returns all known users in the specified role. Cannot return
   * <code>null</code>
   * @param authority The authority to look users up by. Cannot be <code>null</code>
   * @return the users. (never <code>null</code>)
   */
  public String[] getUsernamesInRole(GrantedAuthority authority);

  /**
   * Returns all authorities granted for a specified user.
   * @param username The name of the user to look up authorities for
   * @return the authorities. (Never <code>null</code>)
   */
  public GrantedAuthority[] getAuthoritiesForUser(String username);

}
