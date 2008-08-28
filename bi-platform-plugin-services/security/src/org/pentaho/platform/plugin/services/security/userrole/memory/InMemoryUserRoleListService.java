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
package org.pentaho.platform.plugin.services.security.userrole.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
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
