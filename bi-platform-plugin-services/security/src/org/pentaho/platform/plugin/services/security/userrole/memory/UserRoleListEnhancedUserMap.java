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
 * Copyright 2007 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */

/* Parts Copyright 2004 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.platform.plugin.services.security.userrole.memory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.memory.UserMap;

public class UserRoleListEnhancedUserMap extends UserMap {
  // ~ Static fields/initializers
  // =============================================

  // ~ Instance fields
  // ========================================================

  private final Map userRoleListEnhanceduserMap = new HashMap();

  private final Map rolesToUsersMap = new HashMap();

  // ~ Methods
  // ================================================================

  @Override
  public void addUser(final UserDetails user) throws IllegalArgumentException {
    super.addUser(user);
    this.userRoleListEnhanceduserMap.put(user.getUsername().toLowerCase(), user);
    GrantedAuthority[] auths = user.getAuthorities();
    for (GrantedAuthority anAuthority : auths) {
      Set userListForAuthority = (Set) rolesToUsersMap.get(anAuthority);
      if (userListForAuthority == null) {
        userListForAuthority = new TreeSet();
        rolesToUsersMap.put(anAuthority, userListForAuthority);
      }
      userListForAuthority.add(user.getUsername());
    }
  }

  public GrantedAuthority[] getAllAuthorities() {
    GrantedAuthority[] typ = {};
    Set authoritiesSet = this.rolesToUsersMap.keySet();
    return (GrantedAuthority[]) authoritiesSet.toArray(typ);
  }

  public String[] getAllUsers() {
    String[] rtn = new String[userRoleListEnhanceduserMap.size()];
    Iterator it = userRoleListEnhanceduserMap.values().iterator();
    int i = 0;
    while (it.hasNext()) {
      rtn[i] = ((UserDetails) it.next()).getUsername();
      i++;
    }
    return rtn;
  }

  public String[] getUserNamesInRole(final GrantedAuthority authority) {
    Set userListForAuthority = (Set) rolesToUsersMap.get(authority);
    String[] typ = {};
    if (userListForAuthority != null) {
      return (String[]) userListForAuthority.toArray(typ);
    } else {
      return typ;
    }
  }

  @Override
  public void setUsers(final Map users) {
    super.setUsers(users);
    Iterator iter = users.values().iterator();
    while (iter.hasNext()) {
      addUser((UserDetails) iter.next());
    }
  }

}