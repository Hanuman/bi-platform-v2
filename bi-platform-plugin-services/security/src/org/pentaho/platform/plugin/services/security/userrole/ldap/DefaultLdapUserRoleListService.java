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
package org.pentaho.platform.plugin.services.security.userrole.ldap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.ldap.search.LdapSearch;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.ldap.LdapUserDetailsService;
import org.springframework.util.Assert;

public class DefaultLdapUserRoleListService implements IUserRoleListService, InitializingBean {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

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

  public DefaultLdapUserRoleListService() {
    super();
  }
  
  public DefaultLdapUserRoleListService(final Comparator<String> usernameComparator, final Comparator<GrantedAuthority> grantedAuthorityComparator) {
    super();
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
