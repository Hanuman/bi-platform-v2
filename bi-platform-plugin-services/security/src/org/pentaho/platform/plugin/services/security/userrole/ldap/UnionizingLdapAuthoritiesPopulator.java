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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Delegates to populators and unions the results.
 * 
 * @author mlowery
 */
public class UnionizingLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator, InitializingBean {

  private Set populators;

  public GrantedAuthority[] getGrantedAuthorities(final LdapUserDetails userDetails) throws LdapDataAccessException {
    Iterator iter = populators.iterator();
    Set allAuthorities = new HashSet();
    while (iter.hasNext()) {
      LdapAuthoritiesPopulator populator = (LdapAuthoritiesPopulator) iter.next();
      GrantedAuthority[] auths = populator.getGrantedAuthorities(userDetails);
      if ((null != auths) && (auths.length > 0)) {
        allAuthorities.addAll(Arrays.asList(auths));
      }
    }
    return (GrantedAuthority[]) allAuthorities.toArray(new GrantedAuthority[0]);
  }

  public void setPopulators(final Set populators) {
    this.populators = populators;
  }

  public void afterPropertiesSet() throws Exception {
    Assert.notNull(populators, Messages.getString("UnionizingLdapAuthoritiesPopulator.ERROR_0001_POPULATOR_NULL")); //$NON-NLS-1$
  }

}
