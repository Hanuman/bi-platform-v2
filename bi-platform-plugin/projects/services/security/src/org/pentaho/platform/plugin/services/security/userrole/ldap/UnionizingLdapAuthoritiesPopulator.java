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
