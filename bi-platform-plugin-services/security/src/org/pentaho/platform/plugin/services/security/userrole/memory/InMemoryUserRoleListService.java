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
package org.pentaho.platform.plugin.services.security.userrole.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

/**
 * An in-memory implementation of <code>UserRoleListService</code>.
 * 
 * @author mlowery
 */
public class InMemoryUserRoleListService implements IUserRoleListService, InitializingBean {

  /**
   * Case-sensitive by default.
   */
  private Comparator<GrantedAuthority> grantedAuthorityComparator;

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> usernameComparator;

  private GrantedAuthority[] allAuthorities;

  private UserRoleListEnhancedUserMap userRoleListEnhancedUserMap;

  private UserDetailsService userDetailsService;

  public GrantedAuthority[] getAllAuthorities() {
    List<GrantedAuthority> results = Arrays.asList(allAuthorities);
    if (null != grantedAuthorityComparator) {
      Collections.sort(results, grantedAuthorityComparator);
    }
    return results.toArray(new GrantedAuthority[0]);
  }

  public String[] getAllUsernames() {
    List<String> results = Arrays.asList(userRoleListEnhancedUserMap.getAllUsers());
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results.toArray(new String[0]);
  }

  public String[] getUsernamesInRole(final GrantedAuthority authority) {
    List<String> results = Arrays.asList(userRoleListEnhancedUserMap.getUserNamesInRole(authority));
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results.toArray(new String[0]);
  }

  public void setAllAuthorities(final GrantedAuthority[] allAuthorities) {
    this.allAuthorities = allAuthorities;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(userRoleListEnhancedUserMap, Messages
        .getErrorString("InMemoryUserRoleListService.ERROR_0001_PROPERTY_LIST_NOT_SPECIFIED")); //$NON-NLS-1$
    Assert.notNull(allAuthorities, Messages
        .getString("InMemoryUserRoleListService.ERROR_0002_ALL_AUTHORITIES_NOT_SPECIFIED")); //$NON-NLS-1$
    Assert.notNull(userDetailsService, Messages
        .getString("InMemoryUserRoleListService.ERROR_0003_USERDETAILSSERVICE_NOT_SPECIFIED")); //$NON-NLS-1$
  }

  public GrantedAuthority[] getAuthoritiesForUser(final String username) throws UsernameNotFoundException {
    UserDetails user = userDetailsService.loadUserByUsername(username);
    List<GrantedAuthority> results = Arrays.asList(user.getAuthorities());
    if (null != grantedAuthorityComparator) {
      Collections.sort(results, grantedAuthorityComparator);
    }
    return results.toArray(new GrantedAuthority[0]);
  }

  public void setUserRoleListEnhancedUserMap(final UserRoleListEnhancedUserMap userRoleListEnhancedUserMap) {
    this.userRoleListEnhancedUserMap = userRoleListEnhancedUserMap;
  }

  public UserDetailsService getUserDetailsService() {
    return userDetailsService;
  }

  public void setUserDetailsService(final UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  public UserRoleListEnhancedUserMap getUserRoleListEnhancedUserMap() {
    return userRoleListEnhancedUserMap;
  }

  public void setGrantedAuthorityComparator(final Comparator<GrantedAuthority> grantedAuthorityComparator) {
    Assert.notNull(grantedAuthorityComparator);
    this.grantedAuthorityComparator = grantedAuthorityComparator;
  }

  public void setUsernameComparator(final Comparator<String> usernameComparator) {
    Assert.notNull(usernameComparator);
    this.usernameComparator = usernameComparator;
  }
}
