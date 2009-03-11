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

import javax.naming.NamingException;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.LdapEntryMapper;
import org.acegisecurity.ldap.LdapUserSearch;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsImpl;
import org.acegisecurity.userdetails.ldap.LdapUserDetailsMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * A <code>UserDetailsService</code> implementation that can communicate with
 * an LDAP repository.
 *
 * <p>
 * See <a href="http://forum.springframework.org/showthread.php?t=22154">this
 * Spring forum thread</a> for some background.
 * </p>
 *
 * @author mlowery
 */
public class LdapUserDetailsService implements UserDetailsService, InitializingBean {
  private static final Log logger = LogFactory.getLog(LdapUserDetailsService.class);

  private LdapUserSearch userSearch;

  private LdapAuthoritiesPopulator populator;

  private LdapUserDetailsMapper userDetailsMapper = new LdapUserDetailsMapper();

  protected LdapEntryMapper getUserDetailsMapper() {
    return userDetailsMapper;
  }

  public void setUserDetailsMapper(final LdapUserDetailsMapper userDetailsMapper) {
    this.userDetailsMapper = userDetailsMapper;
  }

  /**
   * Unfortunately, this method copies code from
   * <code>AbstractLdapAuthenticator</code>,
   * <code>LdapAuthenticationProvider</code>, and <code>LdapTemplate</code>.
   */
  public UserDetails loadUserByUsername(final String username) {
    LdapUserDetails ldapUser = userSearch.searchForUser(username);
    LdapUserDetailsImpl.Essence user = null;
    try {
      user = (LdapUserDetailsImpl.Essence) userDetailsMapper.mapAttributes(ldapUser.getDn(), ldapUser.getAttributes());
    } catch (NamingException e) {
      if (LdapUserDetailsService.logger.isErrorEnabled()) {
        LdapUserDetailsService.logger.error(LdapUserDetailsServiceMessages
            .getString("LdapUserDetailsService.ERROR_0001_NAMING_EXCEPTION"), e); //$NON-NLS-1$
      }
    }

    user.setUsername(username);

    GrantedAuthority[] extraAuthorities = populator.getGrantedAuthorities(ldapUser);

    for (GrantedAuthority element : extraAuthorities) {
      user.addAuthority(element);
    }

    return user.createUserDetails();
  }

  public void setPopulator(final LdapAuthoritiesPopulator populator) {
    this.populator = populator;
  }

  public void setUserSearch(final LdapUserSearch userSearch) {
    this.userSearch = userSearch;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(userSearch, LdapUserDetailsServiceMessages
        .getString("LdapUserDetailsService.ERROR_0002_USERSEARCH_NOT_SPECIFIED")); //$NON-NLS-1$
    Assert.notNull(populator, LdapUserDetailsServiceMessages
        .getString("LdapUserDetailsService.ERROR_0003_POPULATOR_NOT_SPECIFIED")); //$NON-NLS-1$
    Assert.notNull(userDetailsMapper, LdapUserDetailsServiceMessages
        .getString("LdapUserDetailsService.ERROR_0004_USERDETAILSMAPPER_NOT_SPECIFIED")); //$NON-NLS-1$
  }
}
