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
package org.pentaho.platform.engine.security.userroledao.userrolelistservice;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.engine.security.userroledao.IUserRoleDao;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * An {@link IUserRoleListService} that delegates to an {@link IUserRoleDao}.
 * 
 * @author mlowery
 */
public class UserRoleDaoUserRoleListService implements IUserRoleListService {

  // ~ Static fields/initializers ====================================================================================== 

  // ~ Instance fields =================================================================================================

  private IUserRoleDao userRoleDao;
  
  private UserDetailsService userDetailsService;

  // ~ Constructors ====================================================================================================

  public UserRoleDaoUserRoleListService() {
    super();
  }

  // ~ Methods =========================================================================================================

  public GrantedAuthority[] getAllAuthorities() {
    List<IPentahoRole> roles = userRoleDao.getRoles();

    List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();

    for (IPentahoRole role : roles) {
      auths.add(new GrantedAuthorityImpl(role.getName()));
    }

    return auths.toArray(new GrantedAuthority[0]);
  }

  public String[] getAllUsernames() {
    List<IPentahoUser> users = userRoleDao.getUsers();

    List<String> usernames = new ArrayList<String>();

    for (IPentahoUser user : users) {
      usernames.add(user.getUsername());
    }

    return usernames.toArray(new String[0]);
  }

  public GrantedAuthority[] getAuthoritiesForUser(String username) throws UsernameNotFoundException,
  DataAccessException {
  UserDetails user = userDetailsService.loadUserByUsername(username);
  return user.getAuthorities();
  }

  public String[] getUsernamesInRole(GrantedAuthority authority) {
    IPentahoRole role = userRoleDao.getRole(authority.getAuthority());
    if (role == null) {
      String[] rtn = {};
      return rtn;
    }

    List<String> usernames = new ArrayList<String>();

    for (IPentahoUser user : role.getUsers()) {
      usernames.add(user.getUsername());
    }

    return usernames.toArray(new String[0]);
  }

  public void setUserRoleDao(IUserRoleDao userRoleDao) {
    this.userRoleDao = userRoleDao;
  }

  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

}
