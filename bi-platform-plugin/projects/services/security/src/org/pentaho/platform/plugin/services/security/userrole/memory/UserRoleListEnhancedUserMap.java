/*
 * Copyright 2007 - 2008 Pentaho Corporation.  All rights reserved.
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
/* Copyright 2004 Acegi Technology Pty Limited
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

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.memory.UserMap;

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