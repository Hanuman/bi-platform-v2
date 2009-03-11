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

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;

/**
 * For use when authorities are stored in the user object (e.g.
 * <code>objectClass=Person</code>) and therefore retrieved by an
 * <code>LdapEntryMapper</code> instance. This class helps since
 * <code>LdapAuthenticationProvider</code> requires a
 * <code>LdapAuthoritiesPopulator</code> instance.
 * 
 * @author mlowery
 * 
 */
public class NoOpLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

  public GrantedAuthority[] getGrantedAuthorities(final LdapUserDetails userDetails) throws LdapDataAccessException {
    return new GrantedAuthority[0];
  }

}
