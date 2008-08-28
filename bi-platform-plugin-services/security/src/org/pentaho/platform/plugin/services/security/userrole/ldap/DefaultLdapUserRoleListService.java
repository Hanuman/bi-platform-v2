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
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.InitialDirContextFactory;
import org.acegisecurity.userdetails.UserDetails;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultLdapUserRoleListService implements IUserRoleListService, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  // private static final Log logger = LogFactory.getLog(DefaultLdapUserRoleListService.class);

  // ~ Instance fields =================================================================================================

  // private InitialDirContextFactory initialDirContextFactory;

  private LdapSearch allUsernamesSearch;

  private LdapSearch allAuthoritiesSearch;

  private LdapSearch usernamesInRoleSearch;

  /**
   * Case-sensitive by default.
   */
  private Comparator<GrantedAuthority> grantedAuthorityComparator;

  /**
   * Case-sensitive by default.
   */
  private Comparator<String> usernameComparator;

  /**
   * Used only for <code>getAuthoritiesForUser</code>. This is preferred
   * over an <code>LdapSearch</code> in
   * <code>authoritiesForUserSearch</code> as it keeps roles returned by
   * <code>UserDetailsService</code> and roles returned by
   * <code>DefaultLdapUserRoleListService</code> consistent.
   */
  private LdapUserDetailsService userDetailsService;

  // ~ Constructors ====================================================================================================

  public DefaultLdapUserRoleListService(final InitialDirContextFactory initialDirContextFactory) {
    // this.initialDirContextFactory = initialDirContextFactory;
  }

  public DefaultLdapUserRoleListService(final InitialDirContextFactory initialDirContextFactory,
      final Comparator<String> usernameComparator, final Comparator<GrantedAuthority> grantedAuthorityComparator) {
    // this.initialDirContextFactory = initialDirContextFactory;
    this.usernameComparator = usernameComparator;
    this.grantedAuthorityComparator = grantedAuthorityComparator;
  }

  // ~ Methods =========================================================================================================

  public void afterPropertiesSet() throws Exception {
  }

  public GrantedAuthority[] getAllAuthorities() {
    List<GrantedAuthority> results = allAuthoritiesSearch.search(new Object[0]);
    if (null != grantedAuthorityComparator) {
      Collections.sort(results, grantedAuthorityComparator);
    }
    return results.toArray(new GrantedAuthority[0]);
  }

  public String[] getAllUsernames() {
    List<String> results = allUsernamesSearch.search(new Object[0]);
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results.toArray(new String[0]);
  }

  public String[] getUsernamesInRole(final GrantedAuthority authority) {
    List<String> results = usernamesInRoleSearch.search(new Object[] { authority });
    if (null != usernameComparator) {
      Collections.sort(results, usernameComparator);
    }
    return results.toArray(new String[0]);
  }

  public GrantedAuthority[] getAuthoritiesForUser(final String username) {
    UserDetails user = userDetailsService.loadUserByUsername(username);
    List<GrantedAuthority> results = Arrays.asList(user.getAuthorities());
    if (null != grantedAuthorityComparator) {
      Collections.sort(results, grantedAuthorityComparator);
    }
    return results.toArray(new GrantedAuthority[0]);
  }

  public void setAllUsernamesSearch(final LdapSearch allUsernamesSearch) {
    this.allUsernamesSearch = allUsernamesSearch;
  }

  public void setAllAuthoritiesSearch(final LdapSearch allAuthoritiesSearch) {
    this.allAuthoritiesSearch = allAuthoritiesSearch;
  }

  public void setUsernamesInRoleSearch(final LdapSearch usernamesInRoleSearch) {
    this.usernamesInRoleSearch = usernamesInRoleSearch;
  }

  public void setUserDetailsService(final LdapUserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
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
